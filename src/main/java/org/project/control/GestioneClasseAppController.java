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
import org.project.view.bean.AggiungiStudenteBean;
import org.project.view.bean.OttieniStudentiClasseBean;

import java.util.ArrayList;
import java.util.List;


public class GestioneClasseAppController {

    public SchoolClassBean creaClasse(SessioneBean sessione, ClasseBean input)
            throws ControllerException {

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
            SchoolClass esistente = classeDAO.getClasseByNomeEProfessore(input.getNomeDellaClasse(), professore);
            if (esistente != null)
                throw new ControllerException("Esiste già una classe \"" + input.getNomeDellaClasse() + "\" per questo professore.");

            SchoolClass nuovaClasse = new SchoolClass(input.getNomeDellaClasse(), professore);
            nuovaClasse.impostaBudget(input.getBudgetIniziale());
            classeDAO.salvaClasse(nuovaClasse);

            return toBean(nuovaClasse);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante la creazione della classe.", e);
        }
    }


    public SchoolClassBean impostaBudget(SessioneBean sessione, ClasseBean inputClass, ImpostaBudgetBean inputBudget)
            throws ControllerException {

        Professore professore = validaSessioneEOttieniProfessore(sessione,
                "Solo i professori possono modificare il budget.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO    = factory.createSchoolClassDAO();
        StudenteDAO   studenteDAO  = factory.createStudenteDAO();
        PortafoglioDAO portafoglioDAO = factory.createPortafoglioDAO();

        try {
            SchoolClass classe = classeDAO.getClasseByNomeEProfessore(inputClass.getNomeDellaClasse(), professore);
            if (classe == null)
                throw new ControllerException("Classe \"" + inputClass.getNomeDellaClasse() + "\" non trovata.");

            double differenza = inputBudget.getNuovoBudget() - classe.budgetIniziale();

            classe.impostaBudget(inputBudget.getNuovoBudget());
            classeDAO.salvaClasse(classe);

            if (differenza != 0)
                aggiornaPortafogli(studenteDAO, portafoglioDAO, classe, differenza);

            sincronizzaSessione(sessione, inputClass.getNomeDellaClasse(), inputBudget.getNuovoBudget());

            return toBean(classe);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante l'aggiornamento del budget o dei portafogli.", e);
        }
    }

// metodi privati per sonarcloud

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


    private void applicaDeltaWallet(VirtualWallet wallet, double differenza) {
        if (differenza > 0) {
            wallet.accreditaSaldo(differenza);
        } else {
            double daScalare = Math.abs(differenza);
            wallet.scalaSaldo(Math.min(wallet.saldoDisponibile(), daScalare));
        }
    }


    private void sincronizzaSessione(SessioneBean sessione, String nomeClasse, double nuovoBudget) {
        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null) return;

        SchoolClass classeInSessione = sessioneModel.getClasseCorrente();
        if (classeInSessione != null && classeInSessione.nome().equals(nomeClasse))
            classeInSessione.impostaBudget(nuovoBudget);
    }


    public StudenteBean aggiungiStudente(SessioneBean sessione, AggiungiStudenteBean input)
            throws ControllerException {

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
            SchoolClass classe = classeDAO.getClasseByNomeEProfessore(input.getNomeClasse(), professore);
            if (classe == null)
                throw new ControllerException("Classe \"" + input.getNomeClasse() + "\" non trovata.");

            // 2. Verifica che l'email non sia già registrata
            Studente esistente = studenteDAO.getStudenteByEmail(input.getEmailStudente());
            if (esistente != null)
                throw new ControllerException("Esiste già uno studente con questa email, dunque riprova.");

            // 3. Crea lo studente pending: solo email e classe, il nome arriverà alla registrazione
            Studente pending = new Studente(
                    input.getEmailStudente(), "—", "—",
                    org.project.ing.enumerations.AuthProvider.LOCAL);
            pending.iscriviClasse(classe);

            studenteDAO.salvaStudente(pending);

            StudenteBean bean = new StudenteBean(pending.presentaEmail(), "—", "—");
            bean.setNomeClasse(input.getNomeClasse());
            return bean;

        } catch (DAOException e) {
            throw new ControllerException("Errore durante l'aggiunta in persistenza dello studente.", e);
        }
    }

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


    public List<StudenteBean> getStudentiDellaClasseProfessore(SessioneBean sessione, OttieniStudentiClasseBean input)
            throws ControllerException {

        Professore professore = validaSessioneEOttieniProfessore(sessione,
                "Solo i professori possono visualizzare gli studenti.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO = factory.createSchoolClassDAO();
        StudenteDAO studenteDAO  = factory.createStudenteDAO();

        try {
            SchoolClass classe = classeDAO.getClasseByNomeEProfessore(input.getNomeClasse(), professore);
            if (classe == null)
                throw new ControllerException("Classe \"" + input.getNomeClasse() + "\" non trovata.");

            List<Studente> studenti = studenteDAO.getStudentiClasse(classe);
            List<StudenteBean> beans = new ArrayList<>();
            if (studenti != null) {
                for (Studente s : studenti) {
                    StudenteBean b = new StudenteBean(s.presentaEmail(), s.presentaNome(), s.presentaCognome());
                    b.setNomeClasse(input.getNomeClasse());
                    beans.add(b);
                }
            }
            return beans;
        } catch (DAOException e) {
            throw new ControllerException("Errore nel recupero degli studenti.", e);
        }
    }



    // Conversione model → bean per controller applicativo

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