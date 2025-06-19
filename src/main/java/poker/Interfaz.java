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
                scanner.next(); // Descarta la entrada no válida
                System.out.print(mensaje);
            }
            numero = scanner.nextInt();
            // Limpia el buffer después de leer un int para evitar problemas con .nextLine()
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

    public static int opcionesJugada(List<Jugador> jugadores) {
        System.out.println("¿Qué quieres hacer?");
        System.out.println("1) Apostar");
        if (Jugador.numJugador == 1 || !jugadores.get(Jugador.numJugador - 1).haApostado) {
            System.out.println("2) Pasar");
            System.out.println("3) Retirarse");
            return 1;
        } else {
            System.out.println("2) Retirarse");
            return 2;
        }
    }

    public static void cerrarScanner() {
        scanner.close();
    }
}