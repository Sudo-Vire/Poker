package poker;

import org.junit.jupiter.api.Test;
import poker.model.Jugador;
import poker.service.ApuestaService;
import poker.service.JugadorService;
import poker.view.ConsolaView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApuestaServiceTest {

    @Test
    void asignarPosicionesYPonerCiegas_shouldBeTrue() {
        ApuestaService apuestas = servicio("");
        List<Jugador> jugadores = List.of(
                new Jugador("Ana", 1000),
                new Jugador("Luis", 1000),
                new Jugador("Eva", 1000));
        int[] aportes = new int[3];

        apuestas.asignarPosicionesIniciales(jugadores);
        apuestas.ponerCiegas(jugadores, aportes);

        assertEquals(1, apuestas.getSmallBlindIndex());
        assertEquals(2, apuestas.getBigBlindIndex());
        assertEquals(990, jugadores.get(1).getSaldo());
        assertEquals(980, jugadores.get(2).getSaldo());
        assertEquals(30, apuestas.getPots().stream()
                .mapToInt(ApuestaService.SidePot::getCantidad).sum());
    }

    @Test
    void pasarEIgualarEnUnaRonda_shouldBeFalse() {
        ApuestaService apuestas = servicio("1\n1\n");
        List<Jugador> jugadores = List.of(
                new Jugador("Ana", 1000),
                new Jugador("Luis", 1000));
        int[] aportes = new int[2];
        apuestas.asignarPosicionesIniciales(jugadores);
        apuestas.ponerCiegas(jugadores, aportes);

        boolean terminada = apuestas.realizarRondaApuestas(
                jugadores, List.of(), "Pre-Flop",
                apuestas.getSmallBlindIndex(), aportes);

        assertFalse(terminada);
        assertEquals(40, aportes[0] + aportes[1]);
    }

    @Test
    void rotarPosicionesDespuesDeUnaMano_shouldBeTrue() {
        ApuestaService apuestas = servicio("");
        List<Jugador> jugadores = List.of(
                new Jugador("Ana", 1000),
                new Jugador("Luis", 1000),
                new Jugador("Eva", 1000));

        apuestas.asignarPosicionesIniciales(jugadores);
        apuestas.rotarPosiciones(jugadores);

        assertEquals(2, apuestas.getSmallBlindIndex());
        assertEquals(0, apuestas.getBigBlindIndex());
    }

    @Test
    void jugadorConSaldoInsuficienteParaLaCiega_shouldBeTrue() {
        ApuestaService apuestas = servicio("");
        List<Jugador> jugadores = List.of(
                new Jugador("Ana", 1000),
                new Jugador("Luis", 5),
                new Jugador("Eva", 1000));
        int[] aportes = new int[3];

        apuestas.asignarPosicionesIniciales(jugadores);
        apuestas.ponerCiegas(jugadores, aportes);

        assertEquals(0, jugadores.get(1).getSaldo());
        assertTrue(jugadores.get(1).isVaAllIn());
        assertEquals(5, aportes[1]);
        assertTrue(apuestas.getPots().stream()
                .mapToInt(ApuestaService.SidePot::getCantidad)
                .sum() >= 5);
    }

    @Test
    void jugadorConSaldoSuficienteParaLaCiega_shouldBeFalse() {
        ApuestaService apuestas = servicio("");
        List<Jugador> jugadores = List.of(
                new Jugador("Ana", 1000),
                new Jugador("Luis", 1000),
                new Jugador("Eva", 1000));
        int[] aportes = new int[3];

        apuestas.asignarPosicionesIniciales(jugadores);
        apuestas.ponerCiegas(jugadores, aportes);

        assertFalse(jugadores.get(1).isVaAllIn());
        assertFalse(jugadores.get(2).isVaAllIn());
    }

    private static ApuestaService servicio(String entrada) {
        ConsolaView view = new ConsolaView(
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(new ByteArrayOutputStream()));
        return new ApuestaService(10, 20, 3, view, new JugadorService());
    }
}
