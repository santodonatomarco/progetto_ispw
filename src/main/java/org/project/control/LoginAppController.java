package org.project.control;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.dao.classi.SchoolClassDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.CredenzialNonValideException;
import org.project.exceptions.DAOException;
import org.project.ing.classifunzionali.Hasher;
import org.project.ing.enumerations.AuthProvider;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller applicativo per il login.
 * Stateless: nessun campo di istanza.
 * Il bean porta sempre authProvider valorizzato (LOCAL se login classico).
 * La distinzione LOCAL / OAuth viene fatta qui con uno switch sull'enum.
 */
public class LoginAppController {

    // ── Login studente ────────────────────────────────────────────────────────

    public SessioneBean loginStudente(StudenteBean bean)
            throws CredenzialNonValideException, ControllerException {

        DAOFactory factory = DAOFactory.getDAOFactory();
        factory.createSchoolClassDAO();
        StudenteDAO studenteDAO = factory.createStudenteDAO();

        try {
            Studente trovato = studenteDAO.getStudenteByEmail(bean.getEmail());

            if (trovato == null) {
                throw new CredenzialNonValideException("Credenziali non valide.");
            }

            AuthProvider provider = trovato.comeAccede();

            if (provider == AuthProvider.LOCAL &&
                    !trovato.getPasswordHash().equals(Hasher.codifica(bean.getPassword()))) {
                throw new CredenzialNonValideException("Password errata.");
            }

            switch (provider) {
                case LOCAL -> { /* hash già verificato sopra */ }
                case GOOGLE, MICROSOFT -> throw new CredenzialNonValideException(
                        "Questo account usa " + provider + ". Caso d'uso non implementato.");
                default -> throw new CredenzialNonValideException("Provider non supportato.");
            }

            return creaSessioneStudente(trovato, factory);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante il login studente.", e);
        }
    }

    // ── Login professore ──────────────────────────────────────────────────────

    public SessioneBean loginProfessore(ProfessoreBean bean)
            throws CredenzialNonValideException, ControllerException {

        DAOFactory factory = DAOFactory.getDAOFactory();
        ProfessoreDAO professoreDAO = factory.createProfessoreDAO();

        try {
            Professore trovato = professoreDAO.getProfessoreByEmail(bean.getEmail());

            if (trovato == null) {
                throw new CredenzialNonValideException("Credenziali non valide.");
            }

            AuthProvider provider = trovato.comeAccede();

            if (provider == AuthProvider.LOCAL &&
                    !trovato.getPasswordHash().equals(Hasher.codifica(bean.getPassword()))) {
                throw new CredenzialNonValideException("Password errata.");
            }

            switch (provider) {
                case LOCAL -> { /* hash già verificato sopra */ }
                case GOOGLE, MICROSOFT -> throw new CredenzialNonValideException(
                        "Questo account usa " + provider + ". Caso d'uso non implementato.");
                default -> throw new CredenzialNonValideException("Provider non supportato.");
            }

            return creaSessioneProfessore(trovato, factory);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante il login professore.", e);
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    public void logout(SessioneBean sessione) {
        SessionManager.getInstance().cancellaSessione(sessione.getId());
    }

    // ── Metodi privati ────────────────────────────────────────────────────────

    private SessioneBean creaSessioneStudente(Studente studente, DAOFactory factory)
            throws DAOException, ControllerException {

        PortafoglioDAO walletDAO = factory.createPortafoglioDAO();
        VirtualWallet wallet = walletDAO.getPortafoglioByEmail(studente.presentaEmail());

        if (wallet == null) {
            throw new ControllerException("Wallet non trovato per lo studente: dato corrotto.");
        }

        studente.assegnaWallet(wallet);
        Sessione sessione = SessionManager.getInstance().creaSessione(studente);

        StudenteBean studenteBean = new StudenteBean(
                studente.presentaEmail(),
                studente.presentaNome(),
                studente.presentaCognome()
        );

        if (studente.classeFrequentata() != null) {
            studenteBean.setNomeClasse(studente.classeFrequentata().nome());
            studenteBean.setBudgetClasse(studente.classeFrequentata().budgetIniziale());
        }

        studenteBean.resetPassword();

        SessioneBean sessioneBean = new SessioneBean(sessione.getToken(), studenteBean);
        sessioneBean.setPortafoglio(convertiWalletInBean(wallet));
        return sessioneBean;
    }

    private SessioneBean creaSessioneProfessore(Professore professore, DAOFactory factory) throws DAOException {

        Sessione sessione = SessionManager.getInstance().creaSessione(professore);

        ProfessoreBean professoreBean = new ProfessoreBean(
                professore.presentaEmail(),
                professore.presentaNome(),
                professore.presentaCognome()
        );

        SchoolClassDAO classDAO = factory.createSchoolClassDAO();
        factory.createStudenteDAO();

        SessioneBean sessioneBean = new SessioneBean(sessione.getToken(), professoreBean);

        List<SchoolClass> classi = classDAO.getClassiByProfessore(professore);
        List<SchoolClassBean> classiBeans = new ArrayList<>();
        for (SchoolClass c : classi) {
            classiBeans.add(toBean(c));
        }
        sessioneBean.setListaClassi(classiBeans);

        return sessioneBean;
    }

    // ── Conversione model → bean ──────────────────────────────────────────────

    private SchoolClassBean toBean(SchoolClass c) {
        ProfessoreBean profBean = new ProfessoreBean(
                c.teacher().presentaEmail(),
                c.teacher().presentaNome(),
                c.teacher().presentaCognome());
        List<StudenteBean> studentiBeans = new ArrayList<>();
        if (c.studenti() != null) {
            for (Studente s : c.studenti()) {
                studentiBeans.add(new StudenteBean(
                        s.presentaEmail(), s.presentaNome(), s.presentaCognome()));
            }
        }
        return new SchoolClassBean(c.nome(), profBean, c.budgetIniziale(), studentiBeans);
    }

    private PortafoglioBean convertiWalletInBean(VirtualWallet wallet) {
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
                double spesaIniziale = p.quantita() * p.prezzoMedioAcquisto();
                wpBean.setProfittoPerdita(p.valoreAttuale() - spesaIniziale);

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

                transazioniBeans.add(txBean);
            }
        }

        return new PortafoglioBean(
                wallet.saldoDisponibile(),
                wallet.calcolaTotalePortafoglio(),
                posizioniBeans,
                transazioniBeans
        );
    }
}
