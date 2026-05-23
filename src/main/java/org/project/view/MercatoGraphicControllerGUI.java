package org.project.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.project.ing.observer.StockObserver;
import org.project.ing.service.StockService;
import org.project.model.Stock;
import org.project.view.bean.PortafoglioBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StockBean;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementazione GUI (JavaFX) della schermata Mercato.
 *
 * Si registra come StockObserver su ogni Stock visualizzato:
 * quando YahooFinanceAdapter aggiorna il prezzo, Stock.aggiornaPrezzo()
 * triggera notificaObserver() → aggiornamento() → Platform.runLater()
 * aggiorna i label senza toccare il thread JavaFX dall'esterno.
 */
public class MercatoGraphicControllerGUI extends MercatoGraphicController
        implements StockObserver {

    // ── FXML Sidebar ──────────────────────────────────────────────────────────
    @FXML private Label   lblRuoloSidebar;
    @FXML private VBox    boxInfoUtente;
    @FXML private Label   lblNomeUtente;
    @FXML private Label   lblSaldoSidebar;
    @FXML private ComboBox<String> cmbSettore;

    // ── FXML Header ───────────────────────────────────────────────────────────
    @FXML private Label   lblSottotitoloMercato;
    @FXML private TextField txtRicerca;

    // ── FXML Stato / feedback ─────────────────────────────────────────────────
    @FXML private Label   lblMessaggio;
    @FXML private HBox    boxCaricamento;

    // ── FXML Pannello dettaglio ───────────────────────────────────────────────
    @FXML private VBox    panelDettaglio;
    @FXML private Label   lblDetSimbolo;
    @FXML private Label   lblDetNome;
    @FXML private Label   lblDetSettore;
    @FXML private Label   lblDetPrezzo;
    @FXML private Label   lblDetVariazione;
    @FXML private Label   lblDetVarSettimanale;
    @FXML private Label   lblDetMarketCap;
    @FXML private Label   lblDetVolume;
    @FXML private Label   lblDetAggiornamento;
    @FXML private VBox    boxAzioneAcquisto;
    @FXML private Label   lblSaldoCheck;
    @FXML private Button  btnCompra;
    @FXML private VBox    boxSolaLettura;

    // ── FXML Griglia stock ────────────────────────────────────────────────────
    @FXML private FlowPane flowStocks;
    @FXML private Label   lblTitoloGriglia;
    @FXML private Label   lblContatore;
    @FXML private Label   lblNessunoStock;

    // ── Stato interno ─────────────────────────────────────────────────────────
    private Parent view;
    private String simboloCorrente;
    private ContextMenu autoCompleteMenu = new ContextMenu();
    private String filtroSettore = null;
    private Boolean filtroTrend  = null;

    /** Mappa simbolo → Label prezzo nella griglia, per aggiornamenti live */
    private final Map<String, Label> prezziInGriglia = new HashMap<>();

    private static final NumberFormat VALUTA =
            NumberFormat.getCurrencyInstance(Locale.ITALY);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ── Setter / Getter view ──────────────────────────────────────────────────

    public void setView(Parent view)    { this.view = view; }
    public Parent getView()             { return view; }
    public void setGuiNavigator(NavigatorGUI nav) { super.setNavigator(nav); }

    // ── start() ───────────────────────────────────────────────────────────────

    @Override
    public void start() {
        nascondiMessaggio();
        nascondiDettaglio();
        filtroSettore = null;
        filtroTrend   = null;

        SessioneBean sessione = navigator.getSessione();
        if (isStudente) {
            String nome = (sessione != null && sessione.getStudente() != null)
                    ? sessione.getStudente().getNome() : "—";
            lblNomeUtente.setText(nome);
            lblRuoloSidebar.setText("Mercato · Studente");
            boxInfoUtente.getStyleClass().removeAll("card-professore");
            boxInfoUtente.getStyleClass().add("card-studente");
            lblSottotitoloMercato.setText("Esplora il mercato e investi nei tuoi titoli preferiti");

            PortafoglioBean pf = navigator.getPortafoglio();
            if (pf != null) {
                lblSaldoSidebar.setText("Saldo: " + VALUTA.format(pf.getSaldoDisponibile()));
            } else {
                lblSaldoSidebar.setText("");
            }
        } else {
            String nome = (sessione != null && sessione.getProfessore() != null)
                    ? "Prof. " + sessione.getProfessore().getNome() : "—";
            lblNomeUtente.setText(nome);
            lblRuoloSidebar.setText("Mercato · Professore");
            boxInfoUtente.getStyleClass().removeAll("card-studente");
            boxInfoUtente.getStyleClass().add("card-professore");
            lblSaldoSidebar.setText("Visualizzazione in sola lettura");
            lblSottotitoloMercato.setText("Monitora gli andamenti del mercato");
        }

        popolaFiltroSettori();
        aggiornaGriglia(navigator.getListaStock());

        // Avvia il polling automatico (ogni 30s) — triggera observer → aggiornamento()
        StockService.getInstance().avviaAggiornamentoAutomatico();
    }

    // ── StockObserver ─────────────────────────────────────────────────────────

    /**
     * Chiamato dal thread "stock-updater" ogni volta che un prezzo cambia.
     * Aggiorna il pannello dettaglio (se lo stock è quello visualizzato)
     * e la card nella griglia, tutto sul thread JavaFX tramite Platform.runLater().
     */
    @Override
    public void aggiornamento(Stock stock) {
        Platform.runLater(() -> {
            // 1. Aggiorna label prezzo nella griglia
            Label lblGriglia = prezziInGriglia.get(stock.simbolo());
            if (lblGriglia != null) {
                lblGriglia.setText(String.format("$ %.2f", stock.prezzoAttuale()));
            }

            // 2. Se questo stock è aperto nel pannello dettaglio, aggiorna tutto
            if (stock.simbolo().equals(simboloCorrente)) {
                lblDetPrezzo.setText(String.format("$ %.2f", stock.prezzoAttuale()));
                lblDetVariazione.setText(String.format("%+.2f%%", stock.variazioneGiornaliera()));
                lblDetAggiornamento.setText("Aggiornato: " + LocalDateTime.now().format(FMT));

                // Ricolora la variazione
                lblDetVariazione.getStyleClass().removeAll("tag-positivo", "tag-negativo");
                lblDetVariazione.getStyleClass().add(
                        stock.variazioneGiornaliera() >= 0 ? "tag-positivo" : "tag-negativo");
            }
        });
    }

    // ── Griglia stock ─────────────────────────────────────────────────────────

    private void aggiornaGriglia(List<StockBean> lista) {
        popolaFiltroSettori();
        flowStocks.getChildren().clear();
        prezziInGriglia.clear();

        List<StockBean> filtrata = filtra(lista);

        if (filtrata == null || filtrata.isEmpty()) {
            lblNessunoStock.setVisible(true);
            lblNessunoStock.setManaged(true);
            lblContatore.setText("");
            return;
        }

        lblNessunoStock.setVisible(false);
        lblNessunoStock.setManaged(false);
        lblContatore.setText("(" + filtrata.size() + " titoli)");

        for (StockBean s : filtrata) {
            flowStocks.getChildren().add(creaCardStock(s));
            // Registra questo controller come observer dello stock nel StockService
            Stock stockModel = StockService.getInstance().trovaStock(s.getSimbolo());
            if (stockModel != null) {
                stockModel.aggiungiObserver(this);
            }
        }
    }

    private VBox creaCardStock(StockBean s) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.getStyleClass().add("card-stock");
        card.setStyle(card.getStyle() + "; -fx-cursor:hand;");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label lblSim = new Label(s.getSimbolo());
        lblSim.getStyleClass().add("simbolo-stock");

        double var = s.getVariazioneGiornaliera();
        Label lblVar = new Label(String.format("%+.2f%%", var));
        lblVar.getStyleClass().add(var >= 0 ? "tag-positivo" : "tag-negativo");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(lblSim, spacer, lblVar);

        Label lblNome = new Label(tronca(s.getNomeAzienda(), 28));
        lblNome.getStyleClass().add("testo-secondario");

        // Questa label viene salvata nella mappa per aggiornamenti live
        Label lblPrezzo = new Label(String.format("$ %.2f", s.getPrezzoAttuale()));
        lblPrezzo.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#1a1a2e;");
        prezziInGriglia.put(s.getSimbolo(), lblPrezzo);

        Label lblSett = new Label(s.getSettore());
        lblSett.getStyleClass().add("testo-secondario");
        lblSett.setStyle(lblSett.getStyle() + "; -fx-font-size:10px;");

        card.getChildren().addAll(top, lblNome, lblPrezzo, lblSett);

        card.setOnMouseClicked(e -> {
            simboloCorrente = s.getSimbolo();
            mostraDettaglioStock(s);
            txtRicerca.setText(s.getSimbolo());
        });

        return card;
    }

    // ── Pannello dettaglio ────────────────────────────────────────────────────

    @Override
    protected void mostraDettaglioStock(StockBean s) {
        simboloCorrente = s.getSimbolo();

        lblDetSimbolo.setText(s.getSimbolo());
        lblDetNome.setText(s.getNomeAzienda());
        lblDetSettore.setText(s.getSettore());
        lblDetPrezzo.setText(String.format("$ %.2f", s.getPrezzoAttuale()));
        lblDetAggiornamento.setText("Aggiornato: " + LocalDateTime.now().format(FMT));

        double varG = s.getVariazioneGiornaliera();
        lblDetVariazione.setText(String.format("%+.2f%%", varG));
        lblDetVariazione.getStyleClass().removeAll("tag-positivo", "tag-negativo");
        lblDetVariazione.getStyleClass().add(varG >= 0 ? "tag-positivo" : "tag-negativo");

        double varS = s.getVariazioneSettimanale();
        lblDetVarSettimanale.setText(String.format("%+.2f%%", varS));
        lblDetVarSettimanale.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:"
                + (varS >= 0 ? "#2e7d32" : "#d32f2f") + ";");

        lblDetMarketCap.setText(formatMarketCap(s.getMarketCap()));
        lblDetVolume.setText(s.getVolumeSettimanale() > 0
                ? String.format("%.0f", s.getVolumeSettimanale()) : "—");

        if (isStudente) {
            boxAzioneAcquisto.setVisible(true);
            boxAzioneAcquisto.setManaged(true);
            boxSolaLettura.setVisible(false);
            boxSolaLettura.setManaged(false);

            PortafoglioBean pf = navigator.getPortafoglio();
            if (pf != null) {
                lblSaldoCheck.setText("Saldo disponibile: " + VALUTA.format(pf.getSaldoDisponibile()));
                btnCompra.setDisable(pf.getSaldoDisponibile() < s.getPrezzoAttuale());
                btnCompra.setText(pf.getSaldoDisponibile() < s.getPrezzoAttuale()
                        ? "💰  Saldo insufficiente" : "💰  Compra ora");
            }
        } else {
            boxAzioneAcquisto.setVisible(false);
            boxAzioneAcquisto.setManaged(false);
            boxSolaLettura.setVisible(true);
            boxSolaLettura.setManaged(true);
        }

        // Registra come observer per aggiornamenti live sul pannello dettaglio
        Stock stockModel = StockService.getInstance().trovaStock(s.getSimbolo());
        if (stockModel != null) stockModel.aggiungiObserver(this);

        panelDettaglio.setVisible(true);
        panelDettaglio.setManaged(true);
        aggiornaGriglia(navigator.getListaStock());
    }

    private void nascondiDettaglio() {
        panelDettaglio.setVisible(false);
        panelDettaglio.setManaged(false);
    }


    // ── Filtri ────────────────────────────────────────────────────────────────

    private void popolaFiltroSettori() {
        List<StockBean> lista = navigator.getListaStock();
        cmbSettore.getItems().clear();
        if (lista == null) return;
        lista.stream()
                .map(StockBean::getSettore)
                .distinct()
                .sorted()
                .forEach(cmbSettore.getItems()::add);
    }

    private List<StockBean> filtra(List<StockBean> lista) {
        if (lista == null) return new ArrayList<>();
        return lista.stream()
                .filter(s -> filtroSettore == null || s.getSettore().equalsIgnoreCase(filtroSettore))
                .filter(s -> filtroTrend == null
                        || (filtroTrend && s.getVariazioneGiornaliera() >= 0)
                        || (!filtroTrend && s.getVariazioneGiornaliera() < 0))
                .collect(Collectors.toList());
    }

    // ── Handler FXML ──────────────────────────────────────────────────────────

    @FXML private void clickCerca() {
        nascondiMessaggio();
        eseguiRicerca(txtRicerca.getText());
    }

    @FXML private void clickAggiorna() {
        aggiornaGriglia(navigator.getListaStock());
        nascondiMessaggio();
    }

    @FXML private void clickQuickSearch(javafx.event.ActionEvent e) {
        String sym = ((Button) e.getSource()).getText();
        txtRicerca.setText(sym);
        eseguiRicerca(sym);
    }

    @FXML private void clickFiltroSettore() {
        filtroSettore = cmbSettore.getValue();
        aggiornaGriglia(navigator.getListaStock());
    }

    @FXML private void clickFiltroPositivi() {
        filtroTrend = true;
        aggiornaGriglia(navigator.getListaStock());
    }

    @FXML private void clickFiltroNegativi() {
        filtroTrend = false;
        aggiornaGriglia(navigator.getListaStock());
    }

    @FXML private void clickResetFiltri() {
        filtroSettore = null;
        filtroTrend   = null;
        cmbSettore.setValue(null);
        aggiornaGriglia(navigator.getListaStock());
    }

    @FXML private void clickCompra() {
        if (simboloCorrente == null) return;
        eseguiAvviaOrdine(simboloCorrente);
    }

    @FXML private void clickDashboard() { tornaDashboard(); }

    @FXML private void clickLogout() {
        StockService.getInstance().fermaAggiornamentoAutomatico();
        eseguiLogout();
    }

    // ── Feedback ─────────────────────────────────────────────────────────────

    @Override
    protected void mostraCaricamento(boolean visible) {
        Platform.runLater(() -> {
            boxCaricamento.setVisible(visible);
            boxCaricamento.setManaged(visible);
        });
    }

    @Override
    protected void mostraErrore(String msg) {
        lblMessaggio.setText(msg);
        lblMessaggio.getStyleClass().removeAll("label-successo");
        lblMessaggio.getStyleClass().add("label-errore");
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
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String tronca(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private String formatMarketCap(double cap) {
        if (cap <= 0) return "—";
        if (cap >= 1_000_000_000_000.0) return String.format("$ %.2f T", cap / 1_000_000_000_000.0);
        if (cap >= 1_000_000_000.0)     return String.format("$ %.2f B", cap / 1_000_000_000.0);
        if (cap >= 1_000_000.0)         return String.format("$ %.2f M", cap / 1_000_000.0);
        return VALUTA.format(cap);
    }
}
