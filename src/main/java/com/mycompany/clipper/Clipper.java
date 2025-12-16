package com.mycompany.clipper;

import javax.swing.SwingUtilities;

//import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.clipper.controllers.Portapapeles;
import com.mycompany.clipper.views.ClipperUI;

import com.mycompany.clipper.controllers.FontLoader;

public class Clipper {

    public static void main(String[] args) {
        System.out.println("\n☕ Versión de Java ejecutándose: " + System.getProperty("java.version"));

        // inicialización de FlatLaf
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Falló al iniciar FlatLaf");
        }

        // carga de fuentes
        FontLoader.getEmojiFont(16);

        // inicialización de portapapeles
        Portapapeles portapapeles = new Portapapeles();
        portapapeles.createTablePortapapeles();

        // inicialización de la UI
        SwingUtilities.invokeLater(() -> {
            new ClipperUI().setVisible(true);
        });
    }
}