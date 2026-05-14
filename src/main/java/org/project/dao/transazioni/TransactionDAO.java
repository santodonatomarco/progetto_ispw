package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.Transaction;
import org.project.model.VirtualWallet;

import java.util.List;

public abstract class TransactionDAO extends CachedDAO<Transaction> {

    @Override
    protected String ottieniChiave(Transaction t) {
        // chiave univoca: email proprietario + timestamp
        return t.stock().simbolo() + "_" + t.quando().toString();
    }

    /**
     * Salva una nuova transazione (PENDING) e la aggiunge alla cache.
     */
    public void salvaTransazione(Transaction t) throws DAOException {
        doSaveTransazione(t);
        addToCache(t);
    }

    /**
     * Aggiorna lo stato di una transazione esistente (es. PENDING → DONE).
     */
    public void aggiornaTransazione(Transaction t) throws DAOException {
        doUpdateTransazione(t);
    }

    /**
     * Restituisce tutte le transazioni di un wallet.
     */
    public List<Transaction> getTransazioniWallet(VirtualWallet wallet) throws DAOException {
        return doRetrieveTransazioniByEmail(wallet.proprietario().presentaEmail());
    }

    protected abstract void doSaveTransazione(Transaction t) throws DAOException;
    protected abstract void doUpdateTransazione(Transaction t) throws DAOException;
    protected abstract List<Transaction> doRetrieveTransazioniByEmail(String email) throws DAOException;
}