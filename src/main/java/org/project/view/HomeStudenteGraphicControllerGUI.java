package org.project.view;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.project.view.bean.PortafoglioBean;
import org.project.view.bean.StudenteBean;
import org.project.view.bean.TransactionBean;
import org.project.view.bean.WalletPositionBean;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


public class HomeStudenteGraphicControllerGUI extends HomeStudenteGraphicController {

    // ── Costanti di Stile ─────────────────────────────────────────────────────
    private static final String STYLE_TESTO_NORMALE    = "testo-normale";
    private static final String STYLE_TESTO_SECONDARIO = "testo-secondario";
    private static final String STYLE_SIMBOLO_STOCK    = "simbolo-stock";
    private static final String STYLE_CARD_TRANSAZIONE = "card-transazione";

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private Label lblNomeUtente;
    @FXML private Label lblEmailUtente;
    @FXML private Label lblNomeClasse;

    // ── Header ────────────────────────────────────────────────────────────────
    @FXML private Label lblDataOra;

    // ── Carte riepilogo ───────────────────────────────────────────────────────
    @FXML private Label lblSaldoDisponibile;
    @FXML private Label lblValoreTotale;
    @FXML private Label lblVariazioneTotale;
    @FXML private Label lblNumPosizioni;
    @FXML private Label lblClasseCard;
    @FXML private Label lblBudgetClasse;

    // ── Posizioni ─────────────────────────────────────────────────────────────
    @FXML private VBox  vboxPosizioni;
    @FXML private Label lblNessunaPosizione;

    // ── Ultime transazioni ────────────────────────────────────────────────────
    @FXML private VBox  vboxUltimeTransazioni;
    @FXML private Label lblNessunaTransazione;

    // ── Messaggio sistema ─────────────────────────────────────────────────────
    @FXML private Label lblMessaggio;

    // ── View root ─────────────────────────────────────────────────────────────
    private Parent view;

    private static final NumberFormat VALUTA =
            NumberFormat.getCurrencyInstance(Locale.ITALY);
    private static final DateTimeFormatter FMT_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

        StudenteBean studente = getStudenteLoggato();
        if (studente == null) {
            mostraMessaggio("Sessione non valida. Effettua nuovamente il login.");
            return;
        }

        // Sidebar
        lblNomeUtente.setText("Ciao, " + studente.getNome() + "!");
        lblEmailUtente.setText(studente.getEmail());

        // Dati classe (se disponibili nel Context)
        if (navigator.getSessione() != null
                && navigator.getSessione().getStudente() != null
                && navigator.getSessione().getStudente().getNomeClasse() != null) {
            String nomeClasse = navigator.getSessione().getStudente().getNomeClasse();
            lblNomeClasse.setText("Classe: " + nomeClasse);
            lblClasseCard.setText(nomeClasse);
        } else {
            lblNomeClasse.setText("Classe: —");
            lblClasseCard.setText("—");
        }

