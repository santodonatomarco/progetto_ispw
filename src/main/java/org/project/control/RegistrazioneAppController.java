package org.project.control;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.CredenzialNonValideException;
import org.project.exceptions.DAOException;
import org.project.ing.classifunzionali.Hasher;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;

public class RegistrazioneAppController {

    // ── Registrazione studente ────────────────────────────────────────────────

    /**
     * Registrazione studente.
     * Il bean deve contenere: email, nome, cognome, nomeClasse.
     * - Locale:  password valorizzata, authProvider null
     * - OAuth:   authProvider valorizzato, password ignorata
     */
    public SessioneBean registraStudente(StudenteBean bean)
            throws CredenzialNonValideException, ControllerException {

        DAOFactory factory = DAOFactory.getDAOFactory();
        StudenteDAO studenteDAO = factory.createStudenteDAO();

        try {
            // 1. Cerca lo studente pending per email
            Studente pending = studenteDAO.getStudenteByEmail(bean.getEmail());

            if (pending == null) {
                throw new CredenzialNonValideException(
                        "Nessun account pending trovato per questa email. " +
                                "Chiedi al tuo professore di aggiungerti alla classe.");
            }

            // 2. Verifica che la classe dichiarata corrisponda a quella assegnata dal professore
            if (pending.classeFrequentata() == null ||
                    !pending.classeFrequentata().nome().equals(bean.getNomeClasse())) {
                throw new CredenzialNonValideException(
                        "La classe indicata non corrisponde a quella associata al tuo account.");
            }

            // 3. Completa il profilo in base al tipo di autenticazione
            if (bean.getAuthProvider() == null) {
                // Registrazione locale
                if (!(pending instanceof AutenticazioneLocale)) {
                    throw new CredenzialNonValideException(
                            "Questo account è registrato con OAuth. Usa Google o Microsoft.");
                }
                AutenticazioneLocale locale = (AutenticazioneLocale) pending;
                locale.inserisciHashPassword(Hasher.codifica(bean.getPassword()));
            } else {
                // Registrazione OAuth
                if (!(pending instanceof AutenticazioneOAuth)) {
                    throw new CredenzialNonValideException(
                            "Questo account è registrato localmente. Usa email e password.");
                }
                AutenticazioneOAuth oauth = (AutenticazioneOAuth) pending;
                if (oauth.ottieniProvider() != bean.getAuthProvider()) {
                    throw new CredenzialNonValideException(
                            "Provider non corrispondente: " + oauth.ottieniProvider());
                }
            }

            // Nome e cognome arrivano dal bean (locale) o dal provider OAuth
            pending.chiamaNome(bean.getNome());
            pending.chiamaCognome(bean.getCognome());

            // 4. Persisti le modifiche (sovrascrive il pending con il profilo completo)
            studenteDAO.salvaStudente(pending);

            // 5. Apri la sessione
            Sessione sessione = SessionManager.getInstance().creaSessione(pending);

            StudenteBean studenteBean = new StudenteBean(
                    pending.presentaEmail(),
                    pending.presentaNome(),
                    pending.presentaCognome()
            );
            studenteBean.resetPassword();

            return new SessioneBean(sessione.getToken(), studenteBean);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante la registrazione dello studente.", e);
        }
    }

    // ── Registrazione professore ──────────────────────────────────────────────

    /**
     * Registrazione professore.
     * Il bean deve contenere: email, nome, cognome.
     * - Locale:  password valorizzata, authProvider null
     * - OAuth:   authProvider valorizzato, password ignorata
     */
    public SessioneBean registraProfessore(ProfessoreBean bean)
            throws CredenzialNonValideException, ControllerException {

        DAOFactory factory = DAOFactory.getDAOFactory();
        ProfessoreDAO professoreDAO = factory.createProfessoreDAO();

        try {
            // Verifica che non esista già un account con questa email
            Professore esistente = professoreDAO.getProfessoreByEmail(bean.getEmail());
            if (esistente != null) {
                throw new CredenzialNonValideException(
                        "Esiste già un account registrato con questa email.");
            }

            // Crea il professore in base al tipo di autenticazione
            Professore nuovo;
            if (bean.getAuthProvider() == null) {
                ProfessoreLocale locale = new ProfessoreLocale(
                        bean.getEmail(), bean.getNome(), bean.getCognome());
                locale.inserisciHashPassword(Hasher.codifica(bean.getPassword()));
                nuovo = locale;
            } else {
                nuovo = new ProfessoreOAuth(
                        bean.getEmail(), bean.getNome(), bean.getCognome(),
                        bean.getAuthProvider());
            }

            // Persisti il nuovo professore
            professoreDAO.salvaProfessore(nuovo);

            Sessione sessione = SessionManager.getInstance().creaSessione(nuovo);

            ProfessoreBean professoreBean = new ProfessoreBean(
                    nuovo.presentaEmail(),
                    nuovo.presentaNome(),
                    nuovo.presentaCognome()
            );

            return new SessioneBean(sessione.getToken(), professoreBean);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante la registrazione del professore.", e);
        }
    }
}