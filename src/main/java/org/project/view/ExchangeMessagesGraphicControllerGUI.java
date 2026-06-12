package org.project.view;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.project.view.bean.MessageBean;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.StudenteBean;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller GUI (JavaFX) per la schermata Inbox / Exchange Messages.
 * Corrisponde a Inbox.fxml — caricato con Approccio A (setController).
 *
 * La schermata è condivisa tra studente e professore:
 *  • la sidebar mostra la navigazione corretta in base al ruolo
 *  • il campo "A:" è pre-compilato con l'email del professore per lo studente
 *
 * Layout principale:
 *  ├── Sezione messaggi ricevuti (VBox dinamica con card)
 *  └── Form di composizione (TextField destinatario + TextArea testo)
 */
public class ExchangeMessagesGraphicControllerGUI extends ExchangeMessagesGraphicController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private VBox  boxInfoUtente;
    @FXML private Label lblNomeUtente;
    @FXML private Label lblEmailUtente;

    // Sezioni di navigazione ruolo-specifiche
    @FXML private VBox boxNavStudente;
    @FXML private VBox boxNavProfessore;

    // ── Inbox ─────────────────────────────────────────────────────────────────
    @FXML private VBox  vboxMessaggi;
    @FXML private Label lblNessunMessaggio;

    // ── Composizione ──────────────────────────────────────────────────────────
    @FXML private TextField txtEmailDestinatario;
    @FXML private TextArea  txtTestoMessaggio;

    // ── Feedback ──────────────────────────────────────────────────────────────
    @FXML private Label lblMessaggio;

    // ── View root ─────────────────────────────────────────────────────────────
    private Parent view;

    public void setView(Parent view)             { this.view = view; }
    public Parent getView()                      { return view; }
    public void setGuiNavigator(NavigatorGUI nav) { this.setNavigator(nav); }

    // ── start() ───────────────────────────────────────────────────────────────

    @Override
    public void start() {
        nascondiMessaggio();
        configuraSidebarPerRuolo();
        precompilaCampoDestinatario();
        caricaInbox();
    }

    // ── Configurazione sidebar ────────────────────────────────────────────────

    private void configuraSidebarPerRuolo() {
        if (isStudente()) {
            StudenteBean s = navigator.getSessione().getStudente();
            lblNomeUtente.setText("Ciao, " + s.getNome() + "!");
            lblEmailUtente.setText(s.getEmail());

            // Stile card studente
            boxInfoUtente.getStyleClass().removeAll("card-professore");
            if (!boxInfoUtente.getStyleClass().contains("card-studente"))
                boxInfoUtente.getStyleClass().add("card-studente");

            mostraSezione(boxNavStudente);
            nascondiSezione(boxNavProfessore);

        } else if (isProfessore()) {
            ProfessoreBean p = navigator.getSessione().getProfessore();
            lblNomeUtente.setText("Prof. " + p.getNome() + " " + p.getCognome());
            lblEmailUtente.setText(p.getEmail());

            // Stile card professore
            boxInfoUtente.getStyleClass().removeAll("card-studente");
            if (!boxInfoUtente.getStyleClass().contains("card-professore"))
                boxInfoUtente.getStyleClass().add("card-professore");

            nascondiSezione(boxNavStudente);
            mostraSezione(boxNavProfessore);
        }
    }

    private void precompilaCampoDestinatario() {
        String emailProf = getEmailProfessore();
        if (emailProf != null) {
            txtEmailDestinatario.setText(emailProf);
            txtEmailDestinatario.setPromptText("Email professore");
        } else {
            txtEmailDestinatario.clear();
            txtEmailDestinatario.setPromptText("email@destinatario.it");
        }
    }

    // ── Inbox ─────────────────────────────────────────────────────────────────

    private void caricaInbox() {
        vboxMessaggi.getChildren().clear();

        List<MessageBean> messaggi = eseguiCaricaInbox();

        if (messaggi == null || messaggi.isEmpty()) {
            lblNessunMessaggio.setVisible(true);
            lblNessunMessaggio.setManaged(true);
            vboxMessaggi.getChildren().add(lblNessunMessaggio);
            return;
        }

        lblNessunMessaggio.setVisible(false);
        lblNessunMessaggio.setManaged(false);

        for (MessageBean m : messaggi) {
            vboxMessaggi.getChildren().add(creaCardMessaggio(m));
        }
    }

    /**
     * Costruisce una card visuale per un singolo messaggio ricevuto.
     * Layout: riga superiore (mittente + data) + riga inferiore (testo).
     */
    private VBox creaCardMessaggio(MessageBean m) {
        VBox card = new VBox(6);
        card.setStyle(
                "-fx-background-color:#ffffff;" +
                        "-fx-background-radius:8;" +
                        "-fx-border-color:#e8eaf6;" +
                        "-fx-border-radius:8;" +
                        "-fx-border-width:1;" +
                        "-fx-padding:12 16 12 16;"
        );

        // Riga superiore: mittente + data
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblMittente = new Label("✉  " + m.getNominativoMittente());
        lblMittente.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1a237e;");

        Label lblEmail = new Label("(" + m.getEmailMittente() + ")");
        lblEmail.setStyle("-fx-font-size:11px; -fx-text-fill:#757575;");

        VBox spacer = new VBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String dataStr = (m.getTimestamp() != null) ? m.getTimestamp().format(FMT) : "—";
        Label lblData = new Label(dataStr);
        lblData.setStyle("-fx-font-size:11px; -fx-text-fill:#9e9e9e;");

        header.getChildren().addAll(lblMittente, lblEmail, spacer, lblData);

        // Riga del testo
        Label lblTesto = new Label(m.getTesto());
        lblTesto.setStyle("-fx-font-size:13px; -fx-text-fill:#424242;");
        lblTesto.setWrapText(true);

        card.getChildren().addAll(header, lblTesto);
        return card;
    }

    // ── Handler FXML ─────────────────────────────────────────────────────────

    @FXML
    private void clickInviaMessaggio() {
        nascondiMessaggio();

        String emailDest = txtEmailDestinatario.getText().trim();
        String testo     = txtTestoMessaggio.getText().trim();

        if (emailDest.isEmpty()) {
            mostraErrore("Inserisci l'email del destinatario.");
            return;
        }
        if (testo.isEmpty()) {
            mostraErrore("Il testo del messaggio non può essere vuoto.");
            return;
        }

        MessageBean inviato = eseguiInviaMessaggio(emailDest, testo);
        if (inviato != null) {
            txtTestoMessaggio.clear();
            // Mantieni il campo destinatario (utile per conversazioni multiple)
            caricaInbox(); // aggiorna la inbox
        }
    }

    @FXML
    private void clickInboxRefresh() {
        nascondiMessaggio();
        caricaInbox();
    }

    // ── Navigazione sidebar ───────────────────────────────────────────────────

    @FXML private void clickDashboard()      { tornaDashboard(); }
    @FXML private void clickMercato()        { vaiAlMercato(); }
    @FXML private void clickPortafoglio()    { vaiAlPortafoglio(); }
    @FXML private void clickStorico()        { vaiAlloStorico(); }
    @FXML private void clickGestioneClasse() { vaiAGestioneClasse(); }
    @FXML private void clickLogout()         { eseguiLogout(); }

    // ── Feedback ─────────────────────────────────────────────────────────────

    @Override
    protected void mostraSuccesso(String msg) {
        lblMessaggio.setText("✅  " + msg);
        lblMessaggio.setStyle(
                "-fx-font-size:13px; -fx-padding:8 12;" +
                        "-fx-background-radius:6;" +
                        "-fx-background-color:#e8f5e9;" +
                        "-fx-text-fill:#2e7d32;"
        );
        lblMessaggio.setVisible(true);
        lblMessaggio.setManaged(true);
    }

    @Override
    protected void mostraErrore(String msg) {
        lblMessaggio.setText("⚠  " + msg);
        lblMessaggio.setStyle(
                "-fx-font-size:13px; -fx-padding:8 12;" +
                        "-fx-background-radius:6;" +
                        "-fx-background-color:#ffebee;" +
                        "-fx-text-fill:#c62828;"
        );
        lblMessaggio.setVisible(true);
        lblMessaggio.setManaged(true);
    }

    private void nascondiMessaggio() {
        lblMessaggio.setText("");
        lblMessaggio.setVisible(false);
        lblMessaggio.setManaged(false);
    }

    // ── Utility visibilità ────────────────────────────────────────────────────

    private void mostraSezione(VBox box)  {
        box.setVisible(true);
        box.setManaged(true);
    }

    private void nascondiSezione(VBox box) {
        box.setVisible(false);
        box.setManaged(false);
    }
}
