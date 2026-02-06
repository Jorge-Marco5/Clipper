package com.mycompany.clipper;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;
// import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.clipper.controllers.Portapapeles;
import com.mycompany.clipper.views.ClipperUI;

import com.mycompany.clipper.controllers.FontLoader;

public class Clipper {

    public static void main(String[] args) {

        // Verificación de Instancia Única
        if (SingleInstanceLock.isAlreadyRunning()) {
            System.exit(0);
        }

        // 1. Iniciar carga de FlatLaf en paralelo
        java.util.concurrent.CompletableFuture<Void> flatLafFuture = java.util.concurrent.CompletableFuture
                .runAsync(() -> {
                    try {
                        FlatDarkLaf.setup();
                    } catch (Exception ex) {
                        System.err.println("Falló al iniciar FlatLaf");
                    }
                });

        // 2. Iniciar carga de fuentes en paralelo
        java.util.concurrent.CompletableFuture<Void> fontFuture = java.util.concurrent.CompletableFuture
                .runAsync(() -> {
                    FontLoader.getEmojiFont(16);
                });

        // 3. Iniciar DB / Portapapeles en paralelo
        java.util.concurrent.CompletableFuture<Portapapeles> dbFuture = java.util.concurrent.CompletableFuture
                .supplyAsync(() -> {
                    Portapapeles portapapeles = new Portapapeles();
                    portapapeles.createTablePortapapeles();
                    return portapapeles;
                });

        // Esperar a que todo lo CRÍTICO para mostrar la UI esté listo.
        java.util.concurrent.CompletableFuture<Void> allFutures = java.util.concurrent.CompletableFuture
                .allOf(flatLafFuture, fontFuture, dbFuture);

        java.util.concurrent.CompletableFuture<Void> uiLaunchFuture = allFutures.thenAccept(v -> {
            try {
                // inicialización de la UI en el EDT
                SwingUtilities.invokeLater(() -> {
                    new ClipperUI().setVisible(true);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            // Bloqueamos main hasta que inicialización acabe y lance la ventana
            uiLaunchFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}