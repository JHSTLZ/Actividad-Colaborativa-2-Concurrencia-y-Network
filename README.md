# Actividad Colaborativa 2: Concurrencia y Network - Juego de Adivinanza

Este repositorio contiene la implementación de un juego de adivinanza multijugador utilizando una arquitectura Cliente-Servidor en Java. El proyecto demuestra el uso práctico de **Sockets** para la comunicación en red y **Concurrencia (Hilos)** para manejar múltiples conexiones simultáneas.

## 🚀 Características y Conceptos Aplicados

*   **Arquitectura Cliente-Servidor:** Comunicación bidireccional mediante `java.net.Socket` y `java.net.ServerSocket`.
*   **Concurrencia en el Servidor:** Utiliza un `ExecutorService` (Pool de hilos) para aceptar y gestionar múltiples clientes al mismo tiempo sin bloquear el hilo principal del servidor.
*   **Concurrencia en el Cliente:** Implementa un hilo secundario (Thread) dedicado exclusivamente a escuchar los mensajes del servidor en tiempo real, permitiendo al hilo principal capturar la entrada del teclado del usuario simultáneamente.
*   **Sincronización:** Uso de bloques `synchronized` en el servidor para gestionar de forma segura la lista compartida de clientes conectados y evaluar el intento de cada jugador sin condiciones de carrera.

## 🛠️ Tecnologías
*   Java (JDK 8 o superior)

## ⚙️ Instrucciones de Compilación y Ejecución

Para probar el proyecto correctamente, es **estrictamente necesario** iniciar primero el servidor y posteriormente los clientes.

### 1. Iniciar el Servidor
1. Abre una terminal y navega hasta el directorio del proyecto.
2. Compila el archivo del servidor:
   ```bash
   javac ServidorAdivinanza.java
Ejecuta el servidor:

Bash
java ServidorAdivinanza
(El servidor mostrará un mensaje indicando que está escuchando en el puerto 12345).

2. Iniciar los Clientes (Jugadores)
Abre nuevas terminales (una por cada jugador que desees conectar al juego).

Compila el archivo del cliente:

Bash
javac ClienteAdivinanza.java
Ejecuta el cliente:

Bash
java ClienteAdivinanza
El sistema solicitará un nombre de usuario y comenzará la interacción con el servidor.

🕹️ Lógica del Juego
Al iniciar, el servidor genera un número secreto aleatorio entre el 1 y el 100.

Los clientes conectados envían sus intentos escribiendo números en la consola.

El servidor evalúa el intento y notifica (hace un broadcast) a todos los clientes si el intento fue "demasiado alto" o "insuficiente", dando pistas sobre si el número secreto es MAYOR o MENOR.

El primer jugador que logre adivinar el número exacto será declarado ganador, y el servidor cerrará el juego para todos los participantes.
