package org.project.ing.provider;

import org.project.model.Stock;
import java.io.IOException;


// se in un futuro userò un diverso provider posso cambiare da qui

public interface StockDataProvider {
    Stock recuperaStock(String simbolo) throws IOException;
    void aggiornaStock(Stock stock) throws IOException;
}

