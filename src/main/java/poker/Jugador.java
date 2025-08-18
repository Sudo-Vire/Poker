package poker;

import java.util.ArrayList;
import java.util.List;

public class Jugador {
    private final String nombre;                       // Nombre del jugador
    private final List<Baraja.Carta> mano;             // Las cartas en la mano del jugador
    private int saldo;                                 // Saldo actual
    boolean enJuego;                                   // En true mientras está en la mano, false solo si se retira
    private boolean haApostado;                        // Indica si ya ha apostado en la ronda
    private boolean vaAllIn;                           // Indica si el jugador está haciendo All-In

    // Setter de jugador que importa algunos valores e inicializa otros
    public Jugador(String nombre, int saldoInicial) {
        this.nombre = nombre;
        this.mano = new ArrayList<>();
        this.saldo = saldoInicial;
        this.enJuego = true;
        this.haApostado = false;
        this.vaAllIn = false;
    }

    // Agrega una carta a la mano del jugador.
    public void recibirCarta(Baraja.Carta carta) {
        if (carta != null) {
            mano.add(carta);
        } else {
            throw new IllegalArgumentException("La carta no puede ser nula");
        }
    }

    // Muestra la mano del jugador en consola
    public void mostrarMano() {
        StringBuilder manoEnLinea = new StringBuilder(nombre + " tiene: ");
        for (Baraja.Carta carta : mano) {
            manoEnLinea.append(carta.toString()).append(" ");
        }
        Interfaz.mostrarMensaje(manoEnLinea.toString());
    }

    // Incrementa el saldo del jugador al ganar el pozo.
    public void ganar(int cantidad) {
        saldo += cantidad;
    }

    // Inicializa el estado de la mano para el siguiente deal (NUNCA marca enJuego=false por saldo).
    public void nuevaMano() {
        mano.clear();
        enJuego = true;
        haApostado = false;
        vaAllIn = false;
    }

    // Getters y Setters varios
    public int getSaldo() { return saldo; }
    public void setSaldo(int saldo) {
        if (saldo < 0) throw new IllegalArgumentException("El saldo no puede ser negativo");
        this.saldo = saldo;
        if (saldo == 0 && enJuego) {
            vaAllIn = true;   // --- Si el saldo llega a 0 estando en juego, el jugador entra en estado ALL-IN ---
        }
    }
    public String getNombre() { return nombre; }
    public boolean isEnJuego() { return enJuego; }
    public List<Baraja.Carta> getMano() { return new ArrayList<>(mano); }
    public boolean isHaApostado() { return haApostado; }
    public void setHaApostado(boolean haApostado) { this.haApostado = haApostado; }
    public boolean isVaAllIn() { return vaAllIn; }
    public void setVaAllIn(boolean vaAllIn) { this.vaAllIn = vaAllIn; }
}