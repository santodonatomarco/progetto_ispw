package org.project.ing.persistenza;



import java.util.HashMap;
import java.util.Map;

public abstract class CachedDAO<T> {

    protected Map<String, T> cache;

    protected CachedDAO() {
        this.cache = new HashMap<>();
    }

    public boolean inCache(String key){
        return this.cache.containsKey(key);
    }

    public boolean inCache (T elemento) {
        if(elemento == null){
            return false;
        }
        String key = ottieniChiave(elemento);
        return inCache(key);
    }

    protected abstract String ottieniChiave(T elemento);

    public void addToCache(T elemento) {
        if (elemento != null && (!inCache(elemento))) {
            String key = ottieniChiave(elemento);
            this.cache.put(key, elemento);
        }
    }

    public T fetchFromCache(String key) {
        return this.cache.get(key);
    }

    public void deleteFromCache(T elemento) {
        if(inCache(elemento)){
            String key = ottieniChiave(elemento);
            this.cache.remove(key);
        }
    }

    public void svuotaCache() {
        this.cache.clear();
    }
}