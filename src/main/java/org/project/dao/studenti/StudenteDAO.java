package org.project.dao.studenti;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.SchoolClass;
import org.project.model.Studente;

import java.util.List;

public abstract class StudenteDAO extends CachedDAO<Studente> {

    @Override
    protected String ottieniChiave(Studente s) {
        return s.presentaEmail();
    }

    public Studente getStudenteByEmail(String mail) throws DAOException {
        if (inCache(mail)) return fetchFromCache(mail);
        Studente s = doRetrieveStudenteByEmail(mail);
        if (s != null) addToCache(s);
        return s;
    }

    public List<Studente> getStudentiClasse(SchoolClass classe) throws DAOException {
        return doRetrieveStudentiClasse(classe.nome());
    }

    /**
     * Salva un nuovo studente in persistenza e lo aggiunge alla cache.
     */
    public void salvaStudente(Studente studente) throws DAOException {
        doSaveStudente(studente);
        addToCache(studente);
    }

    protected abstract Studente doRetrieveStudenteByEmail(String mail) throws DAOException;
    protected abstract List<Studente> doRetrieveStudentiClasse(String nomeClasse) throws DAOException;
    protected abstract void doSaveStudente(Studente studente) throws DAOException;
}