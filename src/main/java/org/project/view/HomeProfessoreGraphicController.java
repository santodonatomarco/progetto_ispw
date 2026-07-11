package org.project.view;

import org.project.control.GestioneClasseAppController;
import org.project.exceptions.ControllerException;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SchoolClassBean;
import org.project.view.bean.SessioneBean;

import java.util.List;


public abstract class HomeProfessoreGraphicController {

    protected Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public abstract void start();

    // ── Navigazione ───────────────────────────────────────────────────────────

    protected void vaiAlMercato() {
        navigator.goToMercato();
    }

    protected void vaiAGestioneClasse() {
        navigator.goToGestioneClasse();
    }

    protected void vaiAllaInbox() {
        navigator.goToInbox();
    }

    protected void eseguiLogout() {
        navigator.refresh();
        navigator.goToLogin();
    }

    // ── Accesso dati sessione ─────────────────────────────────────────────────

    protected ProfessoreBean getProfessoreLoggato() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) return null;
        return sessione.getProfessore();
    }

    protected List<SchoolClassBean> getListaClassi() {
        List<SchoolClassBean> lista = navigator.getListaClassi();
        if (lista == null) {
            // prima volta: carica e memorizza nel contesto
            try {
                lista = new GestioneClasseAppController()
                        .getClassiDelProfessore(navigator.getSessione());
                navigator.impostaListaClassi(lista);
            } catch (ControllerException e) {
                showMessage("Impossibile caricare le classi: " + e.getMessage());
            }
        }
        return lista; // le volte successive usa quella già in contesto (già aggiornata da replaceAll)
    }

    // ── Metodi astratti ───────────────────────────────────────────────────────

    protected abstract void aggiornaUIClassi(List<SchoolClassBean> classi);

    protected abstract void mostraMessaggio(String msg);

    protected abstract void showMessage(String msg);
}