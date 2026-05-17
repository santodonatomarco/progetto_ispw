package org.project;

import javafx.application.Platform;
import org.project.view.Navigator;
import org.project.ing.factory.NavigatorFactory;

public class Main {
    public static void main(String[] args) {
        Platform.startup(() -> {
            Navigator navigator = NavigatorFactory.getNavigatorFactory().createNavigator();
            // Chiamo il navigatore senza sapere quale sto usando
            navigator.startUp();
        });
    }
}
