package org.project.model;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static SessionManager instance = null;

    private final Map<Integer, Sessione> sessioniAttive;

    private int contatore = 0;

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

    public synchronized Sessione creaSessione(Studente studente) {
        Sessione s = nuovaSessione();
        s.setStudenteCorrente(studente);
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


    public synchronized Sessione creaSessione(Professore professore) {
        Sessione s = nuovaSessione();
        s.setProfessorCorrente(professore);
        sessioniAttive.put(s.getToken(), s);
        return s;
    }

    // ── Accesso / rimozione ───────────────────────────────────────────────────


    public synchronized Sessione ottieniSessione(int token) {
        return sessioniAttive.get(token);
    }

    public synchronized void cancellaSessione(int token) {
        sessioniAttive.remove(token);
    }

    // ── Metodo di utilità privato ──

    private Sessione nuovaSessione() {
        contatore++;
        Sessione s = new Sessione();
        s.setToken(contatore);
        return s;
    }
}