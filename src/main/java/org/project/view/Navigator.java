package org.project.view;

import org.project.view.bean.*;
import java.util.List;

/**
 * Navigator astratto — coordina la navigazione tra schermate
 * e conserva lo stato condiviso tramite il Context.
 *
 * ── Architettura ─────────────────────────────────────────────────────────────
 *  • NavigatorCLI  — I/O testuale; loop while(running) in startUp()
 *  • NavigatorGUI  — JavaFX event-loop; ogni goTo*() chiama nextScreen()
 *
 * ── Caso d'uso ManageWallets ─────────────────────────────────────────────────
 * Più Schermate convergono sullo stesso controller grafico ManageWallets:
 *   MERCATO         → ManageWallets.start()
 *   CONFERMA_ORDINE → ManageWallets.startConfermaOrdine()
 *   PORTAFOGLIO     → ManageWallets.startPortafoglio()
 *   STORICO         → ManageWallets.startStorico()
 *   WALLET_STUDENTE → ManageWallets.startWalletEsterno(studenteTarget)
 *
 * Prima di goToWalletStudente(), chi naviga deve chiamare
 * impostaStudenteTarget(bean) per indicare di chi mostrare il wallet.
 */
public abstract class Navigator {

    protected boolean running;

    private Context   contesto;
    private Schermate schermataCorrente;

