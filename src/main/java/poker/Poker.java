package poker;

import java.util.ArrayList;
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
            jugarMano(jugadores, baraja, apuesta);                          // Se juega la mano completa
            apuesta.rotarPosiciones(jugadores);                             // Se rota dealer/ciegas para la siguiente mano
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

        // Pre-Flop
        int primerJugadorPreFlop = (jugadores.size() == 2)
                ? apuesta.getSmallBlindIndex()
                : (apuesta.getBigBlindIndex() + 1) % jugadores.size();
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Pre-Flop", primerJugadorPreFlop, apuestas);
        if (terminoMano(pozo, jugadores)) return; // Sale si solo queda un jugador

        // Flop
        apuestas = new int[n];
        for (int i = 0; i < 3; i++) {
            comunitarias.add(baraja.repartirCarta());
        }
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Flop", apuesta.getSmallBlindIndex(), apuestas);
        if (terminoMano(pozo, jugadores)) return;

        // Turn
        apuestas = new int[n];
        comunitarias.add(baraja.repartirCarta());
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Turn", apuesta.getSmallBlindIndex(), apuestas);
        if (terminoMano(pozo, jugadores)) return;

        // River
        apuestas = new int[n];
        comunitarias.add(baraja.repartirCarta());
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "River", apuesta.getSmallBlindIndex(), apuestas);
        if (terminoMano(pozo, jugadores)) return;

        // Showdown
        showdown(jugadores, comunitarias, pozo);
    }

    // Determina si la mano terminó
    private static boolean terminoMano(int[] pozo, List<Jugador> jugadores) {
        int activos = 0;
        for (Jugador j : jugadores) {
            if (j.isEnJuego() && j.getSaldo() > 0) activos++;
        }
        return activos <= 1 || pozo[0] == 0;
    }

    // Método que muestra las manos y determina el ganador
    private static void showdown(List<Jugador> jugadores, List<Baraja.Carta> comunitarias, int[] pozo) {
        Jugador ganador = null;
        String mejorMano = "";

        List<Jugador> jugadoresEnJuego = new ArrayList<>();
        for (Jugador jugador : jugadores) {
            if (jugador.isEnJuego()) jugadoresEnJuego.add(jugador);
        }

        // Evalúa las manos de todos los jugadores activos y muestra sus jugadas
        for (Jugador jugador : jugadoresEnJuego) {
            EvaluarManos.ResultadoEvaluacion resultado = EvaluarManos.evaluarManoCompleta(jugador.getMano(), comunitarias);
            String mano = resultado.nombreJugada;
            List<Baraja.Carta> mejores5Cartas = resultado.cartasPrincipales;

            Interfaz.mostrarMensaje(jugador.getNombre() + " tiene: " + mano);

            StringBuilder cartasEnLinea = new StringBuilder("Cartas usadas: ");
            for (Baraja.Carta carta : mejores5Cartas) {
                cartasEnLinea.append(carta.toString()).append(" ");
            }
            Interfaz.mostrarMensaje(cartasEnLinea.toString());
            Interfaz.mostrarMensaje("");

            if (ganador == null || CompararManos.compararManos(mano, mejorMano, jugador.getMano(), ganador.getMano(), comunitarias) > 0) {
                ganador = jugador;
                mejorMano = mano;
            }
        }

        // Comprueba el empate y reparte las fichas
        if (ganador != null) {
            boolean empate = false;
            for (Jugador jugador : jugadoresEnJuego) {
                if (jugador != ganador) {
                    String manoJugador = EvaluarManos.evaluarManoCompleta(jugador.getMano(), comunitarias).toString();
                    if (CompararManos.compararManos(mejorMano, manoJugador, ganador.getMano(), jugador.getMano(), comunitarias) == 0) {
                        empate = true;
                        break;
                    }
                }
            }

            if (empate) {
                Interfaz.mostrarMensaje("Empate. Las fichas del pozo se devuelven a los jugadores.");
                int fichasPorJugador = pozo[0] / jugadoresEnJuego.size();
                for (Jugador jugador : jugadoresEnJuego) {
                    jugador.ganar(fichasPorJugador);
                }
                pozo[0] = 0;
            } else {
                Interfaz.mostrarMensaje(ganador.getNombre() + " gana con " + mejorMano + " y se lleva " + pozo[0] + " fichas.");
                ganador.ganar(pozo[0]);
            }
        } else {
            Interfaz.mostrarMensaje("No hay ganador en esta mano.");
        }
        Apuesta.aumentarCiegas();
    }
}