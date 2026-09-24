package poker;

import org.junit.jupiter.api.Test;
import poker.model.Carta;
import poker.model.Jugador;

import static org.junit.jupiter.api.Assertions.*;

class JugadoresTest {

    @Test
    void crearUnJugador_shouldBeTrue() {
        Jugador jugador = new Jugador("Ana", 1000);

        assertEquals("Ana", jugador.getNombre());
        assertEquals(1000, jugador.getSaldo());
        assertTrue(jugador.isEnJuego());
        assertFalse(jugador.isVaAllIn());
        assertTrue(jugador.getMano().isEmpty());
    }

    @Test
    void crearUnJugador_shouldBeFalse() {
        Jugador jugador = new Jugador("Ana", 1000);

        assertFalse(jugador.isVaAllIn());
        assertFalse(!jugador.isEnJuego());
    }

    @Test
    void recibirCartaYReiniciarLaMano_shouldBeTrue() {
        Jugador jugador = new Jugador("Ana", 1000);
        jugador.recibirCarta(new Carta("Picas", "A"));
        jugador.setVaAllIn(true);
        jugador.setEnJuego(false);

        jugador.nuevaMano();

        assertTrue(jugador.isEnJuego());
        assertFalse(jugador.isVaAllIn());
        assertTrue(jugador.getMano().isEmpty());
    }

    @Test
    void establecerSaldoCero_shouldBeTrue() {
        Jugador jugador = new Jugador("Ana", 1000);

        jugador.setSaldo(0);

        assertEquals(0, jugador.getSaldo());
        assertTrue(jugador.isVaAllIn());
    }

    @Test
    void establecerSaldoPositivo_shouldBeFalse() {
        Jugador jugador = new Jugador("Ana", 1000);

        jugador.setSaldo(500);

        assertFalse(jugador.isVaAllIn());
    }

    @Test
    void ganarFichas_shouldBeTrue() {
        Jugador jugador = new Jugador("Ana", 1000);

        jugador.ganar(250);

        assertEquals(1250, jugador.getSaldo());
    }

    @Test
    void establecerSaldoNegativo_shouldThrowException() {
        Jugador jugador = new Jugador("Ana", 1000);

        assertThrows(IllegalArgumentException.class, () -> jugador.setSaldo(-1));
    }
}
