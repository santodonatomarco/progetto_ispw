package org.project.view;

import org.project.view.bean.*;

import java.util.List;

/**
 * Navigator astratto — gestisce la navigazione tra schermate
 * e conserva lo stato tramite Context.
 *
 * Le sottoclassi (NavigatorCLI, NavigatorGUI) implementano
 * i metodi visualizza* con la tecnologia concreta.
 */
public abstract class Navigator {

    protected boolean running;

    private Context contesto;
    private Schermate schermataCorrente;

    protected Navigator() {
        this.contesto = new Context();
        this.running = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Gestione sessione e utente loggato
    // ─────────────────────────────────────────────────────────────────────────

    public void impostaSessione(SessioneBean sessione) {
        this.contesto.setSessione(sessione);
    }

    public SessioneBean getSessione() {
        return this.contesto.getSessione();
    }

    public void impostaStudente(StudenteBean studente) {
        this.contesto = new Context(studente);
    }

    public StudenteBean getStudente() {
        return this.contesto.getStudenteLoggato();
    }

    public void impostaProfessore(ProfessoreBean professore) {
        this.contesto = new Context(professore);
    }

    public ProfessoreBean getProfessore() {
        return this.contesto.getProfessoreLoggato();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mercato
    // ─────────────────────────────────────────────────────────────────────────

    public void impostaStockCorrente(StockBean stock) {
        this.contesto.setStockCorrente(stock);
    }

    public StockBean getStockCorrente() {
        return this.contesto.getStockCorrente();
    }

    public void impostaListaStock(List<StockBean> lista) {
        this.contesto.setListaStock(lista);
    }

    public List<StockBean> getListaStock() {
        return this.contesto.getListaStock();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ordine in corso
    // ─────────────────────────────────────────────────────────────────────────

    public void impostaTransazionePending(TransactionBean transazione) {
        this.contesto.setTransazionePending(transazione);
    }

    public TransactionBean getTransazionePending() {
        return this.contesto.getTransazionePending();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Portafoglio
    // ─────────────────────────────────────────────────────────────────────────

    public void impostaPortafoglio(PortafoglioBean portafoglio) {
        this.contesto.setPortafoglio(portafoglio);
    }

    public PortafoglioBean getPortafoglio() {
        return this.contesto.getPortafoglio();
    }

    public void impostaStoricoTransazioni(List<TransactionBean> storico) {
        this.contesto.setStoricoTransazioni(storico);
    }

    public List<TransactionBean> getStoricoTransazioni() {
        return this.contesto.getStoricoTransazioni();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Classe e studenti (uso professore)
    // ─────────────────────────────────────────────────────────────────────────

    public void impostaClasseCorrente(SchoolClassBean classe) {
        this.contesto.setClasseCorrente(classe);
    }

    public SchoolClassBean getClasseCorrente() {
        return this.contesto.getClasseCorrente();
    }

    public void impostaStudentiClasse(List<StudenteBean> studenti) {
        this.contesto.setStudentiClasse(studenti);
    }

    public List<StudenteBean> getStudentiClasse() {
        return this.contesto.getStudentiClasse();
    }

    public void impostaListaClassi(List<SchoolClassBean> classi) {
        this.contesto.setListaClassi(classi);
    }

    public List<SchoolClassBean> getListaClassi() {
        return this.contesto.getListaClassi();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigazione
    // ─────────────────────────────────────────────────────────────────────────

    protected void setSchermata(Schermate schermata) {
        this.schermataCorrente = schermata;
    }

    protected void nextScreen() {
        if (this.schermataCorrente == null) return;

        switch (this.schermataCorrente) {
            case LOGIN              -> visualizzaLogin();
            case REGISTRAZIONE      -> visualizzaRegistrazione();
            case HOME_STUDENTE      -> visualizzaHomeStudente();
            case MERCATO            -> visualizzaMercato();
            case DETTAGLIO_STOCK    -> visualizzaDettaglioStock();
            case CONFERMA_ORDINE    -> visualizzaConfermaOrdine();
            case PORTAFOGLIO        -> visualizzaPortafoglio();
            case STORICO            -> visualizzaStorico();
            case HOME_PROFESSORE    -> visualizzaHomeProfessore();
            case GESTIONE_CLASSE    -> visualizzaGestioneClasse();
            case ELENCO_STUDENTI    -> visualizzaElencoStudenti();
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

    public void goToLogin()             { setSchermata(Schermate.LOGIN);           nextScreen(); }
    public void goToRegistrazione()     { setSchermata(Schermate.REGISTRAZIONE);   nextScreen(); }
    public void goToHomeStudente()      { setSchermata(Schermate.HOME_STUDENTE);   nextScreen(); }
    public void goToMercato()           { setSchermata(Schermate.MERCATO);         nextScreen(); }
    public void goToDettaglioStock()    { setSchermata(Schermate.DETTAGLIO_STOCK); nextScreen(); }
    public void goToConfermaOrdine()    { setSchermata(Schermate.CONFERMA_ORDINE); nextScreen(); }
    public void goToPortafoglio()       { setSchermata(Schermate.PORTAFOGLIO);     nextScreen(); }
    public void goToStorico()           { setSchermata(Schermate.STORICO);         nextScreen(); }
    public void goToHomeProfessore()    { setSchermata(Schermate.HOME_PROFESSORE); nextScreen(); }
    public void goToGestioneClasse()    { setSchermata(Schermate.GESTIONE_CLASSE); nextScreen(); }
    public void goToElencoStudenti()    { setSchermata(Schermate.ELENCO_STUDENTI); nextScreen(); }

    public void esci() {
        running = false;
        System.exit(0);
    }

    // ── Metodi astratti da implementare nelle sottoclassi ─────────────────────

    protected abstract void visualizzaLogin();
    protected abstract void visualizzaRegistrazione();
    protected abstract void visualizzaHomeStudente();
    protected abstract void visualizzaMercato();
    protected abstract void visualizzaDettaglioStock();
    protected abstract void visualizzaConfermaOrdine();
    protected abstract void visualizzaPortafoglio();
    protected abstract void visualizzaStorico();
    protected abstract void visualizzaHomeProfessore();
    protected abstract void visualizzaGestioneClasse();
    protected abstract void visualizzaElencoStudenti();
}
