package org.project.ing.adapter;

import org.project.model.Stock;
import java.io.IOException;

/**
 * TARGET INTERFACE per il pattern Adapter
 * Definisce il contratto che il cliente conosce e vuole usare
 */
public interface StockDataProvider {
    Stock recuperaStock(String simbolo) throws IOException;
    void aggiornaStock(Stock stock) throws IOException;
}

