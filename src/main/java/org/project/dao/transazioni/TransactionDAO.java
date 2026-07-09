package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.Transaction;
import org.project.model.VirtualWallet;

import java.util.List;

public abstract class TransactionDAO extends CachedDAO<Transaction> {

    @Override
    protected String ottieniChiave(Transaction t) {
        return t.stock().simbolo() + "_" + t.quando().toString();
    }

    // ── Operazioni di scrittura ───

    public void salvaTransazione(String email, Transaction t) throws DAOException {
        doSaveTransazione(email, t);
        addToCache(t);
    }

    public void aggiornaTransazione(String email, Transaction t) throws DAOException {
        doUpdateTransazione(email, t);
    }


    public void aggiornaTransazione(String email, Transaction t, java.time.LocalDateTime oldTimestamp) throws DAOException {
        doUpdateTransazione(email, t, oldTimestamp);
    }


    public void rimuoviTransazioniByEmail(String email) throws DAOException {
        doDeleteTransazioniByEmail(email);
        svuotaCache();
    }

    // ── Operazioni di lettura ───

    public List<Transaction> getTransazioniWallet(VirtualWallet wallet) throws DAOException {
        return doRetrieveTransazioniByEmail(wallet.proprietario().presentaEmail());
    }

    // ── Metodi astratti ──

    protected abstract void doSaveTransazione(String email, Transaction t) throws DAOException;
    protected abstract void doUpdateTransazione(String email, Transaction t) throws DAOException;
    protected abstract void doUpdateTransazione(String email, Transaction t, java.time.LocalDateTime oldTimestamp) throws DAOException;
    protected abstract List<Transaction> doRetrieveTransazioniByEmail(String email) throws DAOException;
    protected abstract void doDeleteTransazioniByEmail(String email) throws DAOException;
}
