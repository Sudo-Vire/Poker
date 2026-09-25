package poker;

import poker.model.Carta;

import java.util.*;

public class PokerBot {

    public enum Posicion { CIEGA_PEQUENA, CIEGA_GRANDE, TEMPRANA, MEDIA, TARDIA }

    // Atributos del bot
    private String nombre;
    private double banca;
    private final List<Carta> cartasOcultas;
    private final List<Carta> cartasComunitarias;
    private double tasaVictorias;
    private Map<String, Double> estadisticasPorPosicion;
    private Random aleatorio;

    // Constructor
    public PokerBot(String nombre, double bancaInicial) {
        this.nombre = nombre;
        this.banca = bancaInicial;
        this.cartasOcultas = new ArrayList<>();
        this.cartasComunitarias = new ArrayList<>();
        this.tasaVictorias = 0.55; // 55% tasa de victoria inicial
        this.aleatorio = new Random();
        this.estadisticasPorPosicion = inicializarEstadisticas();
    }

    public PokerBot() {
        this("Máquina", 1000);
    }

    // Inicializa estadísticas por posición
    private Map<String, Double> inicializarEstadisticas() {
        Map<String, Double> estadisticas = new HashMap<>();
        estadisticas.put("TEMPRANA", 0.15);        // 15% rango de apertura
        estadisticas.put("MEDIA", 0.25);           // 25% rango de apertura
        estadisticas.put("TARDÍA", 0.40);          // 40% rango de apertura
        estadisticas.put("CIEGA_PEQUENA", 0.35);
        estadisticas.put("CIEGA_GRANDE", 0.30);
        return estadisticas;
    }

    /**
     * Determina la acción del bot basada en múltiples factores
     */
    public String tomarDecision(double apuestaActual, double tamanoPote,
                                Posicion posicion, int jugadoresRestantes) {

        double fuerzaMano = evaluarFuerzaMano();
        double oddsRecibidas = apuestaActual / (tamanoPote + apuestaActual);
        double oddsImplicitas = calcularOddsImplicitas(tamanoPote, apuestaActual);
        double bonificacionPosicion = obtenerBonificacionPosicion(posicion);

        // Análisis de decisión
        if (debeHacerFold(fuerzaMano, oddsRecibidas, posicion)) {
            return "FOLD";
        } else if (debeHacerSubida(fuerzaMano, oddsRecibidas, bonificacionPosicion, jugadoresRestantes)) {
            double montoSubida = calcularMontoSubida(apuestaActual, tamanoPote);
            return "SUBIDA:" + montoSubida;
        } else if (debeHacerIgualdad(fuerzaMano, oddsRecibidas, oddsImplicitas)) {
            return "IGUALDAD";
        } else {
            return "PASAR";
        }
    }

    /**
     * Evalúa la fuerza relativa de la mano actual
     */
    private double evaluarFuerzaMano() {
        if (cartasOcultas.isEmpty()) return 0;

        double fuerzaMano = 0;

        // Evalúa pares
        if (esPareja()) {
            fuerzaMano = 0.7;
        }
        // Evalúa conectores de alto valor
        else if (esConectado() && tieneCartasAltas()) {
            fuerzaMano = 0.65;
        }
        // Evalúa cartas altas
        else if (tieneCartasAltas()) {
            fuerzaMano = 0.55;
        }
        // Evalúa cartas medias
        else if (tieneCartasMedias()) {
            fuerzaMano = 0.45;
        }
        // Cartas bajas
        else {
            fuerzaMano = 0.30;
        }

        // Ajusta según el número de cartas comunitarias
        if (!cartasComunitarias.isEmpty()) {
            fuerzaMano += evaluarConexionTablero() * 0.1;
        }

        return Math.min(fuerzaMano, 1.0);
    }

    /**
     * Verifica si la mano es un par
     */
    private boolean esPareja() {
        return cartasOcultas.size() >= 2 &&
                cartasOcultas.get(0).getValor().equals(cartasOcultas.get(1).getValor());
    }

    /**
     * Verifica si son cartas conectadas
     */
    private boolean esConectado() {
        if (cartasOcultas.size() < 2) return false;
        int diferencia = Math.abs(cartasOcultas.get(0).getValorNumerico() -
                cartasOcultas.get(1).getValorNumerico());
        return diferencia == 1;
    }

