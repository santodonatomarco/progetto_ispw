package org.project.view;

import org.project.control.ExchangeMessagesAppController;
import org.project.exceptions.ControllerException;
import org.project.view.bean.InvioMessaggioBean;
import org.project.view.bean.MessageBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;

import java.util.Collections;
import java.util.List;

public abstract class ExchangeMessagesGraphicController {

    protected Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public abstract void start();

    // ── Operazioni applicative condivise ──────────────────────────────────────

    protected List<MessageBean> eseguiCaricaInbox() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida. Effettua nuovamente il login.");
            return Collections.emptyList();
        }
        try {
            return new ExchangeMessagesAppController().ottieniInbox(sessione);
        } catch (ControllerException e) {
            mostraErrore("Impossibile caricare la inbox: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    protected MessageBean eseguiInviaMessaggio(String emailDestinatario, String testo) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida.");
            return null;
        }
        try {
            InvioMessaggioBean input = new InvioMessaggioBean(emailDestinatario, testo);
            MessageBean inviato = new ExchangeMessagesAppController()
                    .inviaMessaggio(sessione, input);
            mostraSuccesso("Messaggio inviato a " + emailDestinatario + ".");
            return inviato;
        } catch (IllegalArgumentException e){
                mostraErrore("Errore nell'invio del messaggio: " + e.getMessage());
                return null;
        }
        catch (ControllerException e) {
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


    protected String getEmailProfessore() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) return null;
        StudenteBean studente = sessione.getStudente();
        if (studente != null) return studente.getEmailProfessore();
        return null;
    }

    // ── Navigazione ───────────────────────────────────────────────────────────

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
