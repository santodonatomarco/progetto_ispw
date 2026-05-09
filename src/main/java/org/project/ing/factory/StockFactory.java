package org.project.ing.factory;

import org.project.model.Stock;

public abstract class StockFactory {
    public abstract Stock creaStock(String simbolo) throws Exception;
}

/* il metodo è reso astratto perché in versione demo non avremo la possibilità
di accedere ad un database o ad un servizio esterno per ottenere i dati reali dello stock,
quindi la classe concreta che implementerà questa factory potrà decidere come creare un oggetto Stock (ad esempio con dati fittizi o hardcoded)
senza dover modificare il codice che dipende da questa factory.
In una versione reale, invece, la classe concreta potrebbe implementare il metodo per recuperare
i dati reali dello stock dall'API esterna,
mantenendo così una separazione chiara tra la logica di creazione degli oggetti Stock e la logica di utilizzo degli stessi. */