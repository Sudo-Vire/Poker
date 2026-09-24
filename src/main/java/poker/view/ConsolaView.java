package poker.view;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Adaptador de consola usado por la capa de aplicación. Solo se encarga de
 * leer la entrada y renderizar texto; las reglas del juego permanecen en los
 * servicios y controladores.
 */
public class ConsolaView implements AutoCloseable {
    private final Scanner scanner;
    private final PrintStream output;

    private String lineaPendiente;

    public ConsolaView() {
        this(System.in, System.out);
    }

    public ConsolaView(InputStream input, PrintStream output) {
        if (input == null || output == null) {
            throw new IllegalArgumentException("La entrada y la salida no pueden ser nulas");
        }
        this.scanner = new Scanner(input);
        this.output = output;
    }

    public int leerNumero(String mensaje, int minimo, int maximo) {
        int numero;
        do {
            output.print(mensaje);
            while (!scanner.hasNextInt()) {
                output.println("Eso no es un número válido. Inténtalo de nuevo.");
                if (scanner.hasNext()) {
                    scanner.next();
                } else {
                    return minimo;
                }
                output.print(mensaje);
            }
            numero = scanner.nextInt();
            scanner.nextLine();
        } while (numero < minimo || numero > maximo);
        return numero;
    }

    public String leerLinea() {
        if (lineaPendiente != null) {
            String linea = lineaPendiente;
            lineaPendiente = null;
            return linea;
        }
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    public Integer leerNumeroOpcional(String mensaje, int minimo, int maximo) {
        while (true) {
            output.print(mensaje);
            if (!scanner.hasNextLine()) {
                return minimo;
            }
            String linea = scanner.nextLine().trim();
            try {
                int numero = Integer.parseInt(linea);
                if (numero >= minimo && numero <= maximo) {
                    return numero;
                }
                output.println("Introduce un número entre " + minimo + " y " + maximo + ".");
            } catch (NumberFormatException excepcion) {
                lineaPendiente = linea;
                return null;
            }
        }
    }

    public void mostrarMensaje(String mensaje) {
        output.println(mensaje);
    }

    public void mostrarOpcionesApuesta(boolean hayCantidadPorIgualar) {
        if (hayCantidadPorIgualar) {
            mostrarMensaje("Opciones: 1) Igualar  2) Subir  3) All-in  4) Retirarse");
        } else {
            mostrarMensaje("Opciones: 1) Pasar  2) Apostar  3) All-in  4) Retirarse");
        }
    }

    public int solicitarCantidad(String mensaje, int minimo, int maximo) {
        return leerNumero(mensaje, minimo, maximo);
    }

    public void limpiarPantalla() {
        for (int i = 0; i < 10; i++) {
            output.println();
        }
    }

    public boolean preguntarSiContinua() {
        while (true) {
            mostrarMensaje("¿Desean jugar otra mano? (s/n)");
            String respuesta = leerLinea();
            if ("s".equalsIgnoreCase(respuesta)) {
                return true;
            }
            if ("n".equalsIgnoreCase(respuesta)) {
                return false;
            }
            mostrarMensaje("Introduce una opción válida");
        }
    }

    @Override
    public void close() {
        // La vista es propietaria del escáner, por lo que cerrarlo es intencional al finalizar.
        scanner.close();
    }
}
