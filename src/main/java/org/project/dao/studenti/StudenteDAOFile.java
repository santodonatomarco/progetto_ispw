package org.project.dao.studenti;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.model.Studente;
import org.project.model.StudenteLocale;
import org.project.model.StudenteOAuth;
import org.project.model.SchoolClass;
import org.project.dao.classi.SchoolClassDAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudenteDAOFile extends StudenteDAO {
    private String fileName;
    private static final String CSV_SEPARATOR = ";";
    private SchoolClassDAO schoolClassDAO;

    public StudenteDAOFile(String file, SchoolClassDAO schoolClassDAO) {
        fileName = file;
        this.schoolClassDAO = schoolClassDAO;
    }

    @Override
    protected Studente doRetrieveStudenteByEmail(String emailCercata) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File studenti non trovato");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Studente s = parseStudenteSeCorrisponde(line, emailCercata);
                if (s != null) {
                    return s;
                }
            }
            return null;
        } catch (IOException e) {
            throw new DAOException("Errore lettura file studenti.csv");
        }
    }

    @Override
    protected List<Studente> doRetrieveStudentiClasse(String nomeClasse) throws DAOException {
        List<Studente> studenti = new ArrayList<>();
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File studenti non trovato");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Studente s = parseStudenteSeCorrisponde(line, nomeClasse, true);
                if (s != null) {
                    studenti.add(s);
                }
            }
            return studenti;
        } catch (IOException e) {
            throw new DAOException("Errore lettura file studenti.csv");
        }
    }

    /**
     * Parse uno studente da una riga CSV se l'email corrisponde
     * Formato CSV: email;nome;cognome;passwordHash;nomeClasse;authProvider
     * NOTA: La classe NON viene assegnata qui. Deve essere assegnata dal service
     */
    private Studente parseStudenteSeCorrisponde(String line, String emailCercata) {
        return parseStudenteSeCorrisponde(line, emailCercata, false);
    }

    /**
     * Parse uno studente da una riga CSV
     * @param line La riga del CSV
     * @param filtro Email da cercare (per filtro email) o nomeClasse (per filtro classe)
     * @param filterByClass true se filtrare per classe, false per email
     */
    private Studente parseStudenteSeCorrisponde(String line, String filtro, boolean filterByClass) {
        if (line.trim().isEmpty()) return null;

        String[] parts = line.split(CSV_SEPARATOR, -1);
        // Formato: email;nome;cognome;passwordHash;nomeClasse;authProvider
        if (parts.length < 6) return null;

        try {
            String email = parts[0].trim();
            String nome = parts[1].trim();
            String cognome = parts[2].trim();
            String passwordHash = parts[3].trim();
            String nomeClasse = parts[4].trim();
            String authProvider = parts[5].trim();

            // Filtro
            if (filterByClass) {
                if (!nomeClasse.equals(filtro)) return null;
            } else {
                if (!email.equals(filtro)) return null;
            }

            // Costruisci lo Studente corretto in base all'AuthProvider
            Studente studente;
            if (AuthProvider.LOCAL.toString().equals(authProvider)) {
                studente = new StudenteLocale(email, nome, cognome);
                ((StudenteLocale) studente).inserisciHashPassword(passwordHash);
            } else {
                // OAuth (GOOGLE, MICROSOFT)
                AuthProvider provider = AuthProvider.valueOf(authProvider);
                studente = new StudenteOAuth(email, nome, cognome, provider);
            }

            // Assegna la classe di appartenenza se disponibile
            // Carica la classe dal SchoolClassDAO che garantisce un singleton (una sola istanza per classe)
            if (nomeClasse != null && !nomeClasse.isEmpty()) {
                try {
                    SchoolClass classe = schoolClassDAO.getClasseByNome(nomeClasse);
                    studente.iscriviClasse(classe);
                } catch (DAOException e) {
                    // Se la classe non può essere caricata, continua comunque
                }
            }

            return studente;

        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            // Ignora le righe eventualmente corrotte
            return null;
        }
    }





}
