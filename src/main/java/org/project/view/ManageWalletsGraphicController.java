package org.project.view;

import org.project.control.ManageWalletsAppController;
import org.project.exceptions.ControllerException;
import org.project.model.SessionManager;
import org.project.view.bean.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller grafico astratto per il caso d'uso "Manage Wallets".
 *
 * Unifica in un solo controller tutti i sotto-flussi visivi:
 *
 *  ┌─ start()                → browsing mercato
 *  ├─ startConfermaOrdine()  → pannello conferma acquisto
 *  ├─ startPortafoglio()     → portafoglio PROPRIO (studente proprietario)
 *  ├─ startStorico()         → storico transazioni PROPRIO
 *  └─ startWalletEsterno()   → portafoglio/storico DI UN ALTRO studente
 *                              (studente stessa classe | professore)
 *
 * La logica condivisa (esegui*) delega a ManageWalletsAppController.
 * Le sottoclassi implementano la presentazione concreta (CLI o JavaFX).
 */
public abstract class ManageWalletsGraphicController {

    protected Navigator navigator;

    /** true se l'utente in sessione è uno studente (può acquistare). */
    protected boolean isStudente;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
        SessioneBean sessione = navigator.getSessione();
        if (sessione != null)
            this.isStudente = (sessione.getStudente() != null);
    }

    // ── Punti di ingresso ─────────────────────────────────────────────────────

    /** Schermata principale: browsing mercato + lista stock monitorati. */
    public abstract void start();

    /** Pannello di conferma ordine (chiamato dopo avviaOrdineAcquisto). */
    public abstract void startConfermaOrdine();

    /** Portafoglio PROPRIO: posizioni aperte + saldo. */
    public abstract void startPortafoglio();

    /** Storico transazioni PROPRIO. */
    public abstract void startStorico();

    /**
     * Portafoglio di un ALTRO studente in sola lettura.
     * Usato da professore → GestioneClasse → "Visualizza wallet"
     * e da studente → ElencoStudenti → "Visualizza wallet compagno".
     *
     * @param studenteTarget il cui portafoglio mostrare (non null)
     */
    public abstract void startWalletEsterno(StudenteBean studenteTarget);

    // ── Logica condivisa — Mercato ────────────────────────────────────────────

    protected void eseguiRicerca(String simbolo) {
        if (simbolo == null || simbolo.isBlank()) {
            mostraErrore("Inserisci un simbolo valido (es. AAPL, TSLA).");
            return;
        }
        String sim = simbolo.trim().toUpperCase();
        mostraCaricamento(true);
        try {
            StockBean stock = new ManageWalletsAppController().cercaStock(sim);
            navigator.impostaStockCorrente(stock);

            List<StockBean> lista = navigator.getListaStock();
            if (lista == null) lista = new ArrayList<>();
            if (lista.stream().noneMatch(s -> s.getSimbolo().equals(stock.getSimbolo()))) {
                lista.add(stock);
                navigator.impostaListaStock(lista);
            }
            mostraCaricamento(false);
            mostraDettaglioStock(stock);
        } catch (ControllerException e) {
            mostraCaricamento(false);
            mostraErrore("Impossibile recuperare \"" + sim + "\". Verifica il simbolo e riprova.");
        }
    }

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
            TransactionBean t = new ManageWalletsAppController()
                    .avviaOrdineAcquisto(sessione, simbolo);
            navigator.impostaTransazionePending(t);
            navigator.goToConfermaOrdine();
        } catch (ControllerException e) {
            mostraErrore("Impossibile avviare l'ordine: " + e.getMessage());
        }
    }

    // ── Logica condivisa — Conferma ordine ────────────────────────────────────

    protected void eseguiConfermaAcquisto(double quantita) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida. Effettua nuovamente il login.");
            return;
        }
        try {
            TransactionBean t = new ManageWalletsAppController()
                    .confermaAcquisto(sessione, quantita);
            navigator.impostaTransazionePending(null);

            // Aggiorna il PortafoglioBean nel Navigator con il wallet fresco
            var sm = SessionManager.getInstance().ottieniSessione(sessione.getId());
            if (sm != null && sm.getWalletCorrente() != null) {
                var pf = new ManageWalletsAppController()
                        .convertiWalletInBean(sm.getWalletCorrente());
                navigator.impostaPortafoglio(pf);
            }
            mostraAcquistoCompletato(t);
        } catch (ControllerException e) {
            mostraErrore("Errore nella conferma: " + e.getMessage());
        }
    }

    protected void eseguiAnnullaOrdine() {
        SessioneBean sessione = navigator.getSessione();
        if (sessione != null) {
            try { new ManageWalletsAppController().annullaOrdine(sessione); }
            catch (ControllerException ignored) { /* best-effort */ }
        }
        navigator.impostaTransazionePending(null);
        navigator.goToMercato();
    }

    // ── Logica condivisa — Portafoglio / Storico ──────────────────────────────

    /**
     * Carica il portafoglio del proprietario corrente e chiama mostraPortafoglio().
     * emailTarget null = proprio wallet.
     */
    protected void eseguiCaricaPortafoglio(String emailTarget) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida.");
            return;
        }
        try {
            PortafoglioBean pf = new ManageWalletsAppController()
                    .ottieniPortafoglio(sessione, emailTarget);
            if (emailTarget == null) navigator.impostaPortafoglio(pf);
            mostraPortafoglio(pf, emailTarget == null);
        } catch (ControllerException e) {
            mostraErrore("Impossibile caricare il portafoglio: " + e.getMessage());
        }
    }

    /**
     * Carica lo storico transazioni e chiama mostraStorico().
     * emailTarget null = proprio storico.
     */
    protected void eseguiCaricaStorico(String emailTarget) {
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) {
            mostraErrore("Sessione non valida.");
            return;
        }
        try {
            List<TransactionBean> storico = new ManageWalletsAppController()
                    .ottieniStorico(sessione, emailTarget);
            if (emailTarget == null) navigator.impostaStoricoTransazioni(storico);
            mostraStorico(storico, emailTarget);
        } catch (ControllerException e) {
            mostraErrore("Impossibile caricare lo storico: " + e.getMessage());
        }
    }

    // ── Navigazione ───────────────────────────────────────────────────────────

    protected void tornaDashboard() {
        if (isStudente) navigator.goToHomeStudente();
        else             navigator.goToHomeProfessore();
    }

    protected void eseguiLogout() {
        navigator.refresh();   // resetta Context (lista stock, portafoglio, ecc.) + controller GUI
        navigator.goToLogin();
    }

    // ── Metodi astratti da implementare nelle sottoclassi ─────────────────────

    protected abstract void mostraDettaglioStock(StockBean stock);
    protected abstract void mostraCaricamento(boolean visible);
    protected abstract void mostraErrore(String msg);
    protected abstract void showMessage(String msg);

    /** Chiamato dopo confermaAcquisto() riuscita. */
    protected abstract void mostraAcquistoCompletato(TransactionBean transazione);

    /**
     * Mostra il portafoglio.
     *
     * @param portafoglio  bean con saldo, posizioni, transazioni
     * @param isProprietario true se il richiedente è il titolare del wallet
     */
    protected abstract void mostraPortafoglio(PortafoglioBean portafoglio, boolean isProprietario);

    /**
     * Mostra lo storico transazioni.
     *
     * @param storico      lista delle transazioni
     * @param emailTarget  email del proprietario; null = proprio storico
     */
    protected abstract void mostraStorico(List<TransactionBean> storico, String emailTarget);
}