package poker.service;

import poker.model.Baraja;
import poker.model.Carta;
import poker.constants.ValorCartaConstants;
import java.util.ArrayList;
import java.util.List;

public class BarajaService {

    // Crea una baraja estándar de 52 cartas barajada
    public Baraja crearBarajaCompleta() {
        Baraja baraja = new Baraja();

        for (String palo : ValorCartaConstants.PALOS) {
            for (String valor : ValorCartaConstants.VALORES) {
                baraja.agregarCarta(new Carta(palo, valor));
            }
        }

        barajar(baraja);
        return baraja;
    }

    // Barajar la baraja
    public void barajar(Baraja baraja) {
        if (baraja != null) {
            baraja.barajar();
        }
    }

    // Reparte una carta. Si la baraja se agota, vuelve a barajar automáticamente
    public Carta repartirCarta(Baraja baraja) {
        if (baraja.getIndice() >= baraja.getTamanio()) {
            barajar(baraja);
        }
        Carta carta = baraja.obtenerCarta(baraja.getIndice());
        baraja.setIndice(baraja.getIndice() + 1);
        return carta;
    }

    // Reparte N cartas
    public List<Carta> repartirCartas(Baraja baraja, int cantidad) {
        List<Carta> cartasRepartidas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            cartasRepartidas.add(repartirCarta(baraja));
        }
        return cartasRepartidas;
    }
}