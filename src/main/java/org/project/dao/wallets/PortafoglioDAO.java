package org.project.dao.wallets;

import org.project.dao.posizioni.WalletPositionDAO;
import org.project.dao.transazioni.TransactionDAO;
import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.VirtualWallet;

public abstract class PortafoglioDAO extends CachedDAO<VirtualWallet> {

    /**
     * Iniettati dalla factory per gestire la cascade delete.
     * Non passati in costruttore per evitare dipendenze circolari.
     */
    protected TransactionDAO transactionDAO;
    protected WalletPositionDAO walletPositionDAO;

    public void setTransactionDAO(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    public void setWalletPositionDAO(WalletPositionDAO walletPositionDAO) {
        this.walletPositionDAO = walletPositionDAO;
    }

    @Override
    protected String ottieniChiave(VirtualWallet v) {
        return v.proprietario().presentaEmail();
    }

    // ── Operazioni di lettura ──────────────────────────────────────────────────

    public VirtualWallet getPortafoglioByEmail(String mail) throws DAOException {
        if (inCache(mail)) return fetchFromCache(mail);
        VirtualWallet v = doRetrievePortafoglioByEmail(mail);
        if (v != null) addToCache(v);
        return v;
    }

    // ── Operazioni di scrittura ────────────────────────────────────────────────

    public void aggiornaPortafoglio(VirtualWallet v) throws DAOException {
        salvaPortafoglio(v);
        addToCache(v);
    }

    /**
     * Rimuove il portafoglio e tutte le sue entità figlie (transazioni, posizioni)
     * sia dalla persistenza sia dalle cache Java.
     *
     * Chiamato da StudenteDAO.rimuoviStudente — invisibile al controller.
     */
    public void rimuoviPortafoglio(String email) throws DAOException {
        // Ordine rimozione: prima le foglie dell'albero di composizione
        if (walletPositionDAO != null) {
            walletPositionDAO.rimuoviPosizioniByEmail(email);
        }
        if (transactionDAO != null) {
            transactionDAO.rimuoviTransazioniByEmail(email);
        }
        // Poi il portafoglio stesso
        doDeletePortafoglio(email);
        // Pulizia cache Java (la chiave del portafoglio è l'email)
        deleteFromCacheByKey(email);
    }

    // ── Metodi astratti ────────────────────────────────────────────────────────

    protected abstract VirtualWallet doRetrievePortafoglioByEmail(String mail) throws DAOException;
    public abstract void salvaPortafoglio(VirtualWallet v) throws DAOException;
    protected abstract void doDeletePortafoglio(String email) throws DAOException;
}
