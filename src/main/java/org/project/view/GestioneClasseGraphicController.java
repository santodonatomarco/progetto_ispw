package org.project.view;

import org.project.control.GestioneClasseAppController;
import org.project.exceptions.ControllerException;
import org.project.view.bean.*;

import java.util.Collections;
import java.util.List;


public abstract class GestioneClasseGraphicController {

    // ── Costanti ──────────────────────────────────────────────────────────────
    private static final String ERRORE_SESSIONE_NON_VALIDA = "Sessione non valida.";

    protected Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public abstract void start();

    // ── Operazioni applicative condivise ──────────────────────────────────────

    protected List<SchoolClassBean> caricaClassi() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore(ERRORE_SESSIONE_NON_VALIDA + " Effettua nuovamente il login.");
            return Collections.emptyList();
        }
        try {
            return new GestioneClasseAppController().getClassiDelProfessore(sessione);
        } catch (ControllerException e) {
            mostraErrore("Impossibile caricare le classi: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    protected SchoolClassBean eseguiCreaClasse(String nomeClasse, double budget) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore(ERRORE_SESSIONE_NON_VALIDA);
            return null;
        }
        try {
            ClasseBean input = new ClasseBean(nomeClasse, budget);
            SchoolClassBean nuova = new GestioneClasseAppController()
                    .creaClasse(sessione, input);
            mostraSuccesso("Classe \"" + nuova.getNome() + "\" creata con budget € " +
                    String.format("%.2f", budget));
            return nuova;
        } catch (IllegalArgumentException e){
            mostraErrore("Errore di sintassi" + e.getMessage());
            return null;
        }
        catch (ControllerException e) {
            mostraErrore(e.getMessage());
            return null;
        }
    }

    protected SchoolClassBean eseguiImpostaBudget(String nomeClasse, double nuovoBudget) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore(ERRORE_SESSIONE_NON_VALIDA);
            return null;
        }
        try {
            ClasseBean inputClass = new ClasseBean(nomeClasse);
            ImpostaBudgetBean inputBudget = new ImpostaBudgetBean(nuovoBudget);
            SchoolClassBean aggiornata = new GestioneClasseAppController()
                    .impostaBudget(sessione, inputClass, inputBudget);

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
        } catch (IllegalArgumentException e){
            mostraErrore("Errore di sintassi" + e.getMessage());
            return null;
        }
        catch (ControllerException e) {
            mostraErrore(e.getMessage());
            return null;
        }
    }

    protected boolean eseguiAggiungiStudente(String email, String nomeClasse) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore(ERRORE_SESSIONE_NON_VALIDA);
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

    protected List<StudenteBean> eseguiCaricaStudenti(String nomeClasse) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore(ERRORE_SESSIONE_NON_VALIDA);
            return Collections.emptyList();
        }
        try {
            return new GestioneClasseAppController()
                    .getStudentiDellaClasseProfessore(sessione, nomeClasse);
        } catch (ControllerException e) {
            mostraErrore("Impossibile caricare gli studenti: " + e.getMessage());
            return Collections.emptyList();
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
        navigator.refresh();
        navigator.goToLogin();
    }

    // ── Metodi astratti ───────────────────────────────────────────────────────

    protected abstract void mostraSuccesso(String msg);
    protected abstract void mostraErrore(String msg);
}