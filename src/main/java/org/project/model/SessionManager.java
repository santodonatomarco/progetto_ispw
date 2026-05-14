package org.project.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton che gestisce tutte le sessioni utente attive.
 *
 * Responsabilità:
 *  - creare una nuova Sessione al login (Studente o Professore)
 *  - restituire la Sessione corretta dato il token
 *  - eliminare la Sessione al logout
 *
 * Pattern: Singleton + Factory method (creaSessione)
 */
public class SessionManager {

    private static SessionManager instance = null;

    /** Mappa token → Sessione attiva. */
    private final Map<Integer, Sessione> sessioniAttive;

    /** Contatore auto-incrementale per i token. */
    private int contatore = 0;

    // ── Costruttore privato (Singleton) ───────────────────────────────────────

    private SessionManager() {
        this.sessioniAttive = new HashMap<>();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ── Creazione sessione ────────────────────────────────────────────────────

    /**
     * Crea e registra una nuova sessione per uno Studente appena autenticato.
     *
     * @param studente lo studente loggato
     * @return la sessione appena creata (contiene già il token)
     */
    public synchronized Sessione creaSessione(Studente studente) {
        Sessione s = nuovaSessione();
        s.setStudenteCorrente(studente);
        // Se lo studente ha già il wallet caricato, lo pre-popoliamo subito
        if (studente.portafoglio() != null) {
            s.setWalletCorrente(studente.portafoglio());
            s.setPosizioniCaricate(studente.portafoglio().posizioni());
        }
        if (studente.classeFrequentata() != null) {
            s.setClasseCorrente(studente.classeFrequentata());
        }
        sessioniAttive.put(s.getToken(), s);
        return s;
    }

    /**
     * Crea e registra una nuova sessione per un Professore appena autenticato.
     *
     * @param professore il professore loggato
     * @return la sessione appena creata
     */
    public synchronized Sessione creaSessione(Professore professore) {
        Sessione s = nuovaSessione();
        s.setProfessorCorrente(professore);
        sessioniAttive.put(s.getToken(), s);
        return s;
    }

    // ── Accesso / rimozione ───────────────────────────────────────────────────

    /**
     * Restituisce la sessione associata al token dato.
     *
     * @param token il token ricevuto al login
     * @return la Sessione, oppure null se il token non esiste (sessione scaduta / non valida)
     */
    public synchronized Sessione ottieniSessione(int token) {
        return sessioniAttive.get(token);
    }

    /**
     * Rimuove la sessione (logout).
     *
     * @param token il token da invalidare
     */
    public synchronized void cancellaSessione(int token) {
        sessioniAttive.remove(token);
    }

    // ── Metodo di utilità privato ─────────────────────────────────────────────

    private Sessione nuovaSessione() {
        contatore++;
        Sessione s = new Sessione();
        s.setToken(contatore);
        return s;
    }
}