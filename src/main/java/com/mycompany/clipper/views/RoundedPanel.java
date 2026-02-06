package com.mycompany.clipper.views;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class RoundedPanel {

    // Aplica un borde redondeado con color de fondo y borde
    public static void applyBorderWithColor(JPanel panel, int radius, Color bgColor, Color borderColor) {
        panel.setOpaque(false); // Permitir transparencia
        panel.setBackground(bgColor); // Establecer color de fondo
        panel.setBorder(new RoundedBorder(radius, bgColor, borderColor));
    }

    // Aplica un borde redondeado solo con color de fondo
    public static void applyBorderNoColor(JPanel panel, int radius, Color bgColor) {
        panel.setOpaque(false); // Permitir transparencia
        panel.setBackground(bgColor); // Establecer color de fondo
        panel.setBorder(new RoundedBorder(radius, bgColor, null));
    }

    static class RoundedBorder implements Border {
        private final int radius;
        private final Color bgColor;
        private final Color borderColor;

        public RoundedBorder(int radius, Color bgColor, Color borderColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dibujar el fondo con bordes redondeados
            g2.setColor(bgColor);
            g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);

            // Dibujar el borde si está definido
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}