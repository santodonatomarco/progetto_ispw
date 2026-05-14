package org.project.dao.professori;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.model.Professore;
import org.project.model.ProfessoreLocale;
import org.project.model.ProfessoreOAuth;

import java.util.ArrayList;
import java.util.List;

public class ProfessoreDAODemo extends ProfessoreDAO {

    private List<Professore> fintoDatabase;

    public ProfessoreDAODemo() {
        super();
        this.fintoDatabase = new ArrayList<>();
        popolaDBFittizio();
    }

    private void popolaDBFittizio() {
        ProfessoreLocale p1 = new ProfessoreLocale("mario.rossi@univ.it", "Mario", "Rossi");
        p1.inserisciHashPassword("isson_oiraM"); // "Mario_rossi" invertito
        fintoDatabase.add(p1);

        ProfessoreOAuth p2 = new ProfessoreOAuth("lucia.bianchi@univ.it", "Lucia", "Bianchi", AuthProvider.GOOGLE);
        fintoDatabase.add(p2);
    }

    @Override
    protected Professore doRetrieveProfessoreByEmail(String email) throws DAOException {
        for (Professore p : fintoDatabase) {
            if (p.presentaEmail().equals(email)) return p;
        }
        return null;
    }

    @Override
    protected void doSaveProfessore(Professore professore) throws DAOException {
        for (Professore p : fintoDatabase) {
            if (p.presentaEmail().equals(professore.presentaEmail())) {
                throw new DAOException("Esiste già un professore con questa email.");
            }
        }
        fintoDatabase.add(professore);
    }
}