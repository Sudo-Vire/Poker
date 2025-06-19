package poker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Poker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Interfaz.mostrarMensaje("Bienvenido al juego de Poker Texas Hold'em!");

        // Configuración inicial del juego con valores personal
        int smallBlind = 10; 
        int bigBlind = 20; 
        int manosParaAumentarCiegas = 4; 
        Apuesta apuesta = new Apuesta(smallBlind, bigBlind, manosParaAumentarCiegas);
        Baraja baraja = new Baraja();
        List<Jugador> jugadores = new ArrayList<>();

        int numJugadores = Interfaz.leerNumero("¿Cuántos jugadores van a jugar? (2-10): ", 2, 10);
        for (int i = 0; i < numJugadores; i++) {
            Interfaz.mostrarMensaje("Jugador " + (i + 1) + ", introduce tu nombre");
            String nombre = Interfaz.leerLinea();
            // El saldo inicial es fijo en 1000
            int saldoInicial = 1000;
            jugadores.add(new Jugador(nombre, saldoInicial));
        }

        // Bucle principal del juego
        int manoActual = 1;
        while (true) {
            Interfaz.mostrarMensaje("Comenzando la mano número: " + manoActual);
            apuesta.mostrarCiegasActuales();
            jugarMano(jugadores, baraja, apuesta);
            apuesta.aumentarCiegas();

            // Preguntar si quieren jugar otra mano
            Interfaz.mostrarMensaje("¿Desean jugar otra mano? (s/n)");
            String respuesta = Interfaz.leerLinea();
            if (!respuesta.equalsIgnoreCase("s")) {
                break;
            }
            manoActual++;
        }

        // Mostrar saldos finales
        for (Jugador jugador : jugadores) {
            Interfaz.mostrarMensaje(jugador.getNombre() + " termina con un saldo de: " + jugador.getSaldo());
        }

        Interfaz.cerrarScanner();
        scanner.close();
    }

    private static void jugarMano(List<Jugador> jugadores, Baraja baraja, Apuesta apuesta) {
        List<Baraja.Carta> comunitarias = new ArrayList<>();
        int[] pozo = {0};

        // Repartir cartas iniciales
        for (Jugador jugador : jugadores) {
            jugador.nuevaMano();
            jugador.recibirCarta(baraja.repartirCarta());
            jugador.recibirCarta(baraja.repartirCarta());
            // Limpiar el terminal
            for (int i = 0; i < 10; i++) {
                Interfaz.mostrarMensaje("");
            }
        }

        // Ronda de apuestas pre-flop
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Pre-Flop");

        // Flop
        for (int i = 0; i < 3; i++) {
            comunitarias.add(baraja.repartirCarta());
        }
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Flop");

        // Turn
        comunitarias.add(baraja.repartirCarta());
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "Turn");

        // River
        comunitarias.add(baraja.repartirCarta());
        apuesta.realizarRondaApuestas(jugadores, pozo, comunitarias, "River");

        // Showdown
        showdown(jugadores, comunitarias, pozo);
    }

    private static void showdown(List<Jugador> jugadores, List<Baraja.Carta> comunitarias, int[] pozo) {
        Jugador ganador = null;
        String mejorMano = "";

        List<Jugador> jugadoresEnJuego = new ArrayList<>();
        for (Jugador jugador : jugadores) {
            if (jugador.isEnJuego()) {
                jugadoresEnJuego.add(jugador);
            }
        }

        // Evaluar y mostrar las manos de los jugadores en juego
        for (Jugador jugador : jugadoresEnJuego) {
            EvaluarManos.ResultadoEvaluacion resultado = EvaluarManos.evaluarManoCompleta(jugador.getMano(), comunitarias);
            String mano = resultado.nombreJugada;
            List<Baraja.Carta> mejores5Cartas = resultado.cartasPrincipales;

            Interfaz.mostrarMensaje(jugador.getNombre() + " tiene: " + mano);

            // Mostrar las 5 mejores cartas del jugador
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


        // Determinar si hay un ganador o un empate
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
                // Devolver las fichas apostadas a cada jugador en juego
                int fichasPorJugador = pozo[0] / jugadoresEnJuego.size();
                for (Jugador jugador : jugadoresEnJuego) {
                    jugador.ganar(fichasPorJugador);
                }
                pozo[0] = 0; // Reiniciar el pozo
            } else {
                Interfaz.mostrarMensaje(ganador.getNombre() + " gana con " + mejorMano + " y se lleva " + pozo[0] + " fichas.");
                ganador.ganar(pozo[0]);
            }
        } else {
            Interfaz.mostrarMensaje("No hay ganador en esta mano.");
        }
    }
}