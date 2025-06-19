package poker;

import java.util.ArrayList;
import java.util.List;

public class Jugador {
    static int numJugador = 0;
    private final String nombre;
    private final List<Baraja.Carta> mano;
    private int saldo;
    boolean enJuego;
    public static boolean haApostado;

    public Jugador(int numJugador, String nombre, int saldoInicial, boolean haApostado) {
        Jugador.numJugador = numJugador;
        this.nombre = nombre;
        this.mano = new ArrayList<>();
        this.setSaldo(saldoInicial);
        this.enJuego = true;
        Jugador.haApostado = haApostado;
    }
    
    public void recibirCarta(Baraja.Carta carta) {
        if (carta != null) {
            mano.add(carta);
        } else {
            throw new IllegalArgumentException("La carta no puede ser nula");
        }
    }

    //muestra la mano cada ronda
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
    
    public void nuevaMano() {
        mano.clear();
        enJuego = true;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        if (saldo < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }
        this.saldo = saldo;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isEnJuego() {
        return enJuego;
    }

    public List<Baraja.Carta> getMano() {
        return new ArrayList<>(mano);
    }
}