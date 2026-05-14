package org.project.dao.classi;

import org.project.exceptions.DAOException;
import org.project.model.Professore;
import org.project.model.SchoolClass;
import org.project.dao.professori.ProfessoreDAO;

import java.util.ArrayList;
import java.util.List;

public class SchoolClassDAODemo extends SchoolClassDAO {

    private List<SchoolClass> fintoDatabase;
    private ProfessoreDAO professoreDAO;

    public SchoolClassDAODemo(ProfessoreDAO professoreDAO) {
        super();
        this.fintoDatabase = new ArrayList<>();
        this.professoreDAO = professoreDAO;
    }

    @Override
    protected SchoolClass doRetrieveClasseByNomeEProfessore(String nomeClasse, Professore professore) throws DAOException {
        for (SchoolClass c : fintoDatabase) {
            if (c.nome().equals(nomeClasse) && c.teacher().presentaEmail().equals(professore.presentaEmail())) {
                return c;
            }
        }
        return null;
    }

    @Override
    public List<SchoolClass> getClassiByProfessore(Professore professore) throws DAOException {
        if (professore == null) {
            throw new DAOException("Il professore non può essere nullo");
        }

        List<SchoolClass> classi = new ArrayList<>();

        for (SchoolClass c : fintoDatabase) {
            if (c.teacher() != null && c.teacher().presentaEmail().equals(professore.presentaEmail())) {
                classi.add(c);
                addToCache(c);
            }
        }

        return classi;
    }


    @Override
    public void salvaClasse(SchoolClass classe) throws DAOException {
        if (classe == null) throw new DAOException("La classe non può essere nulla");

        // Simula un "Upsert" tipico dei database
        fintoDatabase.removeIf(c -> c.nome().equals(classe.nome()));
        fintoDatabase.add(classe);

        // Aggiorniamo anche la cache ereditata dal CachedDAO
        addToCache(classe);
    }
}