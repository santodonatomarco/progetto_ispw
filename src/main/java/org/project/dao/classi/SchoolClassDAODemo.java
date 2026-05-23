package org.project.dao.classi;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.model.Professore;
import org.project.model.SchoolClass;
import org.project.model.Studente;

import java.util.ArrayList;
import java.util.List;

public class SchoolClassDAODemo extends SchoolClassDAO {

    private List<SchoolClass> fintoDatabase;
    private ProfessoreDAO professoreDAO;
    private StudenteDAO studenteDAO;  // iniettato opzionalmente per caricare gli studenti

    public SchoolClassDAODemo(ProfessoreDAO professoreDAO) {
        super();
        this.fintoDatabase = new ArrayList<>();
        this.professoreDAO = professoreDAO;
        try {
            this.popolaDBFittizio();
        } catch (DAOException e) {
            System.err.println("[SchoolClassDAODemo] Avviso: impossibile popolare il DB fittizio: " + e.getMessage());
        }
    }

    /** Permette di iniettare lo StudenteDAO dopo la costruzione (evita ciclo di dipendenze). */
    public void setStudenteDAO(StudenteDAO studenteDAO) {
        this.studenteDAO = studenteDAO;
    }

    private void popolaDBFittizio() throws DAOException {
        Professore marioRossi = professoreDAO.getProfessoreByEmail("mario.rossi@univ.it");
        Professore luciaBianchi = professoreDAO.getProfessoreByEmail("lucia.bianchi@univ.it");

        if (marioRossi == null || luciaBianchi == null) {
            throw new DAOException("Professori demo non trovati — verifica ProfessoreDAODemo");
        }

        SchoolClass classe1A = new SchoolClass("1A", marioRossi);
        classe1A.impostaBudget(10000.0);

        SchoolClass classe1B = new SchoolClass("1B", luciaBianchi);
        classe1B.impostaBudget(10000.0);

        fintoDatabase.add(classe1A);
        fintoDatabase.add(classe1B);
        addToCache(classe1A);
        addToCache(classe1B);
    }

    @Override
    protected SchoolClass doRetrieveClasseByNomeEProfessore(String nomeClasse, Professore professore) throws DAOException {
        for (SchoolClass c : fintoDatabase) {
            if (c.nome().equals(nomeClasse) &&
                    c.teacher().presentaEmail().equals(professore.presentaEmail())) {
                return c;  // Ritorna la classe che ha già il budget settato
            }
        }
        return null;
    }

    @Override
    public List<SchoolClass> getClassiByProfessore(Professore professore) throws DAOException {
        if (professore == null) throw new DAOException("Il professore non può essere nullo");

        List<SchoolClass> classi = new ArrayList<>();
        for (SchoolClass c : fintoDatabase) {
            if (c.teacher() != null &&
                    c.teacher().presentaEmail().equals(professore.presentaEmail())) {
                // Carica gli studenti iscritti a questa classe
                if (studenteDAO != null) {
                    try {
                        List<Studente> studenti = studenteDAO.getStudentiClasse(c);
                        for (Studente s : studenti) c.iscriviStudente(s);
                    } catch (DAOException e) {
                        // Non bloccare se gli studenti non si caricano
                    }
                }
                classi.add(c);
                addToCache(c);
            }
        }
        return classi;
    }

    @Override
    public void salvaClasse(SchoolClass classe) throws DAOException {
        if (classe == null) throw new DAOException("La classe non può essere nulla");
        fintoDatabase.removeIf(c -> c.nome().equals(classe.nome()) &&
                c.teacher().presentaEmail().equals(classe.teacher().presentaEmail()));
        fintoDatabase.add(classe);
        addToCache(classe);
    }
}