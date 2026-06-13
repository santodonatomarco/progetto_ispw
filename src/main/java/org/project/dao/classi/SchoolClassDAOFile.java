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
        if (professore == null) {
            throw new DAOException("Professore non può essere nullo");
        }

        File file = new File(fileName);
        if (!file.exists()) {
            throw new DAOException("File classi non trovato");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Delega la logica di controllo e parsing a un metodo esterno
                SchoolClass classe = analizzaRiga(line, nomeCercato, professore);
                if (classe != null) {
                    return classe;
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file classi: " + e.getMessage());
        }

        return null;
    }

    private SchoolClass analizzaRiga(String line, String nomeCercato, Professore professore) {
        if (line.trim().isEmpty()) {
            return null;
        }

        String[] parts = line.split(SEP, -1);

        // Guard clause: se non ci sono abbastanza campi, scarta la riga
        if (parts.length < 2) {
            return null;
        }

        String nome = parts[0].trim();
        String emailProf = parts[1].trim();

        if (!nome.equals(nomeCercato) || !emailProf.equals(professore.presentaEmail())) {
            return null;
        }

        SchoolClass classe = new SchoolClass(nome, professore);
        impostaBudgetSePresente(classe, parts);
        return classe;
    }


    private void impostaBudgetSePresente(SchoolClass classe, String[] parts) {
        if (parts.length >= 3) {
            try {
                classe.impostaBudget(Double.parseDouble(parts[2].trim()));
            } catch (NumberFormatException ignored) {
                // Ignorato volontariamente come da codice originale
            }
        }
    }

    @Override
    public List<SchoolClass> getClassiByProfessore(Professore professore) throws DAOException {
        if (professore == null) {
            throw new DAOException("Il professore non può essere nullo");
        }

        File file = new File(fileName);
        if (!file.exists()) {
            throw new DAOException("File classi non trovato");
        }

        List<SchoolClass> classi = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Delega tutta la logica della singola riga a un helper
                elaboraRiga(line, professore, classi);
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file classi: " + e.getMessage());
        }

        return classi;
    }


    private void elaboraRiga(String line, Professore professore, List<SchoolClass> classi) {
        if (line.trim().isEmpty()) {
            return;
        }

        String[] parts = line.split(SEP, -1);

        if (parts.length < 2 || !parts[1].trim().equals(professore.presentaEmail())) {
            return;
        }

        SchoolClass classe = new SchoolClass(parts[0].trim(), professore);

        impostaBudgetSePresente(classe, parts);
        caricaStudenti(classe);

        classi.add(classe);
        addToCache(classe);
    }

    private void caricaStudenti(SchoolClass classe) {
        if (studenteDAO == null) {
            return;
        }

        try {
            List<Studente> studenti = studenteDAO.getStudentiClasse(classe);
            for (Studente s : studenti) {
                classe.iscriviStudente(s);
            }
        } catch (DAOException e) {
            // Non bloccare se gli studenti non si caricano
        }
    }

    @Override
    public void salvaClasse(SchoolClass classe) throws DAOException {
        if (classe == null) {
            throw new DAOException("La classe non può essere nulla");
        }

        File file = new File(fileName);

        // 1. Fase di Lettura e Aggiornamento
        List<String> righe = leggiEAggiornaRighe(file, classe);

        // 2. Fase di Scrittura
        scriviSuFile(file, righe);

        addToCache(classe);
    }


    private List<String> leggiEAggiornaRighe(File file, SchoolClass classe) throws DAOException {
        List<String> righe = new ArrayList<>();
        boolean trovata = false;

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    if (isRigaCorrispondente(line, classe)) {
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

        if (!trovata) {
            righe.add(toCSVLine(classe));
        }

        return righe;
    }


    private boolean isRigaCorrispondente(String line, SchoolClass classe) {
        String[] parts = line.split(SEP, -1);

        if (parts.length < 2) {
            return false;
        }

        boolean matchNome = parts[0].trim().equals(classe.nome());
        boolean matchProfessore = parts[1].trim().equals(classe.teacher().presentaEmail());

        return matchNome && matchProfessore;
    }


    private void scriviSuFile(File file, List<String> righe) throws DAOException {
        File tmp = new File(file.getAbsolutePath() + ".tmp");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
            for (String r : righe) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file classi", e);
        }

        try {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DAOException("Errore nel salvataggio file classi", e);
        }
    }

    private String toCSVLine(SchoolClass c) {
        return c.nome() + SEP + c.teacher().presentaEmail() + SEP + c.budgetIniziale();
    }
}