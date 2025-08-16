package poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Clase que representa la baraja y todas las interacciones con las cartas
public class Baraja {
    private final List<Carta> cartas;                               // Lista de todas las cartas en la baraja
    private int indice;                                             // Índice de la próxima carta a repartir
    public static final Map<String, Integer> VALOR_A_NUMERICO;      // Mapa para convertir valores de carta tipo String a int

    // Bloque estático: inicializa el mapa estático de valores numéricos de las cartas
    static {
        VALOR_A_NUMERICO = Map.ofEntries(
                Map.entry("2", 2),  Map.entry("3", 3),  Map.entry("4", 4), Map.entry("5", 5),
                Map.entry("6", 6),  Map.entry("7", 7),  Map.entry("8", 8), Map.entry("9", 9),
                Map.entry("10", 10), Map.entry("J", 11), Map.entry("Q", 12), Map.entry("K", 13), Map.entry("A", 14)
        );
    }

    // Constructor: crea y baraja una baraja estándar de 52 cartas
    public Baraja() {
        cartas = new ArrayList<>();
        String[] palos = {"Corazones", "Diamantes", "Tréboles", "Picas"};
        for (String palo : palos) {
            String[] valores = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
            for (String valor : valores) {
                cartas.add(new Carta(palo, valor)); // Cada combinación de palo y valor se convierte en una carta
            }
        }
        barajar();
    }

    // Método privado para barajar las cartas y reiniciar el índice de jugador
    private void barajar() {
        Collections.shuffle(cartas);
        indice = 0;
    }

    // Reparte una carta. Si la baraja se agota, vuelve a barajar automáticamente
    public Carta repartirCarta() {
        if (indice >= cartas.size()) {
            barajar();
        }
        return cartas.get(indice++);
    }

    // Clase interna que representa una carta individual de la baraja
    public static class Carta implements Comparable<Carta> {
        String palo;              // El palo (corazones, tréboles, etc.)
        String valor;             // El valor ("2", "J", "Q",...)
        int valorNumerico;        // Valor numérico para comparar ("J"=11, "Q"=12...)

        // Constructor: establece palo, valor y determina el valor numérico a partir del mapa
        public Carta(String palo, String valor) {
            this.palo = palo;
            this.valor = valor;
            this.valorNumerico = VALOR_A_NUMERICO.get(valor);
        }

        // Representación textual de la carta
        @Override
        public String toString() {
            return valor + " de " + palo;
        }

        // Permite comparar dos cartas para ordenarlas por valorNumérico (de menor a mayor)
        @Override
        public int compareTo(Carta o) {
            return Integer.compare(this.valorNumerico, o.valorNumerico);
        }
    }
}