package org.project.control;

import org.project.dao.posizioni.WalletPositionDAO;
import org.project.dao.transazioni.TransactionDAO;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.DAOException;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.persistenza.DAOFactory;
import org.project.ing.service.StockService;
import org.project.model.*;
import org.project.view.bean.*;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


public class ManageWalletsAppController {

    private static final int TIMEOUT_MINUTI = 5;

    // ── Consultazione mercato ─────────────────────────────────────────────────

    /**
     * Recupera i dati aggiornati di uno stock dato il suo simbolo.
     * Accessibile a qualsiasi utente autenticato; l'acquisto è riservato
     * al solo studente proprietario e gestito da avviaOrdineAcquisto().
     */
    public StockBean cercaStock(RicercaStockBean input) throws ControllerException {
        String simbolo = input.getSimbolo();
        try {
            Stock stock = StockService.getInstance().ottieniOCreaStock(simbolo);
            if (stock == null)
                throw new ControllerException(
                        "Stock \"" + simbolo + "\" non trovato. Verifica il simbolo e riprova.");
            return toStockBean(stock);
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    // ── Acquisto (solo studente proprietario) ─────────────────────────────────

    /**
     * Avvia un ordine: crea una Transaction PENDING e la salva nella sessione.
     * Lo studente ha {@value #TIMEOUT_MINUTI} minuti per confermare.
     */
    public TransactionBean avviaOrdineAcquisto(SessioneBean sessione, AvvioOrdineBean input)
            throws ControllerException {

        Sessione sm = validaSessione(sessione);

        if (sm.getStudenteCorrente() == null)
            throw new ControllerException("Nessuno studente associato alla sessione.");

        VirtualWallet wallet = sm.getWalletCorrente();
        if (wallet == null)
            throw new ControllerException("Wallet non trovato per lo studente.");

        try {
            Stock stock = StockService.getInstance().ottieniOCreaStock(input.getSimbolo());
            Transaction t = new Transaction(stock, TipoTransazione.BUY, 1, stock.prezzoAttuale());
            sm.setTransazionePending(t);
            sm.setStockCorrente(stock);
            return toTransactionBean(t);
        } catch (Exception e) {
            throw new ControllerException(
                    "Errore nell'avvio dell'ordine per: " + input.getSimbolo(), e);
        }
    }

    /**
     * Conferma l'ordine pending con la quantità scelta dallo studente.
     * Verifica: timeout, saldo, poi delega l'acquisto a VirtualWallet (Expert).
     */
    public TransactionBean confermaAcquisto(SessioneBean sessione, ConfermaAcquistoBean input)
            throws ControllerException {

        Sessione sm = validaSessione(sessione);

        Transaction transazione = sm.getTransazionePending();
        if (transazione == null)
            throw new ControllerException("Nessun ordine pending trovato. Riprova.");

        long minuti = ChronoUnit.MINUTES.between(
                transazione.quando().atZone(java.time.ZoneId.systemDefault()),
                java.time.ZonedDateTime.now(ZoneId.systemDefault())
        );        if (minuti > TIMEOUT_MINUTI) {

            sm.setTransazionePending(null);
            throw new ControllerException(
                    "Il tempo per confermare è scaduto (limite: " + TIMEOUT_MINUTI + " min). Riprova.");
        }

        transazione.impostaQuantita(input.getQuantitaScelta());

        VirtualWallet wallet = sm.getWalletCorrente();
        if (wallet == null)
            throw new ControllerException("Wallet non trovato per lo studente.");

        if (wallet.saldoDisponibile() < transazione.importoTotale())
            throw new ControllerException(
                    "Saldo insufficiente. Disponibile: " + wallet.saldoDisponibile()
                            + ", richiesto: " + transazione.importoTotale());

        DAOFactory factory = DAOFactory.getDAOFactory();
        TransactionDAO transactionDAO = factory.createTransactionDAO();
        WalletPositionDAO posizioneDAO = factory.createWalletPositionDAO();
        PortafoglioDAO walletDAO       = factory.createPortafoglioDAO();

        try {
            boolean posizioneEsisteva = wallet.trovaPosizione(transazione.stock()) != null;
            WalletPosition posizione  = wallet.eseguiAcquisto(
                    transazione.stock(), input.getQuantitaScelta(), transazione.prezzoAlMomento());

            transazione.completaTransazione();
            wallet.aggiungiTransazione(transazione);

            String email = wallet.proprietario().presentaEmail();
            transactionDAO.salvaTransazione(email, transazione);
            if (posizioneEsisteva) posizioneDAO.aggiornaPosizione(email, posizione);
            else                   posizioneDAO.salvaPosizione(email, posizione);
            walletDAO.aggiornaPortafoglio(wallet);

            sm.setTransazionePending(null);
            return toTransactionBean(transazione);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante il salvataggio dell'ordine.", e);
        }
    }

    /** Annulla l'ordine pending best-effort. */
    public void annullaOrdine(SessioneBean sessione) throws ControllerException {
        Sessione sm = validaSessione(sessione);
        sm.setTransazionePending(null);
        sm.setStockCorrente(null);
    }

    // ── Portafoglio ───────────────────────────────────────────────────────────


    public PortafoglioBean ottieniPortafoglio(SessioneBean sessione, UtenteBean input)
            throws ControllerException {

        Sessione sm = validaSessione(sessione);
        DAOFactory factory = DAOFactory.getDAOFactory();
        PortafoglioDAO walletDAO = factory.createPortafoglioDAO();

        try {
            Studente studenteLoggato = sm.getStudenteCorrente();

            // Caso 1 — proprietario: lo studente legge il suo
            if (studenteLoggato != null &&
                    (input == null || input.getEmail().equals(studenteLoggato.presentaEmail()))) {
                VirtualWallet wallet = sm.getWalletCorrente();
                if (wallet == null)
                    wallet = walletDAO.getPortafoglioByEmail(studenteLoggato.presentaEmail());
                return convertiWalletInBean(wallet);
            }

            // Caso 2 — non proprietario: verifica accesso poi legge

            VirtualWallet walletTarget = walletDAO.getPortafoglioByEmail(input.getEmail());
            if (walletTarget == null)
                throw new ControllerException("Portafoglio non trovato per: " + input.getEmail());

            verificaAccesso(sm, walletTarget);
            return convertiWalletInBean(walletTarget);

        } catch (ControllerException e) {
            throw e;
        } catch (DAOException e) {
            throw new ControllerException("Errore nel recupero del portafoglio.", e);
        }
    }

    /**
     * Restituisce lo storico transazioni del soggetto target.
     * Stesse regole di accesso di {@link #ottieniPortafoglio}.
     */
    public List<TransactionBean> ottieniStorico(SessioneBean sessione, UtenteBean input)
            throws ControllerException {

        Sessione sm = validaSessione(sessione);
        DAOFactory factory = DAOFactory.getDAOFactory();
        PortafoglioDAO walletDAO       = factory.createPortafoglioDAO();
        TransactionDAO transactionDAO  = factory.createTransactionDAO();

        try {
            VirtualWallet wallet;
            Studente studenteLoggato = sm.getStudenteCorrente();

            if (studenteLoggato != null &&
                    (input.getEmail() == null
                            || input.getEmail().equals(studenteLoggato.presentaEmail()))) {
                wallet = sm.getWalletCorrente();
                if (wallet == null)
                    wallet = walletDAO.getPortafoglioByEmail(studenteLoggato.presentaEmail());
            } else {
                if (input.getEmail() == null)
                    throw new ControllerException("Nessun target specificato per lo storico.");
                wallet = walletDAO.getPortafoglioByEmail(input.getEmail());
                if (wallet == null)
                    throw new ControllerException("Portafoglio non trovato per: " + input.getEmail());
                verificaAccesso(sm, wallet);
            }

            List<Transaction> transazioni = transactionDAO.getTransazioniWallet(wallet);
            List<TransactionBean> beans = new ArrayList<>();
            if (transazioni != null)
                for (Transaction t : transazioni) beans.add(toTransactionBean(t));
            return beans;

        } catch (ControllerException e) {
            throw e;
        } catch (DAOException e) {
            throw new ControllerException("Errore nel recupero dello storico transazioni.", e);
        }
    }

    // ── Conversione model → bean (usata anche dai graphic controller) ─────────

    /**
     * Converte un VirtualWallet in PortafoglioBean per la view.
     * Esposto pubblicamente perché usato dai graphic controller dopo un acquisto
     * per aggiornare il PortafoglioBean nel Navigator senza un secondo accesso DAO.
     */
    public PortafoglioBean convertiWalletInBean(VirtualWallet wallet) {
        if (wallet == null) return null;

        List<WalletPositionBean> posizioniBeans = new ArrayList<>();
        if (wallet.posizioni() != null) {
            for (WalletPosition p : wallet.posizioni()) {
                StockBean stockBean = new StockBean();
                stockBean.setSimbolo(p.stock().simbolo());
                stockBean.setNomeAzienda(p.stock().nomeAzienda());

                WalletPositionBean wpBean = new WalletPositionBean();
                wpBean.setStock(stockBean);
                wpBean.setQuantita(p.quantita());
                wpBean.setPrezzoMedioAcquisto(p.prezzoMedioAcquisto());
                wpBean.setValoreAttuale(p.valoreAttuale());
                wpBean.setProfittoPerdita(p.valoreAttuale() - p.quantita() * p.prezzoMedioAcquisto());
                posizioniBeans.add(wpBean);
            }
        }

        List<TransactionBean> transazioniBeans = new ArrayList<>();
        if (wallet.transazioni() != null) {
            for (Transaction t : wallet.transazioni()) {
                StockBean stockBean = new StockBean();
                stockBean.setSimbolo(t.stock().simbolo());

                TransactionBean txBean = new TransactionBean();
                txBean.setStock(stockBean);
                txBean.setTipo(t.tipo());
                txBean.setImportoTotale(t.importoTotale());
                txBean.setStato(t.stato());
                txBean.setQuando(t.quando());
                transazioniBeans.add(txBean);
            }
        }

        return new PortafoglioBean(
                wallet.saldoDisponibile(),
                wallet.calcolaTotalePortafoglio(),
                posizioniBeans,
                transazioniBeans);
    }

    private Sessione validaSessione(SessioneBean sessione) throws ControllerException {
        Sessione s = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (s == null) throw new ControllerException("Sessione non valida o scaduta.");
        return s;
    }

    private void verificaAccesso(Sessione sm, VirtualWallet walletTarget)
            throws ControllerException {

        Studente proprietario  = walletTarget.proprietario();
        SchoolClass classeTarget = proprietario.classeFrequentata();

        Professore professore = sm.getProfessorCorrente();
        if (professore != null) {
            if (classeTarget == null ||
                    !classeTarget.teacher().presentaEmail().equals(professore.presentaEmail()))
                throw new ControllerException(
                        "Accesso negato: lo studente non appartiene alle tue classi.");
            return;
        }

        Studente richiedente = sm.getStudenteCorrente();
        if (richiedente != null) {
            SchoolClass classeRichiedente = richiedente.classeFrequentata();
            if (classeTarget == null || classeRichiedente == null ||
                    !classeTarget.nome().equals(classeRichiedente.nome()))
                throw new ControllerException(
                        "Accesso negato: puoi visualizzare solo i portafogli della tua classe.");
            return;
        }

        throw new ControllerException("Accesso negato: nessun utente autenticato.");
    }

    private StockBean toStockBean(Stock s) {
        StockBean bean = new StockBean(s.simbolo(), s.nomeAzienda(), s.settore(), s.prezzoAttuale());
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