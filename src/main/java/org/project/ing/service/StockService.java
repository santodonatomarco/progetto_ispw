package org.project.ing.service;

import org.project.ing.provider.StockDataProvider;
import org.project.ing.factory.StockFactory;
import org.project.model.Stock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class StockService {

    private static final Logger LOG = Logger.getLogger(StockService.class.getName());
    private static final int INTERVALLO_AGGIORNAMENTO_SECONDI = 30;

    private static StockService instance = null;

    private final Map<String, Stock> stockMonitorati = new HashMap<>();
    private final StockDataProvider dataProvider;
    private ScheduledExecutorService scheduler;

    protected StockService() {
        this.dataProvider = StockFactory.getInstance().getDataProvider();
    }

    public static synchronized StockService getInstance() {
        if (instance == null) {
            instance = new StockService();
        }
        return instance;
    }

    // ── Recupero stock ────────────────────────────────────────────────────────


    public Stock ottieniOCreaStock(String simbolo) throws IOException {
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


    public void fermaAggiornamentoAutomatico() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }


    public void aggiornaStocksOra() throws IOException {
        for (Stock stock : stockMonitorati.values()) {
            dataProvider.aggiornaStock(stock);
        }
    }
}
