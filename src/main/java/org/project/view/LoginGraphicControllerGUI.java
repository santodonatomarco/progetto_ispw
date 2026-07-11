package org.project.view;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;


public class LoginGraphicControllerGUI extends LoginGraphicController {

    // ── Sezione Studente ──────────────────────────────────────────────────────
    @FXML private TextField txtEmailStudente;
    @FXML private PasswordField txtPasswordStudente;
    @FXML private Label lblErroreStudente;

    // ── Sezione Professore ────────────────────────────────────────────────────
    @FXML private TextField txtEmailProfessore;
    @FXML private PasswordField txtPasswordProfessore;
    @FXML private Label lblErroreProfessore;

    private Parent view;

    public void setView(Parent view)    { this.view = view; }
    public Parent getView()             { return view; }

    public void setGuiNavigator(NavigatorGUI navigator) {
        super.setNavigator(navigator);
    }

    @Override
    public void start() {
        txtEmailStudente.clear();
        txtPasswordStudente.clear();
        txtEmailProfessore.clear();
        txtPasswordProfessore.clear();
        nascondiErrore(lblErroreStudente);
        nascondiErrore(lblErroreProfessore);
    }

    // ── Handler Studente ──────────────────────────────────────────────────────

    @FXML
    private void clickLoginStudente() {
        nascondiErrore(lblErroreStudente);

        String email = txtEmailStudente.getText().trim();
        String password = txtPasswordStudente.getText();

        // Validazione campi vuoti — gestita qui, non arriva mai al controller applicativo
        if (email.isEmpty() || password.isEmpty()) {
            mostraErroreStudente("Inserisci email e password per continuare.");
            return;
        }

        super.isStudente = true;
        super.email = email;
        super.password = password;
        super.eseguiLogin();
    }

    @FXML
    private void clickRegistratiStudente() {
        navigator.goToRegistrazione();
    }

    // ── Handler Professore ────────────────────────────────────────────────────

    @FXML
    private void clickLoginProfessore() {
        nascondiErrore(lblErroreProfessore);

        String email = txtEmailProfessore.getText().trim();
        String password = txtPasswordProfessore.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostraErroreProfessore("Inserisci email e password per continuare.");
            return;
        }

        super.isStudente = false;
        super.email = email;
        super.password = password;
        super.eseguiLogin();
    }

    @FXML
    private void clickRegistratiProfessore() {
        navigator.goToRegistrazione();
    }

    // ── Feedback errori ───────────────────────────────────────────────────────

    @Override
    protected void mostraErrore(String msg) {
        if (super.isStudente) {
            mostraErroreStudente(msg);
        } else {
            mostraErroreProfessore(msg);
        }
    }

    @Override
    protected void showMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore di sistema");
        alert.setHeaderText("Si è verificato un problema");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ── Utility private ───────────────────────────────────────────────────────

    private void mostraErroreStudente(String msg) {
        lblErroreStudente.setText(msg);
        lblErroreStudente.setVisible(true);
        lblErroreStudente.setManaged(true);
    }

    private void mostraErroreProfessore(String msg) {
        lblErroreProfessore.setText(msg);
        lblErroreProfessore.setVisible(true);
        lblErroreProfessore.setManaged(true);
    }

    private void nascondiErrore(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }
}