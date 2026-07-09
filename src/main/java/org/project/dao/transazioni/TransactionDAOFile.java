package org.project.dao.transazioni;

import org.project.ing.classifunzionali.DAOFileUtils;
import org.project.exceptions.DAOException;
import org.project.ing.enumerations.StatoTransazione;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.service.StockService;
import org.project.model.Stock;
import org.project.model.Transaction;

import java.io.File;
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
    protected void doSaveTransazione(String email, Transaction t) throws DAOException {
        DAOFileUtils.appendiRiga(fileName, toCSV(email, t));
    }

    @Override
    protected void doUpdateTransazione(String email, Transaction t) throws DAOException {
        if (!new File(fileName).exists()) throw new DAOException("File transazioni non trovato.");
        String chiave = t.stock().simbolo() + SEP + t.quando().toString();
        DAOFileUtils.scriviRighe(fileName, aggiornaRighe(email, t, chiave));
    }

    @Override
    protected void doUpdateTransazione(String email, Transaction t, LocalDateTime oldTimestamp) throws DAOException {
        if (!new File(fileName).exists()) throw new DAOException("File transazioni non trovato.");
        String chiaveOld = t.stock().simbolo() + SEP + oldTimestamp.toString();
        DAOFileUtils.scriviRighe(fileName, aggiornaRighe(email, t, chiaveOld));
    }

    @Override
    protected List<Transaction> doRetrieveTransazioniByEmail(String email) throws DAOException {
        List<Transaction> lista = new ArrayList<>();
        for (String line : DAOFileUtils.leggiRighe(fileName)) {
            String[] parts = line.split(SEP, -1);
            // Expect CSV format: email;simbolo;tipo;stato;quantita;prezzo;timestamp
            if (parts.length >= 7 && parts[0].trim().equals(email)) {
                Transaction t = parse(parts);
                if (t != null) lista.add(t);
            }
        }
        return lista;
    }

    @Override
    protected void doDeleteTransazioniByEmail(String email) throws DAOException {
        if (!new File(fileName).exists()) return;

        List<String> righe = new ArrayList<>();
        for (String line : DAOFileUtils.leggiRighe(fileName)) {
            String[] parts = line.split(SEP, -1);
            if (parts.length == 0 || !parts[0].trim().equals(email)) righe.add(line);
        }
        DAOFileUtils.scriviRighe(fileName, righe);
    }

    // ── Utility private ──

    private List<String> aggiornaRighe(String email, Transaction t, String chiave) throws DAOException {
        // chiave: simbolo (indice 1) + SEP + timestamp (indice 6)
        List<String> righe = new ArrayList<>();
        for (String line : DAOFileUtils.leggiRighe(fileName)) {
            String[] parts = line.split(SEP, -1);
            if (parts.length >= 7 && parts[0].trim().equals(email) &&
                    (parts[1].trim() + SEP + parts[6].trim()).equals(chiave)) {
                righe.add(toCSV(email, t));
            } else {
                righe.add(line);
            }
        }
        return righe;
    }

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
            String simbolo           = parts[1].trim();
            TipoTransazione tipo     = TipoTransazione.valueOf(parts[2].trim());
            StatoTransazione stato   = StatoTransazione.valueOf(parts[3].trim());
            double quantita          = Double.parseDouble(parts[4].trim());
            double prezzo            = Double.parseDouble(parts[5].trim());
            Stock stock              = StockService.getInstance().ottieniOCreaStock(simbolo);
            // ripristina il timestamp salvato (campo indice 6)
            LocalDateTime ts         = LocalDateTime.parse(parts[6].trim());
            Transaction tx           = new Transaction(stock, tipo, quantita, prezzo, ts);
            if (stato == StatoTransazione.DONE) tx.completaTransazione();
            return tx;
        } catch (Exception e) {
            return null;
        }
    }
}