package org.project.control;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.DAOException;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.*;
import org.project.view.bean.StudenteBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller applicativo per la gestione classe da parte del professore.
 * Responsabilità:
 *  - Crea una nuova classe assegnata al professore loggato
 *  - Imposta/modifica il budget iniziale di una classe esistente
 *  - Legge l'elenco delle classi del professore
 */
public class GestioneClasseAppController {

    /**
     * Crea una nuova classe con il budget dato e la assegna al professore in sessione.
     */
    public SchoolClassBean creaClasse(SessioneBean sessione, String nomeClasse, double budgetIniziale)
            throws ControllerException {

        if (nomeClasse == null || nomeClasse.isBlank())
            throw new ControllerException("Il nome della classe non può essere vuoto.");
        if (budgetIniziale < 0)
            throw new ControllerException("Il budget per la classe non può essere negativo.");

        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Professore professore = sessioneModel.getProfessorCorrente();
        if (professore == null)
            throw new ControllerException("Solo i professori possono creare classi.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO = factory.createSchoolClassDAO();

        try {
            // Verifica che la classe non esista già per questo professore
            SchoolClass esistente = classeDAO.getClasseByNomeEProfessore(nomeClasse, professore);
            if (esistente != null)
                throw new ControllerException("Esiste già una classe \"" + nomeClasse + "\" per questo professore.");

            SchoolClass nuovaClasse = new SchoolClass(nomeClasse, professore);
            nuovaClasse.impostaBudget(budgetIniziale);
            classeDAO.salvaClasse(nuovaClasse);

            return toBean(nuovaClasse);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante la creazione della classe.", e);
        }
    }

    /**
     * Aggiorna il budget iniziale di una classe esistente
     * e aggiorna RETROATTIVAMENTE i portafogli di tutti gli studenti iscritti.
     */

    public SchoolClassBean impostaBudget(SessioneBean sessione, String nomeClasse, double nuovoBudget)
            throws ControllerException {

        if (nuovoBudget < 0)
            throw new ControllerException("Il budget non può essere negativo.");

        // Validazione sessione e recupero professore — estratti per ridurre nesting
        Professore professore = validaSessioneEOttieniProfessore(sessione,
                "Solo i professori possono modificare il budget.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO    = factory.createSchoolClassDAO();
        StudenteDAO   studenteDAO  = factory.createStudenteDAO();
        PortafoglioDAO portafoglioDAO = factory.createPortafoglioDAO();

        try {
            SchoolClass classe = classeDAO.getClasseByNomeEProfessore(nomeClasse, professore);
            if (classe == null)
                throw new ControllerException("Classe \"" + nomeClasse + "\" non trovata.");

            double differenza = nuovoBudget - classe.budgetIniziale();

            classe.impostaBudget(nuovoBudget);
            classeDAO.salvaClasse(classe);

            if (differenza != 0)
                aggiornaPortafogli(studenteDAO, portafoglioDAO, classe, differenza);

            sincronizzaSessione(sessione, nomeClasse, nuovoBudget);

            return toBean(classe);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante l'aggiornamento del budget o dei portafogli.", e);
        }
    }

// ── Metodi privati estratti ───────────────────────────────────────────────────

    /**
     * Valida la sessione e restituisce il professore corrente.
     * Centralizza la logica ripetuta in tutti i metodi del controller.
     */
    private Professore validaSessioneEOttieniProfessore(SessioneBean sessione, String msgErroreProfessore)
            throws ControllerException {

        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Professore professore = sessioneModel.getProfessorCorrente();
        if (professore == null)
            throw new ControllerException(msgErroreProfessore);

        return professore;
    }

    /**
     * Aggiorna retroattivamente i portafogli di tutti gli studenti della classe
     * in base alla variazione di budget (delta positivo = accredito, negativo = addebito).
     */
    private void aggiornaPortafogli(StudenteDAO studenteDAO, PortafoglioDAO portafoglioDAO,
                                    SchoolClass classe, double differenza)
            throws DAOException {

        List<Studente> studenti = studenteDAO.getStudentiClasse(classe);
        if (studenti == null) return;

        for (Studente s : studenti) {
            VirtualWallet wallet = portafoglioDAO.getPortafoglioByEmail(s.presentaEmail());
            if (wallet == null) continue;

            applicaDeltaWallet(wallet, differenza);
            portafoglioDAO.salvaPortafoglio(wallet);
        }
    }

    /**
     * Applica la variazione di budget al singolo portafoglio.
     * Se il delta è negativo e il saldo è insufficiente, azzera il portafoglio
     * invece di andare in negativo.
     */
    private void applicaDeltaWallet(VirtualWallet wallet, double differenza) {
        if (differenza > 0) {
            wallet.accreditaSaldo(differenza);
        } else {
            double daScalare = Math.abs(differenza);
            wallet.scalaSaldo(Math.min(wallet.saldoDisponibile(), daScalare));
        }
    }

    /**
     * Se la classe modificata è quella correntemente in sessione,
     * aggiorna anche il riferimento in-memory per mantenerlo consistente.
     */
    private void sincronizzaSessione(SessioneBean sessione, String nomeClasse, double nuovoBudget) {
        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null) return;

        SchoolClass classeInSessione = sessioneModel.getClasseCorrente();
        if (classeInSessione != null && classeInSessione.nome().equals(nomeClasse))
            classeInSessione.impostaBudget(nuovoBudget);
    }

    /**
     * Pre-aggiunge uno studente alla classe del professore come "pending".
     * Lo studente viene creato con email e classe, senza password né nome:
     * completerà la registrazione autonomamente (controller da implementare)
     * Vincoli:
     * - L'email non deve appartenere a uno studente già registrato
     * - La classe deve esistere e appartenere al professore in sessione
     */
    public StudenteBean aggiungiStudente(SessioneBean sessione, String emailStudente, String nomeClasse)
            throws ControllerException {

        if (emailStudente == null || emailStudente.isBlank() || !emailStudente.contains("@"))
            throw new ControllerException("Email non valida.");
        if (nomeClasse == null || nomeClasse.isBlank())
            throw new ControllerException("Nome classe non valido.");

        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Professore professore = sessioneModel.getProfessorCorrente();
        if (professore == null)
            throw new ControllerException("Solo i professori possono aggiungere studenti.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO = factory.createSchoolClassDAO();
        StudenteDAO studenteDAO = factory.createStudenteDAO();

        try {
            // 1. Verifica che la classe esista e appartenga a questo professore
            SchoolClass classe = classeDAO.getClasseByNomeEProfessore(nomeClasse, professore);
            if (classe == null)
                throw new ControllerException("Classe \"" + nomeClasse + "\" non trovata.");

            // 2. Verifica che l'email non sia già registrata
            Studente esistente = studenteDAO.getStudenteByEmail(emailStudente.trim().toLowerCase());
            if (esistente != null)
                throw new ControllerException("Esiste già uno studente con questa email, dunque riprova.");

            // 3. Crea lo studente pending: solo email e classe, il nome arriverà alla registrazione
            Studente pending = new Studente(
                    emailStudente.trim().toLowerCase(), "—", "—",
                    org.project.ing.enumerations.AuthProvider.LOCAL);
            pending.iscriviClasse(classe);

            studenteDAO.salvaStudente(pending);

            StudenteBean bean = new StudenteBean(pending.presentaEmail(), "—", "—");
            bean.setNomeClasse(nomeClasse);
            return bean;

        } catch (DAOException e) {
            throw new ControllerException("Errore durante l'aggiunta in persistenza dello studente.", e);
        }
    }

    /**
     * Recupera la lista delle classi del professore loggato.
     */
    public List<SchoolClassBean> getClassiDelProfessore(SessioneBean sessione) throws ControllerException {
        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Professore professore = sessioneModel.getProfessorCorrente();
        if (professore == null)
            throw new ControllerException("Solo i professori possono accedere alle classi.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO = factory.createSchoolClassDAO();

        try {
            List<SchoolClass> classi = classeDAO.getClassiByProfessore(professore);
            List<SchoolClassBean> beans = new ArrayList<>();
            for (SchoolClass c : classi) beans.add(toBean(c));
            return beans;
        } catch (DAOException e) {
            throw new ControllerException("Errore nel recupero delle classi.", e);
        }
    }

    /**
     * Recupera gli studenti di una classe specifica del professore loggato.
     * Usato dal professore in GestioneClasse per vedere la lista e aprire i portafogli.
     */
    public List<StudenteBean> getStudentiDellaClasseProfessore(SessioneBean sessione, String nomeClasse)
            throws ControllerException {

        Professore professore = validaSessioneEOttieniProfessore(sessione,
                "Solo i professori possono visualizzare gli studenti.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO = factory.createSchoolClassDAO();
        StudenteDAO studenteDAO  = factory.createStudenteDAO();

        try {
            SchoolClass classe = classeDAO.getClasseByNomeEProfessore(nomeClasse, professore);
            if (classe == null)
                throw new ControllerException("Classe \"" + nomeClasse + "\" non trovata.");

            List<Studente> studenti = studenteDAO.getStudentiClasse(classe);
            List<StudenteBean> beans = new ArrayList<>();
            if (studenti != null) {
                for (Studente s : studenti) {
                    StudenteBean b = new StudenteBean(s.presentaEmail(), s.presentaNome(), s.presentaCognome());
                    b.setNomeClasse(nomeClasse);
                    beans.add(b);
                }
            }
            return beans;
        } catch (DAOException e) {
            throw new ControllerException("Errore nel recupero degli studenti.", e);
        }
    }

    /**
     * Recupera gli studenti della stessa classe dello studente loggato.
     * Usato da ElencoStudenti per permettere allo studente di vedere i compagni.
     */

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
}