package poker.service;

import poker.model.Carta;
import poker.model.Jugador;
import poker.PokerBot;
import poker.view.ConsolaView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsula las ciegas, las rondas de apuestas y los botes laterales para una
 * mesa de póker.
 */
public class ApuestaService {
    private int smallBlind;
    private int bigBlind;
    private final int manosParaAumentarCiegas;
    private int contadorManos;
    private int dealerIndex = -1;
    private int smallBlindIndex = -1;
    private int bigBlindIndex = -1;
    private final ConsolaView view;
    private final JugadorService jugadorService;
    private final List<SidePot> pots = new ArrayList<>();
    private final Map<Jugador, Integer> contribuciones = new HashMap<>();

    public static class SidePot {
        public int cantidad;
        public final List<Jugador> participantes;

        public SidePot(int cantidad, List<Jugador> jugadores) {
            this.cantidad = cantidad;
            this.participantes = new ArrayList<>(jugadores);
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public List<Jugador> getParticipantes() {
            return Collections.unmodifiableList(participantes);
        }
    }

    public ApuestaService(int smallBlind, int bigBlind, int manosParaAumentarCiegas,
                          ConsolaView view, JugadorService jugadorService) {
        if (smallBlind <= 0 || bigBlind <= 0 || manosParaAumentarCiegas <= 0) {
            throw new IllegalArgumentException("Los valores de las ciegas y el número de manos deben ser positivos.");
        }
        if (view == null || jugadorService == null) {
            throw new IllegalArgumentException("Las dependencias no pueden ser nulas");
        }
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.manosParaAumentarCiegas = manosParaAumentarCiegas;
        this.view = view;
        this.jugadorService = jugadorService;
    }

