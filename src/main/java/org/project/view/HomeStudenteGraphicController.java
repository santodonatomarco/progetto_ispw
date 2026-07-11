package org.project.view;

import org.project.view.bean.PortafoglioBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;


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

    protected StudenteBean getStudenteLoggato() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) return null;
        return sessione.getStudente();
    }

    protected PortafoglioBean getPortafoglio() {
        return navigator.getPortafoglio();
    }

    // ── Metodi astratti ───────────────────────────────────────────────────────


    protected abstract void aggiornaUIPortafoglio(PortafoglioBean portafoglio);

    protected abstract void mostraMessaggio(String msg);

    protected abstract void showMessage(String msg);
}