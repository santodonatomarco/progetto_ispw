package org.project.dao.professori;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.model.Professore;

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
        Professore p1 = new Professore("mario.rossi@univ.it", "Mario", "Rossi", AuthProvider.LOCAL);
        p1.impostaPasswordHash(org.project.ing.classifunzionali.Hasher.codifica("mario_rossi"));
        fintoDatabase.add(p1);

        Professore p2 = new Professore("lucia.bianchi@univ.it", "Lucia", "Bianchi", AuthProvider.GOOGLE);
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