    /**
     * Verifica si hay cartas altas (Sota o mejor)
     */
    private boolean tieneCartasAltas() {
        return cartasOcultas.stream()
                .allMatch(c -> c.getValorNumerico() >= 11);
    }

    /**
     * Verifica si hay cartas medias (7-10)
     */
    private boolean tieneCartasMedias() {
        return cartasOcultas.stream()
                .allMatch(c -> c.getValorNumerico() >= 7 &&
                        c.getValorNumerico() <= 10);
    }

    /**
     * Evalúa cuánto conecta la mano con el tablero
     */
    private double evaluarConexionTablero() {
        double conexion = 0;
        for (Carta oculta : cartasOcultas) {
            for (Carta comunitaria : cartasComunitarias) {
                if (oculta.getValor().equals(comunitaria.getValor())
                        || oculta.getPalo().equals(comunitaria.getPalo())) {
                    conexion += 0.1;
                }
            }
        }
        return conexion;
    }

    /**
     * Calcula las probabilidades implícitas
     */
    private double calcularOddsImplicitas(double tamanoPote, double apuesta) {
        if (apuesta == 0) return 1.0;
        return (tamanoPote + apuesta) / apuesta;
    }

    /**
     * Obtiene el bonificador por posición
     */
    private double obtenerBonificacionPosicion(Posicion posicion) {
        return switch (posicion) {
            case TARDIA, CIEGA_PEQUENA -> 1.15;
            case MEDIA -> 1.05;
            case TEMPRANA, CIEGA_GRANDE -> 0.95;
        };
    }

    /**
     * Determina si debe hacer fold
     */
    private boolean debeHacerFold(double fuerzaMano, double oddsRecibidas, Posicion posicion) {
        double umbral = 0.35 * obtenerBonificacionPosicion(posicion);
        return fuerzaMano < umbral && oddsRecibidas < 2.0;
    }

    /**
     * Determina si debe hacer raise
     */
    private boolean debeHacerSubida(double fuerzaMano, double oddsRecibidas,
                                    double bonificacionPosicion, int jugadoresRestantes) {
        double umbral = 0.70 * bonificacionPosicion;
        return fuerzaMano > umbral &&
                jugadoresRestantes <= 4 &&
                oddsRecibidas < 3.0;
    }

    /**
     * Determina si debe hacer call
     */
    private boolean debeHacerIgualdad(double fuerzaMano, double oddsRecibidas, double oddsImplicitas) {
        return fuerzaMano > 0.40 &&
                (oddsRecibidas < 2.0 || oddsImplicitas > 3.0);
    }

    /**
     * Calcula el monto del raise
     */
    private double calcularMontoSubida(double apuestaActual, double tamanoPote) {
        return apuestaActual * (1.5 + aleatorio.nextDouble() * 1.0);
    }

    /**
     * Actualiza el historial de manos y estadísticas
     */
    public void actualizarEstadisticas(boolean gano, double monto, Posicion posicion) {
        if (gano) {
            banca += monto;
        } else {
            banca -= monto;
        }

        // Actualiza tasa de victorias con media móvil
        tasaVictorias = tasaVictorias * 0.95 + (gano ? 1.0 : 0.0) * 0.05;
    }

    /**
     * Recibe cartas de hoyo (cartas ocultas)
     */
    public void recibirCartasOcultas(Carta carta1, Carta carta2) {
        cartasOcultas.clear();
        cartasOcultas.add(carta1);
        cartasOcultas.add(carta2);
    }

    /**
     * Recibe cartas comunitarias (flop, turn, river)
     */
    public void agregarCartaComunitaria(Carta carta) {
        cartasComunitarias.add(carta);
    }

    /**
     * Reinicia el estado para una nueva mano
     */
    public void reiniciarMano() {
        cartasOcultas.clear();
        cartasComunitarias.clear();
    }

    // Getters
    public String getNombre() { return nombre; }
    public double getBanca() { return banca; }
    public double getTasaVictorias() { return tasaVictorias; }
    public List<Carta> getCartasOcultas() { return new ArrayList<>(cartasOcultas); }

    @Override
    public String toString() {
        return String.format("%s - Banca: $%.2f, Tasa de Victorias: %.1f%%",
                nombre, banca, tasaVictorias * 100);
    }
}