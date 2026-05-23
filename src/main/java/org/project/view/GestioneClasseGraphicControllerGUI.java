package org.project.view;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.project.control.GestioneClasseAppController;
import org.project.exceptions.ControllerException;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SchoolClassBean;
import org.project.view.bean.SessioneBean;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Controller GUI per la schermata Gestione Classe (professore).
 *
 * Permette di:
 *  - Selezionare una classe esistente e modificarne il budget
 *  - Creare una nuova classe con budget iniziale
 */
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

    // ── Pannello modifica ─────────────────────────────────────────────────────

    private void mostraPanelModifica(SchoolClassBean classe) {
        lblNomeClasseSelezionata.setText("Classe: " + classe.getNome());
        lblBudgetAttuale.setText("Budget attuale: " + VALUTA.format(classe.getBudgetIniziale()));
        panelModificaBudget.setVisible(true);
        panelModificaBudget.setManaged(true);
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