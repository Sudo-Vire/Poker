package poker.constants;

import java.util.Map;

public class ValorCartaConstants {
    public static final Map<String, Integer> VALOR_A_NUMERICO = Map.ofEntries(
            Map.entry("2", 2),   Map.entry("3", 3),   Map.entry("4", 4),
            Map.entry("5", 5),   Map.entry("6", 6),   Map.entry("7", 7),
            Map.entry("8", 8),   Map.entry("9", 9),   Map.entry("10", 10),
            Map.entry("J", 11),  Map.entry("Q", 12),  Map.entry("K", 13),
            Map.entry("A", 14)
    );

    public static final String[] PALOS = {"Corazones", "Diamantes", "Tréboles", "Picas"};
    public static final String[] VALORES = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

    private ValorCartaConstants() {}
}
