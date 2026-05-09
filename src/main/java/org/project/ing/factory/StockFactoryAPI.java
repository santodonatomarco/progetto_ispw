package org.project.ing.factory;

import org.project.ing.adapter.StockDataProvider;
import org.project.ing.adapter.YahooFinanceAdapter;
import org.project.model.Stock;

public class StockFactoryAPI extends StockFactory {

    private StockDataProvider dataProvider = new YahooFinanceAdapter();   // Dipende dall'interfaccia, non dalla classe concreta!

    @Override
    public Stock creaStock(String simbolo) throws Exception {
        return dataProvider.recuperaStock(simbolo);
    }


    // seguirà implementazione dello stock sul database basandosi sull'API

}