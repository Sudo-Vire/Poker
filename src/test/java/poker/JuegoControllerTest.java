package poker;

import org.junit.jupiter.api.Test;
import poker.controller.JuegoController;
import poker.service.ApuestaService;
import poker.service.BarajaService;
import poker.service.JugadorService;
import poker.view.ConsolaView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JuegoControllerTest {

    @Test
    void jugarUnaManoConDosAllIn_shouldBeTrue() {
        ByteArrayInputStream input = new ByteArrayInputStream(
                "2\nAna\nLuis\n3\n3\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ConsolaView view = new ConsolaView(input, new PrintStream(output))) {
            ApuestaService apuestas = new ApuestaService(
                    10, 20, 3, view, new JugadorService());
            JuegoController controller = new JuegoController(
                    view, new BarajaService(), new JugadorService(), apuestas);

            controller.iniciar();

            String resultado = output.toString(StandardCharsets.UTF_8);
            assertTrue(resultado.contains("es el ganador del torneo"));
            assertTrue(resultado.contains("Cartas usadas:"));
        }
    }
}
