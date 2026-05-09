package org.project.ing.factory;

import org.project.ing.adapter.YahooFinanceAdapter;
import org.project.model.Stock;

public class StockFactoryAPI extends StockFactory {

    private YahooFinanceAdapter adapter = new YahooFinanceAdapter();   // necessario per conversioni API

    @Override
    public Stock creaStock(String simbolo) throws Exception {
        return adapter.recuperaStock(simbolo);
    }
}