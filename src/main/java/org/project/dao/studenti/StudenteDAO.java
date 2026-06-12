package org.project.dao.studenti;

import org.project.dao.wallets.PortafoglioDAO;
import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.SchoolClass;
import org.project.model.Studente;

import java.util.List;

public abstract class StudenteDAO extends CachedDAO<Studente> {

    /**
     * Iniettato dalla factory dopo la costruzione per evitare la dipendenza
     * circolare (PortafoglioDAO → StudenteDAO → PortafoglioDAO).
     * Serve solo a rimuoviStudente: il controller non deve sapere nulla di questo.
     */
    private PortafoglioDAO portafoglioDAO;

    public void setPortafoglioDAO(PortafoglioDAO portafoglioDAO) {
        this.portafoglioDAO = portafoglioDAO;
    }

    @Override
    protected String ottieniChiave(Studente s) {
        return s.presentaEmail();
    }

    // ── Operazioni di lettura ──────────────────────────────────────────────────

    public Studente getStudenteByEmail(String mail) throws DAOException {
        if (inCache(mail)) return fetchFromCache(mail);
        Studente s = doRetrieveStudenteByEmail(mail);
        if (s != null) addToCache(s);
        return s;
    }

    public List<Studente> getStudentiClasse(SchoolClass classe) throws DAOException {
        return doRetrieveStudentiClasse(classe.nome());
    }

    // ── Operazioni di scrittura ────────────────────────────────────────────────

    public void salvaStudente(Studente studente) throws DAOException {
        doSaveStudente(studente);
        addToCache(studente);
    }

    /**
     * Rimuove lo studente e tutte le sue entità figlie (portafoglio, transazioni,
     * posizioni) sia dalla persistenza sia dalle cache Java.
     *
     * Il controller chiama solo questo metodo: la cascata è completamente
     * opaca rispetto a GRASP Creator / Low Coupling.
     */
    public void rimuoviStudente(String email) throws DAOException {
        // 1. Prima le entità figlie (composizione → il wallet non esiste senza lo studente)
        if (portafoglioDAO != null) {
            portafoglioDAO.rimuoviPortafoglio(email); // propaga a transazioni e posizioni
        }
        // 2. Poi il payload principale
        doDeleteStudente(email);
        // 3. Pulizia cache Java
        deleteFromCacheByKey(email);
    }

    // ── Metodi astratti ────────────────────────────────────────────────────────

    protected abstract Studente doRetrieveStudenteByEmail(String mail) throws DAOException;
    protected abstract List<Studente> doRetrieveStudentiClasse(String nomeClasse) throws DAOException;
    protected abstract void doSaveStudente(Studente studente) throws DAOException;
    protected abstract void doDeleteStudente(String email) throws DAOException;
}
