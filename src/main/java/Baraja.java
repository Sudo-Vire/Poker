import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Baraja {
    private final List<Carta> cartas;
    private int indice;
    public static final Map<String, Integer> VALOR_A_NUMERICO;
    
    static {
        VALOR_A_NUMERICO = Map.ofEntries(Map.entry("2", 2), Map.entry("3", 3), Map.entry("4", 4), Map.entry("5", 5), Map.entry("6", 6), Map.entry("7", 7), Map.entry("8", 8), Map.entry("9", 9), Map.entry("10", 10), Map.entry("J", 11), Map.entry("Q", 12), Map.entry("K", 13), Map.entry("A", 14));
    }
    
    public Baraja() {
        cartas = new ArrayList<>();
        String[] palos = {"Corazones", "Diamantes", "Tréboles", "Picas"};
        for (String palo : palos) {
            String[] valores = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
            for (String valor : valores) {
                cartas.add(new Carta(palo, valor));
            }
        }
        barajar();
    }
    
    private void barajar() {
        Collections.shuffle(cartas);
        indice = 0;
    }
    
    public Carta repartirCarta() {
        if (indice >= cartas.size()) {
            barajar();
        }
        return cartas.get(indice++);
    }

    public static class Carta implements Comparable<Carta> {
        String palo;
        String valor;
        int valorNumerico;
        
        public Carta(String palo, String valor) {
            this.palo = palo;
            this.valor = valor;
            this.valorNumerico = VALOR_A_NUMERICO.get(valor);
        }
        
        @Override
        public String toString() {
            return valor + " de " + palo;
        }

        @Override
        public int compareTo(Carta o) {
            // Comparamos en orden ascendente
            return Integer.compare(this.valorNumerico, o.valorNumerico);
        }
    }
}