package org.project.dao.professori;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.Professore;

public abstract class ProfessoreDAO extends CachedDAO<Professore> {

    @Override
    protected String ottieniChiave(Professore p) {
        return p.presentaEmail();
    }

    public Professore getProfessoreByEmail(String mail) throws DAOException {
        if (inCache(mail)) return fetchFromCache(mail);
        Professore p = doRetrieveProfessoreByEmail(mail);
        if (p != null) addToCache(p);
        return p;
    }

    /**
     * Salva un nuovo professore in persistenza e lo aggiunge alla cache.
     */

    public void salvaProfessore(Professore professore) throws DAOException {
        doSaveProfessore(professore);
        addToCache(professore);
    }

    protected abstract Professore doRetrieveProfessoreByEmail(String mail) throws DAOException;
    protected abstract void doSaveProfessore(Professore professore) throws DAOException;
}