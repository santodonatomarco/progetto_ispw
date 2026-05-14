package org.project.control;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.CredenzialNonValideException;
import org.project.exceptions.DAOException;
import org.project.ing.classifunzionali.Hasher;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;

/**
 * Controller applicativo per il login (locale e OAuth).
 *
 * Stateless: nessun campo di istanza.
 * Riceve sempre StudenteBean o ProfessoreBean dalla view:
 *   - login locale:  bean con email + password, authProvider null
 *   - login OAuth:   bean con email + authProvider, password ignorata
 */
public class LoginAppController {

    // ── Login studente ────────────────────────────────────────────────────────

    public SessioneBean loginStudente(StudenteBean bean)
            throws CredenzialNonValideException, ControllerException {

        DAOFactory factory = DAOFactory.getDAOFactory();
        StudenteDAO studenteDAO = factory.createStudenteDAO();

        try {
            Studente trovato = studenteDAO.getStudenteByEmail(bean.getEmail());

            if (trovato == null) {
                throw new CredenzialNonValideException("Credenziali non valide.");
            }

            if (bean.getAuthProvider() == null) {
                // Login locale: verifica password
                if (!(trovato instanceof AutenticazioneLocale)) {
                    throw new CredenzialNonValideException(
                            "Questo account usa OAuth. Accedi con Google o Microsoft.");
                }
                AutenticazioneLocale locale = (AutenticazioneLocale) trovato;
                if (!locale.passwordHash().equals(Hasher.codifica(bean.getPassword()))) {
                    throw new CredenzialNonValideException("Password errata.");
                }
            } else {
                // Login OAuth: verifica provider
                if (!(trovato instanceof AutenticazioneOAuth)) {
                    throw new CredenzialNonValideException(
                            "Questo account usa email e password. Accedi localmente.");
                }
                AutenticazioneOAuth oauth = (AutenticazioneOAuth) trovato;
                if (oauth.ottieniProvider() != bean.getAuthProvider()) {
                    throw new CredenzialNonValideException(
                            "Questo account è registrato con: " + oauth.ottieniProvider());
                }
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

            if (bean.getAuthProvider() == null) {
                // Login locale
                if (!(trovato instanceof AutenticazioneLocale)) {
                    throw new CredenzialNonValideException(
                            "Questo account usa OAuth. Accedi con Google o Microsoft.");
                }
                AutenticazioneLocale locale = (AutenticazioneLocale) trovato;
                if (!locale.passwordHash().equals(Hasher.codifica(bean.getPassword()))) {
                    throw new CredenzialNonValideException("Password errata.");
                }
            } else {
                // Login OAuth
                if (!(trovato instanceof AutenticazioneOAuth)) {
                    throw new CredenzialNonValideException(
                            "Questo account usa email e password. Accedi localmente.");
                }
                AutenticazioneOAuth oauth = (AutenticazioneOAuth) trovato;
                if (oauth.ottieniProvider() != bean.getAuthProvider()) {
                    throw new CredenzialNonValideException(
                            "Questo account è registrato con: " + oauth.ottieniProvider());
                }
            }

            return creaSessioneProfessore(trovato);

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
        studenteBean.resetPassword();

        return new SessioneBean(sessione.getToken(), studenteBean);
    }

    private SessioneBean creaSessioneProfessore(Professore professore) {

        Sessione sessione = SessionManager.getInstance().creaSessione(professore);

        ProfessoreBean professoreBean = new ProfessoreBean(
                professore.presentaEmail(),
                professore.presentaNome(),
                professore.presentaCognome()
        );

        return new SessioneBean(sessione.getToken(), professoreBean);
    }
}