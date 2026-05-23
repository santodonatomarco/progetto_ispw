package org.project.ing.observer;

import org.project.model.Stock;

/**
 * Observer nel pattern Observer applicato agli Stock.
 * Chi vuole essere notificato di un cambio prezzo implementa questa interfaccia.
 */
public interface StockObserver {
    /**
     * Chiamato automaticamente da Stock.aggiornaPrezzo() ogni volta che il
     * prezzo di uno stock cambia.
     *
     * @param stock lo stock che ha cambiato prezzo (con il nuovo valore già impostato)
     */
    void aggiornamento(Stock stock);
}
