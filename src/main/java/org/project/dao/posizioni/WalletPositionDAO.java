package org.project.dao.posizioni;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.VirtualWallet;
import org.project.model.WalletPosition;

import java.util.List;

public abstract class WalletPositionDAO extends CachedDAO<WalletPosition> {


    @Override
    protected String ottieniChiave(WalletPosition p) {
        return p.stock().simbolo();
    }

    // ── Operazioni di scrittura ────────────────────────────────────────────────

    public void salvaPosizione(String email, WalletPosition p) throws DAOException {
        doSavePosizione(email, p);
        addToCache(p);
    }

    public void aggiornaPosizione(String email, WalletPosition p) throws DAOException {
        doUpdatePosizione(email, p);
    }

    public void rimuoviPosizione(String email, WalletPosition p) throws DAOException {
        doDeletePosizione(email, p);
        deleteFromCache(p);
    }


    public void rimuoviPosizioniByEmail(String email) throws DAOException {
        doDeletePosizioniByEmail(email);
        // Stessa motivazione di TransactionDAO: chiave senza email → svuotiamo.
        svuotaCache();
    }

    // ── Operazioni di lettura ──────────────────────────────────────────────────

    public List<WalletPosition> getPosizioniWallet(VirtualWallet wallet) throws DAOException {
        return doRetrievePosizioniByEmail(wallet.proprietario().presentaEmail());
    }

    // ── Metodi astratti ────────────────────────────────────────────────────────

    protected abstract void doSavePosizione(String email, WalletPosition p) throws DAOException;
    protected abstract void doUpdatePosizione(String email, WalletPosition p) throws DAOException;
    protected abstract void doDeletePosizione(String email, WalletPosition p) throws DAOException;
    protected abstract List<WalletPosition> doRetrievePosizioniByEmail(String email) throws DAOException;
    protected abstract void doDeletePosizioniByEmail(String email) throws DAOException;
}
