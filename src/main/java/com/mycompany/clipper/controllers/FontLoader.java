package com.mycompany.clipper.controllers;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class FontLoader {

    public static Font textfont = null;
    public static Font emojiFont = null;

    public static Font getEmojiFont(float size) {
        if (emojiFont != null) {
            return emojiFont.deriveFont(size);
        }

        try {
            InputStream is = FontLoader.class.getResourceAsStream("/fonts/NotoColorEmoji.ttf");
            if (is == null) {
                System.err.println("❌ No se encontró el archivo .ttf");
                return new Font(Font.SANS_SERIF, Font.PLAIN, (int) size);
            }

            // Crear la fuente desde el archivo
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);

            // Intentar registrarla (esto ayuda a Swing a mezclarla con texto)
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            boolean registered = ge.registerFont(font);

            System.out.println("✅ Fuente cargada: " + font.getFontName());
            // Si registered es false, NO importa, porque usaremos el objeto 'font'
            // directamente

            emojiFont = font;
            return emojiFont.deriveFont(size);

        } catch (Exception e) {
            e.printStackTrace();
            return new Font(Font.SANS_SERIF, Font.PLAIN, (int) size);
        }
    }
}