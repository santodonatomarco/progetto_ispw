package org.project.view;


public class NavigatorCLI extends Navigator {

    private LoginGraphicControllerCLI            login;
    private HomeStudenteGraphicControllerCLI     homeStudente;
    private HomeProfessoreGraphicControllerCLI   homeProfessore;
    private ManageWalletsGraphicControllerCLI    manageWallets;
    private GestioneClasseGraphicControllerCLI   gestioneClasse;
    private ExchangeMessagesGraphicControllerCLI exchangeMessages;

    public NavigatorCLI() { super(); }

    @Override
    public void startUp() {
        goToLogin();
        while (this.running) super.nextScreen();
    }

    // ── Login / Registrazione ─────────────────────────────────────────────────

    @Override
    protected void visualizzaLogin() {
        if (this.login == null) this.login = new LoginGraphicControllerCLI(this);
        this.login.start();
    }

    @Override
    protected void visualizzaRegistrazione() {
        System.out.println("[Registrazione CLI — da implementare]");
    }

    // ── Home ──────────────────────────────────────────────────────────────────

    @Override
    protected void visualizzaHomeStudente() {
        if (this.homeStudente == null)
            this.homeStudente = new HomeStudenteGraphicControllerCLI(this);
        this.homeStudente.start();
    }

    @Override
    protected void visualizzaHomeProfessore() {
        if (this.homeProfessore == null)
            this.homeProfessore = new HomeProfessoreGraphicControllerCLI(this);
        this.homeProfessore.start();
    }

    // ── ManageWallets — tutti gli entry-point ─────────────────────────────────

    private ManageWalletsGraphicControllerCLI getManageWallets() {
        if (this.manageWallets == null)
            this.manageWallets = new ManageWalletsGraphicControllerCLI(this);
        return this.manageWallets;
    }

    @Override
    protected void visualizzaMercato() {
        getManageWallets().start();
    }

    @Override
    protected void visualizzaDettaglioStock() {
        // Gestito internamente da ManageWalletsGraphicControllerCLI.mostraDettaglioStock()
    }

    @Override
    protected void visualizzaConfermaOrdine() {
        getManageWallets().startConfermaOrdine();
    }

    @Override
    protected void visualizzaPortafoglio() {
        getManageWallets().startPortafoglio();
    }

    @Override
    protected void visualizzaStorico() {
        getManageWallets().startStorico();
    }


    @Override
    protected void visualizzaWalletStudente() {
        var target = getStudenteTarget();
        if (target == null) {
            System.out.println("  ⚠  Nessun studente target impostato.");
            return;
        }
        getManageWallets().startWalletEsterno(target);
    }

    // ── GestioneClasse / ElencoStudenti ──────────────────────────────────────

    @Override
    protected void visualizzaGestioneClasse() {
        if (this.gestioneClasse == null)
            this.gestioneClasse = new GestioneClasseGraphicControllerCLI(this);
        this.gestioneClasse.start();
    }

    // ── Inbox / Exchange Messages ─────────────────────────────────────────────

    @Override
    protected void visualizzaInbox() {
        if (this.exchangeMessages == null)
            this.exchangeMessages = new ExchangeMessagesGraphicControllerCLI(this);
        this.exchangeMessages.start();
    }


    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    public void logout() {
        this.login          = null;
        this.homeStudente   = null;
        this.homeProfessore = null;
        this.manageWallets  = null;
        this.gestioneClasse = null;
        this.exchangeMessages = null;
    }
}