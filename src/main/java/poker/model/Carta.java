package poker.model;

import poker.constants.ValorCartaConstants;

public class Carta implements Comparable<Carta> {
    private String palo;
    private String valor;
    private int valorNumerico;

    public Carta(String palo, String valor) {
        this.palo = palo;
        this.valor = valor;
        this.valorNumerico = ValorCartaConstants.VALOR_A_NUMERICO.get(valor);
    }

    public String getPalo() {
        return palo;
    }

    public String getValor() {
        return valor;
    }

    public int getValorNumerico() {
        return valorNumerico;
    }

    @Override
    public String toString() {
        return valor + " de " + palo;
    }

    @Override
    public int compareTo(Carta o) {
        return Integer.compare(this.valorNumerico, o.valorNumerico);
    }
}