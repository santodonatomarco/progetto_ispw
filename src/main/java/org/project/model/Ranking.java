package org.project.model;

import org.project.ing.enumerations.TipoRanking;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ranking {

    private TipoRanking tipo;           // CLASS o GLOBAL
    private SchoolClass schoolClass;    // null se GLOBAL
    private LocalDate settimanaRiferimento;
    private List<RankingEntry> entries;

    // Costruttore per ranking di classe
    public Ranking(SchoolClass schoolClass, LocalDate settimana) {
        this.tipo = TipoRanking.CLASSE;
        this.collegaClasse(schoolClass);
        this.impostaSettimana(settimana);
        this.entries = new ArrayList<>();
    }

    // Costruttore per ranking globale
    public Ranking(LocalDate settimana) {
        this.tipo = TipoRanking.GLOBALE;
        this.schoolClass = null;
        this.impostaSettimana(settimana);
        this.entries = new ArrayList<>();
    }

    public final void collegaClasse(SchoolClass classe) {
        if (classe == null)
            throw new IllegalArgumentException("La classe non può essere nulla.");
        this.schoolClass = classe;
    }

    public final void impostaSettimana(LocalDate settimana) {
        if (settimana == null)
            throw new IllegalArgumentException("La settimana di riferimento è obbligatoria.");
        this.settimanaRiferimento = settimana;
    }

    public final void aggiungiEntry(RankingEntry entry) {
        if (entry == null)
            throw new IllegalArgumentException("L'entry non può essere nulla.");
        this.entries.add(entry);
    }

    public TipoRanking tipo()                   { return tipo; }
    public SchoolClass schoolClass()            { return schoolClass; }
    public LocalDate settimanaRiferimento()     { return settimanaRiferimento; }
    public List<RankingEntry> entries()         { return entries; }
}
