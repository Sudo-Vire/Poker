import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompararManos {
    
    public static int compararManos(String mano1, String mano2, List<Baraja.Carta> manoJugador1, List<Baraja.Carta> manoJugador2, List<Baraja.Carta> cartasComunitarias) {
        // Separamos la mano principal del kicker (si existe)
        String[] partesMano1 = mano1.split(" con kicker ");
        String[] partesMano2 = mano2.split(" con kicker ");
        String manoPrincipal1 = partesMano1[0];
        String manoPrincipal2 = partesMano2[0];
        String kicker1 = partesMano1.length > 1 ? partesMano1[1] : "";
        String kicker2 = partesMano2.length > 1 ? partesMano2[1] : "";
        
        // Comparar primero el valor asignado a la mano (por ejemplo, Escalera Real = 10, Carta Alta = 1, etc.)
        int valorMano1 = EvaluarManos.obtenerValorMano(manoPrincipal1);
        int valorMano2 = EvaluarManos.obtenerValorMano(manoPrincipal2);
        if (valorMano1 != valorMano2) {
            return Integer.compare(valorMano1, valorMano2);
        }
        
        // Si ambos tipos de mano tienen el mismo valor, se comparan las combinaciones.
        int comparacionCombinaciones = compararCombinaciones(manoPrincipal1, manoPrincipal2, manoJugador1, manoJugador2, cartasComunitarias);
        if (comparacionCombinaciones != 0) {
            return comparacionCombinaciones;
        }
        
        // Si la comparación de combinaciones quedó empatada, se usa el kicker SI y SOLO SI ambos lo tienen.
        if (!kicker1.isEmpty() && !kicker2.isEmpty()) {
            int valorKicker1 = Baraja.VALOR_A_NUMERICO.get(kicker1);
            int valorKicker2 = Baraja.VALOR_A_NUMERICO.get(kicker2);
            return Integer.compare(valorKicker1, valorKicker2);
        }
        
        // Si no hay diferencia, se considera un empate.
        return 0;
    }

    private static int compararCombinaciones(String manoPrincipal1, String manoPrincipal2, List<Baraja.Carta> manoJugador1, List<Baraja.Carta> manoJugador2, List<Baraja.Carta> cartasComunitarias) {
        List<Baraja.Carta> todasCartas1 = new ArrayList<>(manoJugador1);
        todasCartas1.addAll(cartasComunitarias);
        List<Baraja.Carta> todasCartas2 = new ArrayList<>(manoJugador2);
        todasCartas2.addAll(cartasComunitarias);
        
        // Ordenamos las cartas de mayor a menor según su valor numérico
        todasCartas1.sort(Collections.reverseOrder());
        todasCartas2.sort(Collections.reverseOrder());
        
        List<Integer> valoresMano1;
        List<Integer> valoresMano2;
        
        // Si la mano es de tipo "Carta Alta", se obtienen las 5 mejores cartas de todas.
        if (manoPrincipal1.startsWith("Carta Alta:")) {
            valoresMano1 = obtenerMejores5Cartas(todasCartas1);
            valoresMano2 = obtenerMejores5Cartas(todasCartas2);
        } else {
            // Para otras combinaciones se extraen los valores indicados en la descripción de la mano.
            valoresMano1 = extraerValoresManoPrincipal(manoPrincipal1, todasCartas1);
            valoresMano2 = extraerValoresManoPrincipal(manoPrincipal2, todasCartas2);
        }
        
        // Se comparan los arreglos elemento a elemento (las cartas más altas primero)
        for (int i = 0; i < Math.min(valoresMano1.size(), valoresMano2.size()); i++) {
            int comparacion = Integer.compare(valoresMano1.get(i), valoresMano2.get(i));
            if (comparacion != 0) {
                return comparacion;
            }
        }
        
        // Si todas las comparaciones de cartas son iguales, se procede a comparar el kicker
        return compararKicker(manoPrincipal1, manoPrincipal2, todasCartas1, todasCartas2);
    }

    // Para manos "Carta Alta": se obtienen las 5 cartas con mayor valor disponibles.
    private static List<Integer> obtenerMejores5Cartas(List<Baraja.Carta> cartas) {
        List<Integer> mejores = new ArrayList<>();
        int cantidad = Math.min(5, cartas.size());
        for (int i = 0; i < cantidad; i++) {
            mejores.add(cartas.get(i).valorNumerico);
        }
        // En caso de tener menos de 5 cartas, se completan con ceros.
        while (mejores.size() < 5) {
            mejores.add(0);
        }
        return mejores;
    }

    // Extrae, a partir de la descripción textual de la mano principal, los valores numéricos relevantes.
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
        
        // Se asegura que siempre se comparen 5 valores, completando con ceros si es necesario.
        while (valores.size() < 5) {
            valores.add(0);
        }
        
        return valores;
    }

    // Compara el kicker excluyendo las cartas que ya forman parte de la combinación principal
    private static int compararKicker(String manoPrincipal1, String manoPrincipal2, List<Baraja.Carta> todasCartas1, List<Baraja.Carta> todasCartas2) {
        List<Integer> kickers1 = obtenerKickers(manoPrincipal1, todasCartas1);
        List<Integer> kickers2 = obtenerKickers(manoPrincipal2, todasCartas2);
        
        // Se comparan los kickers elemento a elemento
        for (int i = 0; i < Math.min(kickers1.size(), kickers2.size()); i++) {
            int comparacion = Integer.compare(kickers1.get(i), kickers2.get(i));
            if (comparacion != 0) {
                return comparacion;
            }
        }
        
        // Si todos los kickers son iguales, se considera un empate
        return 0;
    }

    // Obtiene los kickers excluyendo las cartas que ya forman parte de la combinación principal
    private static List<Integer> obtenerKickers(String manoPrincipal, List<Baraja.Carta> todasCartas) {
        List<Integer> kickers = new ArrayList<>();
        List<Integer> valoresPrincipales = extraerValoresManoPrincipal(manoPrincipal, todasCartas);
        
        for (Baraja.Carta carta : todasCartas) {
            // Si la carta no está en los valores principales, es un posible kicker
            if (!valoresPrincipales.contains(carta.valorNumerico)) {
                kickers.add(carta.valorNumerico);
            }
        }
        
        // Ordenamos los kickers de mayor a menor
        kickers.sort(Collections.reverseOrder());
        
        // Solo nos quedamos con el mejor kicker
        if (!kickers.isEmpty()) {
            List<Integer> mejorKicker = new ArrayList<>();
            mejorKicker.add(kickers.getFirst());
            return mejorKicker;
        }
        
        // Si no hay kickers, devolvemos una lista vacía
        return new ArrayList<>();
    }
}