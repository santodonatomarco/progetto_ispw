package org.project.model;

import org.junit.jupiter.api.Test;
import org.project.ing.enumerations.AuthProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestMessage {

    @Test
    void testMessage() {
        Studente mittente = new Studente("paolo@gmail.com","Paolo","Rossi", AuthProvider.LOCAL);
        Professore destinatario = new Professore("luca@gmail.com","Luca","Bianchi", AuthProvider.LOCAL);
        String testo = "Salve professore, quando posso venire a ricevimento?";
        String expected = "Salve professore, quando posso venire a ricevimento?";
        Message message = new Message(mittente, destinatario, testo);
        assertEquals(expected, message.getTesto());
    }
}
