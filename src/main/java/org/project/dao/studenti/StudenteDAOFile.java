package org.project.dao.studenti;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.model.*;
import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.professori.ProfessoreDAO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StudenteDAOFile extends StudenteDAO {

    private String fileName;
    private static final String CSV_SEPARATOR = ";";
    private SchoolClassDAO schoolClassDAO;
    private ProfessoreDAO professoreDAO;

    public StudenteDAOFile(String file, SchoolClassDAO schoolClassDAO, ProfessoreDAO professoreDAO) {
        this.fileName = file;
        this.schoolClassDAO = schoolClassDAO;
        this.professoreDAO = professoreDAO;
    }

    @Override
    protected Studente doRetrieveStudenteByEmail(String emailCercata) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File studenti non trovato");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Studente s = parseStudenteSeCorrisponde(line, emailCercata, false);
                if (s != null) return s;
            }
            return null;
        } catch (IOException e) {
            throw new DAOException("Errore lettura file studenti: " + e.getMessage());
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
                if (s != null) studenti.add(s);
            }
            return studenti;
        } catch (IOException e) {
            throw new DAOException("Errore lettura file studenti: " + e.getMessage());
        }
    }

    // Formato CSV: email;nome;cognome;passwordHash;nomeClasse;professore_email;authProvider
    @Override
    protected void doSaveStudente(Studente studente) throws DAOException {
        File file = new File(fileName);
        List<String> righe = new ArrayList<>();
        boolean aggiornato = false;

        if (file.exists()) {
            aggiornato = leggiEAggiornaRighe(file, studente, righe);
        }

        if (!aggiornato) {
            righe.add(toCSV(studente));
        }

        scriviRigheSuFile(file, righe);
    }

    // ── Metodi di utilità ───

    private boolean leggiEAggiornaRighe(File file, Studente studente, List<String> righe) throws DAOException {
        boolean aggiornato = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(CSV_SEPARATOR, -1);
                if (parts.length > 0 && parts[0].trim().equals(studente.presentaEmail())) {
                    righe.add(toCSV(studente));
                    aggiornato = true;
                } else {
                    righe.add(line);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file studenti: " + e.getMessage());
        }
        return aggiornato;
    }

    private void scriviRigheSuFile(File file, List<String> righe) throws DAOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String riga : righe) {
                bw.write(riga);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file studenti: " + e.getMessage());
        }
    }

    private String toCSV(Studente studente) {
        String nomeClasse = studente.classeFrequentata() != null ? studente.classeFrequentata().nome() : "";
        String emailProf  = (studente.classeFrequentata() != null && studente.classeFrequentata().teacher() != null)
                ? studente.classeFrequentata().teacher().presentaEmail() : "";
        return String.join(CSV_SEPARATOR,
                studente.presentaEmail(),
                studente.presentaNome(),
                studente.presentaCognome(),
                studente.getPasswordHash(),
                nomeClasse,
                emailProf,
                studente.comeAccede().toString());
    }

    private Studente parseStudenteSeCorrisponde(String line, String filtro, boolean filterByClass) {
        if (line.trim().isEmpty()) return null;
        String[] parts = line.split(CSV_SEPARATOR, -1);
        if (parts.length < 7) return null;

        try {
            String email        = parts[0].trim();
            String nome         = parts[1].trim();
            String cognome      = parts[2].trim();
            String passwordHash = parts[3].trim();
            String nomeClasse   = parts[4].trim();
            String profEmail    = parts[5].trim();
            String authProvider = parts[6].trim();

            boolean corrisponde = filterByClass ? nomeClasse.equals(filtro) : email.equals(filtro);
            if (!corrisponde) return null;

            AuthProvider provider = AuthProvider.valueOf(authProvider);
            Studente studente = new Studente(email, nome, cognome, provider);
            if (provider == AuthProvider.LOCAL && !passwordHash.isEmpty()) {
                studente.impostaPasswordHash(passwordHash);
            }

            assegnaClasseSePresente(studente, nomeClasse, profEmail);
            return studente;

        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private void assegnaClasseSePresente(Studente studente, String nomeClasse, String profEmail) {
        if (nomeClasse.isEmpty() || profEmail.isEmpty()) return;
        try {
            Professore professore = professoreDAO.getProfessoreByEmail(profEmail);
            if (professore == null) return;
            SchoolClass classe = schoolClassDAO.getClasseByNomeEProfessore(nomeClasse, professore);
            if (classe != null) studente.iscriviClasse(classe);
        } catch (DAOException e) {
            // Classe non caricabile — lo studente rimane senza classe
        }
    }


    @Override
    protected void doDeleteStudente(String email) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) return;

        List<String> righe = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(CSV_SEPARATOR, -1);

                if (parts.length == 0 || !parts[0].trim().equals(email)) {
                    righe.add(line);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file studenti per delete: " + e.getMessage());
        }
        scriviRigheSuFile(file, righe);
    }



}
