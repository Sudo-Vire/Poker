package poker;

import java.util.List;
import java.util.Scanner;

// Clase que se encarga de gestionar la interacción con el usuario mediante la consola
public class Interfaz {
    private static final Scanner scanner = new Scanner(System.in); // Scanner único para leer desde consola

    // Método para leer un número entero entre min y max, mostrando un mensaje y validando la entrada
    public static int leerNumero(String mensaje, int min, int max) {
        int numero;
        do {
            System.out.print(mensaje);                                                    // Muestra el mensaje al usuario
            while (!scanner.hasNextInt()) {                                               // Valida que la entrada sea un número
                System.out.println("Eso no es un número válido. Inténtalo de nuevo.");
                scanner.next();                                                           // Descarta lo ingresado y vuelve a pedir el número
                System.out.print(mensaje);
            }
            numero = scanner.nextInt();                                                   // Lee el número
            scanner.nextLine();                                                           // Limpia el buffer
        } while (numero < min || numero > max);                                           // Si no está en el rango, repite
        return numero;
    }

    // Lee una línea completa de texto
    public static String leerLinea() {
        return scanner.nextLine();
    }

    // Muestra un mensaje en la consola
    public static void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    // Limpia la pantalla de la consola con 10 saltos de linea
    public static void limpiarPantalla() {
        for (int i = 0; i < 10; i++) {
            Interfaz.mostrarMensaje("");
        }
    }

    // Cierra el Scanner
    public static void cerrarScanner() {
        scanner.close();
    }

    /**
     * aspr: método para solicitar la acción del jugador durante su turno de apuestas.
     * Retorna true si el jugador hace una acción válida y, si corresponde, modifica el pozo, apuestas y estado del jugador.
     *
     * @param jugador            El jugador en turno
     * @param cantidadPorIgualar Cuánto debería poner el jugador para igualar la apuesta actual
     * @param pozo               Array de un solo elemento con el total del pozo global
     * @param apuestas           Array con lo que ha aportado cada jugador en la ronda actual
     * @param indiceJugador      Índice del jugador en la lista
     * @param apuestaActual      Array de una sola posición con el valor de la apuesta actual
     * @param jugadores          Lista de todos los jugadores
     * @param fase               Fase de la mano ("Pre-Flop", "Flop", "Turn", "River")
     * @param bigBlindAmount     Cantidad actual de la ciega grande
     * @return true si la acción fue válida
     */
    public static boolean aspr(Jugador jugador, int cantidadPorIgualar, int[] pozo, int[] apuestas, int indiceJugador, int[] apuestaActual, List<Jugador> jugadores, String fase, int bigBlindAmount) {
        boolean accionValida = false; // Indica si la acción debe terminar el ciclo de petición al jugador
        boolean terminarMano = false; // Marca si la mano termina de golpe por retirada de todos los demás

        // Opciones ofrecidas dependiendo de si hay apuesta que igualar o no
        if (cantidadPorIgualar > 0) {
            Interfaz.mostrarMensaje("Opciones:");
            Interfaz.mostrarMensaje("1) Igualar (" + cantidadPorIgualar + ")");
            Interfaz.mostrarMensaje("2) Subir");
            Interfaz.mostrarMensaje("3) Retirarse");
        } else {
            Interfaz.mostrarMensaje("Opciones:");
            Interfaz.mostrarMensaje("1) Pasar");
            Interfaz.mostrarMensaje("2) Apostar");
            Interfaz.mostrarMensaje("3) Retirarse");
        }
        String jugada = Interfaz.leerLinea().toLowerCase(); // Lee la opción elegida

        // Lógica para cada una de las acciones que puede tomar el jugador en su turno
        if ((jugada.equals("igualar") || jugada.equals("1")) && cantidadPorIgualar > 0) {
            // Igualar la apuesta: se resta del saldo y suma al pozo; se suma también al aporte
            int cantidad = Math.min(jugador.getSaldo(), cantidadPorIgualar);
            jugador.setSaldo(jugador.getSaldo() - cantidad);
            pozo[0] += cantidad;
            apuestas[indiceJugador] += cantidad;
            accionValida = true;
        } else if ((jugada.equals("pasar") || jugada.equals("1")) && cantidadPorIgualar == 0) {
            // Pasar si nadie ha apostado aún
            accionValida = true;
        } else if ((jugada.equals("apostar") || jugada.equals("2")) && cantidadPorIgualar == 0) {
            // Hacer apuesta inicial en la ronda
            int minApuesta = (fase.equals("Pre-Flop")) ? bigBlindAmount : 1;
            int cantidad = Interfaz.leerNumero("¿Cuánto quieres apostar? (min " + minApuesta + "): ", minApuesta, jugador.getSaldo());
            jugador.setSaldo(jugador.getSaldo() - cantidad);
            pozo[0] += cantidad;
            apuestas[indiceJugador] += cantidad;
            apuestaActual[0] = cantidad; // Marca la nueva apuesta actual a igualar
            accionValida = true;
        } else if ((jugada.equals("subir") || jugada.equals("2")) && cantidadPorIgualar > 0) {
            // Subir la apuesta
            int minSubida = (fase.equals("Pre-Flop")) ? Math.max(cantidadPorIgualar + bigBlindAmount, cantidadPorIgualar + 1) : cantidadPorIgualar + 1;
            int cantidad = Interfaz.leerNumero("¿Cuánto quieres subir? (min " + minSubida + "): ", minSubida, jugador.getSaldo());
            jugador.setSaldo(jugador.getSaldo() - cantidad);
            pozo[0] += cantidad;
            apuestas[indiceJugador] += cantidadPorIgualar + (cantidad - cantidadPorIgualar);
            apuestaActual[0] = apuestas[indiceJugador];
            accionValida = true;
        } else if (jugada.equals("retirarse") || jugada.equals("3")) {
            // El jugador se retira de la mano: ya no participa hasta la siguiente ronda
            jugador.enJuego = false;
            accionValida = true;

            // Si tras retirarse solo queda uno activo, ese jugador gana automáticamente el pozo
            int jugadoresActivosRestantes = 0;
            int indiceRestante = -1;
            int n = jugadores.size();
            for (int x = 0; x < n; x++) {
                if (jugadores.get(x).isEnJuego() && jugadores.get(x).getSaldo() > 0) {
                    jugadoresActivosRestantes++;
                    indiceRestante = x;
                }
            }
            if (jugadoresActivosRestantes == 1) {
                Jugador ganador = jugadores.get(indiceRestante);
                Interfaz.mostrarMensaje("Todos los demás jugadores se han retirado.");
                Interfaz.mostrarMensaje(ganador.getNombre() + " gana la mano y se lleva " + pozo[0] + " fichas.");
                ganador.ganar(pozo[0]);
                pozo[0] = 0;
                terminarMano = true;
            }
        } else {
            // Si no reconoce la opción, vuelve a preguntar
            Interfaz.mostrarMensaje("Opción no válida.");
        }

        // Si la mano termina por retiro, pone código -999 en apuestaActual[0] para detener la ronda
        if (terminarMano) {
            apuestaActual[0] = -999;
        }
        return accionValida;
    }
}