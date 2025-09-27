package poker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class CartasTest {
    @Test
    void BarajaTiene52CartasDistintasTest() {
        Baraja baraja = new Baraja();
        Set<String> cartasUnicas = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            Baraja.Carta carta = baraja.repartirCarta();
            assertNotNull(carta, "No debería repartir una carta nula");
            cartasUnicas.add(carta.toString());
        }
        assertEquals(52, cartasUnicas.size(), "La baraja debe tener 52 cartas únicas");
    }

    @Test
    void RepartirMasDe52CartasBarajaTest() {
        Baraja baraja = new Baraja();
        for (int i = 0; i < 52; i++) {
            baraja.repartirCarta(); // gasta toda la baraja
        }
        // La siguiente carta debería repartirse sin problemas
        assertDoesNotThrow(baraja::repartirCarta,
                "Repartir más de 52 cartas debería barajar y no lanzar excepción");
    }

    @Test
    void ComparacionCartasTest() {
        Baraja.Carta as = new Baraja.Carta("Picas", "A");
        Baraja.Carta tres = new Baraja.Carta("Tréboles", "3");

        assertTrue(as.compareTo(tres) > 0, "El As debería ser mayor que el Rey");
        assertTrue(tres.compareTo(as) < 0, "El 3 debería ser menor que el As");
    }
}
