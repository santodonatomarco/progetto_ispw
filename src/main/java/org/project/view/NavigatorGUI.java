package org.project.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class NavigatorGUI extends Navigator {

    private final Stage stage;

    private LoginGraphicControllerGUI            login;
    private HomeStudenteGraphicControllerGUI     homeStudente;
    private HomeProfessoreGraphicControllerGUI   homeProfessore;
    private ManageWalletsGraphicControllerGUI    manageWallets;
    private GestioneClasseGraphicControllerGUI   gestioneClasse;
    private ExchangeMessagesGraphicControllerGUI exchangeMessages;

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

    // ── Cambio schermata ──────────────────────────────────────────────────────

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


    private ManageWalletsGraphicControllerGUI caricaManageWallets() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(
                            getClass().getResource("/fxml/ManageWallets.fxml"),
                            "FXML non trovato: ManageWallets.fxml"));
            Parent view = loader.load();
            ManageWalletsGraphicControllerGUI ctrl = loader.getController();
            ctrl.setGuiNavigator(this);
            ctrl.setView(view);
            return ctrl;
        } catch (IOException e) {
            throw new RuntimeException("Impossibile caricare ManageWallets.fxml", e);
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
        System.out.println("[Registrazione GUI — da implementare]");
    }

    @Override
    protected void visualizzaHomeStudente() {
        if (this.homeStudente == null) {
            this.homeStudente = new HomeStudenteGraphicControllerGUI();
            this.homeStudente.setGuiNavigator(this);
            Parent view = caricaFXML("HomeStudente.fxml", this.homeStudente);
            this.homeStudente.setView(view);
        }
        this.homeStudente.start();
        mostraSchermata(this.homeStudente.getView());
    }

    // ── ManageWallets — tutti gli entry-point riusano la stessa istanza ───────

    private ManageWalletsGraphicControllerGUI getManageWallets() {
        if (this.manageWallets == null)
            this.manageWallets = caricaManageWallets();
        return this.manageWallets;
    }

    @Override
    protected void visualizzaMercato() {
        getManageWallets().start();
        mostraSchermata(this.manageWallets.getView());
    }

    @Override
    protected void visualizzaDettaglioStock() {
        // Gestito inline dal panel dettaglio all'interno di ManageWallets.fxml
    }

    @Override
    protected void visualizzaConfermaOrdine() {
        getManageWallets().startConfermaOrdine();
        mostraSchermata(this.manageWallets.getView());
    }

    @Override
    protected void visualizzaPortafoglio() {
        getManageWallets().startPortafoglio();
        mostraSchermata(this.manageWallets.getView());
    }

    @Override
    protected void visualizzaStorico() {
        getManageWallets().startStorico();
        mostraSchermata(this.manageWallets.getView());
    }


    @Override
    protected void visualizzaWalletStudente() {
        var target = getStudenteTarget();
        if (target == null) {
            System.err.println("[NavigatorGUI] visualizzaWalletStudente: studenteTarget non impostato");
            return;
        }
        getManageWallets().startWalletEsterno(target);
        mostraSchermata(this.manageWallets.getView());
    }

    // ── Professore ────────────────────────────────────────────────────────────

    @Override
    protected void visualizzaHomeProfessore() {
        if (this.homeProfessore == null) {
            this.homeProfessore = new HomeProfessoreGraphicControllerGUI();
            this.homeProfessore.setGuiNavigator(this);
            Parent view = caricaFXML("HomeProfessore.fxml", this.homeProfessore);
            this.homeProfessore.setView(view);
        }
        this.homeProfessore.start();
        mostraSchermata(this.homeProfessore.getView());
    }

    @Override
    protected void visualizzaGestioneClasse() {
        if (this.gestioneClasse == null) {
            this.gestioneClasse = new GestioneClasseGraphicControllerGUI();
            this.gestioneClasse.setGuiNavigator(this);
            Parent view = caricaFXML("GestioneClasse.fxml", this.gestioneClasse);
            this.gestioneClasse.setView(view);
        }
        this.gestioneClasse.start();
        mostraSchermata(this.gestioneClasse.getView());
    }

    // ── Inbox / Exchange Messages ─────────────────────────────────────────────

    @Override
    protected void visualizzaInbox() {
        if (this.exchangeMessages == null) {
            this.exchangeMessages = new ExchangeMessagesGraphicControllerGUI();
            this.exchangeMessages.setGuiNavigator(this);
            Parent view = caricaFXML("Inbox.fxml", this.exchangeMessages);
            this.exchangeMessages.setView(view);
        }
        this.exchangeMessages.start();
        mostraSchermata(this.exchangeMessages.getView());
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