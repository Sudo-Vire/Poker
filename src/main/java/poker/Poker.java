package poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;

// Clase principal del programa Poker Texas Hold'em
public class Poker {
    public static void main(String[] args) {
        Interfaz.mostrarMensaje("Bienvenido al juego de Poker Texas Hold'em!");

        int smallBlind = 10;                                                          // Cantidad para la ciega pequeña inicial
        int bigBlind = 20;                                                            // Cantidad para la ciega grande inicial
        int manosParaAumentarCiegas = 3;                                              // Tras cuántas manos se doblan las ciegas
        Apuesta apuesta = new Apuesta(smallBlind, bigBlind, manosParaAumentarCiegas); // Manejador de apuestas, ciegas y side pots
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
            } else if (!respuesta.equalsIgnoreCase("n")) {
                continue;
            } else {
                Interfaz.mostrarMensaje("Introduce una opción válida");
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
        int n = jugadores.size();
        int[] apuestas = new int[n];                            // Rastrea las apuestas individuales en cada ronda

        // Reparte 2 cartas a cada jugador
        for (Jugador jugador : jugadores) {
            jugador.nuevaMano();
            jugador.recibirCarta(baraja.repartirCarta());
            jugador.recibirCarta(baraja.repartirCarta());
            Interfaz.limpiarPantalla();
        }

        // --- MODIFICADO: ahora las ciegas van al sistema de Side Pots ---
        apuesta.ponerCiegas(jugadores, apuestas);

        boolean finalizarPorAllIn;

        // Pre-Flop
        int primerJugadorPreFlop = (jugadores.size() == 2)
                ? apuesta.getSmallBlindIndex()
                : (apuesta.getBigBlindIndex() + 1) % jugadores.size();
        apuesta.realizarRondaApuestas(jugadores, comunitarias, "Pre-Flop", primerJugadorPreFlop, apuestas);

        finalizarPorAllIn = todosAllInOSoloUnoActivo(jugadores);

        // Flop
        Arrays.fill(apuestas, 0);
        for (int i = 0; i < 3; i++) {
            comunitarias.add(baraja.repartirCarta());
        }
        mostrarCartasComunitarias(comunitarias, "Flop");
        if (!finalizarPorAllIn) {
            apuesta.realizarRondaApuestas(jugadores, comunitarias, "Flop", apuesta.getSmallBlindIndex(), apuestas);
            finalizarPorAllIn = todosAllInOSoloUnoActivo(jugadores);
        }

        // Turn
        Arrays.fill(apuestas, 0);
        comunitarias.add(baraja.repartirCarta());
        mostrarCartasComunitarias(comunitarias, "Turn");
        if (!finalizarPorAllIn) {
            apuesta.realizarRondaApuestas(jugadores, comunitarias, "Turn", apuesta.getSmallBlindIndex(), apuestas);
            finalizarPorAllIn = todosAllInOSoloUnoActivo(jugadores);
        }

        // River
        Arrays.fill(apuestas, 0);
        comunitarias.add(baraja.repartirCarta());
        mostrarCartasComunitarias(comunitarias, "River");
        if (!finalizarPorAllIn) {
            apuesta.realizarRondaApuestas(jugadores, comunitarias, "River", apuesta.getSmallBlindIndex(), apuestas);
        }

        // --- MODIFICADO: showdown ahora utiliza la lista de side pots ---
        showdown(comunitarias, apuesta.getPots());
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

    // Muestra el jugador con más fichas tras la mano y los saldos actuales
    private static void mostrarResumenManoFinal(List<Jugador> jugadores) {
        // --- MODIFICADO: usar streams para evitar warnings sobre maxJugador ---
        jugadores.stream()
                .max(Comparator.comparingInt(Jugador::getSaldo))
                .ifPresent(maxJugador ->
                        Interfaz.mostrarMensaje("El jugador con más fichas tras la mano es: "
                                + maxJugador.getNombre() + " (" + maxJugador.getSaldo() + " fichas)"));

        Interfaz.mostrarMensaje("---- Saldo de todos los jugadores tras la mano ----");
        for (Jugador jugador : jugadores) {
            Interfaz.mostrarMensaje(jugador.getNombre() + ": " + jugador.getSaldo() + " fichas");
        }
        Interfaz.mostrarMensaje("---------------------------------------------");
    }

    // --- MODIFICADO: showdown reparte cada side pot y acumula el total ---
    private static void showdown(List<Baraja.Carta> comunitarias, List<Apuesta.SidePot> pots) {
        java.util.Map<Jugador, Integer> ganancias = new java.util.HashMap<>();

        for (Apuesta.SidePot pot : pots) {
            List<Jugador> candidatos = new ArrayList<>();
            List<List<Baraja.Carta>> combinaciones = new ArrayList<>();
            List<String> nombresJugada = new ArrayList<>();

            for (Jugador j : pot.participantes) {
                if (j.isEnJuego()) {
                    EvaluarManos.ResultadoEvaluacion res = EvaluarManos.evaluarManoCompleta(j.getMano(), comunitarias);
                    combinaciones.add(res.cartasPrincipales);
                    nombresJugada.add(res.nombreJugada);
                    candidatos.add(j);

                    Interfaz.mostrarMensaje(j.getNombre() + " juega " + res.nombreJugada);

                    // Mostrar cartas usadas
                    StringBuilder cartasUsadas = new StringBuilder("Cartas usadas: ");
                    for (Baraja.Carta carta : res.cartasPrincipales) {
                        cartasUsadas.append(carta.toString()).append(" ");
                    }
                    Interfaz.mostrarMensaje(cartasUsadas.toString());
                    Interfaz.mostrarMensaje("");
                }
            }

            if (candidatos.isEmpty()) continue;

            int idxGanador = -1;
            List<Jugador> ganadores = new ArrayList<>();
            for (int i = 0; i < candidatos.size(); i++) {
                if (idxGanador == -1) {
                    idxGanador = i;
                    ganadores.add(candidatos.get(i));
                } else {
                    int cmp = CompararManos.compararManos2(
                            combinaciones.get(i), nombresJugada.get(i),
                            combinaciones.get(idxGanador), nombresJugada.get(idxGanador)
                    );
                    if (cmp > 0) {
                        ganadores.clear();
                        ganadores.add(candidatos.get(i));
                        idxGanador = i;
                    } else if (cmp == 0) {
                        ganadores.add(candidatos.get(i));
                    }
                }
            }

            int premioPorJugador = pot.cantidad / ganadores.size();
            for (Jugador g : ganadores) {
                g.ganar(premioPorJugador);
                ganancias.put(g, ganancias.getOrDefault(g, 0) + premioPorJugador);
            }
        }

        // Mostrar el total de ganancias acumuladas por jugador en esta mano
        for (var entry : ganancias.entrySet()) {
            Jugador j = entry.getKey();
            int totalGanado = entry.getValue();
            Interfaz.mostrarMensaje(j.getNombre() + " gana un total de " + totalGanado + " fichas en esta mano.");
        }

        Apuesta.aumentarCiegas();
    }
}