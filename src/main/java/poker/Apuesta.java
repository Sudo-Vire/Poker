package poker;

import java.util.List;

public class Apuesta {
    private static int smallBlind;
    private static int bigBlind;
    private static int contadorManos;
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

    public void ponerCiegas(List<Jugador> jugadores, int[] pozo, int[] apuestas) {
        // Small Blind
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

        // Big Blind
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

    public void mostrarCiegasActuales() {
        Interfaz.mostrarMensaje("Ciega pequeña actual: " + smallBlind + ", Ciega grande actual: " + bigBlind);
    }

    public static void aumentarCiegas() {
        contadorManos++;
        int manosParaAumentarCiegas = 3;
        if (contadorManos % manosParaAumentarCiegas == 0) {
            smallBlind *= 2;
            bigBlind *= 2;
            Interfaz.mostrarMensaje("Las ciegas han aumentado. Nueva ciega pequeña: " + smallBlind + ", Nueva ciega grande: " + bigBlind);
        }
    }

    public void realizarRondaApuestas(List<Jugador> jugadores, int[] pozo, List<Baraja.Carta> comunitarias, String fase, int primerJugador, int[] apuestas) {
        int n = jugadores.size();
        int[] apuestaActual = new int[1];
        apuestaActual[0] = (fase.equals("Pre-Flop")) ? bigBlind : 0;

        while (true) {
            for (int offset = 0; offset < n; offset++) {
                int i = (primerJugador + offset) % n;
                Jugador jugador = jugadores.get(i);

                if (!jugador.isEnJuego() || jugador.getSaldo() <= 0) continue;
                if (apuestas[i] == apuestaActual[0] && apuestaActual[0] != 0) continue;

                mostrarCartasComunitarias(comunitarias, fase);
                jugador.mostrarMano();
                Interfaz.mostrarMensaje(jugador.getNombre() + " tiene " + jugador.getSaldo() + " fichas.");
                Interfaz.mostrarMensaje("La apuesta actual es: " + apuestaActual[0]);
                Interfaz.mostrarMensaje("Tu aporte esta ronda: " + apuestas[i]);

                int cantidadPorIgualar = apuestaActual[0] - apuestas[i];

                boolean accionValida = false;

                while (!accionValida) {
                    accionValida = Interfaz.aspr(jugador, cantidadPorIgualar, pozo, apuestas, i, apuestaActual, jugadores, fase, bigBlind);
                    if (apuestaActual[0] == -999) {
                        return;
                    }
                }
            }
            // Si todos igualaron la apuesta o pasaron, termina la ronda
            boolean rondaTerminada = true;
            int jugadoresActivos = 0;
            for (int i = 0; i < n; i++) {
                Jugador j = jugadores.get(i);
                if (j.isEnJuego() && j.getSaldo() > 0) {
                    jugadoresActivos++;
                    if (apuestas[i] != apuestaActual[0]) {
                        rondaTerminada = false;
                    }
                }
            }
            if (jugadoresActivos <= 1) break;
            if (rondaTerminada) break;
        }
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