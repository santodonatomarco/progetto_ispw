package org.project.ing.observer;

import org.project.model.Stock;

import java.util.ArrayList;
import java.util.List;

public abstract class StockSubject {

    private final List<StockObserver> observers = new ArrayList<>();

    public void aggiungiObserver(StockObserver o) {
        if (!observers.contains(o)) observers.add(o);
    }

    public void rimuoviObserver(StockObserver o) {
        observers.remove(o);
    }

    public void notificaObserver() {
        for (StockObserver o : new ArrayList<>(observers)) {
            o.aggiornamento();
        }
    }
}
