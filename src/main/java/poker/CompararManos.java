package poker;

import java.util.List;

// Clase encargada de comparar dos manos de póker para determinar cuál gana
public class CompararManos {

    /* Versión simplificada para comparar manos: comparación por valor de carta
    * Ahora no extrae
    * */

    public static int compararManos2(
            List<Baraja.Carta> cartasPrincipales1,
            String nombreJugada1,
            List<Baraja.Carta> cartasPrincipales2,
            String nombreJugada2
    ) {
        int valorMano1 = EvaluarManos.obtenerValorMano(nombreJugada1);
        int valorMano2 = EvaluarManos.obtenerValorMano(nombreJugada2);
        if (valorMano1 != valorMano2) return Integer.compare(valorMano1, valorMano2);

        // Compara de mayor a menor cada carta usada en la jugada principal
        for (int i = 0; i < Math.min(cartasPrincipales1.size(), cartasPrincipales2.size()); i++) {
            int diff = cartasPrincipales1.get(i).valorNumerico - cartasPrincipales2.get(i).valorNumerico;
            if (diff != 0) return diff;
        }
        return 0; // Empate total
    }
}