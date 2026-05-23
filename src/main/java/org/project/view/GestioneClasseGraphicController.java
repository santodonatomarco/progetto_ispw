package org.project.view;

import org.project.control.GestioneClasseAppController;
import org.project.exceptions.ControllerException;
import org.project.view.bean.SchoolClassBean;
import org.project.view.bean.SessioneBean;

import java.util.List;

/**
 * Controller grafico astratto per la schermata Gestione Classe (professore).
 *
 * Logica condivisa tra CLI e GUI:
 *  - caricamento classi del professore
 *  - creazione nuova classe
 *  - impostazione budget su classe esistente
 *  - navigazione
 *
 * Le sottoclassi implementano la visualizzazione concreta (CLI o JavaFX).
 */
public abstract class GestioneClasseGraphicController {

    protected Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public abstract void start();

    // ── Operazioni applicative condivise ──────────────────────────────────────

    protected List<SchoolClassBean> caricaClassi() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida. Effettua nuovamente il login.");
            return null;
        }
        try {
            return new GestioneClasseAppController().getClassiDelProfessore(sessione);
        } catch (ControllerException e) {
            mostraErrore("Impossibile caricare le classi: " + e.getMessage());
            return null;
        }
    }

    protected SchoolClassBean eseguiCreaClasse(String nomeClasse, double budget) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida.");
            return null;
        }
        try {
            SchoolClassBean nuova = new GestioneClasseAppController()
                    .creaClasse(sessione, nomeClasse.trim().toUpperCase(), budget);
            mostraSuccesso("Classe \"" + nuova.getNome() + "\" creata con budget € " +
                    String.format("%.2f", budget));
            return nuova;
        } catch (ControllerException e) {
            mostraErrore(e.getMessage());
            return null;
        }
    }

    // GestioneClasseGraphicController.java
    protected SchoolClassBean eseguiImpostaBudget(String nomeClasse, double nuovoBudget) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida.");
            return null;
        }
        try {
            SchoolClassBean aggiornata = new GestioneClasseAppController()
                    .impostaBudget(sessione, nomeClasse, nuovoBudget);

            // Aggiorna classeCorrente nel contesto
            navigator.impostaClasseCorrente(aggiornata);

            List<SchoolClassBean> lista = navigator.getListaClassi();
            if (lista != null) {
                lista.replaceAll(c -> {
                    if (c.getNome().equals(aggiornata.getNome())) {
                        // Preserva gli studenti già caricati nel contesto,
                        // aggiorna solo il budget
                        aggiornata.setStudenti(c.getStudenti());
                        return aggiornata;
                    }
                    return c;
                });
            }

            mostraSuccesso("Budget aggiornato: € " + String.format("%.2f", nuovoBudget));
            return aggiornata;
        } catch (ControllerException e) {
            mostraErrore(e.getMessage());
            return null;
        }
    }

    protected boolean eseguiAggiungiStudente(String email, String nomeClasse) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida.");
            return false;
        }
        try {
            new GestioneClasseAppController().aggiungiStudente(sessione, email, nomeClasse);
            mostraSuccesso("Studente " + email + " aggiunto alla classe " + nomeClasse
                    + ". Potrà registrarsi con questa email.");
            return true;
        } catch (ControllerException e) {
            mostraErrore(e.getMessage());
            return false;
        }
    }

    // ── Navigazione ───────────────────────────────────────────────────────────

    protected void tornaDashboard() {
        navigator.goToHomeProfessore();
    }

    protected void vaiAlMercato() {
        navigator.goToMercato();
    }

    protected void eseguiLogout() {
        navigator.goToLogin();
        navigator.logout();
    }

    // ── Metodi astratti ───────────────────────────────────────────────────────

    protected abstract void mostraSuccesso(String msg);
    protected abstract void mostraErrore(String msg);
}