package poker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class JugadoresActivosTest {

    // ------------------------------------
    // Caso 1: Dos jugadores activos
    // Esperado: TRUE (Todos activos)
    // ------------------------------------
    @Test
    void testTodosConSaldoActivos_True() {
        Jugador j1 = new Jugador("Ana", 1000);
        Jugador j2 = new Jugador("Luis", 1000);

        j1.enJuego=true;
        j2.enJuego=true;

        boolean resultado = invokeTodosAllInOSoloUnoActivo(List.of(j1, j2));
        assertFalse(resultado);
    }

    // ------------------------------------
    // Caso 1: Dos jugadores activos
    // Esperado: True (1 activo)
    // ------------------------------------
    @Test
    void testTodosConSaldoActivos_False() {
        Jugador j1 = new Jugador("Ana", 1000);
        Jugador j2 = new Jugador("Luis", 1000);

        j1.enJuego=true;
        j2.enJuego=false;

        boolean resultado = invokeTodosAllInOSoloUnoActivo(List.of(j1, j2));
        assertTrue(resultado);
    }

    // ------------------------------------
    // Caso 2: Solo un jugador activo
    // Esperado: TRUE (la mano termina porque solo queda uno)
    // ------------------------------------
    @Test
    void testSoloUnJugadorActivo_True() {
        Jugador j3 = new Jugador("Ana", 1000);
        Jugador j4 = new Jugador("Luis", 1000);

        j3.enJuego=true;
        j4.enJuego=false;

        boolean resultado = invokeTodosAllInOSoloUnoActivo(List.of(j3, j4));
        assertTrue(resultado);
    }

    // ------------------------------------
    // Caso 3: Todos están all-in
    // Esperado: TRUE (nadie puede apostar más)
    // ------------------------------------
    @Test
    void testTodosAllIn_True() {
        Jugador j5 = new Jugador("Ana", 0);
        Jugador j6 = new Jugador("Luis", 0);

        j5.enJuego=true;
        j5.setVaAllIn(true);

        j6.enJuego=true;
        j6.setVaAllIn(true);

        boolean resultado = invokeTodosAllInOSoloUnoActivo(List.of(j5, j6));
        assertTrue(resultado);
    }

    private boolean invokeTodosAllInOSoloUnoActivo(List<Jugador> jugadores) {
        try {
            var metodo = Poker.class.getDeclaredMethod("todosAllInOSoloUnoActivo", List.class);
            metodo.setAccessible(true);
            return (boolean) metodo.invoke(null, jugadores);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}