package org.project.ing.factory;

import org.project.exceptions.DAOException;
import org.project.model.Stock;

import java.io.*;
import java.util.Properties;

public class StockFactoryFile extends StockFactory {

    private String fileName;
    private static final String SEP = ";";

    public StockFactoryFile() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(fis);
            this.fileName = prop.getProperty("file.stocks");
        } catch (IOException e) {
            this.fileName = "stocks.csv";  // fallback
        }
    }

    @Override
    public Stock creaStock(String simboloCercato) throws Exception {
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File stocks non trovato: " + fileName);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Stock s = parseStockSeCorrisponde(line, simboloCercato);
                if (s != null) return s;
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file stocks", e);
        }
        return null;  // stock non trovato nel file
    }

    private Stock parseStockSeCorrisponde(String line, String simboloCercato) {
        if (line.trim().isEmpty()) return null;

        String[] parts = line.split(SEP, -1);
        if (parts.length < 5) return null;

        String simbolo = parts[0].trim();
        if (!simbolo.equals(simboloCercato)) return null;

        // È la riga giusta — costruisco lo Stock
        String nomeAzienda = parts[1].trim();
        String settore     = parts[2].trim();
        double prezzo      = Double.parseDouble(parts[3].trim());
        double varDaily    = Double.parseDouble(parts[4].trim());

        Stock s = new Stock(simbolo, nomeAzienda, settore, prezzo);
        s.aggiornaVariazioni(varDaily, 0);
        return s;
    }
}
