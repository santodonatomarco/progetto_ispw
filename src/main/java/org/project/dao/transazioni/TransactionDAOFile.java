package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.StatoTransazione;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.factory.StockFactoryProducer;
import org.project.ing.service.StockService;
import org.project.model.Stock;
import org.project.model.Transaction;

import java.io.*;
import java.time.LocalDateTime;
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
    protected void doSaveTransazione(Transaction t) throws DAOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(toCSV(t));
            bw.newLine();
        } catch (IOException e) {
            throw new DAOException("Errore scrittura transazione: " + e.getMessage());
        }
    }

    @Override
    protected void doUpdateTransazione(Transaction t) throws DAOException {
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
                // chiave: simbolo (indice 1) + timestamp (indice 6)
                if (parts.length >= 7 && (parts[1].trim() + SEP + parts[6].trim()).equals(chiave)) {
                    righe.add(toCSV(t));
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

    // ── Utility ──────────────────────────────────────────────────────────────

    private String toCSV(Transaction t) {
        return String.join(SEP,
                t.stock().simbolo(),
                t.tipo().toString(),
                t.stato().toString(),
                String.valueOf(t.quantita()),
                String.valueOf(t.prezzoAlMomento()),
                t.quando().toString());
    }

    private Transaction parse(String[] parts) {
        try {
            String simbolo   = parts[0].trim();
            TipoTransazione tipo  = TipoTransazione.valueOf(parts[1].trim());
            StatoTransazione stato = StatoTransazione.valueOf(parts[2].trim());
            double quantita  = Double.parseDouble(parts[3].trim());
            double prezzo    = Double.parseDouble(parts[4].trim());

            Stock stock = StockService.getInstance().ottieniOCreaStock(simbolo);
            Transaction t = new Transaction(stock, tipo, quantita, prezzo);
            if (stato == StatoTransazione.DONE) t.completaTransazione();
            return t;
        } catch (Exception e) {
            return null;
        }
    }
}