package org.project.dao.wallets;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.Studente;
import org.project.model.VirtualWallet;

public abstract class PortafoglioDAO extends CachedDAO<VirtualWallet> {
    @Override
    protected String ottieniChiave(VirtualWallet v) {
        Studente x = v.proprietario();
        return x.presentaEmail();
    }

    public VirtualWallet getPortafoglioByEmail(String mail) throws DAOException {
        VirtualWallet v;
        if(inCache(mail)){
            v = fetchFromCache(mail);
        } else {

            v = doRetrievePortafoglioByEmail(mail);

            if(v != null){
                addToCache(v);
            }
        }
        return v;
    }

    public void aggiornaPortafoglio(VirtualWallet v) throws DAOException {
        salvaPortafoglio(v);
        addToCache(v);  // aggiorna anche la cache
    }

    protected abstract VirtualWallet doRetrievePortafoglioByEmail(String mail) throws DAOException;
    public abstract void salvaPortafoglio(VirtualWallet v) throws DAOException;
}
