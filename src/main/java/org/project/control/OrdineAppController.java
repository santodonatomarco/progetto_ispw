package org.project.control;

import org.project.dao.posizioni.WalletPositionDAO;
import org.project.dao.transazioni.TransactionDAO;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.DAOException;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.TransactionBean;
import org.project.view.bean.StockBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Controller applicativo per la conferma (o annullamento) di un ordine.
 *
 * Responsabilità:
 *  - verificare che la transazione PENDING non sia scaduta (> 5 minuti)
 *  - aggiornare quantita e importo in base alla scelta finale dello studente
 *  - scalare il saldo, aggiornare/creare la WalletPosition, completare la transazione
 *  - persistere tutto (transazione, posizione, wallet)
 */
public class OrdineAppController {

    private static final int TIMEOUT_MINUTI = 5;

    /**
     * Conferma l'ordine di acquisto.
     *
     * @param sessione        sessione corrente
     * @param quantitaScelta  quantità di azioni che lo studente ha deciso di acquistare
     * @return TransactionBean con stato DONE
     */
    public TransactionBean confermaAcquisto(SessioneBean sessione, double quantitaScelta)
            throws ControllerException {

        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null) {
            throw new ControllerException("Sessione non valida o scaduta.");
        }

        Transaction transazione = sessioneModel.getTransazionePending();
        if (transazione == null) {
            throw new ControllerException("Nessun ordine pending trovato. Riprova.");
        }

        // 1. Verifica timeout — controlla passivamente al momento della conferma
        long minutiTrascorsi = ChronoUnit.MINUTES.between(transazione.quando(), LocalDateTime.now());
        if (minutiTrascorsi > TIMEOUT_MINUTI) {
            sessioneModel.setTransazionePending(null);
            throw new ControllerException(
                    "Il tempo per confermare l'ordine è scaduto (limite: " + TIMEOUT_MINUTI + " minuti). Riprova.");
        }

        // 2. Aggiorna quantita e importo con la scelta finale dello studente
        transazione.impostaQuantita(quantitaScelta);

        VirtualWallet wallet = sessioneModel.getWalletCorrente();
        if (wallet == null) {
            throw new ControllerException("Wallet non trovato per lo studente.");
        }

        // 3. Verifica saldo sufficiente
        if (wallet.saldoDisponibile() < transazione.importoTotale()) {
            throw new ControllerException(
                    "Saldo insufficiente. Disponibile: " + wallet.saldoDisponibile() +
                            ", richiesto: " + transazione.importoTotale());
        }

        DAOFactory factory = DAOFactory.getDAOFactory();
        TransactionDAO transactionDAO = factory.createTransactionDAO();
        WalletPositionDAO posizioneDAO = factory.createWalletPositionDAO();
        PortafoglioDAO walletDAO = factory.createPortafoglioDAO();

        try {
            Stock stock = transazione.stock();

            // 4. Scala il saldo
            wallet.scalaSaldo(transazione.importoTotale());

            // 5. Aggiorna o crea la WalletPosition
            WalletPosition posizione = wallet.trovaPosizione(stock);
            if (posizione == null) {
                // Primo acquisto di questo stock
                posizione = new WalletPosition(stock, quantitaScelta, transazione.prezzoAlMomento());
                stock.aggiungiObserver(posizione);   // registra come observer per aggiornamenti prezzo
                wallet.aggiungiPosizione(posizione);
                posizioneDAO.salvaPosizione(posizione);
            } else {
                // Acquisto aggiuntivo — aggiorna prezzo medio
                posizione.aggiungiAzioni(quantitaScelta, transazione.prezzoAlMomento());
                posizioneDAO.aggiornaPosizione(posizione);
            }

            // 6. Completa la transazione
            transazione.completaTransazione();
            wallet.aggiungiTransazione(transazione);

            // 7. Persisti transazione e wallet
            transactionDAO.salvaTransazione(transazione);
            walletDAO.aggiornaPortafoglio(wallet);

            // 8. Pulisce la pending dalla sessione
            sessioneModel.setTransazionePending(null);

            return toTransactionBean(transazione);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante il salvataggio dell'ordine.", e);
        }
    }

    /**
     * Annulla l'ordine pending (lo studente clicca "annulla" prima dei 5 minuti).
     */
    public void annullaOrdine(SessioneBean sessione) throws ControllerException {
        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null) {
            throw new ControllerException("Sessione non valida o scaduta.");
        }
        sessioneModel.setTransazionePending(null);
        sessioneModel.setStockCorrente(null);
    }

    // ── Conversione model → bean ──────────────────────────────────────────────

    private TransactionBean toTransactionBean(Transaction t) {
        StockBean stockBean = new StockBean(
                t.stock().simbolo(), t.stock().nomeAzienda(),
                t.stock().settore(), t.stock().prezzoAttuale());
        return new TransactionBean(
                stockBean, t.tipo(), t.stato(),
                t.quantita(), t.prezzoAlMomento(),
                t.importoTotale(), t.quando());
    }
}