package poker;

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

    public static void cerrarScanner() {
        scanner.close();
    }
}