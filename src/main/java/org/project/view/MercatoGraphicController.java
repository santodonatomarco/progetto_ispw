package org.project.view;

import org.project.control.MercatoAppController;
import org.project.exceptions.ControllerException;
import org.project.ing.enumerations.Ruolo;
import org.project.model.SessionManager;
import org.project.model.Sessione;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StockBean;

import java.util.List;

/**
 * Controller grafico astratto per la schermata Mercato.
 *
 * Logica condivisa tra CLI e GUI:
 *  - ricerca di uno stock tramite MercatoAppController
 *  - avvio ordine di acquisto (solo studente)
 *  - filtraggio della lista stock per settore o variazione
 *
 * Le sottoclassi implementano la visualizzazione concreta (CLI o JavaFX).
 */
public abstract class MercatoGraphicController {

    protected Navigator navigator;
    protected boolean isStudente;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
        // Determina il ruolo dall'utente in sessione
        SessioneBean sessione = navigator.getSessione();
        if (sessione != null) {
            this.isStudente = (sessione.getStudente() != null);
        }
    }

    public abstract void start();

    // ── Logica condivisa ──────────────────────────────────────────────────────

    /**
     * Cerca uno stock per simbolo tramite MercatoAppController.
     * Se trovato, lo aggiunge alla lista del navigator e lo mostra.
     */
    protected void eseguiRicerca(String simbolo) {
        if (simbolo == null || simbolo.isBlank()) {
            mostraErrore("Inserisci un simbolo valido (es. AAPL, TSLA).");
            return;
        }

        String sim = simbolo.trim().toUpperCase();
        mostraCaricamento(true);

        try {
            MercatoAppController appController = new MercatoAppController();
            StockBean stock = appController.cercaStock(sim);

            // Aggiorna il navigator con lo stock corrente
            navigator.impostaStockCorrente(stock);

            // Aggiunge alla lista se non già presente
            List<StockBean> lista = navigator.getListaStock();
            if (lista != null && lista.stream().noneMatch(s -> s.getSimbolo().equals(stock.getSimbolo()))) {
                lista.add(stock);
                navigator.impostaListaStock(lista);
            } else if (lista == null) {
                lista = new java.util.ArrayList<>();
                lista.add(stock);
                navigator.impostaListaStock(lista);
            }

            mostraCaricamento(false);
            mostraDettaglioStock(stock);

        } catch (ControllerException e) {
            mostraCaricamento(false);
            mostraErrore("Impossibile recuperare lo stock \"" + sim + "\". Verifica il simbolo e riprova.");
        }
    }

    /**
     * Avvia un ordine di acquisto per lo stock corrente (solo studente).
     * Imposta la transazione pending nel navigator e naviga alla conferma ordine.
     */
    protected void eseguiAvviaOrdine(String simbolo) {
        if (!isStudente) {
            mostraErrore("I professori non possono effettuare acquisti.");
            return;
        }

        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida. Effettua nuovamente il login.");
            return;
        }

        try {
            MercatoAppController appController = new MercatoAppController();
            var transazione = appController.avviaOrdineAcquisto(sessione, simbolo);
            navigator.impostaTransazionePending(transazione);
            navigator.goToConfermaOrdine();

        } catch (ControllerException e) {
            mostraErrore("Impossibile avviare l'ordine: " + e.getMessage());
        }
    }

    // ── Navigazione ───────────────────────────────────────────────────────────

    protected void tornaDashboard() {
        if (isStudente) {
            navigator.goToHomeStudente();
        } else {
            navigator.goToHomeProfessore();
        }
    }

    protected void eseguiLogout() {
        navigator.goToLogin();
        navigator.logout();
    }

    // ── Metodi astratti ───────────────────────────────────────────────────────

    protected abstract void mostraDettaglioStock(StockBean stock);
    protected abstract void mostraCaricamento(boolean visible);
    protected abstract void mostraErrore(String msg);
    protected abstract void showMessage(String msg);
}