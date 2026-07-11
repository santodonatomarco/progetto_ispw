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
import java.time.LocalDateTime;



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

        VirtualWallet wallet = leggiWalletBase(studente, mailCercata);
        if (wallet == null) return null;

        popolaPosizioni(wallet, mailCercata);
        popolaTransazioni(wallet, mailCercata);
        allineaBudgetClasse(wallet, studente);

        return wallet; // niente più build()
    }

    private VirtualWallet leggiWalletBase(Studente s, String mailCercata) throws DAOException {
        File file = new File(walletFile);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(CSV_SEPARATOR, -1);
                if (parts[0].trim().equals(mailCercata)) {
                    double saldo = Double.parseDouble(parts[1].trim());
                    s.creaWallet(saldo);       // nasce dentro Studente
                    return s.portafoglio();    // solo navigazione
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new DAOException("Errore lettura file wallet", e);
        }
        return null;
    }


    private void allineaBudgetClasse(VirtualWallet wallet, Studente studente) throws DAOException {
        if (studente.classeFrequentata() == null) {
            return;
        }

        double budgetAttualeClasse = studente.classeFrequentata().budgetIniziale();
        double spesaStorica = calcolaSpesaStorica(wallet);
        double budgetAssegnatoInPassato = wallet.saldoDisponibile() + spesaStorica;

        double differenza = budgetAttualeClasse - budgetAssegnatoInPassato;

        if (Math.abs(differenza) > 0.01) {
            applicaDifferenzaBudget(wallet, differenza);   // il budget della classe è stato modificato
            salvaPortafoglio(wallet);
        }
    }


    private double calcolaSpesaStorica(VirtualWallet wallet) {
        if (wallet.posizioni() == null) {
            return 0.0;
        }

        double spesa = 0.0;
        for (WalletPosition p : wallet.posizioni()) {
            spesa += (p.quantita() * p.prezzoMedioAcquisto());
        }
        return spesa;
    }

    private void applicaDifferenzaBudget(VirtualWallet wallet, double differenza) {
        if (differenza > 0) {
            wallet.accreditaSaldo(differenza);
            return;
        }

        double daScalare = Math.abs(differenza);
        // Non può andare in negativo
        wallet.scalaSaldo(Math.min(wallet.saldoDisponibile(), daScalare));
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

                    wallet.caricaPosizione(stock, quantita, prezzoMedio);
                }
            }
        } catch (Exception ignored) {
            // file non letto
        }
    }

    private void popolaTransazioni(VirtualWallet wallet, String mailCercata) {
        File file = new File(transazioniFile);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                processaRigaTransazione(wallet, mailCercata, line);
            }
        } catch (Exception e) {
            System.err.println("[WARN] popolaTransazioni: errore lettura file → " + e.getMessage());
        }
    }

    private void processaRigaTransazione(VirtualWallet wallet, String mailCercata, String line) {
        if (line.trim().isEmpty()) {
            return;
        }

        String[] parts = line.split(CSV_SEPARATOR, -1);

        if (parts.length < 5 || !parts[0].trim().equals(mailCercata)) {
            return;
        }

        try {
            aggiungiTransazioneDaArray(wallet, parts);
        } catch (Exception e) {
            System.err.println("[WARN] popolaTransazioni: riga ignorata → " + e.getMessage());
        }
    }


    private void aggiungiTransazioneDaArray(VirtualWallet wallet, String[] parts) throws IOException {
        String simbolo = parts[1].trim();
        TipoTransazione tipo = TipoTransazione.valueOf(parts[2].trim());
        StatoTransazione stato;
        double quantita;
        double prezzo;

        // email;simbolo;tipo;stato;quantita;prezzo;timestamp
        if (parts.length < 7) {
            throw new IOException("Formato transazione non valido: numero campi < 7");
        }

        // Parse fields
        stato = StatoTransazione.valueOf(parts[3].trim());
        quantita = Double.parseDouble(parts[4].trim());
        prezzo = Double.parseDouble(parts[5].trim());
        LocalDateTime ts = LocalDateTime.parse(parts[6].trim());

        Stock stock = stockFactory.creaStock(simbolo);
        wallet.caricaTransazione(stock, tipo, quantita, prezzo, ts,
                stato == StatoTransazione.DONE);
    }

    @Override
    public void salvaPortafoglio(VirtualWallet wallet) throws DAOException {
        if (wallet == null) {
            throw new DAOException("Il portafoglio non può essere nullo");
        }

        String email = wallet.proprietario().presentaEmail();

        aggiornaFileWalletBase(email, wallet.saldoDisponibile());

        aggiornaFilePosizioni(email, wallet.posizioni());

        aggiornaFileTransazioni(email, wallet.transazioni());

        addToCache(wallet);
    }


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

            sostituisciFile(originalFile, tempFile);

        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiornamento del file wallet: " + e.getMessage());
        }
    }


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
                // email;simbolo;tipo;stato;quantita;prezzo;timestamp
                for (Transaction t : transazioniAttuali) {
                    bw.write(email + CSV_SEPARATOR +
                            t.stock().simbolo() + CSV_SEPARATOR +
                            t.tipo().name() + CSV_SEPARATOR +
                            t.stato().toString() + CSV_SEPARATOR +
                            t.quantita() + CSV_SEPARATOR +
                            t.prezzoAlMomento() + CSV_SEPARATOR +
                            t.quando().toString());
                    bw.newLine();
                }
            }

            sostituisciFile(originalFile, tempFile);

        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiornamento del file transazioni: " + e.getMessage());
        }
    }


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
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(CSV_SEPARATOR, -1);

                if (parts.length == 0 || !parts[0].trim().equals(email)) {
                    righe.add(line);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura wallet file per delete: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String r : righe) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new DAOException("Errore scrittura wallet file per delete: " + e.getMessage());
        }
    }

}