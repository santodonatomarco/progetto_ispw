package org.project.dao.posizioni;

import org.project.exceptions.DAOException;
import org.project.ing.service.StockService;
import org.project.model.Stock;
import org.project.model.WalletPosition;

import java.io.*;
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(toCSV(email, p));
            bw.newLine();
        } catch (IOException e) {
            throw new DAOException("Errore scrittura posizione: " + e.getMessage());
        }
    }

    @Override
    protected void doUpdatePosizione(String email, WalletPosition p) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File posizioni non trovato.");

        List<String> righe = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEP, -1);
                if (parts.length >= 4 && parts[0].trim().equals(email) && parts[1].trim().equals(p.stock().simbolo())) {
                    if (p.quantita() > 0) {
                        righe.add(toCSV(email, p));
                    }
                    // quantita == 0: non si aggiunge → rimozione implicita
                } else {
                    righe.add(line);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file posizioni: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String r : righe) { bw.write(r); bw.newLine(); }
        } catch (IOException e) {
            throw new DAOException("Errore aggiornamento file posizioni: " + e.getMessage());
        }
    }

    @Override
    protected void doDeletePosizione(String email, WalletPosition p) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) return;

        List<String> righe = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(SEP, -1);

                if (parts.length < 2 || !parts[0].trim().equals(email) || !parts[1].trim().equals(p.stock().simbolo())) {
                    righe.add(line);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file posizioni: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String r : righe) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new DAOException("Errore eliminazione posizione: " + e.getMessage());
        }
    }

    @Override
    protected List<WalletPosition> doRetrievePosizioniByEmail(String email) throws DAOException {
        File file = new File(fileName);
        List<WalletPosition> lista = new ArrayList<>();
        if (!file.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEP, -1);
                if (parts.length >= 4 && parts[0].trim().equals(email)) {
                    WalletPosition pos = parse(parts);
                    if (pos != null) lista.add(pos);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura posizioni: " + e.getMessage());
        }
        return lista;
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

    @Override
    protected void doDeletePosizioniByEmail(String email) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) return;

        List<String> righe = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(SEP, -1);
                    // Add to 'righe' only if it doesn't match the email
                    if (parts.length == 0 || !parts[0].trim().equals(email)) {
                        righe.add(line);
                    }
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file posizioni per delete: " + e.getMessage());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String r : righe) { bw.write(r); bw.newLine(); }
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file posizioni per delete: " + e.getMessage());
        }
    }



}