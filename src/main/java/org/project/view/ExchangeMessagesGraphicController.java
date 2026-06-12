package org.project.view;

import org.project.control.ExchangeMessagesAppController;
import org.project.exceptions.ControllerException;
import org.project.view.bean.MessageBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;

import java.util.List;

/**
 * Controller grafico astratto per la schermata Inbox / Exchange Messages.
 *
 * Logica condivisa tra CLI e GUI:
 *  - caricamento della inbox dell'utente loggato
 *  - invio di un messaggio a un destinatario
 *  - navigazione verso dashboard / altre schermate
 *  - utility di ruolo (isStudente, isProfessore, getEmailProfessore)
 *
 * Il caso d'uso prevede:
 *  • Studente → manda messaggi al proprio professore di classe
 *  • Professore → manda messaggi agli studenti delle proprie classi
 *
 * Le sottoclassi implementano la visualizzazione concreta (CLI o JavaFX).
 */
public abstract class ExchangeMessagesGraphicController {

    protected Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public abstract void start();

    // ── Operazioni applicative condivise ──────────────────────────────────────

    /**
     * Recupera tutti i messaggi ricevuti dall'utente loggato.
     * Restituisce null in caso di errore (già segnalato tramite mostraErrore).
     */
    protected List<MessageBean> eseguiCaricaInbox() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida. Effettua nuovamente il login.");
            return null;
        }
        try {
            return new ExchangeMessagesAppController().ottieniInbox(sessione);
        } catch (ControllerException e) {
            mostraErrore("Impossibile caricare la inbox: " + e.getMessage());
            return null;
        }
    }

    /**
     * Invia un messaggio al destinatario indicato.
     * Restituisce il bean del messaggio inviato, o null in caso di errore.
     */
    protected MessageBean eseguiInviaMessaggio(String emailDestinatario, String testo) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida.");
            return null;
        }
        try {
            MessageBean inviato = new ExchangeMessagesAppController()
                    .inviaMessaggio(sessione, emailDestinatario, testo);
            mostraSuccesso("Messaggio inviato a " + emailDestinatario + ".");
            return inviato;
        } catch (ControllerException e) {
            mostraErrore(e.getMessage());
            return null;
        }
    }

    // ── Utilità di ruolo ──────────────────────────────────────────────────────

    protected boolean isStudente() {
        SessioneBean sessione = navigator.getSessione();
        return sessione != null && sessione.getStudente() != null;
    }

    protected boolean isProfessore() {
        SessioneBean sessione = navigator.getSessione();
        return sessione != null && sessione.getProfessore() != null;
    }

    /**
     * Restituisce l'email del professore della classe dello studente loggato.
     * Utile per pre-popolare il campo "A:" nel form di composizione.
     * Restituisce null se l'utente è un professore o se lo studente
     * non è ancora assegnato a una classe.
     */
    protected String getEmailProfessore() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) return null;
        StudenteBean studente = sessione.getStudente();
        if (studente != null) return studente.getEmailProfessore();
        return null;
    }

    // ── Navigazione ───────────────────────────────────────────────────────────

    /** Torna alla dashboard corretta in base al ruolo. */
    protected void tornaDashboard() {
        if (isStudente()) {
            navigator.goToHomeStudente();
        } else {
            navigator.goToHomeProfessore();
        }
    }

    protected void vaiAlMercato()       { navigator.goToMercato(); }
    protected void vaiAlPortafoglio()   { navigator.goToPortafoglio(); }
    protected void vaiAlloStorico()     { navigator.goToStorico(); }
    protected void vaiAGestioneClasse() { navigator.goToGestioneClasse(); }

    protected void eseguiLogout() {
        navigator.refresh();
        navigator.goToLogin();
    }

    // ── Metodi astratti ───────────────────────────────────────────────────────

    protected abstract void mostraSuccesso(String msg);
    protected abstract void mostraErrore(String msg);
}
