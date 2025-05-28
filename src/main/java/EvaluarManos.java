import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvaluarManos {

    public static String evaluarMano(List<Baraja.Carta> manoJugador, List<Baraja.Carta> cartasComunitarias) {
        List<Baraja.Carta> todasLasCartas = new ArrayList<>(manoJugador);
        todasLasCartas.addAll(cartasComunitarias);

        // Generar todas las combinaciones posibles de 5 cartas
        List<List<Baraja.Carta>> combinaciones = generarCombinaciones(todasLasCartas);

        String mejorMano = "";
        int mejorValor = 0;
        List<Baraja.Carta> mejorCombinacion = null;

        for (List<Baraja.Carta> combinacion : combinaciones) {
            String manoActual = evaluarCombinacion(combinacion);
            int valorActual = obtenerValorMano(manoActual);

            if (valorActual > mejorValor) {
                mejorValor = valorActual;
                mejorMano = manoActual;
                mejorCombinacion = new ArrayList<>(combinacion);
            }
        }

        // Añadimos el kicker a la descripción de la mano si es aplicable
        if (mejorCombinacion != null) {
            String kicker = obtenerKicker(mejorCombinacion, mejorMano);
            if (!kicker.isEmpty()) {
                mejorMano += " con kicker " + kicker;
            }
        }

        return mejorMano;
    }

    public static List<List<Baraja.Carta>> generarCombinaciones(List<Baraja.Carta> cartas) {
        List<List<Baraja.Carta>> combinaciones = new ArrayList<>();
        generarCombinacionesRecursivo(cartas, new ArrayList<>(), combinaciones, 0, 5);
        return combinaciones;
    }

    private static void generarCombinacionesRecursivo(List<Baraja.Carta> cartas, List<Baraja.Carta> actual, List<List<Baraja.Carta>> combinaciones, int inicio, int longitud) {
        if (longitud == 0) {
            combinaciones.add(new ArrayList<>(actual));
        } else {
            for (int i = inicio; i <= cartas.size() - longitud; i++) {
                actual.add(cartas.get(i));
                generarCombinacionesRecursivo(cartas, actual, combinaciones, i + 1, longitud - 1);
                actual.remove(actual.size() - 1); // Cambio aquí, porque ArrayList no tiene removeLast()
            }
        }
    }

    public static String evaluarCombinacion(List<Baraja.Carta> combinacion) {
        // Ordenar las cartas de mayor a menor valor
        combinacion.sort(Collections.reverseOrder());

        if (esEscaleraReal(combinacion)) return "Escalera Real";
        if (esEscaleraColor(combinacion)) return "Escalera de Color";
        if (esPoker(combinacion)) return "Póker";
        if (esFullHouse(combinacion)) return "Full House";
        if (esColor(combinacion)) return "Color";
        if (esEscalera(combinacion)) return "Escalera";
        if (esTrio(combinacion)) return "Trío";
        if (esDoblePareja(combinacion)) return "Doble Pareja";
        if (esPareja(combinacion)) return "Pareja";

        return "Carta Alta: " + combinacion.get(0).valor;
    }

    private static boolean esEscaleraReal(List<Baraja.Carta> combinacion) {
        return esEscaleraColor(combinacion) && combinacion.get(0).valor.equals("A");
    }

    private static boolean esEscaleraColor(List<Baraja.Carta> combinacion) {
        return esEscalera(combinacion) && esColor(combinacion);
    }

    private static boolean esPoker(List<Baraja.Carta> combinacion) {
        return contarRepeticiones(combinacion, 4) == 1;
    }

    private static boolean esFullHouse(List<Baraja.Carta> combinacion) {
        return contarRepeticiones(combinacion, 3) == 1 && contarRepeticiones(combinacion, 2) == 1;
    }

    private static boolean esColor(List<Baraja.Carta> combinacion) {
        String palo = combinacion.get(0).palo;
        for (Baraja.Carta carta : combinacion) {
            if (!carta.palo.equals(palo)) {
                return false;
            }
        }
        return true;
    }

    private static boolean esEscalera(List<Baraja.Carta> combinacion) {
        for (int i = 0; i < combinacion.size() - 1; i++) {
            if (combinacion.get(i).valorNumerico - combinacion.get(i + 1).valorNumerico != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean esTrio(List<Baraja.Carta> combinacion) {
        return contarRepeticiones(combinacion, 3) == 1;
    }

    private static boolean esDoblePareja(List<Baraja.Carta> combinacion) {
        return contarRepeticiones(combinacion, 2) == 2;
    }

    private static boolean esPareja(List<Baraja.Carta> combinacion) {
        return contarRepeticiones(combinacion, 2) == 1;
    }

    private static int contarRepeticiones(List<Baraja.Carta> combinacion, int numero) {
        int count = 0;
        for (int i = 0; i < combinacion.size(); i++) {
            int repeticiones = 1;
            for (int j = i + 1; j < combinacion.size(); j++) {
                if (combinacion.get(i).valor.equals(combinacion.get(j).valor)) {
                    repeticiones++;
                }
            }
            if (repeticiones == numero) {
                count++;
                i += numero - 1;
            }
        }
        return count;
    }

    public static int obtenerValorMano(String mano) {
        return switch (mano) {
            case "Escalera Real" -> 10;
            case "Escalera de Color" -> 9;
            case "Póker" -> 8;
            case "Full House" -> 7;
            case "Color" -> 6;
            case "Escalera" -> 5;
            case "Trío" -> 4;
            case "Doble Pareja" -> 3;
            case "Pareja" -> 2;
            default -> 1;
        };
    }

    private static String obtenerKicker(List<Baraja.Carta> combinacion, String mano) {
        if (mano.startsWith("Pareja") || mano.startsWith("Trío") || mano.startsWith("Doble Pareja")) {
            List<Baraja.Carta> kickers = new ArrayList<>();

            List<String> valoresPrincipales = getStrings(mano);

            for (Baraja.Carta carta : combinacion) {
                if (!valoresPrincipales.contains(carta.valor)) {
                    kickers.add(carta);
                }
            }

            kickers.sort(Collections.reverseOrder());
            return kickers.isEmpty() ? "" : kickers.get(0).valor;
        }
        return "";
    }

    private static List<String> getStrings(String mano) {
        String[] partes = mano.split(" ");
        List<String> valoresPrincipales = new ArrayList<>();
        for (String parte : partes) {
            if (!parte.equals("de") && !parte.equals("Carta") && !parte.equals("Alta:") &&
                    !parte.equals("Pareja") && !parte.equals("Trío") && !parte.equals("Doble") &&
                    !parte.equals("Full") && !parte.equals("House") && !parte.equals("Póker") &&
                    !parte.equals("Color") && !parte.equals("Escalera") && !parte.equals("con") && !parte.equals("kicker")) {
                valoresPrincipales.add(parte);
            }
        }
        return valoresPrincipales;
    }
}
