package poker;

import java.util.Arrays;
import java.util.List;

public class Apuesta {
    // Variables estáticas para las ciegas y el contador de manos
    private static int smallBlind;
    private static int bigBlind;
    private static int contadorManos;

    // Índices para dealer, small blind y big blind en la lista de jugadores
    private int dealerIndex;
    private int smallBlindIndex;
    private int bigBlindIndex;

    public Apuesta(int smallBlind, int bigBlind, int manosParaAumentarCiegas) {
        if (smallBlind <= 0 || bigBlind <= 0 || manosParaAumentarCiegas <= 0) {
            throw new IllegalArgumentException("Los valores de las ciegas y el número de manos deben ser positivos.");
        }
        Apuesta.smallBlind = smallBlind;
        Apuesta.bigBlind = bigBlind;
        contadorManos = 0;
        dealerIndex = -1;
        smallBlindIndex = -1;
        bigBlindIndex = -1;
    }

    public int getSmallBlindIndex() {
        return smallBlindIndex;
    }

    public int getBigBlindIndex() {
        return bigBlindIndex;
    }

    /**
     * Asigna las posiciones iniciales de dealer, small blind y big blind en la lista de jugadores.
     */
    public void asignarPosicionesIniciales(List<Jugador> jugadores) {
        if (jugadores.size() < 2) {
            throw new IllegalStateException("No hay suficientes jugadores para asignar posiciones.");
        }
        dealerIndex = 0;
        smallBlindIndex = (dealerIndex + 1) % jugadores.size();
        bigBlindIndex = (dealerIndex + 2) % jugadores.size();
        Interfaz.mostrarMensaje(jugadores.get(smallBlindIndex).getNombre() + " es la Ciega Pequeña.");
        Interfaz.mostrarMensaje(jugadores.get(bigBlindIndex).getNombre() + " es la Ciega Grande.");
    }

    /**
     * Rota las posiciones de dealer, small blind y big blind para la siguiente mano.
     */
    public void rotarPosiciones(List<Jugador> jugadores) {
        if (jugadores.size() < 2) {
            throw new IllegalStateException("No hay suficientes jugadores para rotar posiciones.");
        }
        dealerIndex = (dealerIndex + 1) % jugadores.size();
        smallBlindIndex = (dealerIndex + 1) % jugadores.size();
        bigBlindIndex = (dealerIndex + 2) % jugadores.size();
        Interfaz.mostrarMensaje(jugadores.get(smallBlindIndex).getNombre() + " es la nueva Ciega Pequeña.");
        Interfaz.mostrarMensaje(jugadores.get(bigBlindIndex).getNombre() + " es la nueva Ciega Grande.");
    }

    /**
     * Descuenta las ciegas a los jugadores correspondientes y suma al pozo.
     */
    public void ponerCiegas(List<Jugador> jugadores, int[] pozo, int[] apuestas) {
        // Ciega pequeña
        if (smallBlindIndex >= 0 && smallBlindIndex < jugadores.size()) {
            Jugador sb = jugadores.get(smallBlindIndex);
            if (sb.isEnJuego() && sb.getSaldo() > 0) {
                int sbMonto = Math.min(sb.getSaldo(), smallBlind);
                sb.setSaldo(sb.getSaldo() - sbMonto);
                pozo[0] += sbMonto;
                apuestas[smallBlindIndex] = sbMonto;
                Interfaz.mostrarMensaje(sb.getNombre() + " pone la ciega pequeña de " + sbMonto);
            }
        }
        // Ciega grande
        if (bigBlindIndex >= 0 && bigBlindIndex < jugadores.size()) {
            Jugador bb = jugadores.get(bigBlindIndex);
            if (bb.isEnJuego() && bb.getSaldo() > 0) {
                int bbMonto = Math.min(bb.getSaldo(), bigBlind);
                bb.setSaldo(bb.getSaldo() - bbMonto);
                pozo[0] += bbMonto;
                apuestas[bigBlindIndex] = bbMonto;
                Interfaz.mostrarMensaje(bb.getNombre() + " pone la ciega grande de " + bbMonto);
            }
        }
    }

    /**
     * Muestra los valores actuales de las ciegas.
     */
    public void mostrarCiegasActuales() {
        Interfaz.mostrarMensaje("Ciega pequeña actual: " + smallBlind + ", Ciega grande actual: " + bigBlind);
    }

