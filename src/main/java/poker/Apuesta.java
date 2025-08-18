package poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Apuesta {
    private static int smallBlind; // Valor ciega pequeña
    private static int bigBlind;   // Valor ciega grande
    private static int contadorManos; // Manos para aumentar las ciegas
    // Índices para calcular la posición del dealer y las ciegas
    private int dealerIndex;
    private int smallBlindIndex;
    private int bigBlindIndex;

    // --- NUEVO: Estructura de Side Pots (Main pot + side pots sucesivos) ---
    public static class SidePot {
        int cantidad;                    // Fichas acumuladas en el pot
        List<Jugador> participantes;     // Jugadores que pueden optar a este pot

        public SidePot(int cantidad, List<Jugador> jugadores) {
            this.cantidad = cantidad;
            this.participantes = new ArrayList<>(jugadores);
        }
    }

    // --- Lista de todos los pots de la mano (al menos 1 = Main pot) ---
    private final List<SidePot> pots = new ArrayList<>();

    public List<SidePot> getPots() {
        return pots;
    }

    // Reinicia todos los pots (para comenzar una nueva mano)
    public void resetPots(List<Jugador> jugadores) {
        pots.clear();
        pots.add(new SidePot(0, jugadores));  // Main pot inicial
    }

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

    public int getSmallBlindIndex() { return smallBlindIndex; }
    public int getBigBlindIndex() { return bigBlindIndex; }

    // Asigna las posiciones iniciales del dealer y de las ciegas
    public void asignarPosicionesIniciales(List<Jugador> jugadores) {
        if (jugadores.size() < 2) {
            throw new IllegalStateException("No hay suficientes jugadores para asignar posiciones.");
        }
        dealerIndex = 0;
        smallBlindIndex = (dealerIndex + 1) % jugadores.size();
        bigBlindIndex = (dealerIndex + 2) % jugadores.size();
    }

    // Rota las posiciones iniciales del dealer y de las ciegas
    public void rotarPosiciones(List<Jugador> jugadores) {
        if (jugadores.size() < 2) {
            throw new IllegalStateException("No hay suficientes jugadores para rotar posiciones.");
        }
        dealerIndex = (dealerIndex + 1) % jugadores.size();
        smallBlindIndex = (dealerIndex + 1) % jugadores.size();
        bigBlindIndex = (dealerIndex + 2) % jugadores.size();
    }

    // --- Método PRIVADO para añadir contribuciones al último pot ---
    private void agregarAlPot(Jugador jugador, int cantidad, List<Jugador> jugadores) {
        if (cantidad <= 0) return;

        // Añadir al último pot activo
        SidePot ultimo = pots.get(pots.size() - 1);
        ultimo.cantidad += cantidad;

        // Si este jugador se quedó en All-In → se crea un nuevo SidePot para separar apuestas extra posteriores
        if (jugador.isVaAllIn()) {
            List<Jugador> nuevos = new ArrayList<>();
            for (Jugador j : jugadores) {
                if (j.isEnJuego()) nuevos.add(j);
            }
            pots.add(new SidePot(0, nuevos));
        }
    }

    // --- Método PUBLICO para que Interfaz pueda registrar aportes ---
    public void registrarAporte(Jugador jugador, int cantidad, List<Jugador> jugadores) {
        agregarAlPot(jugador, cantidad, jugadores);
    }

    // Descuenta las ciegas a los jugadores correspondientes y suma al pot (ya usando side pots)
    public void ponerCiegas(List<Jugador> jugadores, int[] apuestas) {
        resetPots(jugadores);

        // Ciega pequeña
        Jugador sb = jugadores.get(smallBlindIndex);
        if (sb.isEnJuego() && sb.getSaldo() > 0) {
            int sbMonto = Math.min(sb.getSaldo(), smallBlind);
            sb.setSaldo(sb.getSaldo() - sbMonto);
            apuestas[smallBlindIndex] = sbMonto;
            registrarAporte(sb, sbMonto, jugadores);
        }

        // Ciega grande
        Jugador bb = jugadores.get(bigBlindIndex);
        if (bb.isEnJuego() && bb.getSaldo() > 0) {
            int bbMonto = Math.min(bb.getSaldo(), bigBlind);
            bb.setSaldo(bb.getSaldo() - bbMonto);
            apuestas[bigBlindIndex] = bbMonto;
            registrarAporte(bb, bbMonto, jugadores);
        }
    }

    // Muestra los valores actuales de las ciegas
    public void mostrarCiegasActuales() {
        Interfaz.mostrarMensaje("Ciega pequeña actual: " + smallBlind + ", Ciega grande actual: " + bigBlind);
    }

    // Aumenta las ciegas cada cierto número fijo de manos.
    public static void aumentarCiegas() {
        contadorManos++;
        int manosParaAumentarCiegas = 3;
        if (contadorManos % manosParaAumentarCiegas == 0) {
            smallBlind *= 2;
            bigBlind *= 2;
            Interfaz.mostrarMensaje("Las ciegas han aumentado. Nueva ciega pequeña: " + smallBlind + ", Nueva ciega grande: " + bigBlind);
        }
    }

    // Controla la ronda de apuestas de acuerdo a la fase actual del juego.
    // --- MODIFICADO: ahora cada apuesta mueve fichas a side pots mediante registrarAporte ---
    public void realizarRondaApuestas(List<Jugador> jugadores, List<Baraja.Carta> comunitarias, String fase, int primerJugador, int[] apuestas) {
        int n = jugadores.size();
        int[] apuestaActual = new int[1];
        apuestaActual[0] = (fase.equals("Pre-Flop")) ? bigBlind : 0;

        boolean[] yaActuo = new boolean[n];
        boolean todosIgualaron;

        do {
            todosIgualaron = true;

            for (int offset = 0; offset < n; offset++) {
                int i = (primerJugador + offset) % n;
                Jugador jugador = jugadores.get(i);

                if (!jugador.isEnJuego() || jugador.isVaAllIn() || jugador.getSaldo() <= 0) continue;

                if ((apuestas[i] == apuestaActual[0] || jugador.isVaAllIn()) && yaActuo[i]) continue;

                mostrarCartasComunitarias(comunitarias, fase);
                jugador.mostrarMano();
                Interfaz.mostrarMensaje(jugador.getNombre() + " tiene " + jugador.getSaldo() + " fichas.");
                Interfaz.mostrarMensaje("La apuesta actual es: " + apuestaActual[0]);
                Interfaz.mostrarMensaje("Tu aporte esta ronda: " + apuestas[i]);

                int cantidadPorIgualar = apuestaActual[0] - apuestas[i];
                boolean accionValida = false;

                // Espera a que el jugador haga una acción válida
                while (!accionValida) {
                    int apuestaPrev = apuestaActual[0];
                    accionValida = Interfaz.aspr(jugador, cantidadPorIgualar, apuestas, i, apuestaActual, jugadores, fase, bigBlind, this);

                    if (apuestaActual[0] == -999) return; // mano terminada

                    if (apuestaActual[0] > apuestaPrev) {
                        Arrays.fill(yaActuo, false);
                        yaActuo[i] = true;
                        todosIgualaron = false;
                    }
                }
                yaActuo[i] = true;
            }

        } while (!todosIgualaron);
    }

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