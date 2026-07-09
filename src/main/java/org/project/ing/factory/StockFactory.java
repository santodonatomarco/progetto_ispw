package org.project.ing.factory;

import org.project.ing.provider.StockDataProvider;
import org.project.ing.provider.YahooFinanceProvider;
import org.project.model.Stock;

import java.io.IOException;

/**
 * Singleton per evitare istanze duplicate del dataProvider.
 */
public class StockFactory {

    private static StockFactory instance = null;

    private final StockDataProvider dataProvider;

    protected StockFactory() {
        this.dataProvider = new YahooFinanceProvider();
    }

    public static synchronized StockFactory getInstance() {
        if (instance == null) {
            instance = new StockFactory();
        }
        return instance;
    }

    public Stock creaStock(String simbolo) throws IOException {
        return dataProvider.recuperaStock(simbolo);
    }

    public StockDataProvider getDataProvider() {
        return dataProvider;
    }
}