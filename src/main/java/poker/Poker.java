package poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Clase principal del programa Poker Texas Hold'em
public class Poker {
    public static void main(String[] args) {
        Interfaz.mostrarMensaje("Bienvenido al juego de Poker Texas Hold'em!");

        int smallBlind = 10;                                                          // Cantidad para la ciega pequeña inicial
        int bigBlind = 20;                                                            // Cantidad para la ciega grande inicial
        int manosParaAumentarCiegas = 3;                                              // Tras cuántas manos se doblan las ciegas
        Apuesta apuesta = new Apuesta(smallBlind, bigBlind, manosParaAumentarCiegas); // Manejador de apuestas y ciegas
        Baraja baraja = new Baraja();                                                 // Baraja del juego
        List<Jugador> jugadores = new ArrayList<>();                                  // Lista de jugadores

        // Pide la cantidad de jugadores
        int numJugadores = Interfaz.leerNumero("¿Cuántos jugadores van a jugar? (2-10): ", 2, 10);
        for (int i = 0; i < numJugadores; i++) {
            int numJugador = i + 1;
            Interfaz.mostrarMensaje("Jugador " + numJugador + ", introduce tu nombre");
            String nombre = Interfaz.leerLinea(); // Lee el nombre del jugador
            int saldoInicial = 1000;              // Saldo inicial asignado
            jugadores.add(new Jugador(nombre, saldoInicial));
        }

        apuesta.asignarPosicionesIniciales(jugadores); // Asigna dealer, ciega pequeña y grande

        int manoActual = 1;
        // Bucle de juego principal (cada iteración es una mano de póker)
        while (true) {
            Interfaz.mostrarMensaje("Comenzando la mano número: " + manoActual);
            apuesta.mostrarCiegasActuales();
            jugarMano(jugadores, baraja, apuesta);                         // Se juega la mano completa
            apuesta.rotarPosiciones(jugadores);                            // Rota dealer/ciegas para la siguiente mano

            // --- ELIMINAR Y NOTIFICAR ELIMINADOS AL FINAL DE LA MANO ---
            List<Jugador> eliminados = jugadores.stream().filter(j -> j.getSaldo() <= 0).toList();
            if (!eliminados.isEmpty()) {
                for (Jugador eliminado : eliminados) {
                    Interfaz.mostrarMensaje("El jugador " + eliminado.getNombre() + " ha sido eliminado.");
                }
            }
            jugadores.removeIf(j -> j.getSaldo() <= 0);

            // Si solo queda uno, termina el juego
            if (jugadores.size() == 1) {
                Interfaz.mostrarMensaje("¡" + jugadores.get(0).getNombre() + " es el ganador del torneo! ¡Enhorabuena!");
                break;
            }
            if (jugadores.isEmpty()) {
                Interfaz.mostrarMensaje("No quedan jugadores, termina el torneo.");
                break;
            }

            Interfaz.mostrarMensaje("¿Desean jugar otra mano? (s/n)");
            String respuesta = Interfaz.leerLinea();
            if (!respuesta.equalsIgnoreCase("s")) {             // Si la respuesta es "n", termina el juego
                break;
            }
            manoActual++;
        }

        // Al terminar la sesión, muestra el saldo final de todos los jugadores
        for (Jugador jugador : jugadores) {
            Interfaz.mostrarMensaje(jugador.getNombre() + " termina con un saldo de: " + jugador.getSaldo());
        }

        Interfaz.cerrarScanner();
    }

    // Ejecuta el ciclo completo de una mano
    private static void jugarMano(List<Jugador> jugadores, Baraja baraja, Apuesta apuesta) {
        List<Baraja.Carta> comunitarias = new ArrayList<>();    // Cartas comunitarias en la mesa
        int[] pozo = {0};                                       // Pozo de fichas por mano
        int n = jugadores.size();
        int[] apuestas = new int[n];                            // Rastrea las apuestas individuales en cada ronda

        // Reparte 2 cartas a cada jugador
        for (Jugador jugador : jugadores) {
            jugador.nuevaMano();
            jugador.recibirCarta(baraja.repartirCarta());
            jugador.recibirCarta(baraja.repartirCarta());
            Interfaz.limpiarPantalla();
        }
        apuesta.ponerCiegas(jugadores, pozo, apuestas);

        boolean finalizarPorAllIn;

        // Pre-Flop
        int primerJugadorPreFlop = (jugadores.size() == 2)
                ? apuesta.getSmallBlindIndex()
                : (apuesta.getBigBlindIndex() + 1) % jugadores.size();
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Pre-Flop", primerJugadorPreFlop, apuestas);

        finalizarPorAllIn = todosAllInOSoloUnoActivo(jugadores);

        // Flop
        Arrays.fill(apuestas, 0);
        for (int i = 0; i < 3; i++) {
            comunitarias.add(baraja.repartirCarta());
        }
        mostrarCartasComunitarias(comunitarias, "Flop");
        if (!finalizarPorAllIn) {
            apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Flop", apuesta.getSmallBlindIndex(), apuestas);
            finalizarPorAllIn = todosAllInOSoloUnoActivo(jugadores);
        }

        // Turn
        Arrays.fill(apuestas, 0);
        comunitarias.add(baraja.repartirCarta());
        mostrarCartasComunitarias(comunitarias, "Turn");
        if (!finalizarPorAllIn) {
            apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Turn", apuesta.getSmallBlindIndex(), apuestas);
            finalizarPorAllIn = todosAllInOSoloUnoActivo(jugadores);
        }

        // River
        Arrays.fill(apuestas, 0);
        comunitarias.add(baraja.repartirCarta());
        mostrarCartasComunitarias(comunitarias, "River");
        if (!finalizarPorAllIn) {
            apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "River", apuesta.getSmallBlindIndex(), apuestas);
        }

