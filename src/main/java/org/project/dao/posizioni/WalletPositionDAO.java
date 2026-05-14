package org.project.dao.posizioni;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.VirtualWallet;
import org.project.model.WalletPosition;

import java.util.List;

public abstract class WalletPositionDAO extends CachedDAO<WalletPosition> {

    @Override
    protected String ottieniChiave(WalletPosition p) {
        // chiave: email proprietario + simbolo stock
        return p.stock().simbolo();
    }

    /**
     * Salva una nuova posizione (primo acquisto di quello stock).
     */
    public void salvaPosizione(WalletPosition p) throws DAOException {
        doSavePosizione(p);
        addToCache(p);
    }

    /**
     * Aggiorna una posizione esistente (acquisto aggiuntivo o vendita parziale).
     */
    public void aggiornaPosizione(WalletPosition p) throws DAOException {
        doUpdatePosizione(p);
    }

    /**
     * Rimuove una posizione (vendita totale — quantità arrivata a zero).
     */
    public void rimuoviPosizione(WalletPosition p) throws DAOException {
        doDeletePosizione(p);
        deleteFromCache(p);
    }

    /**
     * Restituisce tutte le posizioni aperte di un wallet.
     */
    public List<WalletPosition> getPosizioniWallet(VirtualWallet wallet) throws DAOException {
        return doRetrievePosizioniByEmail(wallet.proprietario().presentaEmail());
    }

    protected abstract void doSavePosizione(WalletPosition p) throws DAOException;
    protected abstract void doUpdatePosizione(WalletPosition p) throws DAOException;
    protected abstract void doDeletePosizione(WalletPosition p) throws DAOException;
    protected abstract List<WalletPosition> doRetrievePosizioniByEmail(String email) throws DAOException;
}