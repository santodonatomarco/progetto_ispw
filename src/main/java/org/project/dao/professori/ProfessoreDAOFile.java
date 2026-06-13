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

    @Override
    protected void doSaveProfessore(Professore professore) throws DAOException {
        File file = new File(fileName);

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

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(toCSV(professore));
            bw.newLine();
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file professori: " + e.getMessage());
        }
    }

    private Professore parseProfessore(String[] parts) {
        String email    = parts[0].trim();
        String nome     = parts[1].trim();
        String cognome  = parts[2].trim();
        String pwdHash  = parts[3].trim();
        String authProv = parts[4].trim();

        AuthProvider provider = AuthProvider.valueOf(authProv);
        Professore prof = new Professore(email, nome, cognome, provider);
        if (provider == AuthProvider.LOCAL && !pwdHash.isEmpty()) {
            prof.impostaPasswordHash(pwdHash);
        }
        return prof;
    }

    private String toCSV(Professore professore) {
        return String.join(CSV_SEPARATOR,
                professore.presentaEmail(),
                professore.presentaNome(),
                professore.presentaCognome(),
                professore.getPasswordHash(),
                professore.comeAccede().toString());
    }
}