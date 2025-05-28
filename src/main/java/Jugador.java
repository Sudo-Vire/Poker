import java.util.ArrayList;
import java.util.List;

public class Jugador {
    private final String nombre;
    private final List<Baraja.Carta> mano;
    private int saldo;
    private boolean enJuego;

    public Jugador(String nombre, int saldoInicial) {
        this.nombre = nombre;
        this.mano = new ArrayList<>();
        this.setSaldo(saldoInicial);
        this.enJuego = true;
    }
    
    public void recibirCarta(Baraja.Carta carta) {
        if (carta != null) {
            mano.add(carta);
        } else {
            throw new IllegalArgumentException("La carta no puede ser nula");
        }
    }
    
    public void mostrarMano() {
        StringBuilder manoEnLinea = new StringBuilder(nombre + " tiene: ");
        for (Baraja.Carta carta : mano) {
            manoEnLinea.append(carta.toString()).append(" ");
        }
        Interfaz.mostrarMensaje(manoEnLinea.toString());
    }

    public void retirarse() {
        enJuego = false;
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