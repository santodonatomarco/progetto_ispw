package org.project.view;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SchoolClassBean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HomeProfessoreGraphicControllerGUI extends HomeProfessoreGraphicController {

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private Label lblNomeUtente;
    @FXML private Label lblEmailUtente;

    // ── Header ────────────────────────────────────────────────────────────────
    @FXML private Label lblDataOra;

    // ── Carte statistiche ─────────────────────────────────────────────────────
    @FXML private Label lblNumClassi;
    @FXML private Label lblNumStudenti;

    // ── Lista classi ──────────────────────────────────────────────────────────
    @FXML private VBox  vboxClassi;
    @FXML private Label lblNessunaClasse;

    // ── Messaggio sistema ─────────────────────────────────────────────────────
    @FXML private Label lblMessaggio;

    // ── View root ─────────────────────────────────────────────────────────────
    private Parent view;

    private static final DateTimeFormatter FMT_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final java.text.NumberFormat VALUTA =
            java.text.NumberFormat.getCurrencyInstance(java.util.Locale.ITALY);

    // ── Setter/Getter view ────────────────────────────────────────────────────

    public void setView(Parent view)    { this.view = view; }
    public Parent getView()             { return view; }

    public void setGuiNavigator(NavigatorGUI navigator) {
        super.setNavigator(navigator);
    }

    // ── start() ───────────────────────────────────────────────────────────────

    @Override
    public void start() {
        nascondiMessaggio();
        lblDataOra.setText(LocalDateTime.now(ZoneId.systemDefault()).format(FMT_DATA));

        ProfessoreBean professore = getProfessoreLoggato();
        if (professore == null) {
            mostraMessaggio("Sessione non valida. Effettua nuovamente il login.");
            return;
        }

        // Sidebar
        lblNomeUtente.setText("Prof. " + professore.getNome() + " " + professore.getCognome());
        lblEmailUtente.setText(professore.getEmail());

        // Classi
        List<SchoolClassBean> classi = getListaClassi();
        aggiornaUIClassi(classi);
    }

    // ── aggiornaUIClassi ──────────────────────────────────────────────────────

    @Override
    protected void aggiornaUIClassi(List<SchoolClassBean> classi) {
        if (classi == null || classi.isEmpty()) {
            lblNumClassi.setText("0");
            lblNumStudenti.setText("0");
            lblNessunaClasse.setVisible(true);
            lblNessunaClasse.setManaged(true);
            return;
        }

        lblNessunaClasse.setVisible(false);
        lblNessunaClasse.setManaged(false);

        lblNumClassi.setText(String.valueOf(classi.size()));

        int totStudenti = 0;
        for (SchoolClassBean c : classi) {
            if (c.getStudenti() != null) totStudenti += c.getStudenti().size();
        }
        lblNumStudenti.setText(String.valueOf(totStudenti));

        vboxClassi.getChildren().clear();
        for (SchoolClassBean classe : classi) {
            vboxClassi.getChildren().add(creaCardClasse(classe));
        }
    }

    // ── Costruzione card classe ───────────────────────────────────────────────


    private HBox creaCardClasse(SchoolClassBean classe) {
        HBox card = new HBox(16);
        card.getStyleClass().add("card-professore");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Nome classe
        VBox infoClasse = new VBox(4);
        Label lblNome = new Label(classe.getNome());
        lblNome.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1a237e;");
        Label lblBudget = new Label("Budget iniziale: " + VALUTA.format(classe.getBudgetIniziale()));
        lblBudget.getStyleClass().add("testo-secondario");
        infoClasse.getChildren().addAll(lblNome, lblBudget);
        HBox.setHgrow(infoClasse, javafx.scene.layout.Priority.ALWAYS);

        // Numero studenti
        int numStudenti = (classe.getStudenti() != null) ? classe.getStudenti().size() : 0;
        VBox infoStudenti = new VBox(4);
        infoStudenti.setAlignment(javafx.geometry.Pos.CENTER);
        Label lblCount = new Label(String.valueOf(numStudenti));
        lblCount.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#1a237e;");
        Label lblLabel = new Label("studenti");
        lblLabel.getStyleClass().add("testo-secondario");
        infoStudenti.getChildren().addAll(lblCount, lblLabel);

        // Bottone gestisci
        Button btnGestisci = new Button("Gestisci →");
        btnGestisci.getStyleClass().add("btn-primario-professore");
        btnGestisci.setStyle("-fx-padding:8 16 8 16;");
        btnGestisci.setOnAction(e -> {
            navigator.impostaClasseCorrente(classe);
            vaiAGestioneClasse();
        });

        card.getChildren().addAll(infoClasse, infoStudenti, btnGestisci);
        return card;
    }

    // ── Handler FXML ─────────────────────────────────────────────────────────

    @FXML
    private void clickHome() {
        start(); // refresh dashboard
    }

    @FXML
    private void clickMercato() {
        vaiAlMercato();
    }

    @FXML
    private void clickGestioneClasse() {
        vaiAGestioneClasse();
    }

    @FXML
    private void clickInbox() {
        vaiAllaInbox();
    }

    @FXML
    private void clickLogout() {
        eseguiLogout();
    }

    // ── Feedback ─────────────────────────────────────────────────────────────

    @Override
    protected void mostraMessaggio(String msg) {
        lblMessaggio.setText(msg);
        lblMessaggio.setVisible(true);
        lblMessaggio.setManaged(true);
    }

    private void nascondiMessaggio() {
        lblMessaggio.setText("");
        lblMessaggio.setVisible(false);
        lblMessaggio.setManaged(false);
    }

    @Override
    protected void showMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore di sistema");
        alert.setHeaderText("Si è verificato un problema");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}