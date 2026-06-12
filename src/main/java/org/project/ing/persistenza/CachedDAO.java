package org.project.ing.persistenza;

import java.util.HashMap;
import java.util.Map;

public abstract class CachedDAO<T> {

    protected Map<String, T> cache;

    protected CachedDAO() {
        this.cache = new HashMap<>();
    }

    public boolean inCache(String key) {
        return this.cache.containsKey(key);
    }

    public boolean inCache(T elemento) {
        if (elemento == null) return false;
        return inCache(ottieniChiave(elemento));
    }

    protected abstract String ottieniChiave(T elemento);

    public void addToCache(T elemento) {
        if (elemento != null && !inCache(elemento)) {
            this.cache.put(ottieniChiave(elemento), elemento);
        }
    }

    public T fetchFromCache(String key) {
        return this.cache.get(key);
    }

    public void deleteFromCache(T elemento) {
        if (inCache(elemento)) {
            this.cache.remove(ottieniChiave(elemento));
        }
    }

    /**
     * Rimuove dalla cache l'entry con questa chiave, senza dover avere
     * l'oggetto in mano. Utile nella cascade delete (es. rimuoviStudente
     * conosce l'email ma non ha necessariamente il Studente in memoria).
     */
    public void deleteFromCacheByKey(String key) {
        this.cache.remove(key);
    }

    public void svuotaCache() {
        this.cache.clear();
    }
}
