import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteAdivinanza {

    private static final String HOST = "localhost"; // O la IP del servidor en red local
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        System.out.println("Conectando al servidor en " + HOST + ":" + PUERTO + "...");

        try (Socket socket = new Socket(HOST, PUERTO);
             BufferedReader entradaRed = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salidaRed = new PrintWriter(socket.getOutputStream(), true);
             Scanner consola = new Scanner(System.in)) {

            System.out.println("Conexion establecida con exito.\n");

            // HILO SECUNDARIO: Escucha mensajes continuos desde el servidor
            Thread hiloEscucha = new Thread(() -> {
                try {
                    String mensajeServidor;
                    while ((mensajeServidor = entradaRed.readLine()) != null) {
                        System.out.println(mensajeServidor);
                    }
                } catch (IOException e) {
                    System.out.println("Se ha cerrado la conexion con el servidor.");
                }
            });
            hiloEscucha.start();

            // HILO PRINCIPAL: Captura las entradas del usuario por la consola
            while (socket.isConnected() && !socket.isClosed()) {
                if (consola.hasNextLine()) {
                    String entradaUsuario = consola.nextLine();
                    salidaRed.println(entradaUsuario);
                    
                    if (entradaUsuario.equalsIgnoreCase("salir")) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor: " + e.getMessage());
        }
    }
}