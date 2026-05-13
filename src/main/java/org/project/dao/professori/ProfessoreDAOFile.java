package org.project.dao.professori;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.model.Professore;
import org.project.model.ProfessoreLocale;
import org.project.model.ProfessoreOAuth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
                if (parts.length >= 5) {
                    String email = parts[0].trim();
                    if (email.equals(emailCercata)) {
                        String nome = parts[1].trim();
                        String cognome = parts[2].trim();
                        String pwdHash = parts[3].trim();
                        String authProv = parts[4].trim();

                        if (AuthProvider.LOCAL.toString().equals(authProv)) {
                            ProfessoreLocale prof = new ProfessoreLocale(email, nome, cognome);
                            prof.inserisciHashPassword(pwdHash);
                            return prof;
                        } else {
                            return new ProfessoreOAuth(email, nome, cognome, AuthProvider.valueOf(authProv));
                        }
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DAOException("Errore lettura file professori: " + e.getMessage());
        }
        return null;
    }
}