    public List<SidePot> getPots() {
        return pots;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public int getSmallBlindIndex() {
        return smallBlindIndex;
    }

    public int getBigBlindIndex() {
        return bigBlindIndex;
    }

    public void resetPots(List<Jugador> jugadores) {
        pots.clear();
        contribuciones.clear();
        pots.add(new SidePot(0, jugadores));
    }

    public void asignarPosicionesIniciales(List<Jugador> jugadores) {
        validarJugadoresParaPosiciones(jugadores);
        dealerIndex = 0;
        actualizarCiegas(jugadores.size());
    }

    public void rotarPosiciones(List<Jugador> jugadores) {
        validarJugadoresParaPosiciones(jugadores);
        dealerIndex = (dealerIndex + 1 + jugadores.size()) % jugadores.size();
        actualizarCiegas(jugadores.size());
    }

    private void validarJugadoresParaPosiciones(List<Jugador> jugadores) {
        if (jugadores == null || jugadores.size() < 2) {
            throw new IllegalStateException("No hay suficientes jugadores para asignar posiciones.");
        }
    }

    private void actualizarCiegas(int numeroJugadores) {
        smallBlindIndex = (dealerIndex + 1) % numeroJugadores;
        bigBlindIndex = (dealerIndex + 2) % numeroJugadores;
    }

    public void registrarAporte(Jugador jugador, int cantidad, List<Jugador> jugadores) {
        if (cantidad <= 0) {
            return;
        }
        contribuciones.merge(jugador, cantidad, Integer::sum);
        recalcularPots(jugadores);
    }

    /**
     * Reconstruye los botes a partir de las aportaciones totales. Esto gestiona
     * un all-in de la ciega y también all-ins posteriores sin depender del array
     * de contribuciones de la ronda actual.
     */
    private void recalcularPots(List<Jugador> jugadores) {
        pots.clear();
        List<Integer> niveles = contribuciones.values().stream()
                .filter(cantidad -> cantidad > 0)
                .distinct()
                .sorted()
                .toList();
        int nivelAnterior = 0;
        for (int nivel : niveles) {
            int cantidadDeJugadores = (int) contribuciones.values().stream()
                    .filter(cantidad -> cantidad >= nivel)
                    .count();
            int cantidad = (nivel - nivelAnterior) * cantidadDeJugadores;
            List<Jugador> participantes = contribuciones.entrySet().stream()
                    .filter(entry -> entry.getValue() >= nivel && entry.getKey().isEnJuego())
                    .map(Map.Entry::getKey)
                    .toList();
            pots.add(new SidePot(cantidad, participantes));
            nivelAnterior = nivel;
        }
    }

    public void ponerCiegas(List<Jugador> jugadores, int[] apuestas) {
        if (apuestas == null || apuestas.length != jugadores.size()) {
            throw new IllegalArgumentException("El registro de apuestas no coincide con los jugadores");
        }
        resetPots(jugadores);
        ponerCiega(jugadores, apuestas, smallBlindIndex, smallBlind);
        ponerCiega(jugadores, apuestas, bigBlindIndex, bigBlind);
    }

    private void ponerCiega(List<Jugador> jugadores, int[] apuestas, int indice, int cantidad) {
        Jugador jugador = jugadores.get(indice);
        if (!jugador.isEnJuego() || jugador.getSaldo() <= 0) {
            return;
        }
        int aporte = Math.min(jugador.getSaldo(), cantidad);
        jugador.setSaldo(jugador.getSaldo() - aporte);
        apuestas[indice] = aporte;
        registrarAporte(jugador, aporte, jugadores);
    }

    public void mostrarCiegasActuales() {
        view.mostrarMensaje("Ciega pequeña actual: " + smallBlind + ", Ciega grande actual: " + bigBlind);
    }

    public void aumentarCiegas() {
        contadorManos++;
        if (contadorManos % manosParaAumentarCiegas == 0) {
            smallBlind *= 2;
            bigBlind *= 2;
            view.mostrarMensaje("Las ciegas han aumentado. Nueva ciega pequeña: "
                    + smallBlind + ", Nueva ciega grande: " + bigBlind);
        }
    }

    /**
     * Ejecuta una ronda de apuestas. El valor devuelto es true cuando un retiro
     * deja a un único jugador, en cuyo caso la mano ya ha sido pagada.
     */
    public boolean realizarRondaApuestas(List<Jugador> jugadores, List<Carta> comunitarias,
                                         String fase, int primerJugador, int[] apuestas) {
        int[] apuestaActual = {( "Pre-Flop".equals(fase) ? bigBlind : 0 )};
        boolean[] yaActuo = new boolean[jugadores.size()];

        while (true) {
            boolean todosIgualaron = true;
            for (int offset = 0; offset < jugadores.size(); offset++) {
                int indice = (primerJugador + offset) % jugadores.size();
                Jugador jugador = jugadores.get(indice);
                if (!jugador.isEnJuego() || jugador.isVaAllIn() || jugador.getSaldo() <= 0) {
                    continue;
                }
                if (yaActuo[indice] && apuestas[indice] == apuestaActual[0]) {
                    continue;
                }

                boolean accionValida = false;
                while (!accionValida) {
                    int apuestaAnterior = apuestaActual[0];
                    if (jugador.esMaquina()) {
                        accionValida = ejecutarAccionBot(jugador, jugadores, comunitarias,
                                apuestas, indice, apuestaActual);
                    } else {
                        mostrarEstadoDeRonda(jugador, comunitarias, fase, apuestas[indice], apuestaActual[0]);
                        accionValida = ejecutarAccion(jugador, jugadores, apuestas, indice,
                                apuestaActual, fase);
                    }
                    if (apuestaActual[0] == -999) {
                        return true;
                    }

                    if (apuestaActual[0] > apuestaAnterior) {
                        Arrays.fill(yaActuo, false);
                        todosIgualaron = false;
                    }
                }

                yaActuo[indice] = true;
            }
            if (todosIgualaron) {
                return false;
            }
        }
    }

    private boolean ejecutarAccionBot(Jugador jugador, List<Jugador> jugadores,
                                      List<Carta> comunitarias, int[] apuestas, int indice,
                                      int[] apuestaActual) {
        PokerBot bot = jugador.getBot();
        bot.reiniciarMano();
        bot.recibirCartasOcultas(jugador.getMano().get(0), jugador.getMano().get(1));
        comunitarias.forEach(bot::agregarCartaComunitaria);
        PokerBot.Posicion posicion = indice == smallBlindIndex
                ? PokerBot.Posicion.CIEGA_PEQUENA
                : indice == bigBlindIndex ? PokerBot.Posicion.CIEGA_GRANDE : PokerBot.Posicion.MEDIA;
        String decision = bot.tomarDecision(apuestaActual[0], obtenerTamanoPote(),
                posicion, jugadores.size());
        int porIgualar = Math.max(0, apuestaActual[0] - apuestas[indice]);

        if ("FOLD".equals(decision)) {
            mostrarAccionBot(jugador.getNombre() + " se retira.");
            jugador.setEnJuego(false);
            Jugador ultimo = unicoJugadorActivo(jugadores);
            if (ultimo != null) {
                for (SidePot pot : pots) {
                    ultimo.ganar(pot.cantidad);
                    pot.cantidad = 0;
                }
                apuestaActual[0] = -999;
            }
            return true;
        }
        if ("IGUALDAD".equals(decision) || "PASAR".equals(decision)) {
            mostrarAccionBot(jugador.getNombre() + ("IGUALDAD".equals(decision)
                    ? " iguala la apuesta."
                    : " pasa."));
            aportar(jugador, Math.min(jugador.getSaldo(), porIgualar), jugadores, apuestas, indice);
            return true;
        }
        if (decision.startsWith("SUBIDA:")) {
            int subida = (int) Double.parseDouble(decision.substring("SUBIDA:".length()));
            int cantidad = Math.min(jugador.getSaldo(), porIgualar + Math.max(1, subida));
            if (cantidad > porIgualar) {
                mostrarAccionBot(jugador.getNombre() + " sube " + (cantidad - porIgualar)
                        + " fichas.");
                aportar(jugador, cantidad, jugadores, apuestas, indice);
                apuestaActual[0] = apuestas[indice];
            }
            return true;
        }
        return false;
    }

    private void mostrarAccionBot(String mensaje) {
        view.mostrarMensaje("");
        view.mostrarMensaje(mensaje);
        view.mostrarMensaje("");
    }

    private int obtenerTamanoPote() {
        return pots.stream().mapToInt(SidePot::getCantidad).sum();
    }

    private void mostrarEstadoDeRonda(Jugador jugador, List<Carta> comunitarias,
                                      String fase, int aporte, int apuestaActual) {
        mostrarCartasComunitarias(comunitarias, fase);
        view.mostrarMensaje(jugadorService.formatearMano(jugador));
        view.mostrarMensaje(jugador.getNombre() + " tiene " + jugador.getSaldo() + " fichas.");
        view.mostrarMensaje("La apuesta actual es: " + apuestaActual);
        view.mostrarMensaje("Tu aporte esta ronda: " + aporte);
    }

    private boolean ejecutarAccion(Jugador jugador, List<Jugador> jugadores, int[] apuestas,
                                   int indice, int[] apuestaActual, String fase) {
        int cantidadPorIgualar = Math.max(0, apuestaActual[0] - apuestas[indice]);
        view.mostrarOpcionesApuesta(cantidadPorIgualar > 0);
        String accion = view.leerLinea().toLowerCase();

        if (("igualar".equals(accion) || "1".equals(accion)) && cantidadPorIgualar > 0) {
            int cantidad = Math.min(jugador.getSaldo(), cantidadPorIgualar);
            aportar(jugador, cantidad, jugadores, apuestas, indice);
            return true;
        }
        if (("pasar".equals(accion) || "1".equals(accion)) && cantidadPorIgualar == 0) {
            return true;
        }
        if (("apostar".equals(accion) || "2".equals(accion)) && cantidadPorIgualar == 0) {
            int minimo = "Pre-Flop".equals(fase) ? bigBlind : 1;
            int cantidad = view.solicitarCantidad(
                    "¿Cuánto quieres apostar? (min " + minimo + ", max " + jugador.getSaldo() + "): ",
                    minimo, jugador.getSaldo());
            aportar(jugador, cantidad, jugadores, apuestas, indice);
            apuestaActual[0] = apuestas[indice];
            return true;
        }
        if (("subir".equals(accion) || "2".equals(accion)) && cantidadPorIgualar > 0) {
            int maxExtra = jugador.getSaldo() - cantidadPorIgualar;
            if (maxExtra < 1) {
                view.mostrarMensaje("No tienes fichas suficientes para subir.");
                return false;
            }
            int subida = view.solicitarCantidad(
                    "¿Cuántas fichas extra quieres subir? (min 1, max " + maxExtra + "): ",
                    1, maxExtra);
            aportar(jugador, cantidadPorIgualar + subida, jugadores, apuestas, indice);
            apuestaActual[0] = apuestas[indice];
            return true;
        }
        if ("all-in".equals(accion) || "3".equals(accion)) {
            int cantidad = jugador.getSaldo();
            aportar(jugador, cantidad, jugadores, apuestas, indice);
            jugador.setVaAllIn(true);
            apuestaActual[0] = Math.max(apuestaActual[0], apuestas[indice]);
            return true;
        }
        if ("retirarse".equals(accion) || "4".equals(accion)) {
            jugador.setEnJuego(false);
            Jugador ultimo = unicoJugadorActivo(jugadores);
            if (ultimo != null) {
                for (SidePot pot : pots) {
                    ultimo.ganar(pot.cantidad);
                    pot.cantidad = 0;
                }
                view.mostrarMensaje("Todos se han retirado. Gana " + ultimo.getNombre());
                apuestaActual[0] = -999;
            }
            return true;
        }
        view.mostrarMensaje("Opción no válida.");
        return false;
    }

    private void aportar(Jugador jugador, int cantidad, List<Jugador> jugadores,
                         int[] apuestas, int indice) {
        jugador.setSaldo(jugador.getSaldo() - cantidad);
        apuestas[indice] += cantidad;
        registrarAporte(jugador, cantidad, jugadores);
    }

    private Jugador unicoJugadorActivo(List<Jugador> jugadores) {
        Jugador unico = null;
        for (Jugador jugador : jugadores) {
            if (jugador.isEnJuego() && (jugador.getSaldo() > 0 || jugador.isVaAllIn())) {
                if (unico != null) {
                    return null;
                }
                unico = jugador;
            }
        }
        return unico;
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
}
