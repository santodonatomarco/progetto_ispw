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
        this.popolaDBFittizio();
    }

    private void popolaDBFittizio() {
        try {
            Professore marioRossi = professoreDAO.getProfessoreByEmail("mario.rossi@univ.it");
            Professore luciaBianchi = professoreDAO.getProfessoreByEmail("lucia.bianchi@univ.it");

            if (marioRossi == null || luciaBianchi == null) {
                throw new DAOException("Impossibile caricare i professori per il DB demo");
            }

            SchoolClass classe1A = schoolClassDAO.getClasseByNomeEProfessore("1A", marioRossi);
            SchoolClass classe1B = schoolClassDAO.getClasseByNomeEProfessore("1B", luciaBianchi);

            StudenteLocale s1 = new StudenteLocale("alice.verdi@student.it", "Alice", "Verdi");
            s1.inserisciHashPassword("idreV_ecilA"); // "Alice_Verdi" invertito (Hasher.codifica)
            s1.iscriviClasse(classe1A);
            fintoDatabase.add(s1);

            StudenteLocale s2 = new StudenteLocale("bob.neri@student.it", "Bob", "Neri");
            s2.inserisciHashPassword("ireN_boB");
            s2.iscriviClasse(classe1A);
            fintoDatabase.add(s2);

            StudenteOAuth s3 = new StudenteOAuth("carlo.smith@student.it", "Carlo", "Smith", AuthProvider.GOOGLE);
            s3.iscriviClasse(classe1B);
            fintoDatabase.add(s3);

            StudenteOAuth s4 = new StudenteOAuth("diana.jones@student.it", "Diana", "Jones", AuthProvider.MICROSOFT);
            s4.iscriviClasse(classe1B);
            fintoDatabase.add(s4);

        } catch (DAOException e) {
            throw new RuntimeException("Errore caricamento classi nel DB demo", e);
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
        // Verifica duplicati
        for (Studente s : fintoDatabase) {
            if (s.presentaEmail().equals(studente.presentaEmail())) {
                // Studente già presente (pending) — aggiorna i dati
                fintoDatabase.remove(s);
                break;
            }
        }
        fintoDatabase.add(studente);
    }
}