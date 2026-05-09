package org.project.ing.observer;

import org.project.model.Stock;

public interface StockObserver {
    void aggiornamento(Stock stock);
}
