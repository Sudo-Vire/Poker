package poker;

import java.util.List;
import java.util.Scanner;

/**
 * Clase para la interacción por consola.
 */
public class Interfaz {
    private static final Scanner scanner = new Scanner(System.in);

    public static int leerNumero(String mensaje, int min, int max) {
        int numero;
        do {
            System.out.print(mensaje);
            while (!scanner.hasNextInt()) {
                System.out.println("Eso no es un número válido. Inténtalo de nuevo.");
                scanner.next();
                System.out.print(mensaje);
            }
            numero = scanner.nextInt();
            scanner.nextLine();
        } while (numero < min || numero > max);
        return numero;
    }

    public static String leerLinea() {
        return scanner.nextLine();
    }

    public static void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    /**
     * Busca si el jugador anterior en juego ha apostado.
     * @param jugadores la lista de jugadores en orden de turno
     * @param indiceActual el índice del jugador actual
     * @return true si el jugador anterior en juego ya apostó
     */
    public static boolean jugadorAnteriorEnJuegoHaApostado(List<Jugador> jugadores, int indiceActual) {
        int n = jugadores.size();
        int i = indiceActual;
        do {
            i = (i - 1 + n) % n;
            Jugador anterior = jugadores.get(i);
            if (anterior.isEnJuego()) {
                return anterior.isHaApostado();
            }
        } while (i != indiceActual);
        return false;
    }

    /**
     * Muestra las opciones de jugada según la posición del turno y si el anterior apostó
     * @return 1 si puede apostar, 2 si solo puede retirarse (para tu lógica, puedes adaptarlo)
     */
    public static int opcionesJugada(List<Jugador> jugadores, int indiceActual) {
        mostrarMensaje("¿Qué quieres hacer?");
        mostrarMensaje("1) Apostar");
        if (jugadorAnteriorEnJuegoHaApostado(jugadores, indiceActual)) {
            mostrarMensaje("2) Retirarse");
            return 2;
        } else {
            mostrarMensaje("2) Pasar");
            mostrarMensaje("3) Retirarse");
            return 1;
        }
    }

    public static void cerrarScanner() {
        scanner.close();
    }
}