package poker;

import org.junit.jupiter.api.Test;
import poker.model.Carta;
import poker.service.CompararManos;
import poker.service.EvaluarManos;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluarManosTest {

    @Test
    void evaluarCincoCartasConTrioYPareja_shouldBeTrue() {
        List<Carta> cartas = List.of(
                carta("A", "Corazones"), carta("A", "Picas"), carta("A", "Diamantes"),
                carta("K", "Corazones"), carta("K", "Picas"));

        EvaluarManos.ResultadoEvaluacion resultado =
                EvaluarManos.evaluarCombinacion(cartas);

        assertEquals("Full", resultado.nombreJugada);
        assertEquals(5, resultado.cartasPrincipales.size());
    }

    @Test
    void evaluarCincoCartasDeMismoPaloConValoresAltos_shouldBeTrue() {
        List<Carta> cartas = List.of(
                carta("A", "Picas"), carta("K", "Picas"), carta("Q", "Picas"),
                carta("J", "Picas"), carta("10", "Picas"));

        EvaluarManos.ResultadoEvaluacion resultado =
                EvaluarManos.evaluarCombinacion(cartas);

        assertEquals("Escalera Real", resultado.nombreJugada);
    }

    @Test
    void evaluarSieteCartasConTrioYPareja_shouldBeTrue() {
        List<Carta> mano = List.of(carta("A", "Picas"), carta("A", "Corazones"));
        List<Carta> comunitarias = List.of(
                carta("A", "Diamantes"), carta("K", "Picas"), carta("K", "Corazones"),
                carta("2", "Treboles"), carta("7", "Diamantes"));

        EvaluarManos.ResultadoEvaluacion resultado =
                EvaluarManos.evaluarManoCompleta(mano, comunitarias);

        assertEquals("Full", resultado.nombreJugada);
    }

    @Test
    void compararUnFullConUnTrio_shouldBeTrue() {
        EvaluarManos.ResultadoEvaluacion full = EvaluarManos.evaluarCombinacion(List.of(
                carta("A", "Picas"), carta("A", "Corazones"), carta("A", "Diamantes"),
                carta("K", "Picas"), carta("K", "Corazones")));
        EvaluarManos.ResultadoEvaluacion trio = EvaluarManos.evaluarCombinacion(List.of(
                carta("A", "Picas"), carta("A", "Corazones"), carta("A", "Diamantes"),
                carta("K", "Picas"), carta("Q", "Corazones")));

        assertTrue(CompararManos.compararManos2(
                full.cartasPrincipales, full.nombreJugada,
                trio.cartasPrincipales, trio.nombreJugada) > 0);
    }

    @Test
    void compararUnTrioConUnFull_shouldBeFalse() {
        EvaluarManos.ResultadoEvaluacion full = EvaluarManos.evaluarCombinacion(List.of(
                carta("A", "Picas"), carta("A", "Corazones"), carta("A", "Diamantes"),
                carta("K", "Picas"), carta("K", "Corazones")));
        EvaluarManos.ResultadoEvaluacion trio = EvaluarManos.evaluarCombinacion(List.of(
                carta("A", "Picas"), carta("A", "Corazones"), carta("A", "Diamantes"),
                carta("K", "Picas"), carta("Q", "Corazones")));

        assertFalse(CompararManos.compararManos2(
                trio.cartasPrincipales, trio.nombreJugada,
                full.cartasPrincipales, full.nombreJugada) > 0);
    }

    private static Carta carta(String valor, String palo) {
        return new Carta(palo, valor);
    }
}