    protected Navigator() {
        this.contesto = new Context();
        this.running  = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sessione e utente loggato
    // ─────────────────────────────────────────────────────────────────────────

    public void         impostaSessione(SessioneBean s)      { this.contesto.setSessione(s); }
    public SessioneBean getSessione()                        { return this.contesto.getSessione(); }

    public void         impostaStudente(StudenteBean s)      { this.contesto.setStudenteLoggato(s); }
    public StudenteBean getStudente()                        { return this.contesto.getStudenteLoggato(); }

    public void           impostaProfessore(ProfessoreBean p){ this.contesto.setProfessoreLoggato(p); }
    public ProfessoreBean getProfessore()                    { return this.contesto.getProfessoreLoggato(); }

    // ─────────────────────────────────────────────────────────────────────────
    // Mercato / stock
    // ─────────────────────────────────────────────────────────────────────────

    public void           impostaStockCorrente(StockBean s)  { this.contesto.setStockCorrente(s); }
    public StockBean      getStockCorrente()                 { return this.contesto.getStockCorrente(); }

    public void            impostaListaStock(List<StockBean> l){ this.contesto.setListaStock(l); }
    public List<StockBean> getListaStock()                   { return this.contesto.getListaStock(); }

    // ─────────────────────────────────────────────────────────────────────────
    // Ordine in corso
    // ─────────────────────────────────────────────────────────────────────────

    public void            impostaTransazionePending(TransactionBean t){ this.contesto.setTransazionePending(t); }
    public TransactionBean getTransazionePending()           { return this.contesto.getTransazionePending(); }

    // ─────────────────────────────────────────────────────────────────────────
    // Portafoglio
    // ─────────────────────────────────────────────────────────────────────────

    public void             impostaPortafoglio(PortafoglioBean p){ this.contesto.setPortafoglio(p); }
    public PortafoglioBean  getPortafoglio()                 { return this.contesto.getPortafoglio(); }

    public void                   impostaStoricoTransazioni(List<TransactionBean> l){ this.contesto.setStoricoTransazioni(l); }
    public List<TransactionBean>  getStoricoTransazioni()    { return this.contesto.getStoricoTransazioni(); }

    // ─────────────────────────────────────────────────────────────────────────
    // Wallet esterno (studente target — WALLET_STUDENTE)
    // ─────────────────────────────────────────────────────────────────────────

    public void         impostaStudenteTarget(StudenteBean s){ this.contesto.setStudenteTarget(s); }
    public StudenteBean getStudenteTarget()                  { return this.contesto.getStudenteTarget(); }

    // ─────────────────────────────────────────────────────────────────────────
    // Classe e studenti (professore)
    // ─────────────────────────────────────────────────────────────────────────

    public void              impostaClasseCorrente(SchoolClassBean c){ this.contesto.setClasseCorrente(c); }
    public SchoolClassBean   getClasseCorrente()             { return this.contesto.getClasseCorrente(); }

    public void               impostaListaClassi(List<SchoolClassBean> l){ this.contesto.setListaClassi(l); }
    public List<SchoolClassBean> getListaClassi()            { return this.contesto.getListaClassi(); }

    // ─────────────────────────────────────────────────────────────────────────
    // Motore di navigazione
    // ─────────────────────────────────────────────────────────────────────────

    private void setSchermata(Schermate s) { this.schermataCorrente = s; }

    protected void nextScreen() {
        if (this.schermataCorrente == null) return;

        Schermate target = this.schermataCorrente;
        this.schermataCorrente = null;

        switch (target) {
            case LOGIN           -> visualizzaLogin();
            case REGISTRAZIONE   -> visualizzaRegistrazione();
            case HOME_STUDENTE   -> visualizzaHomeStudente();
            case MERCATO         -> visualizzaMercato();
            case DETTAGLIO_STOCK -> visualizzaDettaglioStock();
            case CONFERMA_ORDINE -> visualizzaConfermaOrdine();
            case PORTAFOGLIO     -> visualizzaPortafoglio();
            case STORICO         -> visualizzaStorico();
            case WALLET_STUDENTE -> visualizzaWalletStudente();
            case HOME_PROFESSORE -> visualizzaHomeProfessore();
            case GESTIONE_CLASSE -> visualizzaGestioneClasse();
            case INBOX           -> visualizzaInbox();
        }
    }

    public void refresh() {
        this.contesto = new Context();
        this.schermataCorrente = null;
        logout();
    }

    public abstract void logout();
    public abstract void startUp();

    // ── Navigazione pubblica ──────────────────────────────────────────────────

    public void goToLogin()            { setSchermata(Schermate.LOGIN);            nextScreen(); }
    public void goToRegistrazione()    { setSchermata(Schermate.REGISTRAZIONE);    nextScreen(); }
    public void goToHomeStudente()     { setSchermata(Schermate.HOME_STUDENTE);    nextScreen(); }
    public void goToMercato()          { setSchermata(Schermate.MERCATO);          nextScreen(); }
    public void goToDettaglioStock()   { setSchermata(Schermate.DETTAGLIO_STOCK);  nextScreen(); }
    public void goToConfermaOrdine()   { setSchermata(Schermate.CONFERMA_ORDINE);  nextScreen(); }
    public void goToPortafoglio()      { setSchermata(Schermate.PORTAFOGLIO);      nextScreen(); }
    public void goToStorico()          { setSchermata(Schermate.STORICO);          nextScreen(); }
    public void goToWalletStudente()   { setSchermata(Schermate.WALLET_STUDENTE);  nextScreen(); }
    public void goToHomeProfessore()   { setSchermata(Schermate.HOME_PROFESSORE);  nextScreen(); }
    public void goToGestioneClasse()   { setSchermata(Schermate.GESTIONE_CLASSE);  nextScreen(); }
    public void goToInbox()            { setSchermata(Schermate.INBOX);            nextScreen(); }
    public void esci() { this.running = false; System.exit(0); }

    protected abstract void visualizzaLogin();
    protected abstract void visualizzaRegistrazione();
    protected abstract void visualizzaHomeStudente();
    protected abstract void visualizzaMercato();
    protected abstract void visualizzaDettaglioStock();
    protected abstract void visualizzaConfermaOrdine();
    protected abstract void visualizzaPortafoglio();
    protected abstract void visualizzaStorico();
    protected abstract void visualizzaWalletStudente();
    protected abstract void visualizzaHomeProfessore();
    protected abstract void visualizzaGestioneClasse();
    protected abstract void visualizzaInbox();
}