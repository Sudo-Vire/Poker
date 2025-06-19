package poker;

import java.util.*;
import java.util.stream.Collectors;

public class EvaluarManos {

    public static class ResultadoEvaluacion {
        public String nombreJugada;
        public List<Baraja.Carta> cartasPrincipales;

        public ResultadoEvaluacion(String nombreJugada, List<Baraja.Carta> cartasPrincipales) {
            this.nombreJugada = nombreJugada;
            this.cartasPrincipales = cartasPrincipales;
        }
    }

    public static ResultadoEvaluacion evaluarManoCompleta(List<Baraja.Carta> manoJugador, List<Baraja.Carta> cartasComunitarias) {
        List<Baraja.Carta> todasLasCartas = new ArrayList<>(manoJugador);
        todasLasCartas.addAll(cartasComunitarias);

        List<List<Baraja.Carta>> combinaciones = generarCombinaciones(todasLasCartas);

        String mejorMano = "";
        int mejorValor = 0;
        List<Baraja.Carta> mejoresPrincipales = null;

        for (List<Baraja.Carta> combinacion : combinaciones) {
            ResultadoEvaluacion resEval = evaluarCombinacion(combinacion);
            String manoActual = resEval.nombreJugada;
            int valorActual = obtenerValorMano(manoActual);

            if (valorActual > mejorValor ||
                    (valorActual == mejorValor && compararDesempate(resEval.cartasPrincipales, mejoresPrincipales) > 0)) {
                mejorValor = valorActual;
                mejorMano = manoActual;
                mejoresPrincipales = new ArrayList<>(resEval.cartasPrincipales);
            }
        }

        return new ResultadoEvaluacion(mejorMano, mejoresPrincipales);
    }

    public static List<List<Baraja.Carta>> generarCombinaciones(List<Baraja.Carta> cartas) {
        List<List<Baraja.Carta>> combinaciones = new ArrayList<>();
        generarCombinacionesRecursivo(cartas, new ArrayList<>(), combinaciones, 0, 5);
        return combinaciones;
    }

    private static void generarCombinacionesRecursivo(List<Baraja.Carta> cartas, List<Baraja.Carta> actual,
                                                      List<List<Baraja.Carta>> combinaciones, int inicio, int longitud) {
        if (longitud == 0) {
            combinaciones.add(new ArrayList<>(actual));
        } else {
            for (int i = inicio; i <= cartas.size() - longitud; i++) {
                actual.add(cartas.get(i));
                generarCombinacionesRecursivo(cartas, actual, combinaciones, i + 1, longitud - 1);
                actual.remove(actual.size() - 1);
            }
        }
    }

