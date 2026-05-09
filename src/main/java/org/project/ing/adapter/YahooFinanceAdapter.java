package org.project.ing.adapter;

import org.project.model.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;

/**
 * ADAPTER che implementa l'interfaccia Target (StockDataProvider)
 * Contiene l'Adaptee (yahoofinance.Stock) e converte la sua interfaccia
 * per renderla compatibile con il nostro modello (Stock)
 */
public class YahooFinanceAdapter implements StockDataProvider {

    // Recupera uno stock dall'API e lo converte nel nostro modello
    public Stock recuperaStock(String simbolo) throws IOException {
        yahoofinance.Stock yahooStock = YahooFinance.get(simbolo);

        Stock nuovoStock = new Stock(                    // QUI SI PUO FARE UNA FACTORY NECESSARIA
                yahooStock.getSymbol(),
                yahooStock.getName(),
                yahooStock.getStockExchange(),
                yahooStock.getQuote().getPrice().doubleValue()
        );

        // variazione settimanale da calcolare separatamente
        nuovoStock.aggiornaVariazioni(yahooStock.getQuote().getChangeInPercent().doubleValue(), 0  );

        nuovoStock.aggiornaMarketData(yahooStock.getStats().getMarketCap().doubleValue(), 0);

        return nuovoStock;
    }

    // Aggiorna un nostro Stock esistente con i dati freschi dall'API
    public void aggiornaStock(Stock nostroStock) throws IOException {
        yahoofinance.Stock yahooStock = YahooFinance.get(nostroStock.simbolo());

        nostroStock.aggiornaPrezzo(yahooStock.getQuote().getPrice().doubleValue());  // questo triggera automaticamente l'Observer!

        nostroStock.aggiornaVariazioni(yahooStock.getQuote().getChangeInPercent().doubleValue(), 0);
    }
}