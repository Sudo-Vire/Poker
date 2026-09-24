package poker;

import org.junit.jupiter.api.Test;
import poker.view.ConsolaView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ConsolaViewTest {

    @Test
    void leerNumeroYLineaValidos_shouldBeTrue() {
        ByteArrayInputStream input = new ByteArrayInputStream(
                "3\nAna\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ConsolaView view = new ConsolaView(input, new PrintStream(output, true, StandardCharsets.UTF_8))) {
            assertEquals(3, view.leerNumero("Número: ", 2, 10));
            assertEquals("Ana", view.leerLinea());
        }
    }

    @Test
    void responderConUnaOpcionInvalidaYDespuesSi_shouldBeTrue() {
        ByteArrayInputStream input = new ByteArrayInputStream(
                "x\ns\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ConsolaView view = new ConsolaView(input, new PrintStream(output, true, StandardCharsets.UTF_8))) {
            assertTrue(view.preguntarSiContinua());
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("opción válida"));
        }
    }
}
