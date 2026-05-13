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
        Professore p;
        if(inCache(mail)){
            p = fetchFromCache(mail);
        } else {

            p = doRetrieveProfessoreByEmail(mail);

            if(p != null){
                addToCache(p);
            }
        }
        return p;
    }

    protected abstract Professore doRetrieveProfessoreByEmail(String mail) throws DAOException;



}
