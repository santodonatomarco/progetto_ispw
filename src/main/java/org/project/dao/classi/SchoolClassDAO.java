package org.project.dao.classi;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.Professore;
import org.project.model.SchoolClass;

import java.util.List;


public abstract class SchoolClassDAO extends CachedDAO<SchoolClass> {

    @Override
    protected String ottieniChiave(SchoolClass c) {
        return c.nome();
    }

    public SchoolClass getClasseByNome(String nomeClasse) throws DAOException {
        if (nomeClasse == null || nomeClasse.trim().isEmpty()) {
            throw new DAOException("Nome classe non valido");
        }

        SchoolClass c;
        if (inCache(nomeClasse)) {
            c = fetchFromCache(nomeClasse);
        } else {
            c = doRetrieveClasseByNome(nomeClasse);
            if (c != null) {
                addToCache(c);
            }
        }
        return c;
    }

    public abstract List<SchoolClass> getClassiByProfessore(Professore professore) throws DAOException;

    protected abstract SchoolClass doRetrieveClasseByNome(String nomeClasse) throws DAOException;

    public abstract void salvaClasse(SchoolClass classe) throws DAOException;
}
