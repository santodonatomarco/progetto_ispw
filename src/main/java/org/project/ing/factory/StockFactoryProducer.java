package org.project.ing.factory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.project.ing.enumerations.PersistenzaSupportata;

public class StockFactoryProducer {

    private static StockFactory instance = null;

    private StockFactoryProducer() {}

    public static synchronized StockFactory getStockFactory() {
        if (instance == null) {
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                Properties prop = new Properties();
                prop.load(fis);

                String type = prop.getProperty("persistence.type").toUpperCase();
                PersistenzaSupportata version = PersistenzaSupportata.valueOf(type);

                instance = switch (version) {
                    case DEMO       -> new StockFactoryDemo();
                    case FILESYSTEM -> new StockFactoryFile();
                    case DATABASE   -> new StockFactoryAPI();
                };

            } catch (IOException e) {
                instance = new StockFactoryDemo();  // fallback sicuro
            }
        }
        return instance;
    }
}