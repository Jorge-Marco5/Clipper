package com.mycompany.clipper;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleInstanceLock {

    // Elegimos un puerto alto y poco común.
    private static final int PORT = 54321;
    private static ServerSocket serverSocket;
    private static JFrame mainFrame;

    /**
     * Verifica si la aplicación ya está corriendo.
     * 
     * @return true si YA existe otra instancia (entonces debemos cerrar esta).
     *         false si somos la primera instancia.
     */
    public static boolean isAlreadyRunning() {
        try {
            // Intentamos abrir el puerto en localhost
            serverSocket = new ServerSocket(PORT, 10, InetAddress.getLoopbackAddress());

            // Iniciamos un hilo para escuchar peticiones de futuras instancias.
            startListener();
            return false;

        } catch (Exception e) {
            // Mandamos la señal para despertar a la otra y retornamos true.
            notifyExistingInstance();
            return true;
        }
    }

    /**
     * Guarda la referencia a la ventana principal para poder restaurarla.
     */
    public static void setMainFrame(JFrame frame) {
        mainFrame = frame;
    }

    // Inicia un hilo para escuchar peticiones de futuras instancias.
    private static void startListener() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String msg = in.readLine();

                    if ("WAKE_UP".equals(msg)) {
                        restoreWindow();
                    }
                    client.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // Envía la señal para despertar a la otra instancia.
    private static void notifyExistingInstance() {
        try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("WAKE_UP");
        } catch (Exception ignored) {
        }
    }

    // Restaura la ventana principal.
    private static void restoreWindow() {
        if (mainFrame == null)
            return;

        SwingUtilities.invokeLater(() -> {
            // Si está minimizada, restaurar estado normal
            int state = mainFrame.getExtendedState();
            state &= ~JFrame.ICONIFIED;
            mainFrame.setExtendedState(state);

            // Traer al frente
            mainFrame.setAlwaysOnTop(true);
            mainFrame.toFront();
            mainFrame.requestFocus();
            mainFrame.setAlwaysOnTop(false);
            mainFrame.repaint();
        });
    }
}