    public static ResultadoEvaluacion evaluarCombinacion(List<Baraja.Carta> mano) {
        mano.sort(Comparator.comparingInt(c -> -c.valorNumerico));
        boolean escalera = esEscalera(mano);
        boolean color = esColor(mano);

        if (escalera && color && mano.get(0).valorNumerico == 14) {
            return new ResultadoEvaluacion("Escalera Real", new ArrayList<>(mano));
        }

        if (escalera && color) {
            return new ResultadoEvaluacion("Escalera de Color", new ArrayList<>(mano));
        }

        Map<Integer, List<Baraja.Carta>> grupos = agruparPorValor(mano);

        if (grupos.values().stream().anyMatch(l -> l.size() == 4)) {
            List<Baraja.Carta> cuarteto = grupos.values().stream().filter(l -> l.size() == 4).findFirst().get();
            Baraja.Carta kicker = mano.stream().filter(c -> !cuarteto.contains(c)).findFirst().get();
            List<Baraja.Carta> resultado = new ArrayList<>(cuarteto);
            resultado.add(kicker);
            return new ResultadoEvaluacion("Poker", resultado);
        }

        if (grupos.values().stream().anyMatch(l -> l.size() == 3) &&
                grupos.values().stream().anyMatch(l -> l.size() == 2)) {
            List<Baraja.Carta> trio = grupos.values().stream().filter(l -> l.size() == 3).findFirst().get();
            List<Baraja.Carta> par = grupos.values().stream().filter(l -> l.size() == 2).findFirst().get();
            List<Baraja.Carta> resultado = new ArrayList<>(trio);
            resultado.addAll(par);
            return new ResultadoEvaluacion("Full", resultado);
        }

        if (color) {
            return new ResultadoEvaluacion("Color", new ArrayList<>(mano));
        }

        if (escalera) {
            return new ResultadoEvaluacion("Escalera", new ArrayList<>(mano));
        }

        if (grupos.values().stream().anyMatch(l -> l.size() == 3)) {
            List<Baraja.Carta> trio = grupos.values().stream().filter(l -> l.size() == 3).findFirst().get();
            List<Baraja.Carta> kicker = mano.stream().filter(c -> !trio.contains(c)).limit(2).toList();
            List<Baraja.Carta> resultado = new ArrayList<>(trio);
            resultado.addAll(kicker);
            return new ResultadoEvaluacion("Trío", resultado);
        }

        List<List<Baraja.Carta>> pares = grupos.values().stream()
                .filter(l -> l.size() == 2)
                .sorted((a, b) -> b.get(0).valorNumerico - a.get(0).valorNumerico)
                .toList();

        if (pares.size() >= 2) {
            List<Baraja.Carta> dosPares = new ArrayList<>();
            dosPares.addAll(pares.get(0));
            dosPares.addAll(pares.get(1));
            Baraja.Carta kicker = mano.stream().filter(c -> !dosPares.contains(c)).findFirst().get();
            dosPares.add(kicker);
            return new ResultadoEvaluacion("Doble Par", dosPares);
        }

        if (pares.size() == 1) {
            List<Baraja.Carta> par = pares.get(0);
            List<Baraja.Carta> kicker = mano.stream().filter(c -> !par.contains(c)).limit(3).toList();
            List<Baraja.Carta> resultado = new ArrayList<>(par);
            resultado.addAll(kicker);
            return new ResultadoEvaluacion("Par", resultado);
        }

        return new ResultadoEvaluacion("Carta Alta", new ArrayList<>(mano.subList(0, 5)));
    }

    private static boolean esEscalera(List<Baraja.Carta> mano) {
        List<Integer> valores = mano.stream().map(c -> c.valorNumerico).distinct().sorted(Comparator.reverseOrder()).toList();
        for (int i = 0; i <= valores.size() - 5; i++) {
            boolean consecutivos = true;
            for (int j = 0; j < 4; j++) {
                if (valores.get(i + j) - 1 != valores.get(i + j + 1)) {
                    consecutivos = false;
                    break;
                }
            }
            if (consecutivos) return true;
        }
        // Comprobación especial A-2-3-4-5
        return new HashSet<>(valores).containsAll(Arrays.asList(14, 5, 4, 3, 2));
    }

    private static boolean esColor(List<Baraja.Carta> mano) {
        return mano.stream().collect(Collectors.groupingBy(c -> c.palo)).values().stream().anyMatch(l -> l.size() >= 5);
    }

    private static Map<Integer, List<Baraja.Carta>> agruparPorValor(List<Baraja.Carta> mano) {
        return mano.stream().collect(Collectors.groupingBy(c -> c.valorNumerico));
    }

    static int obtenerValorMano(String nombre) {
        return switch (nombre) {
            case "Escalera Real" -> 10;
            case "Escalera de Color" -> 9;
            case "Poker" -> 8;
            case "Full" -> 7;
            case "Color" -> 6;
            case "Escalera" -> 5;
            case "Trío" -> 4;
            case "Doble Par" -> 3;
            case "Par" -> 2;
            case "Carta Alta" -> 1;
            default -> 0;
        };
    }

    private static int compararDesempate(List<Baraja.Carta> mano1, List<Baraja.Carta> mano2) {
        if (mano1 == null || mano2 == null) return 0;
        for (int i = 0; i < Math.min(mano1.size(), mano2.size()); i++) {
            int diff = mano1.get(i).valorNumerico - mano2.get(i).valorNumerico;
            if (diff != 0) return diff;
        }
        return 0;
    }
}
