package poker;

import java.util.List;
import java.util.Scanner;

// Clase que se encarga de gestionar la interacción con el usuario mediante la consola
public class Interfaz {
    private static final Scanner scanner = new Scanner(System.in); // Scanner único para leer desde consola

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

    public static void limpiarPantalla() {
        for (int i = 0; i < 10; i++) {
            Interfaz.mostrarMensaje("");
        }
    }

    public static void cerrarScanner() {
        scanner.close();
    }

    // Interfaz para apostar subir pasar o retirarse
    public static boolean aspr(Jugador jugador, int cantidadPorIgualar, int[] apuestas, int indiceJugador,
                               int[] apuestaActual, List<Jugador> jugadores, String fase, int bigBlindAmount, Apuesta apuesta) {
        boolean accionValida = false;

        if (cantidadPorIgualar > 0) {
            Interfaz.mostrarMensaje("Opciones: 1) Igualar (" + cantidadPorIgualar + ")  2) Subir  3) All-in  4) Retirarse");
        } else {
            Interfaz.mostrarMensaje("Opciones: 1) Pasar  2) Apostar  3) All-in  4) Retirarse");
        }
        String jugada = Interfaz.leerLinea().toLowerCase();

        if ((jugada.equals("igualar") || jugada.equals("1")) && cantidadPorIgualar > 0) {
            int cantidad = Math.min(jugador.getSaldo(), cantidadPorIgualar);
            jugador.setSaldo(jugador.getSaldo() - cantidad);
            apuestas[indiceJugador] += cantidad;
            apuesta.registrarAporte(jugador, cantidad, jugadores); // side pot
            accionValida = true;

        } else if ((jugada.equals("pasar") || jugada.equals("1")) && cantidadPorIgualar == 0) {
            accionValida = true;

        } else if ((jugada.equals("apostar") || jugada.equals("2")) && cantidadPorIgualar == 0) {
            int minApuesta = (fase.equals("Pre-Flop")) ? bigBlindAmount : 1;
            int maxApuesta = jugador.getSaldo();
            int cantidad = Interfaz.leerNumero("¿Cuánto quieres apostar? (min " + minApuesta + ", max " + maxApuesta + "): ", minApuesta, maxApuesta);
            jugador.setSaldo(jugador.getSaldo() - cantidad);
            apuestas[indiceJugador] += cantidad;
            apuestaActual[0] = cantidad;
            apuesta.registrarAporte(jugador, cantidad, jugadores);
            accionValida = true;

        } else if ((jugada.equals("subir") || jugada.equals("2")) && cantidadPorIgualar > 0) {
            int minExtra = 1;
            int maxExtra = jugador.getSaldo() - cantidadPorIgualar;
            if (maxExtra >= minExtra) {
                int subida = Interfaz.leerNumero("¿Cuántas fichas extra quieres subir? (min " + minExtra + ", max " + maxExtra + "): ", minExtra, maxExtra);
                int totalAPagar = cantidadPorIgualar + subida;
                jugador.setSaldo(jugador.getSaldo() - totalAPagar);
                apuestas[indiceJugador] += totalAPagar;
                apuestaActual[0] = apuestas[indiceJugador];
                apuesta.registrarAporte(jugador, totalAPagar, jugadores);
                accionValida = true;
            }

        } else if (jugada.equals("all-in") || jugada.equals("3")) {
            int cantidad = jugador.getSaldo();
            jugador.setSaldo(0);
            apuestas[indiceJugador] += cantidad;
            jugador.setVaAllIn(true);
            apuesta.registrarAporte(jugador, cantidad, jugadores);
            accionValida = true;

        } else if (jugada.equals("retirarse") || jugada.equals("4")) {
            jugador.enJuego = false;
            accionValida = true;

            int activos = 0;
            Jugador posibleGanador = null;
            for (Jugador j : jugadores) {
                if (j.isEnJuego() && (j.getSaldo() > 0 || j.isVaAllIn())) {
                    activos++;
                    posibleGanador = j;
                }
            }
            if (activos == 1 && !posibleGanador.isVaAllIn()) {
                Interfaz.mostrarMensaje("Todos se han retirado. Gana " + posibleGanador.getNombre());
                for (Apuesta.SidePot pot : apuesta.getPots()) {
                    posibleGanador.ganar(pot.cantidad);
                    pot.cantidad = 0;
                }
                apuestaActual[0] = -999;
            }
        } else {
            Interfaz.mostrarMensaje("Opción no válida.");
        }

        return accionValida;
    }
}