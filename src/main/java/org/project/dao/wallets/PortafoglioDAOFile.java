package org.project.dao.wallets;

import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.ing.enumerations.StatoTransazione;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.factory.StockFactory;
import org.project.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;



public class PortafoglioDAOFile extends PortafoglioDAO {

    private final String walletFile;
    private final String posizioniFile;
    private final String transazioniFile;
    private final StudenteDAO studenteDAO;
    private final StockFactory stockFactory;
    private static final String CSV_SEPARATOR = ";";


    public PortafoglioDAOFile(String walletFile, String posizioniFile, String transazioniFile,
                              StudenteDAO studenteDAO, StockFactory stockFactory) {
        this.walletFile = walletFile;
        this.posizioniFile = posizioniFile;
        this.transazioniFile = transazioniFile;
        this.studenteDAO = studenteDAO;
        this.stockFactory = stockFactory;
    }

    @Override
    protected VirtualWallet doRetrievePortafoglioByEmail(String mailCercata) throws DAOException {
        Studente studente = studenteDAO.getStudenteByEmail(mailCercata);
        if (studente == null) return null;

        // 1. Legge il vecchio portafoglio dal file (es. 10.000€)
        VirtualWallet wallet = leggiWalletBase(studente, mailCercata);
        if (wallet == null) return null;

        popolaPosizioni(wallet, mailCercata);
        popolaTransazioni(wallet, mailCercata);

        // --- INIZIO CORREZIONE: Allineamento automatico al nuovo budget della classe ---

        // Verifica se lo studente è iscritto a una classe e qual è il suo budget attuale
        if (studente.classeFrequentata() != null) {
            double budgetAttualeClasse = studente.classeFrequentata().budgetIniziale();

            // Calcola quanti soldi sono stati effettivamente dati allo studente in passato
            // (Saldo attuale + tutti i soldi spesi per le posizioni)
            double spesaStorica = 0.0;
            if (wallet.posizioni() != null) {
                for (WalletPosition p : wallet.posizioni()) {
                    spesaStorica += (p.quantita() * p.prezzoMedioAcquisto());
                }
            }
            double budgetAssegnatoInPassato = wallet.saldoDisponibile() + spesaStorica;

            // Se il prof ha alzato il budget mentre lo studente era offline, gli diamo la differenza!
            double differenza = budgetAttualeClasse - budgetAssegnatoInPassato;

            if (Math.abs(differenza) > 0.01) { // Evita micro-bug sui decimali
                if (differenza > 0) {
                    wallet.accreditaSaldo(differenza);
                } else {
                    double daScalare = Math.abs(differenza);
                    if (wallet.saldoDisponibile() >= daScalare) {
                        wallet.scalaSaldo(daScalare);
                    } else {
                        wallet.scalaSaldo(wallet.saldoDisponibile()); // Non può andare in negativo
                    }
                }

                // Salva subito il nuovo portafoglio aggiornato nel file CSV
                salvaPortafoglio(wallet);
            }
        }
        // --- FINE CORREZIONE ---

        return wallet;
    }

    private VirtualWallet leggiWalletBase(Studente s, String mailCercata) throws DAOException {
        File file = new File(walletFile);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(CSV_SEPARATOR, -1);
                // Formato: email;saldoDisponibile
                if (parts[0].trim().equals(mailCercata)) {
                    double saldo = Double.parseDouble(parts[1].trim());
                    return new VirtualWallet(s, saldo);
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new DAOException("Errore lettura file wallet", e);
        }
        return null;
    }

