package org.project.ing.observer;

public interface StockObservable {
    void aggiungiObserver(StockObserver o);
    void rimuoviObserver(StockObserver o);
    void notificaObserver();
}
