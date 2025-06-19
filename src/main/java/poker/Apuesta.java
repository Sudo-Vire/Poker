package poker;

import java.util.List;

public class Apuesta {
    private int smallBlind;
    private int bigBlind;
    private final int manosParaAumentarCiegas;
    private int contadorManos;

    public Apuesta(int smallBlind, int bigBlind, int manosParaAumentarCiegas) {
        if (smallBlind <= 0 || bigBlind <= 0 || manosParaAumentarCiegas <= 0) {
            throw new IllegalArgumentException("Los valores de las ciegas y el número de manos deben ser positivos.");
        }
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.manosParaAumentarCiegas = manosParaAumentarCiegas;
        this.contadorManos = 0;
    }

    //Aumenta las ciegas
    public void aumentarCiegas() {
        contadorManos++;
        if (contadorManos % manosParaAumentarCiegas == 0) {
            smallBlind *= 2;
            bigBlind *= 2;
            Interfaz.mostrarMensaje("Las ciegas han aumentado. Nueva ciega pequeña: " + smallBlind + ", Nueva ciega grande: " + bigBlind);
        }
    }

    public void realizarApuesta(Jugador jugador, int cantidad, int[] pozo) {
        if (cantidad > jugador.getSaldo()) {
            throw new IllegalArgumentException(jugador.getNombre() + " no tiene suficientes fichas para apostar " + cantidad);
        }
        jugador.setSaldo(jugador.getSaldo() - cantidad);
        pozo[0] += cantidad;
    }

    public void realizarRondaApuestas(List<Jugador> jugadores, int[] pozo, List<Baraja.Carta> comunitarias, String fase) {
        for (Jugador jugador : jugadores) {
            if (jugador.isEnJuego()) {
                // Mostrar las cartas comunitarias antes de que el jugador realice su acción
                mostrarCartasComunitarias(comunitarias, fase);
                // Mostrar la mano del jugador
                jugador.mostrarMano();
                // Mostrar las fichas del jugador
                Interfaz.mostrarMensaje(jugador.getNombre() + " tiene " + jugador.getSaldo() + " fichas.");
                int apuestaJugador = Interfaz.leerNumero("¿Cuánto quieres apostar? (0 para retirarse): ", 0, jugador.getSaldo());
                if (apuestaJugador == 0) {
                    jugador.retirarse();
                } else {
                    realizarApuesta(jugador, apuestaJugador, pozo);
                }
                for (int i = 0; i < 20; i++) {
                    Interfaz.mostrarMensaje("");
                }
            }
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

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public void mostrarCiegasActuales() {
        Interfaz.mostrarMensaje("Ciega pequeña actual: " + smallBlind + ", Ciega grande actual: " + bigBlind);
    }
}