    private void popolaPosizioni(VirtualWallet wallet, String mailCercata) {
        File file = new File(posizioniFile);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(CSV_SEPARATOR, -1);
                // Formato: email;simbolo;quantita;prezzoMedio
                if (parts.length >= 4 && parts[0].trim().equals(mailCercata)) {
                    Stock stock = stockFactory.creaStock(parts[1].trim());
                    double quantita = Double.parseDouble(parts[2].trim());
                    double prezzoMedio = Double.parseDouble(parts[3].trim());

                    wallet.aggiungiPosizione(new WalletPosition(stock, quantita, prezzoMedio));
                }
            }
        } catch (Exception ignored) {
            // da fare
        }
    }

    private void popolaTransazioni(VirtualWallet wallet, String mailCercata) {
        File file = new File(transazioniFile);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(CSV_SEPARATOR, -1);
                // Formato reale scritto da TransactionDAOFile:
                // email(0);simbolo(1);tipo(2);stato(3);quantita(4);prezzo(5);timestamp(6)
                if (parts.length >= 6 && parts[0].trim().equals(mailCercata)) {
                    Stock stock          = stockFactory.creaStock(parts[1].trim());
                    TipoTransazione tipo  = TipoTransazione.valueOf(parts[2].trim());
                    StatoTransazione stato = StatoTransazione.valueOf(parts[3].trim());
                    double quantita      = Double.parseDouble(parts[4].trim());
                    double prezzo        = Double.parseDouble(parts[5].trim());

                    Transaction t = new Transaction(stock, tipo, quantita, prezzo);
                    if (stato == StatoTransazione.DONE) t.completaTransazione();
                    wallet.aggiungiTransazione(t);
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] popolaTransazioni: " + e.getMessage()); // almeno logga
        }
    }

    @Override
    public void salvaPortafoglio(VirtualWallet wallet) throws DAOException {
        if (wallet == null) {
            throw new DAOException("Il portafoglio non può essere nullo");
        }

        String email = wallet.proprietario().presentaEmail();

        // 1. Aggiorna il file base del wallet (il saldo)
        aggiornaFileWalletBase(email, wallet.saldoDisponibile());

        // 2. Aggiorna le posizioni associate all'utente
        aggiornaFilePosizioni(email, wallet.posizioni());

        // 3. Aggiorna le transazioni associate all'utente
        aggiornaFileTransazioni(email, wallet.transazioni());

        // 4. Infine, sincronizza la cache ereditata da CachedDAO
        addToCache(wallet);
    }

    /**
     * Aggiorna solo la riga corrispondente allo studente nel file wallet.csv.
     * Se non esiste, la aggiunge.
     */
    private void aggiornaFileWalletBase(String email, double nuovoSaldo) throws DAOException {
        File originalFile = new File(walletFile);
        File tempFile = new File(walletFile + ".tmp");
        boolean utenteTrovato = false;

        try {
            if (!originalFile.exists()) {
                boolean creato = originalFile.createNewFile();
                if (!creato) {
                    throw new IOException("Impossibile creare il file wallet base: " + originalFile.getName());
                }
            }

            try (BufferedReader br = new BufferedReader(new FileReader(originalFile));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(CSV_SEPARATOR, -1);
                    if (parts[0].trim().equals(email)) {
                        // Riga dell'utente trovata: scriviamo i dati aggiornati
                        bw.write(email + CSV_SEPARATOR + nuovoSaldo);
                        utenteTrovato = true;
                    } else {
                        // Ricopia gli altri utenti senza modifiche
                        bw.write(line);
                    }
                    bw.newLine();
                }

                // Se è un nuovo wallet mai salvato prima, aggiungilo in fondo
                if (!utenteTrovato) {
                    bw.write(email + CSV_SEPARATOR + nuovoSaldo);
                    bw.newLine();
                }
            }

            // Sostituisce il vecchio file con quello aggiornato
            sostituisciFile(originalFile, tempFile);

        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiornamento del file wallet: " + e.getMessage());
        }
    }

    /**
     * Riscrive il file delle posizioni filtrando via quelle vecchie dell'utente
     * e accodando lo stato attuale della lista.
     */
    private void aggiornaFilePosizioni(String email, List<WalletPosition> posizioniAttuali) throws DAOException {
        File originalFile = new File(posizioniFile);
        File tempFile = new File(posizioniFile + ".tmp");

        try {
            if (!originalFile.exists()) {
                boolean creato = originalFile.createNewFile();
                if (!creato) {
                    throw new IOException("Impossibile creare il file posizioni: " + originalFile.getName());
                }
            }
            try (BufferedReader br = new BufferedReader(new FileReader(originalFile));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                // 1. Ricopia tutto TRANNE le vecchie posizioni di questo specifico utente
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(CSV_SEPARATOR, -1);
                    if (!parts[0].trim().equals(email)) {
                        bw.write(line);
                        bw.newLine();
                    }
                }

                // 2. Scrivi le posizioni attuali (aggiornate)
                for (WalletPosition p : posizioniAttuali) {
                    // Formato: email;simbolo;quantita;prezzoMedio
                    bw.write(email + CSV_SEPARATOR +
                            p.stock().simbolo() + CSV_SEPARATOR +
                            p.quantita() + CSV_SEPARATOR +
                            p.prezzoMedioAcquisto());
                    bw.newLine();
                }
            }

            sostituisciFile(originalFile, tempFile);

        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiornamento del file posizioni: " + e.getMessage());
        }
    }

    /**
     * Riscrive il file delle transazioni allo stesso modo delle posizioni.
     */
    private void aggiornaFileTransazioni(String email, List<Transaction> transazioniAttuali) throws DAOException {
        File originalFile = new File(transazioniFile);
        File tempFile = new File(transazioniFile + ".tmp");

        try {
            if (!originalFile.exists()) {
                boolean creato = originalFile.createNewFile();
                if (!creato) {
                    throw new IOException("Impossibile creare il file transazioni: " + originalFile.getName());
                }
            }
            try (BufferedReader br = new BufferedReader(new FileReader(originalFile));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(CSV_SEPARATOR, -1);
                    if (!parts[0].trim().equals(email)) {
                        bw.write(line);
                        bw.newLine();
                    }
                }

                // Scrivi le transazioni attuali dell'utente
                for (Transaction t : transazioniAttuali) {
                    // Formato: email;simbolo;tipo;quantita;prezzo
                    bw.write(email + CSV_SEPARATOR +
                            t.stock().simbolo() + CSV_SEPARATOR +
                            t.tipo().name() + CSV_SEPARATOR +
                            t.quantita() + CSV_SEPARATOR +
                            t.prezzoAlMomento());
                    bw.newLine();
                }
            }

            sostituisciFile(originalFile, tempFile);

        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiornamento del file transazioni: " + e.getMessage());
        }
    }

    /**
     * Helper per gestire l'operazione sicura di rinomina/sovrascrittura del file
     */
    private void sostituisciFile(File originale, File temporaneo) throws IOException {
        Files.move(temporaneo.toPath(), originale.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    protected void doDeletePortafoglio(String email) throws DAOException {
        File file = new File(walletFile); // usa il path del file wallet
        if (!file.exists()) return;

        List<String> righe = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(CSV_SEPARATOR, -1);
                // Colonna 0 = emailStudente nel tuo CSV wallet
                if (parts.length > 0 && parts[0].trim().equals(email)) continue;
                righe.add(line);
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura wallet file per delete: " + e.getMessage());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String r : righe) { bw.write(r); bw.newLine(); }
        } catch (IOException e) {
            throw new DAOException("Errore scrittura wallet file per delete: " + e.getMessage());
        }
    }

}