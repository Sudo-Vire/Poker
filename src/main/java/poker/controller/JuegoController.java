package poker.controller;

import poker.model.Baraja;
import poker.model.Carta;
import poker.model.Jugador;
import poker.service.ApuestaService;
import poker.service.BarajaService;
import poker.service.CompararManos;
import poker.service.EvaluarManos;
import poker.service.JugadorService;
import poker.view.ConsolaView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de la aplicación para un torneo de Texas Hold'em.
 */
public class JuegoController {
    private static final int SALDO_INICIAL = 1000;
    private final ConsolaView view;
    private final BarajaService barajaService;
    private final JugadorService jugadorService;
    private final ApuestaService apuestaService;

    public JuegoController(ConsolaView view, BarajaService barajaService,
                           JugadorService jugadorService, ApuestaService apuestaService) {
        if (view == null || barajaService == null || jugadorService == null || apuestaService == null) {
            throw new IllegalArgumentException("Las dependencias no pueden ser nulas");
        }
        this.view = view;
        this.barajaService = barajaService;
        this.jugadorService = jugadorService;
        this.apuestaService = apuestaService;
    }

    public void iniciar() {
        view.mostrarMensaje("Bienvenido al juego de Poker Texas Hold'em!");
        List<Jugador> jugadores = crearJugadores();
        if (jugadores.size() < 2) {
            return;
        }

        Baraja baraja = barajaService.crearBarajaCompleta();
        apuestaService.asignarPosicionesIniciales(jugadores);
        int manoActual = 1;
        boolean continuar = true;

        while (continuar && jugadores.size() > 1) {
            view.mostrarMensaje("Comenzando la mano número: " + manoActual);
            apuestaService.mostrarCiegasActuales();
            jugarMano(jugadores, baraja);

            eliminarJugadoresSinSaldo(jugadores);
            if (jugadores.size() == 1) {
                view.mostrarMensaje("¡" + jugadores.get(0).getNombre()
                        + " es el ganador del torneo! ¡Enhorabuena!");
                break;
            }
            if (jugadores.isEmpty()) {
                view.mostrarMensaje("No quedan jugadores, termina el torneo.");
                break;
            }

            apuestaService.rotarPosiciones(jugadores);
            continuar = view.preguntarSiContinua();
            manoActual++;
        }
        mostrarSaldosFinales(jugadores);
    }

    private List<Jugador> crearJugadores() {
        int jugadoresHumanos = view.leerNumero("¿Cuántos jugadores humanos van a jugar? (0-10): ", 0, 10);
        Integer maquinasIntroducidas =
                view.leerNumeroOpcional("¿Cuántas máquinas van a jugar? (0-10): ", 0, 10);
        int maquinas = maquinasIntroducidas == null ? 0 : maquinasIntroducidas;
        while (jugadoresHumanos + maquinas < 2 || jugadoresHumanos + maquinas > 10) {
            view.mostrarMensaje("La mesa debe tener entre 2 y 10 jugadores en total.");
            jugadoresHumanos = view.leerNumero("¿Cuántos jugadores humanos van a jugar? (0-10): ", 0, 10);
            maquinas = view.leerNumero("¿Cuántas máquinas van a jugar? (0-10): ", 0, 10);
        }
        List<Jugador> jugadores = new ArrayList<>();
        for (int i = 0; i < jugadoresHumanos; i++) {
            view.mostrarMensaje("Jugador " + (i + 1) + ", introduce tu nombre");
            jugadores.add(new Jugador(view.leerLinea(), SALDO_INICIAL));
        }
        for (int i = 0; i < maquinas; i++) {
            jugadores.add(new Jugador("Máquina " + (i + 1), SALDO_INICIAL, true));
        }
        return jugadores;
    }

    private void eliminarJugadoresSinSaldo(List<Jugador> jugadores) {
        List<Jugador> eliminados = jugadores.stream()
                .filter(jugador -> jugador.getSaldo() <= 0)
                .toList();
        for (Jugador eliminado : eliminados) {
            view.mostrarMensaje("El jugador " + eliminado.getNombre() + " ha sido eliminado.");
        }
        jugadores.removeIf(jugador -> jugador.getSaldo() <= 0);
    }

