package org.project.control;

import org.project.exceptions.ControllerException;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.service.StockService;
import org.project.model.*;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StockBean;
import org.project.view.bean.TransactionBean;

/**
 * Controller applicativo per l'analisi del mercato e l'avvio di un ordine.
 *
 * Responsabilità:
 *  - fornire alla view i dati di uno stock (prezzo attuale, variazioni, ecc.)
 *  - creare una Transaction PENDING quando lo studente decide di comprare
 *
 * NON esegue l'acquisto — quello spetta a OrdineAppController dopo la conferma.
 */
public class MercatoAppController {

    /**
     * Recupera i dati aggiornati di uno stock dato il suo simbolo.
     * Lo StockService garantisce che non esistano duplicati in memoria.
     *
     * @param simbolo es. "AAPL", "TSLA"
     * @return StockBean pronto per la view
     */
    public StockBean cercaStock(String simbolo) throws ControllerException {
        try {
            Stock stock = StockService.getInstance().ottieniOCreaStock(simbolo);
            return toBean(stock);
        } catch (Exception e) {
            throw new ControllerException("Impossibile recuperare lo stock: " + simbolo, e);
        }
    }

    /**
     * Avvia un ordine di acquisto: crea una Transaction PENDING e la salva nella sessione.
     * Lo studente ha 5 minuti per confermare tramite OrdineAppController.
     *
     * @param sessione  sessione corrente (contiene il wallet dello studente)
     * @param simbolo   simbolo dello stock da acquistare
     * @return TransactionBean con stato PENDING da mostrare alla view
     */
    public TransactionBean avviaOrdineAcquisto(SessioneBean sessione, String simbolo)
            throws ControllerException {

        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null) {
            throw new ControllerException("Sessione non valida o scaduta.");
        }

        Studente studente = sessioneModel.getStudenteCorrente();
        if (studente == null) {
            throw new ControllerException("Nessuno studente associato alla sessione.");
        }

        VirtualWallet wallet = sessioneModel.getWalletCorrente();
        if (wallet == null) {
            throw new ControllerException("Wallet non trovato per lo studente.");
        }

        try {
            Stock stock = StockService.getInstance().ottieniOCreaStock(simbolo);

            // Crea la transazione PENDING con quantita=0 e prezzo attuale
            // La quantita verrà definita dallo studente nella schermata di conferma
            Transaction transazione = new Transaction(
                    stock,
                    TipoTransazione.BUY,
                    1,                      // quantita placeholder — verrà aggiornata alla conferma
                    stock.prezzoAttuale()
            );

            // Salva la transazione pending nella sessione per recuperarla alla conferma
            sessioneModel.setTransazionePending(transazione);
            sessioneModel.setStockCorrente(stock);

            return toTransactionBean(transazione);

        } catch (Exception e) {
            throw new ControllerException("Errore nell'avvio dell'ordine per: " + simbolo, e);
        }
    }

    // ── Conversioni model → bean ──────────────────────────────────────────────

    private StockBean toBean(Stock s) {
        StockBean bean = new StockBean(
                s.simbolo(), s.nomeAzienda(), s.settore(), s.prezzoAttuale());
        bean.setVariazioneGiornaliera(s.variazioneGiornaliera());
        bean.setVariazioneSettimanale(s.variazioneSettimanale());
        bean.setMarketCap(s.marketCap());
        bean.setVolumeSettimanale(s.volumeSettimanale());
        return bean;
    }

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