package org.project.dao.wallets;

import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.factory.StockFactory;
import org.project.ing.persistenza.DBConnection;
import org.project.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PortafoglioDAODB extends PortafoglioDAO {

    private final StudenteDAO studenteDAO;
    private final StockFactory stockFactory;

    public PortafoglioDAODB(StudenteDAO studenteDAO, StockFactory stockFactory) {
        this.studenteDAO = studenteDAO;
        this.stockFactory = stockFactory;
    }

    @Override
    protected VirtualWallet doRetrievePortafoglioByEmail(String mail) throws DAOException {
        Studente studente = studenteDAO.getStudenteByEmail(mail);
        if (studente == null) return null;

        VirtualWallet wallet = null;

        String sqlSaldoDisponibile = "SELECT saldo_disponibile FROM virtual_wallet WHERE studente_email = ?";
        String sqlPosizioni = "SELECT simbolo_stock, quantita, prezzo_medio FROM wallet_position WHERE studente_email = ?";
        String sqlTransazioni = "SELECT simbolo_stock, tipo, quantita, prezzo_al_momento, timestamp FROM transaction WHERE studente_email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {

            // 1. Carica il Wallet base
            try (PreparedStatement ps = conn.prepareStatement(sqlSaldoDisponibile)) {
                ps.setString(1, mail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double saldo = rs.getDouble("saldo_disponibile");
                        wallet = new VirtualWallet(studente, saldo);
                    } else {
                        return null; // Il portafoglio non esiste nel DB
                    }
                }
            }

            // 2. Carica le Posizioni (WalletPositions)
            try (PreparedStatement ps = conn.prepareStatement(sqlPosizioni)) {
                ps.setString(1, mail);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String simbolo = rs.getString("simbolo_stock");
                        double quantita = rs.getDouble("quantita");
                        double prezzoMedio = rs.getDouble("prezzo_medio");

                        Stock stock = stockFactory.creaStock(simbolo);
                        WalletPosition wp = new WalletPosition(stock, quantita, prezzoMedio);
                        wallet.aggiungiPosizione(wp);
                    }
                }
            }

            // 3. Carica le Transazioni
            try (PreparedStatement ps = conn.prepareStatement(sqlTransazioni)) {
                ps.setString(1, mail);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String simbolo = rs.getString("simbolo_stock");
                        TipoTransazione tipo = TipoTransazione.valueOf(rs.getString("tipo"));
                        double quantita = rs.getDouble("quantita");
                        double prezzo = rs.getDouble("prezzo_al_momento");

                        Stock stock = stockFactory.creaStock(simbolo);
                        Transaction t = new Transaction(stock, tipo, quantita, prezzo);
                        t.completaTransazione(); // Da DB si presumono già completate
                        wallet.aggiungiTransazione(t);
                    }
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Errore DB caricamento portafoglio: " + e.getMessage());
        } catch (Exception e) {
            throw new DAOException("Errore caricamento stock per il portafoglio: " + e.getMessage());
        }

        return wallet;
    }

    @Override
    public void salvaPortafoglio(VirtualWallet wallet) throws DAOException {
        String email = wallet.proprietario().presentaEmail();

        // 1. Salva/Aggiorna il saldo base usando un UPSERT (INSERT ... ON DUPLICATE KEY UPDATE)
        String sqlWallet = "INSERT INTO virtual_wallet (studente_email, saldo_disponibile) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE saldo_disponibile = VALUES(saldo_disponibile)";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Inizio transazione SQL

            try (PreparedStatement ps = conn.prepareStatement(sqlWallet)) {
                ps.setString(1, email);
                ps.setDouble(2, wallet.saldoDisponibile());
                ps.executeUpdate();
            }

            addToCache(wallet); // Sincronizza la cache RAM

        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio portafoglio: " + e.getMessage());
        }
    }



}