package org.project.dao.classi;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.Professore;
import org.project.model.SchoolClass;

import java.util.List;


public abstract class SchoolClassDAO extends CachedDAO<SchoolClass> {

    @Override
    protected String ottieniChiave(SchoolClass c) {
        // Chiave unica: nome della classe + email del professore
        return c.nome() + "|" + c.teacher().presentaEmail();
    }

    public SchoolClass getClasseByNomeEProfessore(String nomeClasse, Professore professore) throws DAOException {
        if (nomeClasse == null || nomeClasse.trim().isEmpty()) {
            throw new DAOException("Nome classe non valido");
        }
        if (professore == null) {
            throw new DAOException("Professore non valido");
        }

        String chiave = nomeClasse + "|" + professore.presentaEmail();
        SchoolClass c;
        if (inCache(chiave)) {
            c = fetchFromCache(chiave);
        } else {
            c = doRetrieveClasseByNomeEProfessore(nomeClasse, professore);
            if (c != null) {
                addToCache(c);
            }
        }
        return c;
    }

    public abstract List<SchoolClass> getClassiByProfessore(Professore professore) throws DAOException;

    protected abstract SchoolClass doRetrieveClasseByNomeEProfessore(String nomeClasse, Professore professore) throws DAOException;

    public abstract void salvaClasse(SchoolClass classe) throws DAOException;
}
