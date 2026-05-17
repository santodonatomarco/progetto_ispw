package org.project.ing.factory;

import org.project.ing.enumerations.UISupportata;
import org.project.view.Navigator;
import org.project.view.NavigatorCLI;
import org.project.view.NavigatorGUI;

import java.io.InputStream;
import java.util.Properties;

public class NavigatorFactory {

    private static NavigatorFactory instance = null;

    private NavigatorFactory(){}

    public static synchronized NavigatorFactory getNavigatorFactory() {
        if (instance == null) {
            instance = new NavigatorFactory();
        }
        return instance;
    }

    public Navigator createNavigator() {
        Navigator toRet = new NavigatorCLI();

        // Carica il file dal classpath (src/main/resources)
        try (InputStream in = NavigatorFactory.class.getClassLoader().getResourceAsStream("config.properties")) {

            if (in == null) {
                System.err.println("ATTENZIONE: File config.properties non trovato nel classpath! Partirà la CLI di default.");
                return toRet;
            }

            Properties prop = new Properties();
            prop.load(in);

            // Aggiungi .trim() per evitare errori dovuti a spazi accidentali nel file properties
            String grafica = prop.getProperty("ui.type");

            if (grafica != null) {
                UISupportata version = UISupportata.valueOf(grafica.trim().toUpperCase());
                if(version == UISupportata.GUI) {
                    toRet = new NavigatorGUI();
                } else {
                    toRet = new NavigatorCLI();
                }
            }
        } catch (Exception e) {
            System.err.println("Si è verificato un errore durante la lettura delle impostazioni della UI:");
        }

        return toRet;
    }
}