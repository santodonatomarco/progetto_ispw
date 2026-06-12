package org.project.ing.persistenza;

import org.project.exceptions.DAOException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection instance = null;
    private Connection connection = null;

    private DBConnection() throws DAOException {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) throw new IOException("config.properties non trovato nel classpath");
            Properties prop = new Properties();
            prop.load(input);

            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.user");
            String pass = prop.getProperty("db.pass");
            String driver = prop.getProperty("db.driver");

            Class.forName(driver);
            this.connection = DriverManager.getConnection(url, user, pass);

        } catch (IOException | ClassNotFoundException | SQLException e) {
            throw new DAOException("Errore durante l'inizializzazione del database", e);
        }
    }

    public static synchronized DBConnection getInstance() throws DAOException {
        try {

            if (instance == null || instance.getConnection().isClosed()) {
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            throw new DAOException("Impossibile verificare lo stato della connessione", e);
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
