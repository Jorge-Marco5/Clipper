package com.mycompany.clipper.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.clipper.controllers.FontLoader;
import com.mycompany.clipper.controllers.Portapapeles;
import com.mycompany.clipper.models.ClipboardEntry;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ClipboardHistoryPanel extends JPanel {

    private Portapapeles portapapelesController;
    private List<ClipboardEntry> historyList;
    private JPanel listContainer;
    private Consumer<String> onPasteAction;

    public ClipboardHistoryPanel(Portapapeles controller, Consumer<String> onPasteAction) {
        this.portapapelesController = controller;
        this.onPasteAction = onPasteAction;
        this.historyList = new ArrayList<>();

        setLayout(new BorderLayout());

        initUI();
        refreshHistory();
    }

    private void initUI() {
        // Panel contenedor vertical para las "tarjetas"
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(UIManager.getColor("List.background"));

        // Envolver en un JScrollPane para permitir desplazamiento
        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Sin borde extra
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll más suave

        add(createResetPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createResetPanel() {
        JPanel resetPanel = new JPanel();
        resetPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton resetBtn = new JButton("Resetear");
        resetBtn.addActionListener(e -> {
            portapapelesController.resetPortapapeles();
            refreshHistory();
        });
        resetPanel.add(resetBtn);
        return resetPanel;
    }

    public void refreshHistory() {
        historyList = portapapelesController.selectPortapapeles();
        updateListUI();
    }

    private void updateListUI() {
        listContainer.removeAll();
        for (ClipboardEntry entry : historyList) {
            listContainer.add(new ClipboardItemCard(entry));
            listContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    public void deleteEntry(ClipboardEntry entry) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar este elemento?",
                "Eliminar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            portapapelesController.deletePortapapeles(entry.getId());
            refreshHistory();
        }
    }

    // ==========================================
    // Inner Class: ClipboardItemCard
    // ==========================================
    private class ClipboardItemCard extends JPanel {
        public ClipboardItemCard(ClipboardEntry entry) {
            setLayout(new BorderLayout(10, 5));
            putClientProperty(FlatClientProperties.STYLE, "arc: 10");
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            setMinimumSize(new Dimension(Integer.MAX_VALUE, 80));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            Color bg = UIManager.getColor("Component.background");
            final Color themeColor = (bg != null) ? bg : new Color(255, 255, 255);

            setBackground(themeColor.brighter());

            Color borderColor = UIManager.getColor("Component.borderColor");
            if (borderColor == null)
                borderColor = Color.GRAY;

            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor),
                    new EmptyBorder(12, 12, 12, 12)));

            // --- Contenido Central (Texto) ---
            JTextArea contentText = new JTextArea(entry.getText());
            contentText.setWrapStyleWord(true);
            contentText.setLineWrap(true);
            contentText.setOpaque(false);
            contentText.setEditable(false);
            contentText.setFocusable(false);
            add(contentText, BorderLayout.CENTER);

            // --- Panel Lateral (Botones de acción) ---
            JPanel actionsPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            actionsPanel.setOpaque(false);

            JButton pinBtn = createActionButton("♡", "Fijar");
            JButton deleteBtn = createActionButton("🗑️", "Eliminar");
            deleteBtn.addActionListener(e -> deleteEntry(entry));

            actionsPanel.add(pinBtn);
            actionsPanel.add(deleteBtn);

            add(actionsPanel, BorderLayout.EAST);

            // Evento para "Pegar y Renovar" al hacer click
            MouseAdapter clickListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 1. Eliminar silenciosamente de la base de datos
                    portapapelesController.deletePortapapeles(entry.getId());
                    // 2. Refresh UI para que desaparezca
                    refreshHistory();
                    // 3. Ejecutar pegado (callback)
                    if (onPasteAction != null) {
                        onPasteAction.accept(entry.getText());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(themeColor.brighter().brighter());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(themeColor.brighter());
                }
            };

            addMouseListener(clickListener);
            contentText.addMouseListener(clickListener);
        }

        private JButton createActionButton(String emojiText, String tooltip) {
            JButton btn = new JButton(emojiText);
            btn.setToolTipText(tooltip);
            btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            btn.setFont(btn.getFont().deriveFont(14f));
            btn.setMargin(new Insets(2, 2, 2, 2));
            return btn;
        }
    }
}
