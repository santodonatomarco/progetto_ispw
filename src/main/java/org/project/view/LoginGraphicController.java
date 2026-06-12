package org.project.view;

import org.project.control.LoginAppController;
import org.project.exceptions.ControllerException;
import org.project.exceptions.CredenzialNonValideException;
import org.project.view.Navigator;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;

/**
 * Controller grafico astratto per la schermata di login.
 * Contiene la logica condivisa tra CLI e GUI:
 * - campi email, password, ruolo selezionato
 * - metodo eseguiLogin() che chiama il controller applicativo
 *
 * Le sottoclassi implementano start(), mostraErrore() e showMessage()
 * con la tecnologia concreta (CLI o JavaFX).
 */
public abstract class LoginGraphicController {

    protected Navigator navigator;

    // Dati inseriti dall'utente — valorizzati dalle sottoclassi prima di chiamare eseguiLogin()
    protected String email;
    protected String password;
    protected boolean isStudente = true; // true = studente, false = professore

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public abstract void start();

    // ── Logica condivisa ──────────────────────────────────────────────────────

    protected void eseguiLogin() {
        LoginAppController appController = new LoginAppController();

        try {
            if (isStudente) {
                StudenteBean bean = new StudenteBean(email, password);
                SessioneBean sessione = appController.loginStudente(bean);

                navigator.impostaStudente(sessione.getStudente());
                navigator.impostaSessione(sessione);
                navigator.impostaPortafoglio(sessione.getPortafoglio());
                navigator.goToHomeStudente();

            } else {
                ProfessoreBean bean = new ProfessoreBean(email, password);
                SessioneBean sessione = appController.loginProfessore(bean);

                navigator.impostaProfessore(sessione.getProfessore());
                navigator.impostaSessione(sessione);
                if (sessione.getListaClassi() != null) {
                    navigator.impostaListaClassi(sessione.getListaClassi());
                }
                navigator.goToHomeProfessore();
            }

        } catch (CredenzialNonValideException e) {
            mostraErrore(e.getMessage());
        } catch (ControllerException e) {
            // 1. STAMPA IL VERO ERRORE NELLA CONSOLE DELL'IDE IN ROSSO
            // e.printStackTrace();

            // 2. MOSTRA LA CAUSA REALE DIRETTAMENTE NEL POP-UP DELLA GUI
            // String causaReale = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            // showMessage("ERRORE: " + causaReale);
            showMessage("Si è verificato un problema. Riprova più tardi.");
        }
    }

    protected abstract void mostraErrore(String msg);
    protected abstract void showMessage(String msg);

    public void chiudiApp() {
        if (navigator != null) navigator.esci();
    }
}