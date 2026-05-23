package org.project.dao.classi;

import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.model.Professore;
import org.project.model.SchoolClass;
import org.project.model.Studente;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class SchoolClassDAOFile extends SchoolClassDAO {

    private String fileName;
    private static final String SEP = ";";
    private StudenteDAO studenteDAO;  // iniettato opzionalmente per caricare gli studenti

    public SchoolClassDAOFile(String fileName) {
        super();
        this.fileName = fileName;
    }

    /** Permette di iniettare lo StudenteDAO dopo la costruzione (evita ciclo di dipendenze). */
    public void setStudenteDAO(StudenteDAO studenteDAO) {
        this.studenteDAO = studenteDAO;
    }


    @Override
    protected SchoolClass doRetrieveClasseByNomeEProfessore(String nomeCercato, Professore professore) throws DAOException {
        if (professore == null) throw new DAOException("Professore non può essere nullo");

        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File classi non trovato");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEP, -1);
                if (parts.length >= 2) {
                    String nome = parts[0].trim();
                    String emailProf = parts[1].trim();
                    if (nome.equals(nomeCercato) && emailProf.equals(professore.presentaEmail())) {
                        SchoolClass classe = new SchoolClass(nome, professore);
                        if (parts.length >= 3) {
                            try { classe.impostaBudget(Double.parseDouble(parts[2].trim())); }
                            catch (NumberFormatException ignored) {}
                        }
                        return classe;
                    }
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file classi: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SchoolClass> getClassiByProfessore(Professore professore) throws DAOException {
        if (professore == null) throw new DAOException("Il professore non può essere nullo");

        List<SchoolClass> classi = new ArrayList<>();
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File classi non trovato");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEP, -1);
                if (parts.length >= 2 && parts[1].trim().equals(professore.presentaEmail())) {
                    SchoolClass classe = new SchoolClass(parts[0].trim(), professore);
                    if (parts.length >= 3) {
                        try { classe.impostaBudget(Double.parseDouble(parts[2].trim())); }
                        catch (NumberFormatException ignored) {}
                    }
                    // Carica gli studenti iscritti a questa classe
                    if (studenteDAO != null) {
                        try {
                            List<Studente> studenti = studenteDAO.getStudentiClasse(classe);
                            for (Studente s : studenti) classe.iscriviStudente(s);
                        } catch (DAOException e) {
                            // Non bloccare se gli studenti non si caricano
                        }
                    }
                    classi.add(classe);
                    addToCache(classe);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file classi: " + e.getMessage());
        }
        return classi;
    }

    @Override
    public void salvaClasse(SchoolClass classe) throws DAOException {
        if (classe == null) throw new DAOException("La classe non può essere nulla");

        File file = new File(fileName);
        List<String> righe = new ArrayList<>();
        boolean trovata = false;

        // Leggi righe esistenti, sostituisci se già presente
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(SEP, -1);
                    if (parts.length >= 2
                            && parts[0].trim().equals(classe.nome())
                            && parts[1].trim().equals(classe.teacher().presentaEmail())) {
                        righe.add(toCSVLine(classe));
                        trovata = true;
                    } else {
                        righe.add(line);
                    }
                }
            } catch (IOException e) {
                throw new DAOException("Errore lettura file classi", e);
            }
        }

        if (!trovata) righe.add(toCSVLine(classe));

        // Scrivi atomicamente su file temporaneo poi rinomina
        File tmp = new File(fileName + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
            for (String r : righe) { bw.write(r); bw.newLine(); }
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file classi", e);
        }
        try {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DAOException("Errore nel salvataggio file classi", e);
        }

        addToCache(classe);
    }

    private String toCSVLine(SchoolClass c) {
        return c.nome() + SEP + c.teacher().presentaEmail() + SEP + c.budgetIniziale();
    }
}