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
        Studente s;
        if(inCache(mail)){
            s = fetchFromCache(mail);
        } else {

            s = doRetrieveStudenteByEmail(mail);

            if(s != null){
                addToCache(s);
            }
        }
        return s;
    }

    public List<Studente> getStudentiClasse(SchoolClass classe) throws  DAOException {
        return doRetrieveStudentiClasse(classe.nome());
    }

    protected abstract List<Studente> doRetrieveStudentiClasse(String nomeClasse) throws  DAOException;
    protected abstract Studente doRetrieveStudenteByEmail(String mail) throws DAOException;

}
