import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorAdivinanza {

    private static final int PUERTO = 12345;
    private static final List<ManejadorCliente> clientes = new ArrayList<>();
    private static int numeroSecreto;
    private static boolean juegoTerminado = false;

    public static void main(String[] args) {
        // Generar el número secreto entre 1 y 100
        Random rand = new Random();
        numeroSecreto = rand.nextInt(100) + 1;
        System.out.println("=== SERVIDOR INICIADO ===");
        System.out.println("Puerto de escucha: " + PUERTO);
        System.out.println("[DEBUG] Numero secreto generado: " + numeroSecreto);

        // Pool de hilos para gestionar múltiples clientes concurrentes
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (!juegoTerminado) {
                // Bloqueante: Espera la conexión de un nuevo cliente
                Socket socketCliente = serverSocket.accept();
                
                // Crear y registrar el manejador del cliente
                ManejadorCliente cliente = new ManejadorCliente(socketCliente);
                synchronized (clientes) {
                    clientes.add(cliente);
                }

                // Asignar la atención del cliente a un hilo del pool
                pool.execute(cliente);
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Envía un mensaje a todos los clientes conectados.
     */
    private static void transmitirMensaje(String mensaje, ManejadorCliente remitente) {
        synchronized (clientes) {
            for (ManejadorCliente c : clientes) {
                if (c != remitente) {
                c.enviarMensaje(mensaje);
            }
        }
    }
}

    /**
     * Evalúa el intento enviado por un jugador y gestiona la sincronización.
     */
    private static synchronized void procesarIntento(ManejadorCliente cliente, int intento) {
        if (juegoTerminado) {
            cliente.enviarMensaje("El juego ya ha terminado. ");
            return;
        }

        System.out.println("El jugador " + cliente.getNombre() + " probo con: " + intento);

        if (intento == numeroSecreto) {
            juegoTerminado = true;
            transmitirMensaje(cliente.getNombre() + " HA GANADO!! El numero era " + numeroSecreto + ".", null);
            transmitirMensaje("FIN DEL JUEGO. Gracias por participar.", null);
        } else if (intento < numeroSecreto) {
            cliente.enviarMensaje("Servidor: El numero secreto es MAYOR que " + intento);
            transmitirMensaje(cliente.getNombre() + " probo con " + intento + " (insuficiente).", cliente);
        } else {
            cliente.enviarMensaje("Servidor: El numero secreto es MENOR que " + intento);
            transmitirMensaje(cliente.getNombre() + " probo con " + intento + " (demasiado alto).", cliente);
        }
    }

    /**
     * Elimina a un cliente de la lista activa al desconectarse.
     */
    private static void removerCliente(ManejadorCliente cliente) {
        synchronized (clientes) {
            clientes.remove(cliente);
        }
        transmitirMensaje("El jugador " + cliente.getNombre() + " ha salido del juego.", null);
    }

    // =========================================================================
    // HILO INDEPENDIENTE PARA CADA CLIENTE
    // =========================================================================
    private static class ManejadorCliente implements Runnable {

        private final Socket socket;
        private PrintWriter salida;
        private BufferedReader entrada;
        private String nombre;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        public String getNombre() {
            return nombre;
        }

        @Override
        public void run() {
            try {
                // Configuración de los canales de I/O de la red
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                salida = new PrintWriter(socket.getOutputStream(), true);

                // Pedir el nombre al cliente
                salida.println("Bienvenido a Adivina el Numero. Ingresa tu nombre: ");
                nombre = entrada.readLine();
                
                if (nombre == null || nombre.trim().isEmpty()) {
                    nombre = "Jugador_" + socket.getPort();
                }

                System.out.println("NUEVA CONEXION: " + nombre + " desde " + socket.getInetAddress());
                salida.println("¡Hola " + nombre + "! Intenta adivinar el numero del 1 al 100.");
                transmitirMensaje(nombre + " se ha unido al juego.", this);

                // Bucle de lectura de mensajes del cliente
                String linea;
                while ((linea = entrada.readLine()) != null) {
                    if (juegoTerminado) break;

                    try {
                        int intento = Integer.parseInt(linea.trim());
                        procesarIntento(this, intento);
                    } catch (NumberFormatException e) {
                        salida.println("Por favor ingresa un numero entero valido.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Conexion perdida con " + nombre);
            } finally {
                removerCliente(this);
                cerrarConexion();
            }
        }

        public void enviarMensaje(String mensaje) {
            if (salida != null) {
                salida.println(mensaje);
            }
        }

        private void cerrarConexion() {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexion con " + nombre + ": " + e.getMessage());
            }
        }
    }
}