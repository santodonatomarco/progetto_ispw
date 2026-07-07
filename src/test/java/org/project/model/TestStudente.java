package org.project.model;

import org.junit.jupiter.api.Test;
import org.project.ing.enumerations.AuthProvider;

import static org.junit.jupiter.api.Assertions.*;

class TestStudente {

    @Test
    void testIscriviClasseEAmici() {
        Studente s = new Studente("s1@uni.it", "Mario", "Neri", AuthProvider.LOCAL);
        Professore p = new Professore("prof@uni.it", "Anna", "Verdi", AuthProvider.LOCAL);
        SchoolClass sc = new SchoolClass("2B", p);

        s.iscriviClasse(sc);
        assertEquals(sc, s.classeFrequentata());

        Studente amico = new Studente("s2@uni.it", "Luca", "Blu", AuthProvider.LOCAL);
        s.aggiungAmico(amico);
        assertTrue(s.presentaAmici().contains(amico));

    }

    @Test
    void testAssegnaWallet() {
        Studente s = new Studente("s1@uni.it", "Mario", "Neri", AuthProvider.LOCAL);
        VirtualWallet w = new VirtualWallet(s, 100.0);
        s.assegnaWallet(w);
        assertEquals(w, s.portafoglio());
    }
}

