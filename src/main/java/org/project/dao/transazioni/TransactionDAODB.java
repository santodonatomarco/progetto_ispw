package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.StatoTransazione;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.persistenza.DBConnection;
import org.project.ing.service.StockService;
import org.project.model.Stock;
import org.project.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAODB extends TransactionDAO {

    // Tabella: transazione (email_studente, simbolo, tipo, stato, quantita, prezzo_al_momento, timestamp)

    @Override
    protected void doSaveTransazione(Transaction t) throws DAOException {
        String sql = "INSERT INTO transazione (simbolo, tipo, stato, quantita, prezzo_al_momento, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, t.stock().simbolo());
            st.setString(2, t.tipo().toString());
            st.setString(3, t.stato().toString());
            st.setDouble(4, t.quantita());
            st.setDouble(5, t.prezzoAlMomento());
            st.setTimestamp(6, Timestamp.valueOf(t.quando()));
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio transazione: " + e.getMessage());
        }
    }

    @Override
    protected void doUpdateTransazione(Transaction t) throws DAOException {
        String sql = "UPDATE transazione SET stato = ?, quantita = ?, prezzo_al_momento = ? " +
                "WHERE simbolo = ? AND timestamp = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, t.stato().toString());
            st.setDouble(2, t.quantita());
            st.setDouble(3, t.prezzoAlMomento());
            st.setString(4, t.stock().simbolo());
            st.setTimestamp(5, Timestamp.valueOf(t.quando()));
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore aggiornamento transazione: " + e.getMessage());
        }
    }

    @Override
    protected List<Transaction> doRetrieveTransazioniByEmail(String email) throws DAOException {
        String sql = "SELECT simbolo, tipo, stato, quantita, prezzo_al_momento, timestamp " +
                "FROM transazione WHERE email_studente = ? ORDER BY timestamp DESC";
        List<Transaction> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String simbolo  = rs.getString("simbolo");
                    TipoTransazione tipo  = TipoTransazione.valueOf(rs.getString("tipo"));
                    StatoTransazione stato = StatoTransazione.valueOf(rs.getString("stato"));
                    double quantita = rs.getDouble("quantita");
                    double prezzo   = rs.getDouble("prezzo_al_momento");

                    Stock stock = StockService.getInstance().ottieniOCreaStock(simbolo);
                    Transaction t = new Transaction(stock, tipo, quantita, prezzo);
                    if (stato == StatoTransazione.DONE) t.completaTransazione();
                    lista.add(t);
                }
            }
        } catch (Exception e) {
            throw new DAOException("Errore lettura transazioni da DB: " + e.getMessage());
        }
        return lista;
    }
}