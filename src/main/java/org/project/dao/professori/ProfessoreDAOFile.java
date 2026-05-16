package org.project.dao.professori;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.model.*;

import java.io.*;


public class ProfessoreDAOFile extends ProfessoreDAO {

    private String fileName;
    private static final String CSV_SEPARATOR = ";";

    public ProfessoreDAOFile(String fileName) {
        super();
        this.fileName = fileName;
    }

    @Override
    protected Professore doRetrieveProfessoreByEmail(String emailCercata) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File professori non trovato");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(CSV_SEPARATOR, -1);
                // Formato: email;nome;cognome;passwordHash;authProvider
                if (parts.length >= 5 && parts[0].trim().equals(emailCercata)) {
                    return parseProfessore(parts);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DAOException("Errore lettura file professori: " + e.getMessage());
        }
        return null;
    }

    /**
     * Aggiunge il professore in fondo al CSV.
     * Lancia DAOException se esiste già una riga con la stessa email.
     */
    @Override
    protected void doSaveProfessore(Professore professore) throws DAOException {
        File file = new File(fileName);

        // Verifica duplicato
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(CSV_SEPARATOR, -1);
                    if (parts.length > 0 && parts[0].trim().equals(professore.presentaEmail())) {
                        throw new DAOException("Esiste già un professore con questa email.");
                    }
                }
            } catch (IOException e) {
                throw new DAOException("Errore lettura file professori: " + e.getMessage());
            }
        }

        // Aggiunge in append
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(toCSV(professore));
            bw.newLine();
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file professori: " + e.getMessage());
        }
    }

    // ── Metodi privati ────────────────────────────────────────────────────────

    private Professore parseProfessore(String[] parts) {
        String email       = parts[0].trim();
        String nome        = parts[1].trim();
        String cognome     = parts[2].trim();
        String pwdHash     = parts[3].trim();
        String authProv    = parts[4].trim();

        if (AuthProvider.LOCAL.toString().equals(authProv)) {
            ProfessoreLocale prof = new ProfessoreLocale(email, nome, cognome);
            prof.inserisciHashPassword(pwdHash);
            return prof;
        } else {
            return new ProfessoreOAuth(email, nome, cognome, AuthProvider.valueOf(authProv));
        }
    }

    private String toCSV(Professore professore) {
        String passwordHash = "";
        if (professore instanceof AutenticazioneLocale) {
            passwordHash = ((AutenticazioneLocale) professore).passwordHash();
        }
        return String.join(CSV_SEPARATOR,
                professore.presentaEmail(),
                professore.presentaNome(),
                professore.presentaCognome(),
                passwordHash,
                professore.comeAccede().toString());
    }
}