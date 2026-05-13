package org.project.dao.studenti;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.model.*;
import org.project.dao.classi.SchoolClassDAO;

import java.util.ArrayList;
import java.util.List;

public class StudenteDAODemo extends StudenteDAO {

    // Simula la persistenza in memoria
    private List<Studente> fintoDatabase;
    private SchoolClassDAO schoolClassDAO;

    public StudenteDAODemo(SchoolClassDAO schoolClassDAO) {
        super();
        this.fintoDatabase = new ArrayList<>();
        this.schoolClassDAO = schoolClassDAO;
        this.popolaDBFittizio();
    }

    /**
     * Popola il database fittizio con dati di test
     */
    private void popolaDBFittizio() {
        // Le classi vengono create direttamente da SchoolClassDAODemo
        // Gli studenti le recuperano da lì
        try {
            // Carica le classi dal DAO (che ne garantisce un solo una istanza per classe)
            SchoolClass classe1A = schoolClassDAO.getClasseByNome("1A");
            SchoolClass classe1B = schoolClassDAO.getClasseByNome("1B");

            // Crea alcuni studenti
            // Studente Locale
            StudenteLocale s1 = new StudenteLocale("alice.verdi@student.it", "Alice", "Verdi");
            s1.inserisciHashPassword("hash_alice_456");
            s1.iscriviClasse(classe1A);
            fintoDatabase.add(s1);

            // Studente Locale
            StudenteLocale s2 = new StudenteLocale("bob.neri@student.it", "Bob", "Neri");
            s2.inserisciHashPassword("hash_bob_789");
            s2.iscriviClasse(classe1A);
            fintoDatabase.add(s2);

            // Studente OAuth
            StudenteOAuth s3 = new StudenteOAuth("carlo.smith@student.it", "Carlo", "Smith", AuthProvider.GOOGLE);
            s3.iscriviClasse(classe1B);
            fintoDatabase.add(s3);

            // Studente OAuth
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
            if (s.presentaEmail().equals(email)) {
                return s;
            }
        }
        return null;
    }

    @Override
    protected List<Studente> doRetrieveStudentiClasse(String nomeClasse) throws DAOException {
        List<Studente> risultato = new ArrayList<>();
        for (Studente s : fintoDatabase) {
            SchoolClass classe = s.classeFrequentata();
            if (classe != null && classe.nome().equals(nomeClasse)) {
                risultato.add(s);
            }
        }
        return risultato;
    }

}