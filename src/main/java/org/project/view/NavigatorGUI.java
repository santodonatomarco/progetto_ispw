package org.project.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Navigator GUI — gestisce la navigazione JavaFX.
 * Ogni schermata viene caricata da un file FXML.
 * I controller grafici GUI vengono istanziati in lazy loading e riusati.
 */
public class NavigatorGUI extends Navigator {

    private final Stage stage;

    private LoginGraphicControllerGUI login;
    // private HomeStudenteGraphicControllerGUI homeStudente;
    // private HomeProfessoreGraphicControllerGUI homeProfessore;
    // ecc.

    public NavigatorGUI() {
        super();
        this.stage = new Stage();
        this.stage.setTitle("UniFinance");
        this.stage.setMinWidth(600);
        this.stage.setMinHeight(400);
    }

    @Override
    public void startUp() {
        goToLogin();
    }

    // ── Metodo centrale per il cambio schermata ───────────────────────────────

    private void mostraSchermata(Parent view) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(view);
            stage.setScene(scene);
        } else {
            scene.setRoot(view);
        }
        stage.show();
    }

    // ── Utility: carica FXML ──────────────────────────────────────────────────

    private <T> T caricaFXML(String nomeFile, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(
                            getClass().getResource("/fxml/" + nomeFile),
                            "FXML non trovato: " + nomeFile));
            loader.setController(controller);
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Impossibile caricare " + nomeFile, e);
        }
    }

    // ── Schermate ─────────────────────────────────────────────────────────────

    @Override
    protected void visualizzaLogin() {
        if (this.login == null) {
            this.login = new LoginGraphicControllerGUI();
            this.login.setGuiNavigator(this);
            Parent view = caricaFXML("Login.fxml", this.login);
            this.login.setView(view);
        }
        this.login.start();
        mostraSchermata(this.login.getView());
    }

    @Override
    protected void visualizzaRegistrazione() {
        // TODO: caricare RegistrazioneGraphicControllerGUI
        System.out.println("[Registrazione GUI — da implementare]");
    }

    @Override
    protected void visualizzaHomeStudente() {
        // TODO: caricare HomeStudenteGraphicControllerGUI
        System.out.println("[Home Studente GUI — da implementare]");
    }

    @Override
    protected void visualizzaMercato() {
        System.out.println("[Mercato GUI — da implementare]");
    }

    @Override
    protected void visualizzaDettaglioStock() {
        System.out.println("[Dettaglio Stock GUI — da implementare]");
    }

    @Override
    protected void visualizzaConfermaOrdine() {
        System.out.println("[Conferma Ordine GUI — da implementare]");
    }

    @Override
    protected void visualizzaPortafoglio() {
        System.out.println("[Portafoglio GUI — da implementare]");
    }

    @Override
    protected void visualizzaStorico() {
        System.out.println("[Storico GUI — da implementare]");
    }

    @Override
    protected void visualizzaHomeProfessore() {
        System.out.println("[Home Professore GUI — da implementare]");
    }

    @Override
    protected void visualizzaGestioneClasse() {
        System.out.println("[Gestione Classe GUI — da implementare]");
    }

    @Override
    protected void visualizzaElencoStudenti() {
        System.out.println("[Elenco Studenti GUI — da implementare]");
    }

    @Override
    public void logout() {
        this.login = null;
        // azzera gli altri controller quando li aggiungi
    }

}