        // Portafoglio
        PortafoglioBean portafoglio = getPortafoglio();
        if (portafoglio != null) {
            aggiornaUIPortafoglio(portafoglio);
        } else {
            lblSaldoDisponibile.setText("—");
            lblValoreTotale.setText("—");
            lblNumPosizioni.setText("0");
            lblBudgetClasse.setText("");
        }
    }

    // ── aggiornaUIPortafoglio ─────────────────────────────────────────────────

    @Override
    protected void aggiornaUIPortafoglio(PortafoglioBean portafoglio) {
        // Saldo
        lblSaldoDisponibile.setText(VALUTA.format(portafoglio.getSaldoDisponibile()));

        // Totale
        double totale = portafoglio.getValoreTotalePortafoglio();
        lblValoreTotale.setText(VALUTA.format(totale));

        // Posizioni
        List<WalletPositionBean> posizioni = portafoglio.getPosizioni();
        int numPos = (posizioni != null) ? posizioni.size() : 0;
        lblNumPosizioni.setText(String.valueOf(numPos));

        if (numPos == 0) {
            lblNessunaPosizione.setVisible(true);
            lblNessunaPosizione.setManaged(true);
        } else {
            lblNessunaPosizione.setVisible(false);
            lblNessunaPosizione.setManaged(false);
            // Rimuove eventuali righe precedenti (tranne il placeholder)
            vboxPosizioni.getChildren().clear();
            for (WalletPositionBean pos : posizioni) {
                vboxPosizioni.getChildren().add(creaRigaPosizione(pos));
            }
        }

        // Ultime 5 transazioni
        List<TransactionBean> transazioni = portafoglio.getTransazioni();
        if (transazioni == null || transazioni.isEmpty()) {
            lblNessunaTransazione.setVisible(true);
            lblNessunaTransazione.setManaged(true);
        } else {
            lblNessunaTransazione.setVisible(false);
            lblNessunaTransazione.setManaged(false);
            vboxUltimeTransazioni.getChildren().clear();
            int limite = Math.min(5, transazioni.size());
            for (int i = 0; i < limite; i++) {
                vboxUltimeTransazioni.getChildren().add(creaRigaTransazione(transazioni.get(i)));
            }
        }
    }

    // ── Costruzione righe dinamiche ───────────────────────────────────────────

    private HBox creaRigaPosizione(WalletPositionBean pos) {
        HBox riga = new HBox();
        riga.getStyleClass().add(STYLE_CARD_TRANSAZIONE);

        Label lblSimbolo = new Label(pos.getStock().getSimbolo());
        lblSimbolo.getStyleClass().add(STYLE_SIMBOLO_STOCK);
        lblSimbolo.setPrefWidth(80);

        Label lblNome = new Label(pos.getStock().getNomeAzienda());
        lblNome.getStyleClass().add(STYLE_TESTO_SECONDARIO);
        lblNome.setPrefWidth(160);

        Label lblQta = new Label(String.format("%.2f", pos.getQuantita()));
        lblQta.getStyleClass().add(STYLE_TESTO_NORMALE);
        lblQta.setPrefWidth(70);

        Label lblPrezzoMedio = new Label(VALUTA.format(pos.getPrezzoMedioAcquisto()));
        lblPrezzoMedio.getStyleClass().add(STYLE_TESTO_NORMALE);
        lblPrezzoMedio.setPrefWidth(100);

        Label lblValore = new Label(VALUTA.format(pos.getValoreAttuale()));
        lblValore.getStyleClass().add(STYLE_TESTO_NORMALE);
        lblValore.setPrefWidth(110);

        double pl = pos.getProfittoPerdita();
        Label lblPL = new Label(String.format("%+.2f €", pl));
        lblPL.getStyleClass().add(pl >= 0 ? "tag-positivo" : "tag-negativo");
        lblPL.setPrefWidth(100);

        riga.getChildren().addAll(lblSimbolo, lblNome, lblQta, lblPrezzoMedio, lblValore, lblPL);
        return riga;
    }

    private HBox creaRigaTransazione(TransactionBean tx) {
        HBox riga = new HBox(12);
        riga.getStyleClass().add(STYLE_CARD_TRANSAZIONE);

        String tipoIcon = (tx.getTipo() != null && tx.getTipo().name().equals("BUY")) ? "🟢" : "🔴";
        Label lblTipo = new Label(tipoIcon + " " + tx.getStock().getSimbolo());
        lblTipo.getStyleClass().add(STYLE_SIMBOLO_STOCK);
        lblTipo.setPrefWidth(100);

        Label lblImporto = new Label(VALUTA.format(tx.getImportoTotale()));
        lblImporto.getStyleClass().add(STYLE_TESTO_NORMALE);
        lblImporto.setPrefWidth(120);

        String statoStyle = "tag-done";
        if (tx.getStato() != null) {
            String s = tx.getStato().name();
            if (s.equals("PENDING"))   statoStyle = "tag-pending";
            else if (s.equals("FAILED")) statoStyle = "tag-negativo";
        }
        Label lblStato = new Label(tx.getStato() != null ? tx.getStato().name() : "—");
        lblStato.getStyleClass().add(statoStyle);

        VBox spacer = new VBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        String dataStr = (tx.getQuando() != null) ?
                tx.getQuando().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "—";
        Label lblData = new Label(dataStr);
        lblData.getStyleClass().add(STYLE_TESTO_SECONDARIO);

        riga.getChildren().addAll(lblTipo, lblImporto, lblStato, spacer, lblData);
        return riga;
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
    private void clickPortafoglio() {
        vaiAlPortafoglio();
    }

    @FXML
    private void clickStorico() {
        vaiAlloStorico();
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