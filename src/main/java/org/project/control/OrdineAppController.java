package org.project.control;

import org.project.dao.posizioni.WalletPositionDAO;
import org.project.dao.transazioni.TransactionDAO;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.DAOException;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StockBean;
import org.project.view.bean.TransactionBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Controller applicativo per la conferma (o annullamento) di un ordine.
 *
 * Stateless e "stupido" (GRASP): orchestra DAO e model, non contiene
 * logica di business — quella sta in VirtualWallet.eseguiAcquisto().
 */
public class OrdineAppController {

    private static final int TIMEOUT_MINUTI = 5;

    /**
     * Conferma l'ordine di acquisto.
     *
     * @param sessione       sessione corrente
     * @param quantitaScelta quantità di azioni scelta dallo studente
     * @return TransactionBean con stato DONE
     */
    public TransactionBean confermaAcquisto(SessioneBean sessione, double quantitaScelta)
            throws ControllerException {

        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Transaction transazione = sessioneModel.getTransazionePending();
        if (transazione == null)
            throw new ControllerException("Nessun ordine pending trovato. Riprova.");

        // 1. Verifica timeout passivamente al momento della conferma
        long minutiTrascorsi = ChronoUnit.MINUTES.between(transazione.quando(), LocalDateTime.now());
        if (minutiTrascorsi > TIMEOUT_MINUTI) {
            sessioneModel.setTransazionePending(null);
            throw new ControllerException(
                    "Il tempo per confermare l'ordine è scaduto (limite: " + TIMEOUT_MINUTI + " minuti). Riprova.");
        }

        // 2. Aggiorna la quantità con la scelta finale dello studente
        transazione.impostaQuantita(quantitaScelta);

        VirtualWallet wallet = sessioneModel.getWalletCorrente();
        if (wallet == null)
            throw new ControllerException("Wallet non trovato per lo studente.");

        // 3. Verifica saldo sufficiente
        if (wallet.saldoDisponibile() < transazione.importoTotale())
            throw new ControllerException(
                    "Saldo insufficiente. Disponibile: " + wallet.saldoDisponibile() +
                            ", richiesto: " + transazione.importoTotale());

        DAOFactory factory = DAOFactory.getDAOFactory();
        TransactionDAO transactionDAO = factory.createTransactionDAO();
        WalletPositionDAO posizioneDAO = factory.createWalletPositionDAO();
        PortafoglioDAO walletDAO = factory.createPortafoglioDAO();

        try {
            // 4. Delega tutta la logica di acquisto al wallet (Expert Pattern)
            boolean posizioneEsisteva = wallet.trovaPosizione(transazione.stock()) != null;
            WalletPosition posizione = wallet.eseguiAcquisto(
                    transazione.stock(), quantitaScelta, transazione.prezzoAlMomento());

            // 5. Completa la transazione e aggiungila al wallet
            transazione.completaTransazione();
            wallet.aggiungiTransazione(transazione);

            // 6. Persisti
            transactionDAO.salvaTransazione(transazione);
            if (posizioneEsisteva) {
                posizioneDAO.aggiornaPosizione(posizione);
            } else {
                posizioneDAO.salvaPosizione(posizione);
            }
            walletDAO.aggiornaPortafoglio(wallet);

            // 7. Pulisce la pending dalla sessione
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
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");
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