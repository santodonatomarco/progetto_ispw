package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.Transaction;
import org.project.model.VirtualWallet;

import java.util.List;

public abstract class TransactionDAO extends CachedDAO<Transaction> {

    /**
     * Chiave: simbolo + "_" + timestamp.
     * NOTA: la chiave non contiene l'email, il che impedisce una purge
     * selettiva dalla cache per studente. Per questo rimuoviTransazioniByEmail
     * esegue svuotaCache() — operazione sicura perché la cache di TransactionDAO
     * è usata solo come write-through (non per accelerare le letture).
     */
    @Override
    protected String ottieniChiave(Transaction t) {
        return t.stock().simbolo() + "_" + t.quando().toString();
    }

    // ── Operazioni di scrittura ────────────────────────────────────────────────

    public void salvaTransazione(String email, Transaction t) throws DAOException {
        doSaveTransazione(email, t);
        addToCache(t);
    }

    public void aggiornaTransazione(String email, Transaction t) throws DAOException {
        doUpdateTransazione(email, t);
    }

    /**
     * Aggiorna una transazione esistente identificata dal suo timestamp precedente.
     * Utile quando si desidera modificare anche il campo timestamp (es. aggiornare
     * la data dell'operazione dopo un'aggregazione).
     */
    public void aggiornaTransazione(String email, Transaction t, java.time.LocalDateTime oldTimestamp) throws DAOException {
        doUpdateTransazione(email, t, oldTimestamp);
    }

    /**
     * Rimuove tutte le transazioni di uno studente dalla persistenza e dalla cache.
     * Chiamato dalla cascade delete di PortafoglioDAO — invisibile al controller.
     */
    public void rimuoviTransazioniByEmail(String email) throws DAOException {
        doDeleteTransazioniByEmail(email);
        // La chiave cache non contiene l'email: svuotiamo tutta la cache.
        // È accettabile perché rimuoviStudente è un'operazione rara e la cache
        // viene ricostruita on-demand alla prima read successiva.
        svuotaCache();
    }

    // ── Operazioni di lettura ──────────────────────────────────────────────────

    public List<Transaction> getTransazioniWallet(VirtualWallet wallet) throws DAOException {
        return doRetrieveTransazioniByEmail(wallet.proprietario().presentaEmail());
    }

    // ── Metodi astratti ────────────────────────────────────────────────────────

    protected abstract void doSaveTransazione(String email, Transaction t) throws DAOException;
    protected abstract void doUpdateTransazione(String email, Transaction t) throws DAOException;
    protected abstract void doUpdateTransazione(String email, Transaction t, java.time.LocalDateTime oldTimestamp) throws DAOException;
    protected abstract List<Transaction> doRetrieveTransazioniByEmail(String email) throws DAOException;
    protected abstract void doDeleteTransazioniByEmail(String email) throws DAOException;
}
