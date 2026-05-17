package org.project.dao.studenti;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.model.*;
import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.professori.ProfessoreDAO;

import java.util.ArrayList;
import java.util.List;

public class StudenteDAODemo extends StudenteDAO {

    private List<Studente> fintoDatabase;
    private SchoolClassDAO schoolClassDAO;
    private ProfessoreDAO professoreDAO;

    public StudenteDAODemo(SchoolClassDAO schoolClassDAO, ProfessoreDAO professoreDAO) {
        super();
        this.fintoDatabase = new ArrayList<>();
        this.schoolClassDAO = schoolClassDAO;
        this.professoreDAO = professoreDAO;
        try {
            this.popolaDBFittizio();
        } catch (DAOException e) {
            // Se il DB demo non si popola correttamente, partiamo con lista vuota
            // e logghiamo il problema — non crashiamo l'app
            System.err.println("[StudenteDAODemo] Avviso: impossibile popolare il DB fittizio: " + e.getMessage());
        }
    }

    private void popolaDBFittizio() throws DAOException {
        try {
            Professore marioRossi = professoreDAO.getProfessoreByEmail("mario.rossi@univ.it");
            Professore luciaBianchi = professoreDAO.getProfessoreByEmail("lucia.bianchi@univ.it");

            if (marioRossi == null || luciaBianchi == null) {
                throw new DAOException("Professori demo non trovati — verifica ProfessoreDAODemo");
            }

            SchoolClass classe1A = schoolClassDAO.getClasseByNomeEProfessore("1A", marioRossi);
            SchoolClass classe1B = schoolClassDAO.getClasseByNomeEProfessore("1B", luciaBianchi);

            if (classe1A == null || classe1B == null) {
                throw new DAOException("Classi demo non trovate — verifica SchoolClassDAODemo");
            }

            // Studente locale
            StudenteLocale s1 = new StudenteLocale("alice.verdi@student.it", "Alice", "Verdi");
            s1.inserisciHashPassword(org.project.ing.classifunzionali.Hasher.codifica("alice123"));
            s1.iscriviClasse(classe1A);
            fintoDatabase.add(s1);

            // Studente locale
            StudenteLocale s2 = new StudenteLocale("bob.neri@student.it", "Bob", "Neri");
            s2.inserisciHashPassword(org.project.ing.classifunzionali.Hasher.codifica("bob123"));
            s2.iscriviClasse(classe1A);
            fintoDatabase.add(s2);

            // Studente OAuth
            StudenteOAuth s3 = new StudenteOAuth("carlo.smith@student.it", "Carlo", "Smith", AuthProvider.GOOGLE);
            s3.iscriviClasse(classe1B);
            fintoDatabase.add(s3);

            StudenteOAuth s4 = new StudenteOAuth("diana.jones@student.it", "Diana", "Jones", AuthProvider.MICROSOFT);
            s4.iscriviClasse(classe1B);
            fintoDatabase.add(s4);

        } catch (IllegalArgumentException e) {
            // Exception chaining: wrappa l'eccezione di dominio in DAOException
            throw new DAOException("Errore nella creazione degli studenti demo: " + e.getMessage(), e);
        }
    }

    @Override
    protected Studente doRetrieveStudenteByEmail(String email) throws DAOException {
        for (Studente s : fintoDatabase) {
            if (s.presentaEmail().equals(email)) return s;
        }
        return null;
    }

    @Override
    protected List<Studente> doRetrieveStudentiClasse(String nomeClasse) throws DAOException {
        List<Studente> risultato = new ArrayList<>();
        for (Studente s : fintoDatabase) {
            SchoolClass classe = s.classeFrequentata();
            if (classe != null && classe.nome().equals(nomeClasse)) risultato.add(s);
        }
        return risultato;
    }

    @Override
    protected void doSaveStudente(Studente studente) throws DAOException {
        for (int i = 0; i < fintoDatabase.size(); i++) {
            if (fintoDatabase.get(i).presentaEmail().equals(studente.presentaEmail())) {
                fintoDatabase.set(i, studente);
                return;
            }
        }
        fintoDatabase.add(studente);
    }
}