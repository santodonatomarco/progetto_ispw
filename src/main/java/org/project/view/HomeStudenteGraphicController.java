package org.project.view;

import org.project.view.bean.PortafoglioBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;


/**
 * Controller grafico astratto per la schermata Home dello Studente.
 *
 * Contiene la logica condivisa tra CLI e GUI:
 *  - avvio schermata con i dati del portafoglio già in sessione
 *  - navigazione verso Mercato, Portafoglio, Storico
 *
 * La view (CLI o GUI) imposta navigator e chiama start() dopo il login.
 * Non chiama controller applicativi: i dati del portafoglio arrivano già
 * dal LoginAppController tramite il Navigator (Context).
 */
public abstract class HomeStudenteGraphicController {

    protected Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public abstract void start();

    // ── Navigazione ───────────────────────────────────────────────────────────

    protected void vaiAlMercato() {
        navigator.goToMercato();
    }

    protected void vaiAlPortafoglio() {
        navigator.goToPortafoglio();
    }

    protected void vaiAlloStorico() {
        navigator.goToStorico();
    }

    protected void vaiAllaInbox() {
        navigator.goToInbox();
    }


    protected void eseguiLogout() {
        navigator.refresh();
        navigator.goToLogin();
    }

    // ── Accesso dati sessione ─────────────────────────────────────────────────

    /**
     * Recupera lo studente loggato dalla sessione del navigator.
     * Restituisce null se la sessione non è valorizzata.
     */
    protected StudenteBean getStudenteLoggato() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) return null;
        return sessione.getStudente();
    }

    /**
     * Recupera il portafoglio dallo stato del navigator.
     * Può essere null se il portafoglio non è stato ancora caricato.
     */
    protected PortafoglioBean getPortafoglio() {
        return navigator.getPortafoglio();
    }

    // ── Metodi astratti ───────────────────────────────────────────────────────

    /**
     * Aggiorna la UI con i dati del portafoglio (saldo, totale, posizioni).
     * Chiamato da start() dopo aver recuperato i dati dal navigator.
     */
    protected abstract void aggiornaUIPortafoglio(PortafoglioBean portafoglio);

    /** Mostra un messaggio di errore o avviso sulla schermata. */
    protected abstract void mostraMessaggio(String msg);

    /** Mostra un dialog modale di errore di sistema. */
    protected abstract void showMessage(String msg);
}