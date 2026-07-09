package org.project.ing.classifunzionali;

import org.project.exceptions.DAOException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class condivisa per le operazioni I/O CSV comuni ai DAO su file.
 * Elimina il boilerplate duplicato tra WalletPositionDAOFile e TransactionDAOFile.
 */
public final class DAOFileUtils {

    private DAOFileUtils() { /* utility class */}

    /**
     * Legge tutte le righe non vuote dal file.
     * Restituisce una lista vuota se il file non esiste.
     */
    public static List<String> leggiRighe(String fileName) throws DAOException {
        File file = new File(fileName);
        List<String> righe = new ArrayList<>();
        if (!file.exists()) return righe;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) righe.add(line);
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file: " + e.getMessage());
        }
        return righe;
    }

    /**
     * Sovrascrive il file con le righe fornite (modalità truncate+write).
     */
    public static void scriviRighe(String fileName, List<String> righe) throws DAOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false))) {
            for (String r : righe) { bw.write(r); bw.newLine(); }
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file: " + e.getMessage());
        }
    }

    /**
     * Appende una singola riga in coda al file.
     */
    public static void appendiRiga(String fileName, String riga) throws DAOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(riga);
            bw.newLine();
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file: " + e.getMessage());
        }
    }
}
