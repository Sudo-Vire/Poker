package poker.service;

import poker.model.Jugador;

public class JugadorService {
    public String formatearMano(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("Jugador no puede ser nulo");
        }

        StringBuilder manoEnLinea = new StringBuilder(jugador.getNombre() + " tiene: ");
        for (var carta : jugador.getMano()) {
            manoEnLinea.append(carta.toString()).append(" ");
        }
        return manoEnLinea.toString();
    }

    public String formatearEstado(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("Jugador no puede ser nulo");
        }

        return jugador.getNombre() + " - Saldo: " + jugador.getSaldo() +
                ", En juego: " + jugador.isEnJuego() +
                ", All-in: " + jugador.isVaAllIn();
    }

    @Deprecated
    public String mostrarMano(Jugador jugador) {
        return formatearMano(jugador);
    }

    @Deprecated
    public String mostrarEstadoJugador(Jugador jugador) {
        return formatearEstado(jugador);
    }

    public void iniciarMano(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("Jugador no puede ser nulo");
        }
        jugador.nuevaMano();
    }
}