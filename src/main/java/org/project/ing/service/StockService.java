package org.project.ing.service;

import org.project.ing.adapter.StockDataProvider;
import org.project.ing.factory.StockFactory;
import org.project.model.Stock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Singleton che gestisce il registro centralizzato degli Stock monitorati.
 *
 * Responsabilità:
 * - Evitare duplicati: se due wallet comprano AAPL, usano la stessa istanza Stock
 * - Creare nuovi stock sempre tramite l'API (StockFactory → YahooFinanceAdapter)
 * - Aggiornare periodicamente i prezzi ogni 30 secondi (solo in GUI)
 *   → aggiornaPrezzo() su Stock triggera automaticamente l'observer (WalletPosition + GUI)
 */
public class StockService {

    private static final Logger LOG = Logger.getLogger(StockService.class.getName());
    private static final int INTERVALLO_AGGIORNAMENTO_SECONDI = 30;

    private static final StockService instance = new StockService();

    private final Map<String, Stock> stockMonitorati = new HashMap<>();
    private final StockDataProvider dataProvider;
    private ScheduledExecutorService scheduler;

    private StockService() {
        this.dataProvider = StockFactory.getInstance().getDataProvider();
    }

    public static StockService getInstance() {
        return instance;
    }

    // ── Recupero stock ────────────────────────────────────────────────────────

    /**
     * Restituisce lo stock se già monitorato, altrimenti lo crea dall'API e lo registra.
     */
    public Stock ottieniOCreaStock(String simbolo) throws Exception {
        String sym = simbolo.toUpperCase();
        if (stockMonitorati.containsKey(sym)) {
            return stockMonitorati.get(sym);
        }
        Stock nuovo = StockFactory.getInstance().creaStock(sym);
        stockMonitorati.put(sym, nuovo);
        return nuovo;
    }

    public void monitoraStock(Stock stock) {
        stockMonitorati.put(stock.simbolo(), stock);
    }

    public Stock trovaStock(String simbolo) {
        return stockMonitorati.get(simbolo.toUpperCase());
    }

    // ── Aggiornamento periodico (per la GUI) ──────────────────────────────────

    /**
     * Avvia il polling periodico ogni 30 secondi.
     * Ogni aggiornamento chiama aggiornaPrezzo() su ogni Stock, che triggera
     * automaticamente notificaObserver() → la GUI si aggiorna via Platform.runLater().
     *
     * Da chiamare all'avvio della GUI (non dalla CLI).
     */
    public void avviaAggiornamentoAutomatico() {
        if (scheduler != null && !scheduler.isShutdown()) return; // già attivo

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "stock-updater");
            t.setDaemon(true); // non blocca lo shutdown dell'app
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            for (Stock stock : stockMonitorati.values()) {
                try {
                    dataProvider.aggiornaStock(stock);
                    // aggiornaPrezzo() interno chiama notificaObserver() automaticamente
                } catch (IOException e) {
                    LOG.warning("Impossibile aggiornare " + stock.simbolo() + ": " + e.getMessage());
                }
            }
        }, INTERVALLO_AGGIORNAMENTO_SECONDI, INTERVALLO_AGGIORNAMENTO_SECONDI, TimeUnit.SECONDS);

        LOG.info("StockService: aggiornamento automatico avviato ogni "
                + INTERVALLO_AGGIORNAMENTO_SECONDI + "s");
    }

    /**
     * Ferma il polling periodico (da chiamare al logout o alla chiusura dell'app GUI).
     */
    public void fermaAggiornamentoAutomatico() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Aggiornamento manuale immediato di tutti gli stock monitorati.
     * Usabile anche dalla CLI su richiesta esplicita dell'utente.
     */
    public void aggiornaStocksOra() throws IOException {
        for (Stock stock : stockMonitorati.values()) {
            dataProvider.aggiornaStock(stock);
        }
    }
}
