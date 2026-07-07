package org.project.ing.provider;

import org.project.model.Stock;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * ADAPTER — chiama il nuovo endpoint Yahoo Finance v8 /chart/ e converte
 * la risposta nel nostro modello Stock.
 *
 * Endpoint: https://query1.finance.yahoo.com/v8/finance/chart/{SIMBOLO}
 */
public class YahooFinanceProvider implements StockDataProvider {

    private static final String BASE_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String PARAMS = "?interval=1d&range=7d";

    private final HttpClient httpClient;

    public YahooFinanceProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public Stock recuperaStock(String simbolo) throws IOException {
        String json = chiamaApi(simbolo);
        return parseStock(simbolo.toUpperCase(), json);
    }

    @Override
    public void aggiornaStock(Stock nostroStock) throws IOException {
        String json = chiamaApi(nostroStock.simbolo());
        String meta = estraiBloccoPrima(json);

        double prezzo = estraiDouble(meta, "\"regularMarketPrice\"");
        double prevClose = estraiDouble(meta, "\"regularMarketPreviousClose\"");
        double varPercent = calcolaVariazione(
                estraiDouble(meta, "\"regularMarketChangePercent\""),
                prezzo, prevClose);

        List<Double> closes = estraiArrayDouble(json, "\"close\"");
        List<Double> volumes = estraiArrayDouble(json, "\"volume\"");

        double weekly = calcolaVariazioneSettimanaleDaArray(closes);
        double volumeSett = calcolaVolumeSettimanaleDaArray(volumes);
        double marketCap = estraiDouble(meta, "\"marketCap\"");

        // Delegato a metodo di supporto per ridurre Cognitive Complexity
        varPercent = ricalcolaVariazioneGiornaliera(varPercent, closes);

        nostroStock.aggiornaPrezzo(prezzo);
        nostroStock.aggiornaVariazioni(varPercent, weekly);
        nostroStock.aggiornaMarketData(marketCap, volumeSett);
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

    private Stock parseStock(String simbolo, String body) throws IOException {
        try {
            String meta = estraiBloccoPrima(body);

            double prezzo = estraiDouble(meta, "\"regularMarketPrice\"");
            double prevClose = estraiDouble(meta, "\"regularMarketPreviousClose\"");

            if (prezzo <= 0) {
                throw new IOException("Dati di prezzo non disponibili per: " + simbolo);
            }

            double varPercent = calcolaVariazione(
                    estraiDouble(meta, "\"regularMarketChangePercent\""),
                    prezzo, prevClose);

            String nome = primoNonNullo(
                    estraiStringa(meta, "\"longName\""),
                    estraiStringa(meta, "\"shortName\""),
                    simbolo);
            String exchange = primoNonNullo(
                    estraiStringa(meta, "\"fullExchangeName\""),
                    estraiStringa(meta, "\"exchangeName\""),
                    "—");
            double marketCap = estraiDouble(meta, "\"marketCap\"");

            List<Double> closes = estraiArrayDouble(body, "\"close\"");
            List<Double> volumes = estraiArrayDouble(body, "\"volume\"");

            double weekly = calcolaVariazioneSettimanaleDaArray(closes);
            double volumeSett = calcolaVolumeSettimanaleDaArray(volumes);

            // Delegato a metodo di supporto per ridurre Cognitive Complexity
            varPercent = ricalcolaVariazioneGiornaliera(varPercent, closes);

            Stock stock = new Stock(simbolo, nome, exchange, prezzo);
            stock.aggiornaVariazioni(varPercent, weekly);
            stock.aggiornaMarketData(marketCap, volumeSett);
            return stock;

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Errore parsing Yahoo per " + simbolo + ": " + e.getMessage(), e);
        }
    }

    private List<Double> estraiArrayDouble(String json, String chiave) {
        List<Double> valori = new ArrayList<>();
        int idx = json.indexOf(chiave);
        if (idx < 0) return valori;
        int i = json.indexOf('[', idx);
        if (i < 0) return valori;
        i++; // posizione dopo '['

        StringBuilder num = new StringBuilder();
        boolean inNumber = false;
        boolean endOfArray = false; // Flag per sostituire il break multiplo

        while (i < json.length() && !endOfArray) {
            char c = json.charAt(i);

            if (c == ']') {
                aggiungiNumeroSePresente(valori, num, inNumber);
                endOfArray = true;
            } else if (c == 'n' && i + 4 <= json.length() && json.startsWith("null", i)) {
                // If nested mergiato e continue rimosso tramite matematica degli indici
                inNumber = false;
                num.setLength(0);
                i += 3; // +3 perché c'è un i++ alla fine del loop
            } else if (Character.isDigit(c) || c == '.' || c == '-' || c == 'E' || c == 'e') {
                num.append(c);
                inNumber = true;
            } else if (c == ',' || Character.isWhitespace(c)) {
                aggiungiNumeroSePresente(valori, num, inNumber);
                inNumber = false;
                num.setLength(0);
            }
            i++;
        }
        return valori;
    }

    private void aggiungiNumeroSePresente(List<Double> valori, StringBuilder num, boolean inNumber) {
        if (inNumber && !num.isEmpty()) {
            try {
                valori.add(Double.parseDouble(num.toString()));
            } catch (NumberFormatException ignored) {
                // Il parsing è fallito, la stringa non è un double valido. Ignoriamo il valore.
            }
        }
    }

    private double ricalcolaVariazioneGiornaliera(double varPercent, List<Double> closes) {
        if (Math.abs(varPercent) >= 1e-9 || closes.size() < 2) {
            return varPercent;
        }

        // Variabili dichiarate su righe separate
        Double last = null;
        Double prev = null;

        for (int i = closes.size() - 1; i >= 0; i--) {
            if (closes.get(i) != null) {
                if (last == null) {
                    last = closes.get(i);
                } else {
                    prev = closes.get(i);
                    break;
                }
            }
        }

        if (last != null && prev != null && prev > 0) {
            return (last - prev) / prev * 100.0;
        }
        return varPercent;
    }

    private double calcolaVariazioneSettimanaleDaArray(List<Double> closes) {
        if (closes == null || closes.size() < 2) return 0.0;

        // Variabili dichiarate su righe separate
        Double first = null;
        Double last = null;

        for (Double d : closes) {
            if (d != null) {
                first = d;
                break;
            }
        }

        for (int i = closes.size() - 1; i >= 0; i--) {
            if (closes.get(i) != null) {
                last = closes.get(i);
                break;
            }
        }

        if (first == null || last == null || first == 0.0) return 0.0;
        return (last - first) / first * 100.0;
    }

    private double calcolaVolumeSettimanaleDaArray(List<Double> volumes) {
        if (volumes == null || volumes.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double v : volumes) if (v != null && v > 0) sum += v;
        return sum;
    }

    private double calcolaVariazione(double varDaYahoo, double prezzo, double prevClose) {
        if (varDaYahoo != 0.0) return varDaYahoo;
        if (prevClose > 0 && prezzo > 0) {
            return (prezzo - prevClose) / prevClose * 100;
        }
        return 0.0;
    }

    // ── Utility di parsing JSON minimale ──────────────────────────────────────

    private String estraiBloccoPrima(String json) {
        int start = json.indexOf("\"meta\"");
        if (start < 0) return json;
        int end = json.indexOf("\"timestamp\"", start);
        return end > start ? json.substring(start, end) : json.substring(start);
    }

    private double estraiDouble(String json, String chiave) {
        int idx = json.indexOf(chiave);
        if (idx < 0) return 0.0;
        int i = idx + chiave.length();
        while (i < json.length() && (json.charAt(i) == ':' || json.charAt(i) == ' ')) i++;

        if (i + 4 <= json.length() && json.startsWith("null", i)) return 0.0;

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