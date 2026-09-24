package poker;

import org.junit.jupiter.api.Test;
import poker.model.Baraja;
import poker.model.Carta;
import poker.service.BarajaService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CartasTest {

    private final BarajaService barajaService = new BarajaService();

    @Test
    void crearUnaBarajaCompleta_shouldBeTrue() {
        Baraja baraja = barajaService.crearBarajaCompleta();

        Set<String> cartas = new HashSet<>();
        for (int i = 0; i < baraja.getTamanio(); i++) {
            cartas.add(baraja.obtenerCarta(i).toString());
        }

        assertEquals(52, baraja.getTamanio());
        assertEquals(52, cartas.size());
        assertEquals(0, baraja.getIndice());
    }

    @Test
    void repartirLaCantidadSolicitada_shouldBeTrue() {
        Baraja baraja = barajaService.crearBarajaCompleta();

        List<Carta> cartas = barajaService.repartirCartas(baraja, 5);

        assertEquals(5, cartas.size());
        assertTrue(cartas.stream().allMatch(carta -> carta != null));
        assertEquals(5, baraja.getIndice());
    }

    @Test
    void repartirDespuesDeAgotarLaBaraja_shouldBeTrue() {
        Baraja baraja = barajaService.crearBarajaCompleta();

        barajaService.repartirCartas(baraja, 52);
        Carta siguiente = assertDoesNotThrow(() -> barajaService.repartirCarta(baraja));

        assertNotNull(siguiente);
        assertEquals(1, baraja.getIndice());
    }

    @Test
    void barajarUnaBarajaConCartasRepartidas_shouldBeTrue() {
        Baraja baraja = barajaService.crearBarajaCompleta();
        barajaService.repartirCartas(baraja, 10);

        barajaService.barajar(baraja);

        assertEquals(0, baraja.getIndice());
    }

    @Test
    void crearUnaCarta_shouldBeTrue() {
        Carta as = new Carta("Picas", "A");
        Carta rey = new Carta("Picas", "K");

        assertEquals("Picas", as.getPalo());
        assertEquals("A", as.getValor());
        assertEquals(14, as.getValorNumerico());
        assertEquals("A de Picas", as.toString());
        assertTrue(as.compareTo(rey) > 0);
    }

    @Test
    void recibirUnaCartaNula_shouldThrowException() {
        poker.model.Jugador jugador = new poker.model.Jugador("Ana", 1000);

        assertThrows(IllegalArgumentException.class, () -> jugador.recibirCarta(null));
    }
}
