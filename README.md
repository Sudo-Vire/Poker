# ♠️ Poker en Java
Este proyecto es una implementación del juego de póker (Texas Hold'em) desarrollada en Java. Está diseñado para ofrecer una experiencia de juego sencilla y educativa, ideal para quienes desean aprender sobre lógica de juegos y programación orientada a objetos.

Está desarrollada en Java con una estructura modular basada en MVC y aplica principios SOLID. El juego incluye reparto de cartas, rondas de apuestas, all-in, retiradas, side pots, showdown y aumento progresivo de las ciegas.

---


## 🎯 Características

- Partidas de Texas Hold'em para 2 a 10 jugadores.
- Baraja estándar de 52 cartas barajada automáticamente.
- Reparto de cartas privadas, flop, turn y river.
- Acciones de pasar, igualar, apostar, subir, all-in y retirarse.
- Side pots y reparto de premios entre varios ganadores.
- Evaluación de manos y resolución de empates.
- Aumento automático de las ciegas cada tres manos.
- Interfaz de línea de comandos.

## 🧱 Estructura del proyecto

La aplicación comienza en `poker.Juego`, que configura las dependencias e inicia `JuegoController`.

```text
src/main/java/poker/
├── Juego.java
├── controller/
│   └── JuegoController.java
├── model/
│   ├── Baraja.java
│   ├── Carta.java
│   └── Jugador.java
├── service/
│   ├── ApuestaService.java
│   ├── BarajaService.java
│   ├── CompararManos.java
│   ├── EvaluarManos.java
│   └── JugadorService.java
├── view/
│   └── ConsolaView.java
└── constants/
    └── ValorCartaConstants.java
```

- `model`: entidades y estado del juego.
- `service`: lógica, reparto, apuestas y evaluación de manos.
- `controller`: coordinación del flujo de la partida.
- `view`: entrada y salida de la consola.
- `constants`: valores fijos de cartas.

---

## 🚀 Cómo ejecutar desde una release

1. Descarga el archivo `.jar` de la release que prefieras (se recomienda usar la última versión)

2. Abre una terminal.

3. Ve al directorio en el que descargaste el archivo `.jar`.

4. Ejecuta el programa: **java -jar poker-~.jar**

> 💡 **Requisitos:** Java 17 o superior.
   ```bash
   java -jar poker-4.11.2.jar
   ```


## 🛠️ Compilar desde el código fuente

Se necesita Java 17 o superior y Maven.

```bash
mvn clean package
java -jar target/poker-4.11.2.jar
```

La carpeta `target/` contiene los archivos generados por Maven, como las clases compiladas, los informes de tests y el archivo `.jar`. No contiene código fuente y puede regenerarse ejecutando la compilación.

## 🎮 Flujo de juego

1. Introduce el número de jugadores y sus nombres.
2. En cada turno, selecciona la acción escribiendo el número o el nombre de la opción.
3. Las cartas comunitarias se muestran durante el flop, turn y river.
4. La mano termina en el showdown o cuando todos los jugadores salvo uno se retiran.
5. Los jugadores sin saldo son eliminados y la partida continúa hasta que queda un ganador.

---

## 📄 Licencia
Este proyecto está licenciado bajo la licencia Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0). Puedes compartir y adaptar el material siempre que se dé crédito y no se use con fines comerciales salvo consentimiento explícito.

---

## 🤝 Colaboraciones

¡Las contribuciones son bienvenidas! Si deseas mejorar el proyecto, puedes:

- Hacer un *fork* del repositorio.
- Crear una nueva rama (`git checkout -b feature-nueva`).
- Realizar tus cambios y hacer *commit* (`git commit -m 'Agrega nueva funcionalidad'`).
- Enviar un *pull request* explicando tus mejoras.

---

## 💼 Uso comercial

Este proyecto se distribuye bajo una licencia **No Comercial**. Si estás interesado en utilizar el código o una parte de él con fines comerciales, por favor contáctame al correo victorreguillo@gmail.com para obtener una licencia especial o permiso explícito.

