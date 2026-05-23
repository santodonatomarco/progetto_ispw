package org.project.view;


/**
 * Navigator CLI — gestisce la navigazione testuale.
 * Istanzia i controller grafici CLI in lazy loading (come nel progetto di riferimento).
 * Il loop while(running) gira in Main.
 */
public class NavigatorCLI extends Navigator {

    private LoginGraphicControllerCLI login;
    private HomeStudenteGraphicControllerCLI homeStudente;
    private HomeProfessoreGraphicControllerCLI homeProfessore;
    private MercatoGraphicControllerCLI mercato;

    public NavigatorCLI() {
        super();
    }

    @Override
    public void startUp() {
        goToLogin();
        while (this.running) {
            super.nextScreen();
        }
    }

    @Override
    protected void visualizzaLogin() {
        if (this.login == null) {
            this.login = new LoginGraphicControllerCLI(this);
        }
        this.login.start();
    }

    @Override
    protected void visualizzaRegistrazione() {
        // TODO: new RegistrazioneGraphicControllerCLI(this).start();
        System.out.println("[Registrazione CLI — da implementare]");
    }

    @Override
    protected void visualizzaHomeStudente() {
        if (this.homeStudente == null) {
            this.homeStudente = new HomeStudenteGraphicControllerCLI(this);
        }
        this.homeStudente.start();
    }

    @Override
    protected void visualizzaMercato() {
        if (this.mercato == null) {
            this.mercato = new MercatoGraphicControllerCLI(this);
        }
        this.mercato.start();
    }

    @Override
    protected void visualizzaDettaglioStock() {
        System.out.println("[Dettaglio Stock CLI — da implementare]");
    }

    @Override
    protected void visualizzaConfermaOrdine() {
        System.out.println("[Conferma Ordine CLI — da implementare]");
    }

    @Override
    protected void visualizzaPortafoglio() {
        System.out.println("[Portafoglio CLI — da implementare]");
    }

    @Override
    protected void visualizzaStorico() {
        System.out.println("[Storico CLI — da implementare]");
    }

    @Override
    protected void visualizzaHomeProfessore() {
        if (this.homeProfessore == null) {
            this.homeProfessore = new HomeProfessoreGraphicControllerCLI(this);
        }
        this.homeProfessore.start();
    }

    @Override
    protected void visualizzaGestioneClasse() {
        new GestioneClasseGraphicControllerCLI(this).start();
    }

    @Override
    protected void visualizzaElencoStudenti() {
        System.out.println("[Elenco Studenti CLI — da implementare]");
    }


    @Override
    public void logout() {
        this.login = null;
        this.homeStudente = null;
        this.homeProfessore = null;
        this.mercato = null;
    }
}