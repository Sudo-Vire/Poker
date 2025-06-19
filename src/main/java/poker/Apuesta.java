package poker;

import java.util.List;

public class Apuesta {
    private static int smallBlind;
    private static int bigBlind;
    private static int contadorManos;

    public Apuesta(int smallBlind, int bigBlind, int manosParaAumentarCiegas) {
        if (smallBlind <= 0 || bigBlind <= 0 || manosParaAumentarCiegas <= 0) {
            throw new IllegalArgumentException("Los valores de las ciegas y el número de manos deben ser positivos.");
        }
        Apuesta.smallBlind = smallBlind;
        Apuesta.bigBlind = bigBlind;
        contadorManos = 0;
    }

    // Aumenta las ciegas cada cierto número de manos jugadas
    public static void aumentarCiegas() {
        contadorManos++;
        int manosParaAumentarCiegas = 3;
        if (contadorManos % manosParaAumentarCiegas == 0) {
            smallBlind *= 2;
            bigBlind *= 2;
            Interfaz.mostrarMensaje("Las ciegas han aumentado. Nueva ciega pequeña: " + smallBlind + ", Nueva ciega grande: " + bigBlind);
        }
    }

    public void realizarRondaApuestas(List<Jugador> jugadores, int[] pozo, List<Baraja.Carta> comunitarias, String fase) {
        // Reiniciar haApostado para todos los jugadores activos
        for (Jugador jugador : jugadores) {
            if (jugador.isEnJuego()) jugador.setHaApostado(false);
        }

        for (int i = 0; i < jugadores.size(); i++) {
            Jugador jugador = jugadores.get(i);
            if (jugador.isEnJuego()) {
                mostrarCartasComunitarias(comunitarias, fase);
                jugador.mostrarMano();
                Interfaz.mostrarMensaje(jugador.getNombre() + " tiene " + jugador.getSaldo() + " fichas.");
                int numJugada = Interfaz.opcionesJugada(jugadores, i);

                // Validación de jugada y ciclo de apuestas
                if (numJugada == 1) {
                    label:
                    while (true) {
                        String jugada = Interfaz.leerLinea().toLowerCase();
                        switch (jugada) {
                            case "apostar":
                                int apuestaJugador = Interfaz.leerNumero("¿Cuánto quieres apostar?", 1, jugador.getSaldo());
                                realizarApuesta(jugador, apuestaJugador, pozo);
                                break label;
                            case "pasar":
                                realizarApuesta(jugador, 0, pozo);
                                break label;
                            case "retirarse":
                                jugador.enJuego = false;
                                break label;
                            default:
                                System.out.println("Jugada no válida. Escribe 'apostar', 'pasar' o 'retirarse'.");
                                break;
                        }
                    }
                } else if (numJugada == 2) {
                    while (true) {
                        String jugada = Interfaz.leerLinea().toLowerCase();
                        if (jugada.equals("apostar")) {
                            int apuestaJugador = Interfaz.leerNumero("¿Cuánto quieres apostar?", 1, jugador.getSaldo());
                            realizarApuesta(jugador, apuestaJugador, pozo);
                            break;
                        } else if (jugada.equals("retirarse")) {
                            jugador.enJuego = false;
                            break;
                        } else {
                            System.out.println("Jugada no válida. Escribe 'apostar' o 'retirarse'.");
                        }
                    }
                } else {
                    throw new IllegalArgumentException("No es una opción válida");
                }
                // Limpiar pantalla
                for (int k = 0; k < 20; k++) {
                    Interfaz.mostrarMensaje("");
                }
            }
        }
    }

    public void realizarApuesta(Jugador jugador, int cantidad, int[] pozo) {
        if (cantidad > jugador.getSaldo()) {
            throw new IllegalArgumentException(jugador.getNombre() + " no tiene suficientes fichas para apostar " + cantidad);
        }
        jugador.setSaldo(jugador.getSaldo() - cantidad);
        jugador.setHaApostado(cantidad > 0); // Solo marca como que apostó si la cantidad es mayor que 0
        pozo[0] += cantidad;
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