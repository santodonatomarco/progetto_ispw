package org.project.view;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.project.control.GestioneClasseAppController;
import org.project.exceptions.ControllerException;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SchoolClassBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


public class GestioneClasseGraphicControllerGUI extends GestioneClasseGraphicController {

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private Label lblNomeUtente;
    @FXML private Label lblEmailUtente;

    // ── Classi esistenti ──────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbClassi;
    @FXML private VBox             panelModificaBudget;
    @FXML private Label            lblNomeClasseSelezionata;
    @FXML private Label            lblBudgetAttuale;
    @FXML private TextField        txtNuovoBudget;
    @FXML private TextField        txtEmailStudente;

    // ── Studenti della classe ─────────────────────────────────────────────────
    @FXML private VBox             vboxStudentiAnteprima;
    @FXML private Label            lblNessunoStudente;

    // ── Nuova classe ──────────────────────────────────────────────────────────
    @FXML private TextField txtNomeNuovaClasse;
    @FXML private TextField txtBudgetNuovaClasse;

    // ── Feedback ──────────────────────────────────────────────────────────────
    @FXML private Label lblMessaggio;

    // ── Root view ─────────────────────────────────────────────────────────────
    private Parent view;

    private List<SchoolClassBean> classiCaricate;

    private static final NumberFormat VALUTA = NumberFormat.getCurrencyInstance(Locale.ITALY);

    public void setView(Parent view)             { this.view = view; }
    public Parent getView()                      { return view; }
    public void setGuiNavigator(NavigatorGUI nav) { this.setNavigator(nav); }

    // ── start() ───────────────────────────────────────────────────────────────

    public void start() {
        nascondiMessaggio();
        nascondiPanelModifica();

        SessioneBean sessione = navigator.getSessione();
        ProfessoreBean professore = (sessione != null) ? sessione.getProfessore() : null;

        if (professore != null) {
            lblNomeUtente.setText("Prof. " + professore.getNome() + " " + professore.getCognome());
            lblEmailUtente.setText(professore.getEmail());
        }

        caricaClassiInComboBox();

        // Se c'è già una classe selezionata nel navigator, pre-selezionala
        SchoolClassBean classeCorrente = navigator.getClasseCorrente();
        if (classeCorrente != null) {
            cmbClassi.setValue(classeCorrente.getNome());
            mostraPanelModifica(classeCorrente);
        }
    }

    private void caricaClassiInComboBox() {
        cmbClassi.getItems().clear();
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) return;