    private void jugarMano(List<Jugador> jugadores, Baraja baraja) {
        List<Carta> comunitarias = new ArrayList<>();
        int[] apuestas = new int[jugadores.size()];

        for (Jugador jugador : jugadores) {
            jugadorService.iniciarMano(jugador);
            jugador.recibirCarta(barajaService.repartirCarta(baraja));
            jugador.recibirCarta(barajaService.repartirCarta(baraja));
            view.limpiarPantalla();
        }

        apuestaService.ponerCiegas(jugadores, apuestas);
        int primerJugador = jugadores.size() == 2
                ? apuestaService.getSmallBlindIndex()
                : (apuestaService.getBigBlindIndex() + 1) % jugadores.size();
        boolean manoTerminada = apuestaService.realizarRondaApuestas(
                jugadores, comunitarias, "Pre-Flop", primerJugador, apuestas);

        Arrays.fill(apuestas, 0);
        for (int i = 0; i < 3; i++) {
            comunitarias.add(barajaService.repartirCarta(baraja));
        }
        boolean finalizarPorAllIn = manoTerminada || todosAllInOSoloUnoActivo(jugadores);
        if (!finalizarPorAllIn) {
            apuestaService.realizarRondaApuestas(jugadores, comunitarias, "Flop",
                    apuestaService.getSmallBlindIndex(), apuestas);
            finalizarPorAllIn = todosAllInOSoloUnoActivo(jugadores);
        } else {
            mostrarCartasComunitarias(comunitarias, "Flop");
        }

        Arrays.fill(apuestas, 0);
        comunitarias.add(barajaService.repartirCarta(baraja));
        if (!finalizarPorAllIn) {
            apuestaService.realizarRondaApuestas(jugadores, comunitarias, "Turn",
                    apuestaService.getSmallBlindIndex(), apuestas);
            finalizarPorAllIn = todosAllInOSoloUnoActivo(jugadores);
        } else {
            mostrarCartasComunitarias(comunitarias, "Turn");
        }

        Arrays.fill(apuestas, 0);
        comunitarias.add(barajaService.repartirCarta(baraja));
        if (!finalizarPorAllIn) {
            apuestaService.realizarRondaApuestas(jugadores, comunitarias, "River",
                    apuestaService.getSmallBlindIndex(), apuestas);
        } else {
            mostrarCartasComunitarias(comunitarias, "River");
        }

        mostrarShowdown(jugadores, comunitarias);
        mostrarResumenManoFinal(jugadores);
        apuestaService.aumentarCiegas();
    }

    private boolean todosAllInOSoloUnoActivo(List<Jugador> jugadores) {
        int activos = 0;
        boolean alguienPuedeApostar = false;
        for (Jugador jugador : jugadores) {
            if (jugador.isEnJuego()) {
                activos++;
                if (!jugador.isVaAllIn() && jugador.getSaldo() > 0) {
                    alguienPuedeApostar = true;
                }
            }
        }
        return !alguienPuedeApostar || activos <= 1;
    }

    private void mostrarCartasComunitarias(List<Carta> comunitarias, String fase) {
        StringBuilder mensaje = new StringBuilder(fase + ": ");
        for (Carta carta : comunitarias) {
            mensaje.append(carta).append(" | ");
        }
        if (!comunitarias.isEmpty()) {
            mensaje.setLength(mensaje.length() - 3);
        }
        view.mostrarMensaje(mensaje.toString());
    }

