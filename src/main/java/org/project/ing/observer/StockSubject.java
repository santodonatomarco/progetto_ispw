package org.project.ing.observer;

import java.util.ArrayList;
import java.util.List;

public class StockSubject {

    private final List<StockObserver> observers = new ArrayList<>();

    public void aggiungiObserver(StockObserver o) {
        observers.add(o);
    }

    public void rimuoviObserver(StockObserver o) {
        observers.remove(o);
    }

    public void notificaObserver() {
        for(StockObserver o : observers) {
            o.aggiornamento();
        }
    }
}
