package org.project.dao.posizioni;

import org.project.ing.classifunzionali.DAOFileUtils;
import org.project.exceptions.DAOException;
import org.project.ing.service.StockService;
import org.project.model.Stock;
import org.project.model.WalletPosition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WalletPositionDAOFile extends WalletPositionDAO {

    private final String fileName;
    private static final String SEP = ";";

    public WalletPositionDAOFile(String fileName) {
        this.fileName = fileName;
    }

    // Formato CSV: emailStudente;simbolo;quantita;prezzoMedioAcquisto

    @Override
    protected void doSavePosizione(String email, WalletPosition p) throws DAOException {
        DAOFileUtils.appendiRiga(fileName, toCSV(email, p));
    }

    @Override
    protected void doUpdatePosizione(String email, WalletPosition p) throws DAOException {
        if (!new File(fileName).exists()) throw new DAOException("File posizioni non trovato.");

        List<String> righe = new ArrayList<>();
        for (String line : DAOFileUtils.leggiRighe(fileName)) {
            String[] parts = line.split(SEP, -1);
            if (parts.length >= 4 && parts[0].trim().equals(email) && parts[1].trim().equals(p.stock().simbolo())) {
                if (p.quantita() > 0) righe.add(toCSV(email, p));
                // quantita == 0: non si aggiunge → rimozione implicita
            } else {
                righe.add(line);
            }
        }
        DAOFileUtils.scriviRighe(fileName, righe);
    }

    @Override
    protected void doDeletePosizione(String email, WalletPosition p) throws DAOException {
        if (!new File(fileName).exists()) return;

        List<String> righe = new ArrayList<>();
        for (String line : DAOFileUtils.leggiRighe(fileName)) {
            String[] parts = line.split(SEP, -1);
            if (parts.length < 2 || !parts[0].trim().equals(email) || !parts[1].trim().equals(p.stock().simbolo())) {
                righe.add(line);
            }
        }
        DAOFileUtils.scriviRighe(fileName, righe);
    }

    @Override
    protected List<WalletPosition> doRetrievePosizioniByEmail(String email) throws DAOException {
        List<WalletPosition> lista = new ArrayList<>();
        for (String line : DAOFileUtils.leggiRighe(fileName)) {
            String[] parts = line.split(SEP, -1);
            if (parts.length >= 4 && parts[0].trim().equals(email)) {
                WalletPosition pos = parse(parts);
                if (pos != null) lista.add(pos);
            }
        }
        return lista;
    }

    @Override
    protected void doDeletePosizioniByEmail(String email) throws DAOException {
        if (!new File(fileName).exists()) return;

        List<String> righe = new ArrayList<>();
        for (String line : DAOFileUtils.leggiRighe(fileName)) {
            String[] parts = line.split(SEP, -1);
            if (parts.length == 0 || !parts[0].trim().equals(email)) righe.add(line);
        }
        DAOFileUtils.scriviRighe(fileName, righe);
    }

    // ── Utility ───

    private String toCSV(String email, WalletPosition p) {
        return String.join(SEP,
                email,
                p.stock().simbolo(),
                String.valueOf(p.quantita()),
                String.valueOf(p.prezzoMedioAcquisto()));
    }

    private WalletPosition parse(String[] parts) {
        try {
            String simbolo     = parts[1].trim();
            double quantita    = Double.parseDouble(parts[2].trim());
            double prezzoMedio = Double.parseDouble(parts[3].trim());
            Stock stock = StockService.getInstance().ottieniOCreaStock(simbolo);
            return new WalletPosition(stock, quantita, prezzoMedio);
        } catch (Exception e) {
            return null;
        }
    }
}