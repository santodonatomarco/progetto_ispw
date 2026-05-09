package org.project.ing.service;

import org.project.ing.adapter.YahooFinanceAdapter;
import org.project.ing.factory.StockFactory;
import org.project.ing.factory.StockFactoryProducer;
import org.project.model.Stock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StockService {

    /* stock service serve per evitare che ci siano duplicati dello stesso stock per wallet diversi
    in particolare, nel momento in cui il caso d'uso è in esecuzione e lo studente compra lo stock,
    sarà il controller passando per stockservice (il controller mantiene alta la coesione)
    a vedere se esiste già uno stock con quel simbolo, se esiste lo usa, altrimenti ne crea uno nuovo e lo monitora
     */

    private static StockService instance = null;
    private Map<String, Stock> stockMonitorati = new HashMap<>();     // sa quanti stocks ci sono attivi
    private YahooFinanceAdapter adapter = new YahooFinanceAdapter();
    private StockFactory stockFactory;


    private StockService() {
        this.stockFactory = StockFactoryProducer.getStockFactory();  // aggiungi questo
    }

    public static synchronized StockService getInstance() {
        if (instance == null) {
            instance = new StockService();
        }
        return instance;
    }

    // Aggiorna tutti gli stock monitorati — da chiamare ogni giorno
    public void aggiornaStocks() throws IOException {
        for (Stock stock : stockMonitorati.values()) {
            adapter.aggiornaStock(stock);
            // aggiornaPrezzo() dentro aggiornaStock() triggera l'Observer
            // quindi WalletPosition e RankingService vengono notificati automaticamente
        }
    }

    public Stock ottieniOCreaStock(String simbolo) throws Exception {
        if (stockMonitorati.containsKey(simbolo)) {
            return stockMonitorati.get(simbolo);  // già esiste — lo restituisce
        }
        // non esiste — lo crea tramite factory giusta (Demo/File/API)
        Stock nuovo = stockFactory.creaStock(simbolo);
        stockMonitorati.put(simbolo, nuovo);
        return nuovo;
    }

    public void monitoraStock(Stock stock) {
        stockMonitorati.put(stock.simbolo(), stock);
    }

    public Stock trovaStock(String simbolo) {
        return stockMonitorati.get(simbolo);
    }
}