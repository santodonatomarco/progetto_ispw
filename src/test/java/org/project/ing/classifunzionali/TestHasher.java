package org.project.ing.classifunzionali;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.project.ing.classifunzionali.Hasher.codifica;


class TestHasher {
    @Test
    void testHash() {
        String noMod = "mario_rossi";
        String attesa = "issor_oiram";
        String attuale = codifica(noMod);
        assertEquals(attesa, attuale);
    }

}
