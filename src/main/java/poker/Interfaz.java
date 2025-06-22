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

    public static void cerrarScanner() {
        scanner.close();
    }

    public static boolean aspr(Jugador jugador, int cantidadPorIgualar, int[] pozo, int[] apuestas, int indiceJugador, int[] apuestaActual, List<Jugador> jugadores, String fase, int bigBlindAmount) {
        boolean accionValida = false;
        boolean terminarMano = false;

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
        String jugada = Interfaz.leerLinea().toLowerCase();

        if ((jugada.equals("igualar") || jugada.equals("1")) && cantidadPorIgualar > 0) {
            int cantidad = Math.min(jugador.getSaldo(), cantidadPorIgualar);
            jugador.setSaldo(jugador.getSaldo() - cantidad);
            pozo[0] += cantidad;
            apuestas[indiceJugador] += cantidad;
            accionValida = true;
        } else if ((jugada.equals("pasar") || jugada.equals("1")) && cantidadPorIgualar == 0) {
            accionValida = true;
        } else if ((jugada.equals("apostar") || jugada.equals("2")) && cantidadPorIgualar == 0) {
            int minApuesta = (fase.equals("Pre-Flop")) ? bigBlindAmount : 1;
            int cantidad = Interfaz.leerNumero("¿Cuánto quieres apostar? (min " + minApuesta + "): ", minApuesta, jugador.getSaldo());
            jugador.setSaldo(jugador.getSaldo() - cantidad);
            pozo[0] += cantidad;
            apuestas[indiceJugador] += cantidad;
            apuestaActual[0] = cantidad;
            accionValida = true;
        } else if ((jugada.equals("subir") || jugada.equals("2")) && cantidadPorIgualar > 0) {
            int minSubida = (fase.equals("Pre-Flop")) ? Math.max(cantidadPorIgualar + bigBlindAmount, cantidadPorIgualar + 1) : cantidadPorIgualar + 1;
            int cantidad = Interfaz.leerNumero("¿Cuánto quieres subir? (min " + minSubida + "): ", minSubida, jugador.getSaldo());
            jugador.setSaldo(jugador.getSaldo() - cantidad);
            pozo[0] += cantidad;
            apuestas[indiceJugador] += cantidadPorIgualar + (cantidad - cantidadPorIgualar);
            apuestaActual[0] = apuestas[indiceJugador];
            accionValida = true;
        } else if (jugada.equals("retirarse") || jugada.equals("3")) {
            jugador.enJuego = false;
            accionValida = true;

            // Victoria automática si solo queda uno activo
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
            Interfaz.mostrarMensaje("Opción no válida.");
        }

        if (terminarMano) {
            apuestaActual[0] = -999;
        }
        return accionValida;
    }
}