        showdown(jugadores, comunitarias, pozo);
        mostrarResumenManoFinal(jugadores);
    }

    // Muestra las cartas comunitarias de la ronda actual
    private static void mostrarCartasComunitarias(List<Baraja.Carta> comunitarias, String fase) {
        StringBuilder mensaje = new StringBuilder(fase + ": ");
        for (Baraja.Carta carta : comunitarias) {
            mensaje.append(carta.toString()).append(" | ");
        }
        if (!comunitarias.isEmpty()) {
            mensaje.setLength(mensaje.length() - 3);
        }
        Interfaz.mostrarMensaje(mensaje.toString());
    }

    // Devuelve true si todos los jugadores activos están all-in, o solo queda uno activo
    private static boolean todosAllInOSoloUnoActivo(List<Jugador> jugadores) {
        int activos = 0;
        boolean alguienPuedeApostar = false;
        for (Jugador j : jugadores) {
            if (j.isEnJuego()) {
                if (!j.isVaAllIn() && j.getSaldo() > 0) {
                    alguienPuedeApostar = true;
                }
                activos++;
            }
        }
        if (!alguienPuedeApostar) return true;
        // Si solo queda uno activo la mano se termina automáticamente también
        return activos <= 1;
    }

    // Muestra el ganador de la mano y el saldo final de todos los jugadores
    private static void mostrarResumenManoFinal(List<Jugador> jugadores) {
        // Busca el jugador con más fichas después del showdown
        Jugador maxJugador = null;
        int maxFichas = -1;
        for (Jugador j : jugadores) {
            if (j.getSaldo() > maxFichas) {
                maxFichas = j.getSaldo();
                maxJugador = j;
            }
        }
        if (maxJugador != null) {
            Interfaz.mostrarMensaje("El jugador con más fichas tras la mano es: " + maxJugador.getNombre() + " (" + maxJugador.getSaldo() + " fichas)");
        }
        Interfaz.mostrarMensaje("---- Saldo de todos los jugadores tras la mano ----");
        for (Jugador jugador : jugadores) {
            Interfaz.mostrarMensaje(jugador.getNombre() + ": " + jugador.getSaldo() + " fichas");
        }
        Interfaz.mostrarMensaje("---------------------------------------------");
    }

    // Método que muestra las manos y determina el ganador
    private static void showdown(List<Jugador> jugadores, List<Baraja.Carta> comunitarias, int[] pozo) {
        Jugador ganador;

        List<Jugador> jugadoresEnJuego = new ArrayList<>();
        List<String> nombresMano = new ArrayList<>();
        List<List<Baraja.Carta>> combinacionesPrincipales = new ArrayList<>();

        for (Jugador jugador : jugadores) {
            if (jugador.isEnJuego())
                jugadoresEnJuego.add(jugador);
        }

        // Evalúa las manos de todos los jugadores activos y muestra sus jugadas
        for (Jugador jugador : jugadoresEnJuego) {
            EvaluarManos.ResultadoEvaluacion resultado = EvaluarManos.evaluarManoCompleta(jugador.getMano(), comunitarias);
            String mano = resultado.nombreJugada;
            List<Baraja.Carta> mejores5Cartas = resultado.cartasPrincipales;

            nombresMano.add(mano);
            combinacionesPrincipales.add(mejores5Cartas);

            Interfaz.mostrarMensaje(jugador.getNombre() + " tiene: " + mano);

            StringBuilder cartasEnLinea = new StringBuilder("Cartas usadas: ");
            for (Baraja.Carta carta : mejores5Cartas) {
                cartasEnLinea.append(carta.toString()).append(" ");
            }
            Interfaz.mostrarMensaje(cartasEnLinea.toString());
            Interfaz.mostrarMensaje("");
        }

        // Determinar ganador correctamente
        int idxGanador = -1;
        Jugador posibleGanador = null;
        for (int idxActual = 0; idxActual < jugadoresEnJuego.size(); idxActual++) {
            if (idxGanador == -1 || CompararManos.compararManos2(
                    combinacionesPrincipales.get(idxActual), nombresMano.get(idxActual),
                    combinacionesPrincipales.get(idxGanador), nombresMano.get(idxGanador)
            ) > 0) {
                idxGanador = idxActual;
                posibleGanador = jugadoresEnJuego.get(idxActual);
            }
        }
        ganador = posibleGanador;

        // Comprueba el empate y reparte las fichas
        boolean empate = false;
        for (int i = 0; i < jugadoresEnJuego.size(); i++) {
            if (i == idxGanador) continue;
            if (CompararManos.compararManos2(
                    combinacionesPrincipales.get(i), nombresMano.get(i),
                    combinacionesPrincipales.get(idxGanador), nombresMano.get(idxGanador)
            ) == 0) {
                empate = true;
                break;
            }
        }

        if (ganador != null) {
            if (empate) {
                Interfaz.mostrarMensaje("Empate. Las fichas del pozo se devuelven a los jugadores.");
                int fichasPorJugador = pozo[0] / jugadoresEnJuego.size();
                for (Jugador jugador : jugadoresEnJuego) {
                    jugador.ganar(fichasPorJugador);
                }
                pozo[0] = 0;
            } else {
                Interfaz.mostrarMensaje(ganador.getNombre() + " gana con " + nombresMano.get(idxGanador) + " y se lleva " + pozo[0] + " fichas.");
                ganador.ganar(pozo[0]);
            }
        } else {
            Interfaz.mostrarMensaje("No hay ganador en esta mano.");
        }
        Apuesta.aumentarCiegas();
    }
}