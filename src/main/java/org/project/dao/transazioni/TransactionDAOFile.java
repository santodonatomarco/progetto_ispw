package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.StatoTransazione;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.service.StockService;
import org.project.model.Stock;
import org.project.model.Transaction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOFile extends TransactionDAO {

    private final String fileName;
    private static final String SEP = ";";

    public TransactionDAOFile(String fileName) {
        this.fileName = fileName;
    }

    // Formato CSV: emailStudente;simbolo;tipo;stato;quantita;prezzoAlMomento;timestamp

    @Override
    protected void doSaveTransazione(String email, Transaction t) throws DAOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(toCSV(email, t));
            bw.newLine();
        } catch (IOException e) {
            throw new DAOException("Errore scrittura transazione: " + e.getMessage());
        }
    }

    @Override
    protected void doUpdateTransazione(String email, Transaction t) throws DAOException {
        // Riscrive il file aggiornando la riga con lo stesso simbolo+timestamp
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File transazioni non trovato.");

        List<String> righe = new ArrayList<>();
        String chiave = t.stock().simbolo() + SEP + t.quando().toString();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEP, -1);
                // chiave: email (indice 0) + simbolo (indice 1) + timestamp (indice 6)
                if (parts.length >= 7 && parts[0].trim().equals(email) &&
                        (parts[1].trim() + SEP + parts[6].trim()).equals(chiave)) {
                    righe.add(toCSV(email, t));
                } else {
                    righe.add(line);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file transazioni: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String r : righe) { bw.write(r); bw.newLine(); }
        } catch (IOException e) {
            throw new DAOException("Errore aggiornamento file transazioni: " + e.getMessage());
        }
    }

    @Override
    protected void doUpdateTransazione(String email, Transaction t, java.time.LocalDateTime oldTimestamp) throws DAOException {
        // Riscrive il file aggiornando la riga con lo stesso simbolo+oldTimestamp
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File transazioni non trovato.");

        List<String> righe = new ArrayList<>();
        String chiaveOld = t.stock().simbolo() + SEP + oldTimestamp.toString();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEP, -1);
                // chiave: email (indice 0) + simbolo (indice 1) + timestamp (indice 6)
                if (parts.length >= 7 && parts[0].trim().equals(email) &&
                        (parts[1].trim() + SEP + parts[6].trim()).equals(chiaveOld)) {
                    righe.add(toCSV(email, t));
                } else {
                    righe.add(line);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file transazioni: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String r : righe) { bw.write(r); bw.newLine(); }
        } catch (IOException e) {
            throw new DAOException("Errore aggiornamento file transazioni: " + e.getMessage());
        }
    }

    @Override
    protected List<Transaction> doRetrieveTransazioniByEmail(String email) throws DAOException {
        File file = new File(fileName);
        List<Transaction> lista = new ArrayList<>();
        if (!file.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEP, -1);
                // Expect CSV format: email;simbolo;tipo;stato;quantita;prezzo;timestamp
                if (parts.length >= 7 && parts[0].trim().equals(email)) {
                    Transaction t = parse(parts);
                    if (t != null) lista.add(t);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura transazioni: " + e.getMessage());
        }
        return lista;
    }

    // ── Utility ──

    private String toCSV(String email, Transaction t) {
        return String.join(SEP,
                email,
                t.stock().simbolo(),
                t.tipo().toString(),
                t.stato().toString(),
                String.valueOf(t.quantita()),
                String.valueOf(t.prezzoAlMomento()),
                t.quando().toString());
    }

    private Transaction parse(String[] parts) {
        try {
            String simbolo = parts[1].trim();
            TipoTransazione tipo = TipoTransazione.valueOf(parts[2].trim());

            StatoTransazione stato;
            double quantita;
            double prezzo;

            stato   = StatoTransazione.valueOf(parts[3].trim());
            quantita = Double.parseDouble(parts[4].trim());
            prezzo   = Double.parseDouble(parts[5].trim());

            Stock stock = StockService.getInstance().ottieniOCreaStock(simbolo);
            // ripristina il timestamp salvato (campo indice 6)
            java.time.LocalDateTime ts = java.time.LocalDateTime.parse(parts[6].trim());
            Transaction t = new Transaction(stock, tipo, quantita, prezzo, ts);
            if (stato == StatoTransazione.DONE) t.completaTransazione();
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doDeleteTransazioniByEmail(String email) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) return;

        List<String> righe = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(SEP, -1);

                if (parts.length == 0 || !parts[0].trim().equals(email)) {
                    righe.add(line);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file transazioni per delete: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String r : righe) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file transazioni per delete: " + e.getMessage());
        }
    }



}