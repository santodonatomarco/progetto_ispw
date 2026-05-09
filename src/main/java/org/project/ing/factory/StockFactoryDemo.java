package org.project.ing.factory;

import org.project.model.Stock;

public class StockFactoryDemo extends StockFactory {

    @Override
    public Stock creaStock(String simbolo) throws Exception {
        Stock s = new Stock(simbolo, simbolo + " Corp.", "Technology", 100.0);
        s.aggiornaVariazioni(+2.5, +5.0);
        return s;
    }
}