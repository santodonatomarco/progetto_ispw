package org.project.ing.observer;

import org.project.model.Stock;

/**
 * Observer nel pattern Observer applicato agli Stock.
 * Chi vuole essere notificato di un cambio prezzo implementa questa interfaccia.
 */
public interface StockObserver {
    void aggiornamento();
}