    private void mostrarShowdown(List<Jugador> jugadores, List<Carta> comunitarias) {
        Map<Jugador, Integer> ganancias = new HashMap<>();
        Map<Jugador, EvaluarManos.ResultadoEvaluacion> evaluacionesPorJugador = new HashMap<>();
        for (Jugador jugador : jugadores) {
            if (jugador.esMaquina()) {
                EvaluarManos.ResultadoEvaluacion evaluacion =
                        EvaluarManos.evaluarManoCompleta(jugador.getMano(), comunitarias);
                evaluacionesPorJugador.put(jugador, evaluacion);
                if (jugador.isEnJuego()) {
                    view.mostrarMensaje(jugador.getNombre() + " muestra sus cartas: "
                            + jugador.getMano());
                    view.mostrarMensaje(jugador.getNombre() + " juega " + evaluacion.nombreJugada);
                    view.mostrarMensaje("Cartas usadas: " + evaluacion.cartasPrincipales);
                } else {
                    view.mostrarMensaje(jugador.getNombre() + " se ha retirado con "
                            + evaluacion.nombreJugada);
                    view.mostrarMensaje("Cartas usadas: " + evaluacion.cartasPrincipales);
                }
            }
        }
        for (ApuestaService.SidePot pot : apuestaService.getPots()) {
            List<Jugador> candidatos = pot.getParticipantes().stream()
                    .filter(Jugador::isEnJuego)
                    .toList();
            if (candidatos.isEmpty() || pot.getCantidad() <= 0) {
                continue;
            }

            List<EvaluarManos.ResultadoEvaluacion> evaluaciones = new ArrayList<>();
            for (Jugador candidato : candidatos) {
                boolean primeraEvaluacion = !evaluacionesPorJugador.containsKey(candidato);
                EvaluarManos.ResultadoEvaluacion evaluacion = evaluacionesPorJugador.computeIfAbsent(
                        candidato, jugador -> EvaluarManos.evaluarManoCompleta(jugador.getMano(), comunitarias));
                evaluaciones.add(evaluacion);
                if (primeraEvaluacion && !candidato.esMaquina()) {
                    view.mostrarMensaje(candidato.getNombre() + " juega " + evaluacion.nombreJugada);
                    view.mostrarMensaje("Cartas usadas: " + evaluacion.cartasPrincipales);
                }
            }

            List<Jugador> ganadores = new ArrayList<>();
            int indiceMejor = 0;
            ganadores.add(candidatos.get(0));
            for (int i = 1; i < candidatos.size(); i++) {
                int comparacion = CompararManos.compararManos2(
                        evaluaciones.get(i).cartasPrincipales, evaluaciones.get(i).nombreJugada,
                        evaluaciones.get(indiceMejor).cartasPrincipales,
                        evaluaciones.get(indiceMejor).nombreJugada);
                if (comparacion > 0) {
                    ganadores.clear();
                    ganadores.add(candidatos.get(i));
                    indiceMejor = i;
                } else if (comparacion == 0) {
                    ganadores.add(candidatos.get(i));
                }
            }

            int premio = pot.getCantidad() / ganadores.size();
            int resto = pot.getCantidad() % ganadores.size();
            for (int i = 0; i < ganadores.size(); i++) {
                int cantidad = premio + (i < resto ? 1 : 0);
                ganadores.get(i).ganar(cantidad);
                ganancias.merge(ganadores.get(i), cantidad, Integer::sum);
            }
        }
        for (Map.Entry<Jugador, Integer> ganancia : ganancias.entrySet()) {
            view.mostrarMensaje(ganancia.getKey().getNombre() + " gana un total de "
                    + ganancia.getValue() + " fichas en esta mano.");
        }
    }

    private void mostrarResumenManoFinal(List<Jugador> jugadores) {
        jugadores.stream()
                .max(Comparator.comparingInt(Jugador::getSaldo))
                .ifPresent(jugador -> view.mostrarMensaje(
                        "El jugador con más fichas tras la mano es: " + jugador.getNombre()
                                + " (" + jugador.getSaldo() + " fichas)"));
        view.mostrarMensaje("---- Saldo de todos los jugadores tras la mano ----");
        for (Jugador jugador : jugadores) {
            view.mostrarMensaje(jugador.getNombre() + ": " + jugador.getSaldo() + " fichas");
        }
        view.mostrarMensaje("---------------------------------------------");
    }

    private void mostrarSaldosFinales(List<Jugador> jugadores) {
        for (Jugador jugador : jugadores) {
            view.mostrarMensaje(jugador.getNombre() + " termina con un saldo de: "
                    + jugador.getSaldo());
        }
    }
}
