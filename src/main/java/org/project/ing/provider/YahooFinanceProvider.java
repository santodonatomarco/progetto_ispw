package org.project.ing.provider;

import org.project.model.Stock;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * ADAPTER — chiama il nuovo endpoint Yahoo Finance v8 /chart/ e converte
 * la risposta nel nostro modello Stock.
 *
 * Endpoint: https://query1.finance.yahoo.com/v8/finance/chart/{SIMBOLO}
 *
 * NOTE sul JSON di Yahoo:
 * - I valori nel blocco "meta" sono scalari (non {"raw":...} — quello è /quoteSummary/)
 * - Fuori dall'orario di mercato "regularMarketChangePercent" arriva come null
 *   → la variazione % viene calcolata da prezzoCorrente e regularMarketPreviousClose
 * - Il parsing lavora SOLO sul blocco "meta" per non confondere i valori storici
 *   presenti negli array "indicators"
 */
public class YahooFinanceProvider implements StockDataProvider {

    private static final String BASE_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String PARAMS = "?interval=1d&range=5d";

    private final HttpClient httpClient;

    public YahooFinanceProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public Stock recuperaStock(String simbolo) throws IOException {
        String json  = chiamaApi(simbolo);
        String meta  = estraiBloccoPrima(json, "\"meta\"", "\"timestamp\"");
        return parseStock(simbolo.toUpperCase(), meta);
    }

    @Override
    public void aggiornaStock(Stock nostroStock) throws IOException {
        String json  = chiamaApi(nostroStock.simbolo());
        String meta  = estraiBloccoPrima(json, "\"meta\"", "\"timestamp\"");

        double prezzo    = estraiDouble(meta, "\"regularMarketPrice\"");
        double prevClose = estraiDouble(meta, "\"regularMarketPreviousClose\"");
        double varPercent = calcolaVariazione(
                estraiDouble(meta, "\"regularMarketChangePercent\""),
                prezzo, prevClose);

        nostroStock.aggiornaPrezzo(prezzo);          // triggera observer
        nostroStock.aggiornaVariazioni(varPercent, 0);
    }

    // ── Chiamata HTTP ─────────────────────────────────────────────────────────

    private String chiamaApi(String simbolo) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + simbolo.toUpperCase() + PARAMS))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Richiesta interrotta per: " + simbolo, e);
        }

        if (response.statusCode() != 200) {
            throw new IOException("Yahoo Finance HTTP " + response.statusCode()
                    + " per simbolo: " + simbolo);
        }

        String body = response.body();
        if (body == null || body.isBlank() || body.contains("\"code\":\"Not Found\"")) {
            throw new IOException("Simbolo non trovato su Yahoo Finance: " + simbolo);
        }
        return body;
    }

    // ── Parsing ───────────────────────────────────────────────────────────────

    private Stock parseStock(String simbolo, String meta) throws IOException {
        try {
            double prezzo    = estraiDouble(meta, "\"regularMarketPrice\"");
            double prevClose = estraiDouble(meta, "\"regularMarketPreviousClose\"");

            // Se prezzo non trovato il simbolo non è valido
            if (prezzo <= 0) {
                throw new IOException("Dati di prezzo non disponibili per: " + simbolo);
            }

            // Variazione %: Yahoo può mandare null fuori orario → calcoliamo noi
            double varPercent = calcolaVariazione(
                    estraiDouble(meta, "\"regularMarketChangePercent\""),
                    prezzo, prevClose);

            String nome     = primoNonNullo(
                    estraiStringa(meta, "\"longName\""),
                    estraiStringa(meta, "\"shortName\""),
                    simbolo);
            String exchange = primoNonNullo(
                    estraiStringa(meta, "\"fullExchangeName\""),
                    estraiStringa(meta, "\"exchangeName\""),
                    "—");
            double marketCap = estraiDouble(meta, "\"marketCap\"");

            Stock stock = new Stock(simbolo, nome, exchange, prezzo);
            stock.aggiornaVariazioni(varPercent, 0);
            stock.aggiornaMarketData(marketCap, 0);
            return stock;

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Errore parsing Yahoo per " + simbolo + ": " + e.getMessage(), e);
        }
    }

    /**
     * Calcola la variazione percentuale giornaliera.
     * Se Yahoo manda un valore valido (mercato aperto) lo usa direttamente.
     * Se manda 0 o null (mercato chiuso) lo calcola da prezzo e previousClose.
     */
    private double calcolaVariazione(double varDaYahoo, double prezzo, double prevClose) {
        if (varDaYahoo != 0.0) return varDaYahoo;          // Yahoo l'ha mandata
        if (prevClose > 0 && prezzo > 0) {
            return (prezzo - prevClose) / prevClose * 100;  // calcoliamo noi
        }
        return 0.0;
    }

    // ── Utility di parsing JSON minimale ──────────────────────────────────────

    /**
     * Estrae il testo che va dall'inizio di {chiaveInizio} fino all'inizio di {chiaveFine}.
     * Serve a lavorare solo sul blocco "meta" ed evitare i valori storici degli array.
     */
    private String estraiBloccoPrima(String json, String chiaveInizio, String chiaveFine) {
        int start = json.indexOf(chiaveInizio);
        if (start < 0) return json;
        int end = json.indexOf(chiaveFine, start);
        return end > start ? json.substring(start, end) : json.substring(start);
    }

    /**
     * Estrae il valore numerico scalare che segue immediatamente la chiave JSON.
     * Gestisce: "chiave":123.45  e  "chiave":null (→ restituisce 0.0).
     * NON gestisce {"raw":...} — che non compare nell'endpoint /chart/.
     */
    private double estraiDouble(String json, String chiave) {
        int idx = json.indexOf(chiave);
        if (idx < 0) return 0.0;
        int i = idx + chiave.length();
        while (i < json.length() && (json.charAt(i) == ':' || json.charAt(i) == ' ')) i++;

        // null esplicito → 0.0
        if (i + 4 <= json.length() && json.substring(i, i + 4).equals("null")) return 0.0;

        StringBuilder num = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == '-' || c == 'E' || c == 'e') {
                num.append(c);
            } else if (!num.isEmpty()) {
                break;
            }
            i++;
        }
        if (num.isEmpty()) return 0.0;
        try { return Double.parseDouble(num.toString()); } catch (NumberFormatException e) { return 0.0; }
    }

    /** Estrae il valore stringa tra virgolette che segue immediatamente la chiave. */
    private String estraiStringa(String json, String chiave) {
        int idx = json.indexOf(chiave);
        if (idx < 0) return null;
        int i = idx + chiave.length();
        while (i < json.length() && (json.charAt(i) == ':' || json.charAt(i) == ' ')) i++;
        if (i >= json.length() || json.charAt(i) != '"') return null;
        i++;
        StringBuilder sb = new StringBuilder();
        while (i < json.length() && json.charAt(i) != '"') {
            if (json.charAt(i) == '\\') i++;
            if (i < json.length()) sb.append(json.charAt(i));
            i++;
        }
        return !sb.isEmpty() ? sb.toString() : null;
    }

    private String primoNonNullo(String... valori) {
        for (String v : valori) if (v != null && !v.isBlank()) return v;
        return "—";
    }
}