        try {
            GestioneClasseAppController controller = new GestioneClasseAppController();
            classiCaricate = controller.getClassiDelProfessore(sessione);
            for (SchoolClassBean c : classiCaricate) {
                cmbClassi.getItems().add(c.getNome());
            }
        } catch (ControllerException e) {
            mostraErrore("Impossibile caricare le classi: " + e.getMessage());
        }
    }

    // ── Handler FXML ─────────────────────────────────────────────────────────

    @FXML
    private void clickCaricaClasse() {
        String nomeSelezionato = cmbClassi.getValue();
        if (nomeSelezionato == null || nomeSelezionato.isBlank()) {
            mostraErrore("Seleziona una classe dalla lista.");
            return;
        }
        SchoolClassBean classe = trovaClasse(nomeSelezionato);
        if (classe == null) {
            mostraErrore("Classe non trovata.");
            return;
        }
        navigator.impostaClasseCorrente(classe);
        mostraPanelModifica(classe);
        nascondiMessaggio();
    }

    @FXML
    private void clickSalvaBudget() {
        nascondiMessaggio();
        SchoolClassBean classeCorrente = navigator.getClasseCorrente();
        if (classeCorrente == null) {
            mostraErrore("Nessuna classe selezionata.");
            return;
        }

        String testo = txtNuovoBudget.getText();
        double nuovoBudget;
        try {
            nuovoBudget = Double.parseDouble(testo.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            mostraErrore("Inserisci un valore numerico valido per il budget.");
            return;
        }

        SchoolClassBean aggiornata = eseguiImpostaBudget(classeCorrente.getNome(), nuovoBudget);
        if (aggiornata != null) {
            mostraPanelModifica(aggiornata);
            txtNuovoBudget.clear();
        }

        caricaClassiInComboBox();
    }

    @FXML
    private void clickCreaClasse() {
        nascondiMessaggio();
        String nome = txtNomeNuovaClasse.getText();
        String budgetTesto = txtBudgetNuovaClasse.getText();

        if (nome == null || nome.isBlank()) {
            mostraErrore("Inserisci il nome della nuova classe.");
            return;
        }

        double budget = 0.0;
        if (budgetTesto != null && !budgetTesto.isBlank()) {
            try {
                budget = Double.parseDouble(budgetTesto.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                mostraErrore("Il budget deve essere un numero valido.");
                return;
            }
        }

        SchoolClassBean nuova = eseguiCreaClasse(nome.trim().toUpperCase(), budget);
        if (nuova != null) {
            txtNomeNuovaClasse.clear();
            txtBudgetNuovaClasse.clear();
            caricaClassiInComboBox();
        }
    }

    @FXML
    private void clickAggiungiStudente() {
        nascondiMessaggio();
        SchoolClassBean classeCorrente = navigator.getClasseCorrente();
        if (classeCorrente == null) {
            mostraErrore("Seleziona prima una classe.");
            return;
        }
        String email = txtEmailStudente.getText().trim();
        if (email.isEmpty()) {
            mostraErrore("Inserisci l'email dello studente.");
            return;
        }
        boolean ok = eseguiAggiungiStudente(email, classeCorrente.getNome());
        if (ok) {
            txtEmailStudente.clear();
        }
    }

    @FXML
    private void clickDashboard() {
        tornaDashboard();
    }

    @FXML
    private void clickMercato() {
        vaiAlMercato();
    }

    @FXML
    private void clickLogout() {
        eseguiLogout();
    }

    @FXML
    private void clickVediStudenti() {
        SchoolClassBean classeCorrente = navigator.getClasseCorrente();
        if (classeCorrente == null) {
            mostraErrore("Nessuna classe selezionata.");
            return;
        }

        List<StudenteBean> studenti = eseguiCaricaStudenti(classeCorrente.getNome());
        if (studenti == null) return; // errore già mostrato da eseguiCaricaStudenti
        if (studenti.isEmpty()) {
            mostraErrore("Nessuno studente iscritto a questa classe.");
            return;
        }

        // Crea una finestra di dialogo con l'elenco degli studenti
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Elenco Studenti - " + classeCorrente.getNome());
        dialog.setHeaderText("Studenti della classe " + classeCorrente.getNome());

        VBox content = new VBox(10);
        content.setPrefWidth(500);
        content.setPrefHeight(400);
        content.setStyle("-fx-padding: 20;");

        ScrollPane scrollPane = new ScrollPane();
        VBox listaStudenti = new VBox(6);
        listaStudenti.setStyle("-fx-padding: 10;");

        for (StudenteBean s : studenti) {
            listaStudenti.getChildren().add(creaRigaStudenteAnteprima(s));
        }

        scrollPane.setContent(listaStudenti);
        scrollPane.setFitToWidth(true);
        content.getChildren().add(scrollPane);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ── Pannello modifica ─────────────────────────────────────────────────────

    private void mostraPanelModifica(SchoolClassBean classe) {
        lblNomeClasseSelezionata.setText("Classe: " + classe.getNome());
        lblBudgetAttuale.setText("Budget attuale: " + VALUTA.format(classe.getBudgetIniziale()));
        panelModificaBudget.setVisible(true);
        panelModificaBudget.setManaged(true);
        popolaAnteprimaStudenti(classe.getNome());
    }

    // ── Anteprima studenti (max 5 righe) ─────────────────────────────────────

    private void popolaAnteprimaStudenti(String nomeClasse) {
        if (vboxStudentiAnteprima == null) return;
        vboxStudentiAnteprima.getChildren().clear();
        SessioneBean sessione = navigator.getSessione();
        if (sessione == null) return;

        try {
            List<StudenteBean> studenti = new GestioneClasseAppController()
                    .getStudentiDellaClasseProfessore(sessione, nomeClasse);

            if (studenti == null || studenti.isEmpty()) {
                if (lblNessunoStudente != null) {
                    lblNessunoStudente.setVisible(true);
                    lblNessunoStudente.setManaged(true);
                }
                return;
            }

            if (lblNessunoStudente != null) {
                lblNessunoStudente.setVisible(false);
                lblNessunoStudente.setManaged(false);
            }

            int limite = Math.min(5, studenti.size());
            for (int i = 0; i < limite; i++) {
                vboxStudentiAnteprima.getChildren().add(creaRigaStudenteAnteprima(studenti.get(i)));
            }
            if (studenti.size() > 5) {
                Label altri = new Label("… e altri " + (studenti.size() - 5) +
                        " studenti. Clicca \"Vedi elenco completo\" per tutti.");
                altri.setStyle("-fx-font-size:11px; -fx-text-fill:#5c6bc0;");
                vboxStudentiAnteprima.getChildren().add(altri);
            }

        } catch (ControllerException e) {
            // silenzioso nell'anteprima, l'utente può cliccare "Vedi elenco"
        }
    }

    private HBox creaRigaStudenteAnteprima(StudenteBean s) {
        HBox riga = new HBox(10);
        riga.setAlignment(Pos.CENTER_LEFT);
        riga.setStyle("-fx-padding:8 12 8 12; -fx-background-color:#f8f9ff; " +
                "-fx-background-radius:6; -fx-border-color:#e8eaf6; -fx-border-radius:6;");

        Label lblNome = new Label(s.getNome() + " " + s.getCognome());
        lblNome.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1a1a2e;");
        Label lblEmail = new Label("(" + s.getEmail() + ")");
        lblEmail.setStyle("-fx-font-size:11px; -fx-text-fill:#757575;");
        HBox.setHgrow(lblEmail, Priority.ALWAYS);

        Button btnPf = new Button("💼 Portafoglio");
        btnPf.setStyle("-fx-background-color:#1a237e; -fx-text-fill:white; " +
                "-fx-font-size:11px; -fx-padding:5 12 5 12; -fx-background-radius:6; -fx-cursor:hand;");
        btnPf.setOnAction(e -> {
            navigator.impostaStudenteTarget(s);
            navigator.goToWalletStudente();
        });

        riga.getChildren().addAll(lblNome, lblEmail, btnPf);
        return riga;
    }


    private void nascondiPanelModifica() {
        panelModificaBudget.setVisible(false);
        panelModificaBudget.setManaged(false);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private SchoolClassBean trovaClasse(String nome) {
        if (classiCaricate == null) return null;
        for (SchoolClassBean c : classiCaricate) {
            if (c.getNome().equals(nome)) return c;
        }
        return null;
    }

    @Override
    protected void mostraSuccesso(String msg) {
        lblMessaggio.setText("✅  " + msg);
        lblMessaggio.setStyle("-fx-font-size:13px; -fx-padding:8 12; -fx-background-radius:6; " +
                "-fx-background-color:#e8f5e9; -fx-text-fill:#2e7d32;");
        lblMessaggio.setVisible(true);
        lblMessaggio.setManaged(true);
    }

    @Override
    protected void mostraErrore(String msg) {
        lblMessaggio.setText("⚠  " + msg);
        lblMessaggio.setStyle("-fx-font-size:13px; -fx-padding:8 12; -fx-background-radius:6; " +
                "-fx-background-color:#ffebee; -fx-text-fill:#c62828;");
        lblMessaggio.setVisible(true);
        lblMessaggio.setManaged(true);
    }

    private void nascondiMessaggio() {
        lblMessaggio.setText("");
        lblMessaggio.setVisible(false);
        lblMessaggio.setManaged(false);
    }
}