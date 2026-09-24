package poker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baraja {
    private final List<Carta> cartas;
    private int indice;

    public Baraja() {
        this.cartas = new ArrayList<>();
        this.indice = 0;
    }

    public void barajar() {
        Collections.shuffle(cartas);
        reiniciarIndice();
    }

    public Carta obtenerCarta(int index) {
        return cartas.get(index);
    }

    public void agregarCarta(Carta carta) {
        cartas.add(carta);
    }

    public int getIndice() {
        return indice;
    }

    public void setIndice(int indice) {
        this.indice = indice;
    }

    public int getTamanio() {
        return cartas.size();
    }

    public void reiniciarIndice() {
        indice = 0;
    }
}