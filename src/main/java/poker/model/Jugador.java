package poker.model;

import java.util.ArrayList;
import java.util.List;

public class Jugador {
    private final String nombre;
    private final List<Carta> mano;
    private int saldo;
    public boolean enJuego;
    private boolean vaAllIn;

    public Jugador(String nombre, int saldoInicial) {
        this.nombre = nombre;
        this.mano = new ArrayList<>();
        this.saldo = saldoInicial;
        this.enJuego = true;
        this.vaAllIn = false;
    }

    // Agrega una carta a la mano del jugador
    public void recibirCarta(Carta carta) {
        if (carta != null) {
            mano.add(carta);
        } else {
            throw new IllegalArgumentException("La carta no puede ser nula");
        }
    }

    // Incrementa el saldo del jugador al ganar el pozo
    public void ganar(int cantidad) {
        saldo += cantidad;
    }

    // Inicializa el estado de la mano para el siguiente deal
    public void nuevaMano() {
        mano.clear();
        enJuego = true;
        vaAllIn = false;
    }

    // Getters y Setters
    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        if (saldo < 0) throw new IllegalArgumentException("El saldo no puede ser negativo");
        this.saldo = saldo;
        if (saldo == 0 && enJuego) {
            vaAllIn = true;
        }
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isEnJuego() {
        return enJuego;
    }

    public void setEnJuego(boolean enJuego) {
        this.enJuego = enJuego;
    }

    public List<Carta> getMano() {
        return new ArrayList<>(mano);
    }

    public boolean isVaAllIn() {
        return vaAllIn;
    }

    public void setVaAllIn(boolean vaAllIn) {
        this.vaAllIn = vaAllIn;
    }
}