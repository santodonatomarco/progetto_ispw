package org.project.view;


/**
 * Navigator CLI — gestisce la navigazione testuale.
 * Istanzia i controller grafici CLI in lazy loading (come nel progetto di riferimento).
 * Il loop while(running) gira in Main.
 */
public class NavigatorCLI extends Navigator {

    private LoginGraphicControllerCLI login;

    /**
     * Le altre schermate verranno aggiunte man mano
     * private HomeStudenteGraphicControllerCLI homeStudente;
     * private HomeProfessoreGraphicControllerCLI homeProfessore;
     * ecc.
     */

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
        // TODO: new HomeStudenteGraphicControllerCLI(this).start();
        System.out.println("[Home Studente CLI — da implementare]");
    }

    @Override
    protected void visualizzaMercato() {
        System.out.println("[Mercato CLI — da implementare]");
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
        System.out.println("[Home Professore CLI — da implementare]");
    }

    @Override
    protected void visualizzaGestioneClasse() {
        System.out.println("[Gestione Classe CLI — da implementare]");
    }

    @Override
    protected void visualizzaElencoStudenti() {
        System.out.println("[Elenco Studenti CLI — da implementare]");
    }


    @Override
    public void logout() {
        this.login = null;
        // azzera gli altri controller quando li aggiungi
    }
}
