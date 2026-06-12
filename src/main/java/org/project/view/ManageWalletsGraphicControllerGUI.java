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
import org.project.view.bean.*;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementazione GUI (JavaFX) di ManageWalletsGraphicController.
 *
 * Gestisce via panel show/hide tutti i sotto-flussi del caso d'uso:
 *  – Mercato (panelDettaglio + griglia stock)
 *  – Conferma ordine (panelConfermaOrdine)
 *  – Portafoglio proprio / esterno (panelPortafoglio)
 *  – Storico transazioni (panelStorico)
 *
 * Tutti i pannelli centrali partono hidden/unmanaged e vengono attivati
 * dal controller nei metodi start*().
 *
 * FXML: ManageWallets.fxml (fx:controller = org.project.view.ManageWalletsGraphicControllerGUI)
 */
public class ManageWalletsGraphicControllerGUI
        extends ManageWalletsGraphicController
        implements StockObserver {

    // ── FXML Sidebar ──────────────────────────────────────────────────────────
    @FXML private Label            lblRuoloSidebar;
    @FXML private VBox             boxInfoUtente;
    @FXML private Label            lblNomeUtente;
    @FXML private Label            lblSaldoSidebar;
    @FXML private VBox             boxNavWallet;   // Portafoglio + Storico nav (solo studente)

    // ── FXML Header ───────────────────────────────────────────────────────────
    @FXML private Label            lblSottotitoloMercato;
    @FXML private TextField        txtRicerca;

    // ── FXML Feedback ─────────────────────────────────────────────────────────
    @FXML private Label            lblMessaggio;
    @FXML private HBox             boxCaricamento;

    // ── FXML Pannello dettaglio stock ─────────────────────────────────────────
    @FXML private VBox             panelDettaglio;
    @FXML private Label            lblDetSimbolo;
    @FXML private Label            lblDetNome;
    @FXML private Label            lblDetSettore;
    @FXML private Label            lblDetPrezzo;
    @FXML private Label            lblDetVariazione;
    @FXML private Label            lblDetVarSettimanale;
    @FXML private Label            lblDetMarketCap;
    @FXML private Label            lblDetVolume;
    @FXML private Label            lblDetAggiornamento;
    @FXML private VBox             boxAzioneAcquisto;
    @FXML private Label            lblSaldoCheck;
    @FXML private Button           btnCompra;
    @FXML private VBox             boxSolaLettura;

    // ── FXML Pannello conferma ordine ─────────────────────────────────────────
    @FXML private VBox             panelConfermaOrdine;
    @FXML private Label            lblOrdineStock;
    @FXML private Label            lblOrdinePrezzo;
    @FXML private Label            lblOrdineSaldo;
    @FXML private TextField        txtQuantita;
    @FXML private Label            lblOrdineTotale;
    @FXML private Button           btnConfermaOrdine;

    // ── FXML Pannello portafoglio ─────────────────────────────────────────────
    @FXML private VBox             panelPortafoglio;
    @FXML private Label            lblTitoloPortafoglio;
    @FXML private Label            lblSaldoPortafoglio;
    @FXML private Label            lblTotalePortafoglio;
    @FXML private VBox             vboxPosizioni;       // righe posizioni popolate dinamicamente
    @FXML private Button           btnPortafoglioStorico;
    @FXML private Button           btnPortafoglioTorna;  // "Torna al Mercato" / "Torna alla Dashboard"

    // ── FXML Pannello storico transazioni ─────────────────────────────────────
    @FXML private VBox             panelStorico;
    @FXML private Label            lblTitoloStorico;
    @FXML private VBox             vboxStorico;         // righe transazioni popolate dinamicamente

    // ── FXML Griglia mercato ──────────────────────────────────────────────────
    @FXML private FlowPane         flowStocks;
    @FXML private Label            lblTitoloGriglia;
    @FXML private Label            lblContatore;
    @FXML private Label            lblNessunoStock;

    // ── Stato locale ──────────────────────────────────────────────────────────
    private Parent  view;
    private String  simboloCorrente;
    private boolean portafoglioEsterno = false; // true = sto guardando wallet altrui

    private final Map<String, Label> prezziInGriglia = new HashMap<>();

    private static final NumberFormat  VALUTA = NumberFormat.getCurrencyInstance(Locale.ITALY);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ── Setter / Getter ───────────────────────────────────────────────────────

    public void setView(Parent view)              { this.view = view; }
    public Parent getView()                       { return view; }
    public void setGuiNavigator(NavigatorGUI nav) { super.setNavigator(nav); }

    // ── start() — schermata mercato ───────────────────────────────────────────

    @Override
    public void start() {
        nascondiTuttiIPannelli();
        nascondiMessaggio();
        portafoglioEsterno = false;

        SessioneBean sessione = navigator.getSessione();
        if (isStudente) {
            String nome = (sessione != null && sessione.getStudente() != null)
                    ? sessione.getStudente().getNome() : "—";
            lblNomeUtente.setText(nome);
            lblRuoloSidebar.setText("Manage Wallets · Studente");
            boxInfoUtente.getStyleClass().removeAll("card-professore");
            boxInfoUtente.getStyleClass().add("card-studente");
            lblSottotitoloMercato.setText("Esplora il mercato e investi nei tuoi titoli preferiti");

            PortafoglioBean pf = navigator.getPortafoglio();
            lblSaldoSidebar.setText(pf != null
                    ? "Saldo: " + VALUTA.format(pf.getSaldoDisponibile()) : "");

            // Mostra navigazione wallet solo per lo studente
            if (boxNavWallet != null) {
                boxNavWallet.setVisible(true);
                boxNavWallet.setManaged(true);
            }
        } else {
            String nome = (sessione != null && sessione.getProfessore() != null)
                    ? "Prof. " + sessione.getProfessore().getNome() : "—";
            lblNomeUtente.setText(nome);
            lblRuoloSidebar.setText("Manage Wallets · Professore");
            boxInfoUtente.getStyleClass().removeAll("card-studente");
            boxInfoUtente.getStyleClass().add("card-professore");
            lblSaldoSidebar.setText("Visualizzazione in sola lettura");
            lblSottotitoloMercato.setText("Monitora gli andamenti del mercato");

            if (boxNavWallet != null) {
                boxNavWallet.setVisible(false);
                boxNavWallet.setManaged(false);
            }
        }

        aggiornaGriglia(navigator.getListaStock());
        StockService.getInstance().avviaAggiornamentoAutomatico();
    }

    // ── startConfermaOrdine() ─────────────────────────────────────────────────

    @Override
    public void startConfermaOrdine() {
        TransactionBean t = navigator.getTransazionePending();
        if (t == null) { mostraErrore("Nessun ordine da confermare."); return; }

        nascondiTuttiIPannelli();
        nascondiMessaggio();

        lblOrdineStock.setText(t.getStock().getSimbolo() + " — " + t.getStock().getNomeAzienda());
        lblOrdinePrezzo.setText(String.format("Prezzo unitario: $ %.2f", t.getPrezzoAlMomento()));

        PortafoglioBean pf = navigator.getPortafoglio();
        lblOrdineSaldo.setText(pf != null
                ? "Saldo disponibile: " + VALUTA.format(pf.getSaldoDisponibile()) : "");

        txtQuantita.clear();
        lblOrdineTotale.setText("Totale: —");
        if (btnConfermaOrdine != null) btnConfermaOrdine.setDisable(true);

        txtQuantita.textProperty().addListener((obs, oldV, newV) -> {
            try {
                double q = Double.parseDouble(newV.replace(",", "."));
                if (q > 0) {
                    double totale = q * t.getPrezzoAlMomento();
                    lblOrdineTotale.setText("Totale: " + VALUTA.format(totale));
                    boolean saldoOk = pf == null || pf.getSaldoDisponibile() >= totale;
                    if (btnConfermaOrdine != null) btnConfermaOrdine.setDisable(!saldoOk);
                    lblOrdineTotale.setStyle(saldoOk
                            ? "-fx-text-fill:#2e7d32; -fx-font-weight:bold;"
                            : "-fx-text-fill:#d32f2f; -fx-font-weight:bold;");
                } else {
                    lblOrdineTotale.setText("Totale: —");
                    if (btnConfermaOrdine != null) btnConfermaOrdine.setDisable(true);
                }
            } catch (NumberFormatException e) {
                lblOrdineTotale.setText("Totale: —");
                if (btnConfermaOrdine != null) btnConfermaOrdine.setDisable(true);
            }
        });

        panelConfermaOrdine.setVisible(true);
        panelConfermaOrdine.setManaged(true);
    }

    // ── startPortafoglio() ────────────────────────────────────────────────────

    @Override
    public void startPortafoglio() {
        nascondiTuttiIPannelli();
        nascondiMessaggio();
        portafoglioEsterno = false;
        eseguiCaricaPortafoglio(null);
    }

    // ── startStorico() ────────────────────────────────────────────────────────

    @Override
    public void startStorico() {
        nascondiTuttiIPannelli();
        nascondiMessaggio();
        eseguiCaricaStorico(null);
    }

    // ── startWalletEsterno() ──────────────────────────────────────────────────

    @Override
    public void startWalletEsterno(StudenteBean studenteTarget) {
        nascondiTuttiIPannelli();
        nascondiMessaggio();
        portafoglioEsterno = true;
        eseguiCaricaPortafoglio(studenteTarget.getEmail());
    }

    // ── mostraPortafoglio() ───────────────────────────────────────────────────

    @Override
    protected void mostraPortafoglio(PortafoglioBean pf, boolean isProprietario) {
        Platform.runLater(() -> {
            lblTitoloPortafoglio.setText(isProprietario
                    ? "📊 Il Tuo Portafoglio"
                    : "📊 Portafoglio (sola lettura)");

            if (pf == null) {
                lblSaldoPortafoglio.setText("Nessun dato disponibile.");
                lblTotalePortafoglio.setText("");
                vboxPosizioni.getChildren().clear();
            } else {
                lblSaldoPortafoglio.setText(
                        "💵 Saldo disponibile: " + VALUTA.format(pf.getSaldoDisponibile()));
                lblTotalePortafoglio.setText(
                        "📈 Valore totale wallet: " + VALUTA.format(pf.getValoreTotalePortafoglio()));

                vboxPosizioni.getChildren().clear();

                if (pf.getPosizioni() == null || pf.getPosizioni().isEmpty()) {
                    Label nessuna = new Label("Nessuna posizione aperta.");
                    nessuna.getStyleClass().add("testo-secondario");
                    vboxPosizioni.getChildren().add(nessuna);
                } else {
                    // Intestazione colonne
                    HBox header = creaRigaIntestazione(
                            "Simbolo", "Azienda", "Quantità", "Pr.Medio", "Val.Att.", "P/L");
                    vboxPosizioni.getChildren().add(header);

                    for (WalletPositionBean p : pf.getPosizioni()) {
                        vboxPosizioni.getChildren().add(creaRigaPosizione(p));
                    }
                }
            }

            // Il pulsante "Storico" appare solo per il proprietario
            if (btnPortafoglioStorico != null) {
                btnPortafoglioStorico.setVisible(isProprietario);
                btnPortafoglioStorico.setManaged(isProprietario);
            }

            // Il pulsante "Torna al Mercato" appare solo per il proprietario;
            // il professore in sola lettura usa la freccia in sidebar — nessun bottone qui
            if (btnPortafoglioTorna != null) {
                btnPortafoglioTorna.setVisible(isProprietario);
                btnPortafoglioTorna.setManaged(isProprietario);
            }

            panelPortafoglio.setVisible(true);
            panelPortafoglio.setManaged(true);
        });
    }

    // ── mostraStorico() ───────────────────────────────────────────────────────

    @Override
    protected void mostraStorico(List<TransactionBean> storico, String emailTarget) {
        Platform.runLater(() -> {
            lblTitoloStorico.setText(emailTarget == null
                    ? "📋 Storico Transazioni"
                    : "📋 Storico Transazioni (sola lettura)");

            vboxStorico.getChildren().clear();

            if (storico == null || storico.isEmpty()) {
                Label nessuna = new Label("Nessuna transazione registrata.");
                nessuna.getStyleClass().add("testo-secondario");
                vboxStorico.getChildren().add(nessuna);
            } else {
                HBox header = creaRigaIntestazione("Data", "Simbolo", "Tipo", "Quantità", "Importo", "Stato");
                vboxStorico.getChildren().add(header);
                for (TransactionBean t : storico) {
                    vboxStorico.getChildren().add(creaRigaTransazione(t));
                }
            }

            panelStorico.setVisible(true);
            panelStorico.setManaged(true);
        });
    }

    // ── StockObserver ─────────────────────────────────────────────────────────

    @Override
    public void aggiornamento(Stock stock) {
        Platform.runLater(() -> {
            Label lblGriglia = prezziInGriglia.get(stock.simbolo());
            if (lblGriglia != null)
                lblGriglia.setText(String.format("$ %.2f", stock.prezzoAttuale()));

            if (stock.simbolo().equals(simboloCorrente)) {
                lblDetPrezzo.setText(String.format("$ %.2f", stock.prezzoAttuale()));
                lblDetVariazione.setText(String.format("%+.2f%%", stock.variazioneGiornaliera()));
                lblDetAggiornamento.setText("Aggiornato: " + LocalDateTime.now().format(FMT));
                lblDetVariazione.getStyleClass().removeAll("tag-positivo", "tag-negativo");
                lblDetVariazione.getStyleClass().add(
                        stock.variazioneGiornaliera() >= 0 ? "tag-positivo" : "tag-negativo");
            }
        });
    }

    // ── Griglia stock ─────────────────────────────────────────────────────────

    private void aggiornaGriglia(List<StockBean> lista) {
        flowStocks.getChildren().clear();
        prezziInGriglia.clear();

        if (lista == null || lista.isEmpty()) {
            lblNessunoStock.setVisible(true);
            lblNessunoStock.setManaged(true);
            lblContatore.setText("");
            return;
        }

        lblNessunoStock.setVisible(false);
        lblNessunoStock.setManaged(false);
        lblContatore.setText("(" + lista.size() + " titoli)");

        for (StockBean s : lista) {
            flowStocks.getChildren().add(creaCardStock(s));
            Stock sm = StockService.getInstance().trovaStock(s.getSimbolo());
            if (sm != null) sm.aggiungiObserver(this);
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
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(lblSim, spacer, lblVar);

        Label lblNome   = new Label(tronca(s.getNomeAzienda(), 28));
        lblNome.getStyleClass().add("testo-secondario");
        Label lblPrezzo = new Label(String.format("$ %.2f", s.getPrezzoAttuale()));
        lblPrezzo.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#1a1a2e;");
        prezziInGriglia.put(s.getSimbolo(), lblPrezzo);
        Label lblSett = new Label(s.getSettore());
        lblSett.getStyleClass().add("testo-secondario");
        lblSett.setStyle(lblSett.getStyle() + "; -fx-font-size:10px;");
        card.getChildren().addAll(top, lblNome, lblPrezzo, lblSett);

        card.setOnMouseClicked(e -> {
            nascondiConfermaOrdine();
            nascondiPortafoglio();
            nascondiStorico();
            simboloCorrente = s.getSimbolo();
            mostraDettaglioStock(s);
        });
        return card;
    }

    // ── Costruttori righe tabella ──────────────────────────────────────────────

    private HBox creaRigaIntestazione(String... colonne) {
        HBox row = new HBox(8);
        row.setStyle("-fx-padding:4 0 4 0; -fx-border-color:transparent transparent #cccccc transparent;");
        for (String col : colonne) {
            Label lbl = new Label(col);
            lbl.setStyle("-fx-font-weight:bold; -fx-font-size:11px; -fx-text-fill:#555555;");
            lbl.setPrefWidth(120);
            row.getChildren().add(lbl);
        }
        return row;
    }

    private HBox creaRigaPosizione(WalletPositionBean p) {
        HBox row = new HBox(8);
        row.setStyle("-fx-padding:6 0 6 0; -fx-border-color:transparent transparent #eeeeee transparent;");

        double pl = p.getProfittoPerdita();
        String plStr = (pl >= 0 ? "+" : "") + VALUTA.format(pl);
        String plColor = pl >= 0 ? "#2e7d32" : "#d32f2f";

        aggiungiCella(row, p.getStock().getSimbolo(), "-fx-font-weight:bold;");
        aggiungiCella(row, tronca(p.getStock().getNomeAzienda(), 20), "-fx-text-fill:#555555;");
        aggiungiCella(row, String.format("%.4f", p.getQuantita()), "");
        aggiungiCella(row, String.format("$ %.2f", p.getPrezzoMedioAcquisto()), "");
        aggiungiCella(row, VALUTA.format(p.getValoreAttuale()), "");
        aggiungiCella(row, plStr, "-fx-font-weight:bold; -fx-text-fill:" + plColor + ";");
        return row;
    }

    private HBox creaRigaTransazione(TransactionBean t) {
        HBox row = new HBox(8);
        row.setStyle("-fx-padding:6 0 6 0; -fx-border-color:transparent transparent #eeeeee transparent;");

        String data = (t.getQuando() != null)
                ? t.getQuando().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")) : "—";
        aggiungiCella(row, data, "-fx-text-fill:#555555;");
        aggiungiCella(row, t.getStock() != null ? t.getStock().getSimbolo() : "?",
                "-fx-font-weight:bold;");
        aggiungiCella(row, t.getTipo() != null ? t.getTipo().name() : "?", "");
        aggiungiCella(row, String.format("%.4f", t.getQuantita()), "");
        aggiungiCella(row, VALUTA.format(t.getImportoTotale()), "");
        aggiungiCella(row, t.getStato() != null ? t.getStato().name() : "?",
                "-fx-text-fill:#555555;");
        return row;
    }

    private void aggiungiCella(HBox row, String testo, String stile) {
        Label lbl = new Label(testo);
        lbl.setPrefWidth(120);
        if (!stile.isEmpty()) lbl.setStyle(stile);
        row.getChildren().add(lbl);
    }

    // ── mostraDettaglioStock() ────────────────────────────────────────────────

    @Override
    protected void mostraDettaglioStock(StockBean s) {
        nascondiPortafoglio();
        nascondiStorico();
        simboloCorrente = s.getSimbolo();
        lblDetSimbolo.setText(s.getSimbolo());
        lblDetNome.setText(s.getNomeAzienda());
        lblDetSettore.setText(s.getSettore());
        lblDetPrezzo.setText(String.format("$ %.4f", s.getPrezzoAttuale()));
        lblDetVariazione.setText(String.format("%+.2f%%", s.getVariazioneGiornaliera()));
        lblDetVariazione.getStyleClass().removeAll("tag-positivo", "tag-negativo");
        lblDetVariazione.getStyleClass().add(
                s.getVariazioneGiornaliera() >= 0 ? "tag-positivo" : "tag-negativo");
        lblDetVarSettimanale.setText(String.format("%+.2f%%", s.getVariazioneSettimanale()));
        lblDetMarketCap.setText(formatMarketCap(s.getMarketCap()));
        lblDetVolume.setText(s.getVolumeSettimanale() > 0
                ? String.format("%.0f", s.getVolumeSettimanale()) : "—");
        lblDetAggiornamento.setText("Aggiornato: " + LocalDateTime.now().format(FMT));

        if (isStudente) {
            boxAzioneAcquisto.setVisible(true);  boxAzioneAcquisto.setManaged(true);
            boxSolaLettura.setVisible(false);    boxSolaLettura.setManaged(false);
            PortafoglioBean pf = navigator.getPortafoglio();
            if (pf != null) {
                lblSaldoCheck.setText("Saldo disponibile: " + VALUTA.format(pf.getSaldoDisponibile()));
                boolean saldoOk = pf.getSaldoDisponibile() >= s.getPrezzoAttuale();
                btnCompra.setDisable(!saldoOk);
                btnCompra.setText(saldoOk ? "💰  Compra ora" : "💰  Saldo insufficiente");
            }
        } else {
            boxAzioneAcquisto.setVisible(false); boxAzioneAcquisto.setManaged(false);
            boxSolaLettura.setVisible(true);     boxSolaLettura.setManaged(true);
        }

        Stock stockModel = StockService.getInstance().trovaStock(s.getSimbolo());
        if (stockModel != null) stockModel.aggiungiObserver(this);

        panelDettaglio.setVisible(true);
        panelDettaglio.setManaged(true);
        aggiornaGriglia(navigator.getListaStock());
    }

    // ── Handler FXML — Mercato ────────────────────────────────────────────────

    @FXML private void clickCerca()          { nascondiMessaggio(); eseguiRicerca(txtRicerca.getText()); }
    @FXML private void clickAggiorna()       { aggiornaGriglia(navigator.getListaStock()); nascondiMessaggio(); }
    @FXML private void clickQuickSearch(javafx.event.ActionEvent e) {
        String sym = ((Button) e.getSource()).getText();
        txtRicerca.setText(sym);
        eseguiRicerca(sym);
    }
    @FXML private void clickCompra()         { if (simboloCorrente != null) eseguiAvviaOrdine(simboloCorrente); }
    @FXML private void clickDashboard()      { tornaDashboard(); }
    @FXML private void clickLogout()         { StockService.getInstance().fermaAggiornamentoAutomatico(); eseguiLogout(); }

    // ── Handler FXML — Sidebar wallet (studente) ──────────────────────────────

    @FXML private void clickNavPortafoglio() { navigator.goToPortafoglio(); }
    @FXML private void clickNavStorico()     { navigator.goToStorico(); }

    // ── Handler FXML — Conferma ordine ───────────────────────────────────────

    @FXML private void clickConfermaOrdine() {
        try {
            double q = Double.parseDouble(txtQuantita.getText().trim().replace(",", "."));
            if (q <= 0) { mostraErrore("La quantità deve essere maggiore di zero."); return; }
            eseguiConfermaAcquisto(q);
        } catch (NumberFormatException e) {
            mostraErrore("Quantità non valida. Inserisci un numero (es. 2.5).");
        }
    }
    @FXML private void clickAnnullaOrdine()  { eseguiAnnullaOrdine(); }

    // ── Handler FXML — Portafoglio ────────────────────────────────────────────

    @FXML private void clickPortafoglioStorico() { navigator.goToStorico(); }
    @FXML private void clickPortafoglioMercato() {
        if (portafoglioEsterno) tornaDashboard();
        else navigator.goToMercato();
    }

    // ── Handler FXML — Storico ────────────────────────────────────────────────

    @FXML private void clickStoricoPortafoglio() { navigator.goToPortafoglio(); }
    @FXML private void clickStoricoMercato()     { navigator.goToMercato(); }

    // ── Feedback ──────────────────────────────────────────────────────────────

    @Override
    protected void mostraCaricamento(boolean visible) {
        Platform.runLater(() -> { boxCaricamento.setVisible(visible); boxCaricamento.setManaged(visible); });
    }

    @Override
    protected void mostraErrore(String msg) {
        lblMessaggio.setText(msg);
        lblMessaggio.getStyleClass().removeAll("label-successo");
        lblMessaggio.getStyleClass().add("label-errore");
        lblMessaggio.setVisible(true);
        lblMessaggio.setManaged(true);
    }

    @Override
    protected void showMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info"); alert.setHeaderText(null); alert.setContentText(msg);
        alert.showAndWait();
    }

    @Override
    protected void mostraAcquistoCompletato(TransactionBean t) {
        nascondiConfermaOrdine();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acquisto completato");
        alert.setHeaderText("✅ Ordine eseguito con successo");
        alert.setContentText(String.format(
                "Stock: %s%nQuantità: %.4f azioni%nPrezzo unitario: $ %.2f%nTotale: %s",
                t.getStock().getSimbolo(), t.getQuantita(),
                t.getPrezzoAlMomento(), VALUTA.format(t.getImportoTotale())));
        alert.showAndWait();
        navigator.goToMercato();
    }

    // ── Show / Hide pannelli ──────────────────────────────────────────────────

    private void nascondiTuttiIPannelli() {
        nascondiDettaglio();
        nascondiConfermaOrdine();
        nascondiPortafoglio();
        nascondiStorico();
    }

    private void nascondiDettaglio() {
        if (panelDettaglio != null) { panelDettaglio.setVisible(false); panelDettaglio.setManaged(false); }
    }
    private void nascondiConfermaOrdine() {
        if (panelConfermaOrdine != null) { panelConfermaOrdine.setVisible(false); panelConfermaOrdine.setManaged(false); }
    }
    private void nascondiPortafoglio() {
        if (panelPortafoglio != null) { panelPortafoglio.setVisible(false); panelPortafoglio.setManaged(false); }
    }
    private void nascondiStorico() {
        if (panelStorico != null) { panelStorico.setVisible(false); panelStorico.setManaged(false); }
    }
    private void nascondiMessaggio() {
        lblMessaggio.setText(""); lblMessaggio.setVisible(false); lblMessaggio.setManaged(false);
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