    /**
     * Aumenta las ciegas cada cierto número fijo de manos.
     */
    public static void aumentarCiegas() {
        contadorManos++;
        int manosParaAumentarCiegas = 3;
        if (contadorManos % manosParaAumentarCiegas == 0) {
            smallBlind *= 2;
            bigBlind *= 2;
            Interfaz.mostrarMensaje("Las ciegas han aumentado. Nueva ciega pequeña: " + smallBlind + ", Nueva ciega grande: " + bigBlind);
        }
    }

    /**
     * Controla la ronda de apuestas de acuerdo a la fase actual del juego.
     */
    public void realizarRondaApuestas(List<Jugador> jugadores, int[] pozo, List<Baraja.Carta> comunitarias, String fase, int primerJugador, int[] apuestas) {
        int n = jugadores.size();
        int[] apuestaActual = new int[1];
        apuestaActual[0] = (fase.equals("Pre-Flop")) ? bigBlind : 0;

        boolean huboApuestaOSubida = false;
        boolean[] yaActuo = new boolean[n];
        int jugadoresActivos;

        do {
            for (int offset = 0; offset < n; offset++) {
                int i = (primerJugador + offset) % n;
                Jugador jugador = jugadores.get(i);

                if (!jugador.isEnJuego() || jugador.getSaldo() <= 0) continue;

                // Control en Pre-Flop y fases posteriores según reglas de turnos
                if (fase.equals("Pre-Flop") && apuestas[i] == apuestaActual[0] && apuestaActual[0] != 0 && yaActuo[i]) continue;
                if (!fase.equals("Pre-Flop") && yaActuo[i]) continue;

                mostrarCartasComunitarias(comunitarias, fase);
                jugador.mostrarMano();
                Interfaz.mostrarMensaje(jugador.getNombre() + " tiene " + jugador.getSaldo() + " fichas.");
                Interfaz.mostrarMensaje("La apuesta actual es: " + apuestaActual[0]);
                Interfaz.mostrarMensaje("Tu aporte esta ronda: " + apuestas[i]);

                int cantidadPorIgualar = apuestaActual[0] - apuestas[i];
                boolean accionValida = false;

                // Espera a que el jugador haga una acción válida (apostar, igualar, subir, retirarse)
                while (!accionValida) {
                    int apuestaPrev = apuestaActual[0];
                    accionValida = Interfaz.aspr(jugador, cantidadPorIgualar, pozo, apuestas, i, apuestaActual, jugadores, fase, bigBlind);
                    if (apuestaActual[0] == -999) {
                        return; // La ronda termina si todos, menos uno, se retiran
                    }
                    if (apuestaActual[0] > apuestaPrev) {
                        huboApuestaOSubida = true;
                        Arrays.fill(yaActuo, false); // Reinicia los turnos si alguien sube la apuesta
                    }
                }
                yaActuo[i] = true;
            }
            // Cuenta el número de jugadores activos para la ronda
            jugadoresActivos = 0;
            for (int i = 0; i < n; i++) {
                if (jugadores.get(i).isEnJuego() && jugadores.get(i).getSaldo() > 0) {
                    jugadoresActivos++;
                }
            }
        } while ((huboApuestaOSubida && jugadoresActivos > 1) || !todosHanActuado(yaActuo, jugadores));
    }

    // Verifica que todos los jugadores activos hayan actuado en la ronda.
    private boolean todosHanActuado(boolean[] yaActuo, List<Jugador> jugadores) {
        for (int i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).isEnJuego() && jugadores.get(i).getSaldo() > 0) {
                if (!yaActuo[i]) return false;
            }
        }
        return true;
    }

    // Muestra por consola las cartas comunitarias y en qué fase están.
    private void mostrarCartasComunitarias(List<Baraja.Carta> comunitarias, String fase) {
        StringBuilder mensaje = new StringBuilder(fase + ": ");
        for (Baraja.Carta carta : comunitarias) {
            mensaje.append(carta).append(" | ");
        }
        if (!comunitarias.isEmpty()) {
            mensaje.setLength(mensaje.length() - 3);
        }
        Interfaz.mostrarMensaje(mensaje.toString());
    }
}