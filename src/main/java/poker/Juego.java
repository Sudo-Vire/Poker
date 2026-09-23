package poker;

import poker.controller.JuegoController;
import poker.service.ApuestaService;
import poker.service.BarajaService;
import poker.service.JugadorService;
import poker.view.ConsolaView;

/**
 * Raíz de composición de la aplicación. Aquí se conectan todas las
 * dependencias en tiempo de ejecución y el controlador gestiona el ciclo
 * de vida del juego.
 */
public final class Juego {
    private Juego() {
    }

    public static void main(String[] args) {
        ConsolaView view = new ConsolaView();
        BarajaService barajaService = new BarajaService();
        JugadorService jugadorService = new JugadorService();
        ApuestaService apuestaService =
                new ApuestaService(10, 20, 3, view, jugadorService);
        JuegoController controller =
                new JuegoController(view, barajaService, jugadorService, apuestaService);
        try {
            controller.iniciar();
        } finally {
            view.close();
        }
    }
}
