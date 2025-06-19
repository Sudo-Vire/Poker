package poker;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un jugador de Poker.
 */
public class Jugador {
    private final int numJugador;
    private final String nombre;
    private final List<Baraja.Carta> mano;
    private int saldo;
    boolean enJuego;
    private boolean haApostado; // <--- Estado individual de apuesta en la ronda

    public Jugador(int numJugador, String nombre, int saldoInicial) {
        this.numJugador = numJugador;
        this.nombre = nombre;
        this.mano = new ArrayList<>();
        this.saldo = saldoInicial;
        this.enJuego = true;
        this.haApostado = false;
    }

    public void recibirCarta(Baraja.Carta carta) {
        if (carta != null) {
            mano.add(carta);
        } else {
            throw new IllegalArgumentException("La carta no puede ser nula");
        }
    }

    // Muestra la mano del jugador
    public void mostrarMano() {
        StringBuilder manoEnLinea = new StringBuilder(nombre + " tiene: ");
        for (Baraja.Carta carta : mano) {
            manoEnLinea.append(carta.toString()).append(" ");
        }
        Interfaz.mostrarMensaje(manoEnLinea.toString());
    }

    public void ganar(int cantidad) {
        saldo += cantidad;
    }

    // Prepara al jugador para una nueva mano
    public void nuevaMano() {
        mano.clear();
        enJuego = true;
        haApostado = false;
    }

    // Getters y setters
    public int getSaldo() { return saldo; }
    public void setSaldo(int saldo) {
        if (saldo < 0) throw new IllegalArgumentException("El saldo no puede ser negativo");
        this.saldo = saldo;
    }
    public String getNombre() { return nombre; }
    public boolean isEnJuego() { return enJuego; }
    public List<Baraja.Carta> getMano() { return new ArrayList<>(mano); }
    public boolean isHaApostado() { return haApostado; }
    public void setHaApostado(boolean haApostado) { this.haApostado = haApostado; }
}