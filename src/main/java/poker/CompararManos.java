package poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// Clase encargada de comparar dos manos de póker para determinar cuál gana
public class CompararManos {

    // Función principal para determinar el ganador entre dos manos
    // Retorna >0 si mano1 gana, <0 si mano2 gana, 0 si empate
    public static int compararManos(
            String mano1, String mano2,
            List<Baraja.Carta> manoJugador1,
            List<Baraja.Carta> manoJugador2,
            List<Baraja.Carta> cartasComunitarias
    ) {
        // Separa la jugada principal y el kicker si existe
        String[] partesMano1 = mano1.split(" con kicker ");
        String[] partesMano2 = mano2.split(" con kicker ");
        String manoPrincipal1 = partesMano1[0];
        String manoPrincipal2 = partesMano2[0];
        String kicker1 = partesMano1.length > 1 ? partesMano1[1] : "";
        String kicker2 = partesMano2.length > 1 ? partesMano2[1] : "";

        // Jerarquía de la jugada
        int valorMano1 = EvaluarManos.obtenerValorMano(manoPrincipal1);
        int valorMano2 = EvaluarManos.obtenerValorMano(manoPrincipal2);
        if (valorMano1 != valorMano2) {
            return Integer.compare(valorMano1, valorMano2);
        }

        // Si la jerarquía es igual, desempata comparando los valores de las cartas principales
        int comparacionCombinaciones = compararCombinaciones(
                manoPrincipal1, manoPrincipal2,
                manoJugador1, manoJugador2, cartasComunitarias
        );
        if (comparacionCombinaciones != 0) {
            return comparacionCombinaciones;
        }

        // Si hay kicker explícito, compara los valores del kicker
        if (!kicker1.isEmpty() && !kicker2.isEmpty()) {
            int valorKicker1 = Baraja.VALOR_A_NUMERICO.get(kicker1);
            int valorKicker2 = Baraja.VALOR_A_NUMERICO.get(kicker2);
            return Integer.compare(valorKicker1, valorKicker2);
        }
        return 0;
    }

    // Compara las combinaciones cuando ambas manos son de la misma jerarquía
    private static int compararCombinaciones(
            String manoPrincipal1, String manoPrincipal2,
            List<Baraja.Carta> manoJugador1,
            List<Baraja.Carta> manoJugador2,
            List<Baraja.Carta> cartasComunitarias
    ) {
        List<Baraja.Carta> todasCartas1 = new ArrayList<>(manoJugador1);
        todasCartas1.addAll(cartasComunitarias);
        List<Baraja.Carta> todasCartas2 = new ArrayList<>(manoJugador2);
        todasCartas2.addAll(cartasComunitarias);

        // Ordena de mayor a menor valor para comparar desde la carta más fuerte
        todasCartas1.sort(Collections.reverseOrder());
        todasCartas2.sort(Collections.reverseOrder());

        List<Integer> valoresMano1;
        List<Integer> valoresMano2;

        /*
        Si es "Carta Alta", compara las 5 mejores
        Para jugadas como par, trío, etc., extrae los valores relevantes de la jugada principal
        */
        if (manoPrincipal1.startsWith("Carta Alta:")) {
            valoresMano1 = obtenerMejores5Cartas(todasCartas1);
            valoresMano2 = obtenerMejores5Cartas(todasCartas2);
        } else {
            valoresMano1 = extraerValoresManoPrincipal(manoPrincipal1, todasCartas1);
            valoresMano2 = extraerValoresManoPrincipal(manoPrincipal2, todasCartas2);
        }

        for (int i = 0; i < Math.min(valoresMano1.size(), valoresMano2.size()); i++) {
            int comparacion = Integer.compare(valoresMano1.get(i), valoresMano2.get(i));
            if (comparacion != 0) {
                return comparacion;
            }
        }

        // Si siguen iguales, compara los kickers (cartas de desempate)
        return compararKicker(manoPrincipal1, manoPrincipal2, todasCartas1, todasCartas2);
    }

    // Extrae las cinco mejores cartas de una lista ordenada
    private static List<Integer> obtenerMejores5Cartas(List<Baraja.Carta> cartas) {
        List<Integer> mejores = new ArrayList<>();
        int cantidad = Math.min(5, cartas.size());
        for (int i = 0; i < cantidad; i++) {
            mejores.add(cartas.get(i).valorNumerico);
        }
        // Completa si faltan cartas
        while (mejores.size() < 5) {
            mejores.add(0);
        }
        return mejores;
    }

    // Extrae los valores de la jugada principal para compararlas correctamente
    private static List<Integer> extraerValoresManoPrincipal(String manoPrincipal, List<Baraja.Carta> todasCartas) {
        List<Integer> valores = new ArrayList<>();
        String[] partes = manoPrincipal.split(" ");

        for (String parte : partes) {
            if (!parte.equals("de") && !parte.equals("Carta") && !parte.equals("Alta:") &&
                    !parte.equals("Pareja") && !parte.equals("Trío") && !parte.equals("Doble") &&
                    !parte.equals("Full") && !parte.equals("House") && !parte.equals("Póker") &&
                    !parte.equals("Color") && !parte.equals("Escalera")) {

                for (Baraja.Carta carta : todasCartas) {
                    if (carta.valor.equals(parte)) {
                        valores.add(carta.valorNumerico);
                        break;
                    }
                }
            }
        }
        // Completa con ceros para llegar a 5 valores
        while (valores.size() < 5) {
            valores.add(0);
        }

        return valores;
    }

    // Compara el kicker
    private static int compararKicker(String manoPrincipal1, String manoPrincipal2, List<Baraja.Carta> todasCartas1, List<Baraja.Carta> todasCartas2) {
        List<Integer> kickers1 = obtenerKickers(manoPrincipal1, todasCartas1);
        List<Integer> kickers2 = obtenerKickers(manoPrincipal2, todasCartas2);

        for (int i = 0; i < Math.min(kickers1.size(), kickers2.size()); i++) {
            int comparacion = Integer.compare(kickers1.get(i), kickers2.get(i));
            if (comparacion != 0) {
                return comparacion;
            }
        }

        return 0;
    }

    // Obtiene el kicker de cada jugador
    private static List<Integer> obtenerKickers(String manoPrincipal, List<Baraja.Carta> todasCartas) {
        List<Integer> kickers = new ArrayList<>();
        List<Integer> valoresPrincipales = extraerValoresManoPrincipal(manoPrincipal, todasCartas);

        for (Baraja.Carta carta : todasCartas) {
            if (!valoresPrincipales.contains(carta.valorNumerico)) {
                kickers.add(carta.valorNumerico);
            }
        }

        kickers.sort(Collections.reverseOrder());

        if (!kickers.isEmpty()) {
            LinkedList<Integer> linkedListKickers = new LinkedList<>(kickers);
            List<Integer> mejorKicker = new ArrayList<>();
            mejorKicker.add(linkedListKickers.getFirst());
            return mejorKicker;
        }

        return new ArrayList<>();
    }
}