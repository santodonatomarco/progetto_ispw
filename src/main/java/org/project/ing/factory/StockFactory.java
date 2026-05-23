package org.project.ing.factory;

import org.project.ing.adapter.StockDataProvider;
import org.project.ing.adapter.YahooFinanceAdapter;
import org.project.model.Stock;

/**
 * Factory per la creazione di Stock.
 * Usa SEMPRE YahooFinanceAdapter — a prescindere dalla modalità di persistenza.
 * Gli stock vengono sempre dall'API esterna: non ha senso variarli in base
 * alla versione demo/file/db dell'app.
 *
 * Singleton per evitare istanze duplicate del dataProvider.
 */
public class StockFactory {

    private static final StockFactory instance = new StockFactory();
    private final StockDataProvider dataProvider;

    private StockFactory() {
        this.dataProvider = new YahooFinanceAdapter();
    }

    public static StockFactory getInstance() {
        return instance;
    }

    public Stock creaStock(String simbolo) throws Exception {
        return dataProvider.recuperaStock(simbolo);
    }

    public StockDataProvider getDataProvider() {
        return dataProvider;
    }
}
