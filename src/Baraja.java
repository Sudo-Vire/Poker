package poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Baraja {
    private List<Carta> cartas;
    private int indice;
    private String[] palos = {"Corazones", "Diamantes", "Tréboles", "Picas"};
    private String[] valores = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    public static final Map<String, Integer> VALOR_A_NUMERICO;
    
    static {
        Map<String, Integer> valorMap = new HashMap<>();
        valorMap.put("2", 2);
        valorMap.put("3", 3);
        valorMap.put("4", 4);
        valorMap.put("5", 5);
        valorMap.put("6", 6);
        valorMap.put("7", 7);
        valorMap.put("8", 8);
        valorMap.put("9", 9);
        valorMap.put("10", 10);
        valorMap.put("J", 11);
        valorMap.put("Q", 12);
        valorMap.put("K", 13);
        valorMap.put("A", 14);
        VALOR_A_NUMERICO = Collections.unmodifiableMap(valorMap);
    }
    
    public Baraja() {
        cartas = new ArrayList<>();
        for (String palo : palos) {
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
    
    public void reiniciarBaraja() {
        barajar();
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