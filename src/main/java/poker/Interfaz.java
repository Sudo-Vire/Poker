package poker;

import java.util.List;
import java.util.Scanner;

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