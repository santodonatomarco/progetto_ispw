package org.project.model;

import org.junit.jupiter.api.Test;
import org.project.ing.enumerations.AuthProvider;
import org.project.ing.enumerations.Ruolo;

import static org.junit.jupiter.api.Assertions.*;

class TestProfessore {

    @Test
    void testPasswordHashAndRuolo() {
        Professore p = new Professore("prof@uni.it", "Anna", "Verdi", AuthProvider.LOCAL);
        p.impostaPasswordHash("hash123");
        assertEquals("hash123", p.getPasswordHash());
        assertEquals(Ruolo.PROFESSORE, p.haRuolo());
    }


    @Test
    void testGestioneClassi() {
        Professore p = new Professore("prof@uni.it", "Anna", "Verdi", AuthProvider.LOCAL);
        SchoolClass sc = new SchoolClass("3A", p);
        p.aggiungiClasse(sc);
        assertTrue(p.presentaClassiInsegnate().contains(sc));
    }
}

