package com.mycompany.clipper.views;

import com.formdev.flatlaf.FlatClientProperties;
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
    private JScrollPane scrollPane;
    private Consumer<ClipboardEntry> onPasteAction;

    public ClipboardHistoryPanel(Portapapeles controller, Consumer<ClipboardEntry> onPasteAction) {
        this.portapapelesController = controller;
        this.onPasteAction = onPasteAction;
        this.historyList = new ArrayList<>();

        setLayout(new BorderLayout());

        initUI();
        // refreshHistory(); // Removed to avoid double loading and blocking UI on
        // creation
    }

    private void initUI() {
        // Panel contenedor vertical para las "tarjetas"
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.decode("#000000")); // Pure Black Background

        // Envolver en un JScrollPane para permitir desplazamiento
        scrollPane = new JScrollPane(listContainer);
        scrollPane.setSize(getPreferredSize());
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Sin borde extra
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll más suave

        add(createResetPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createResetPanel() {
        JPanel resetPanel = new JPanel();
        resetPanel.setLayout(new BoxLayout(resetPanel, BoxLayout.X_AXIS));
        resetPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        resetPanel.setBackground(Color.decode("#000000"));
        resetPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        JLabel label = new JLabel("Portapapeles");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Inter, SansSerif", Font.BOLD, 14));
        resetPanel.add(label);

        resetPanel.add(Box.createHorizontalGlue());

        JButton resetBtn = new JButton("Limpiar");
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(Color.decode("#0D1117"));
        resetBtn.setFocusPainted(false);
        resetBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        resetBtn.setFont(new Font("Inter, SansSerif", Font.PLAIN, 12));
        resetBtn.addActionListener(e -> {
            portapapelesController.resetPortapapeles();
            refreshHistory();
        });
        resetPanel.add(resetBtn);
        return resetPanel;

    }

    public void setHistoryList(List<ClipboardEntry> list) {
        this.historyList = list;
        updateListUI();
    }

    public void refreshHistory() {
        // This should essentially request an update.
        // For now, to keep local functionality working without big refactor:
        SwingWorker<List<ClipboardEntry>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ClipboardEntry> doInBackground() throws Exception {
                return portapapelesController.selectPortapapeles();
            }

            @Override
            protected void done() {
                try {
                    setHistoryList(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateListUI() {
        listContainer.removeAll();
        for (ClipboardEntry entry : historyList) {
            listContainer.add(new ClipboardItemCard(entry));
            listContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        listContainer.revalidate();
        listContainer.repaint();

        // Fix: Asegurar que el scroll vuelva arriba al actualizar (especialmente al
        // abrir)
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
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

    // Tarjeta de elemento del portapapeles
    private class ClipboardItemCard extends JPanel {
        public ClipboardItemCard(ClipboardEntry entry) {
            setLayout(new BorderLayout(10, 5));

            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            setMinimumSize(new Dimension(Integer.MAX_VALUE, 80));

            RoundedPanel.applyBorderWithColor(this, 15, Color.decode("#0D1117"), Color.decode("#0097b2"));

            // color y tamaño del borde
            BorderFactory.createLineBorder(Color.decode("#0097b2"), 5, true);

            // Padding inside the card
            setBorder(BorderFactory.createCompoundBorder(
                    getBorder(),
                    new EmptyBorder(10, 10, 10, 10)));

            // Set text color for label consistency
            setForeground(Color.WHITE);

            // --- Contenido Central (Texto o Imagen) ---
            if (entry.hasImage()) {
                // Renderizar imagen
                byte[] imgData = entry.getImageData();
                if (imgData != null && imgData.length > 0) {
                    ImageIcon icon = new ImageIcon(imgData);
                    // Escalar si es muy grande
                    Image img = icon.getImage();
                    // Altura fija de 60px para mantener consistencia
                    Image newImg = img.getScaledInstance(-1, 60, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(newImg));
                    imageLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    add(imageLabel, BorderLayout.CENTER);
                }
            } else {
                // Renderizar texto
                JTextArea contentText = new JTextArea(entry.getText());
                contentText.setWrapStyleWord(true);
                contentText.setLineWrap(true);
                contentText.setOpaque(false);
                contentText.setEditable(false);
                contentText.setFocusable(false);
                contentText.setForeground(Color.decode("#C9D1D9")); // Github text color
                add(contentText, BorderLayout.CENTER);
                // Propagar eventos de ratón al panel padre para el click
                contentText.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        dispatchEvent(SwingUtilities.convertMouseEvent(contentText, e, ClipboardItemCard.this));
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        ClipboardItemCard.this.processMouseEvent(
                                SwingUtilities.convertMouseEvent(contentText, e, ClipboardItemCard.this));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        ClipboardItemCard.this.processMouseEvent(
                                SwingUtilities.convertMouseEvent(contentText, e, ClipboardItemCard.this));
                    }
                });
            }

            // --- Panel Lateral (Botones de acción) ---
            JPanel actionsPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            actionsPanel.setOpaque(false);

            JButton pinBtn = createActionButton("♡", "Fijar");
            JButton deleteBtn = createActionButton("🗑️", "Eliminar");
            deleteBtn.addActionListener(e -> deleteEntry(entry));

            pinBtn.setBackground(Color.decode("#0D1117"));
            deleteBtn.setBackground(Color.decode("#0D1117"));
            pinBtn.setForeground(Color.decode("#ffffff"));
            deleteBtn.setForeground(Color.decode("#ffffff"));
            actionsPanel.add(pinBtn);
            actionsPanel.add(deleteBtn);

            add(actionsPanel, BorderLayout.EAST);

            // Evento para "Pegar y Renovar" al hacer click
            MouseAdapter clickListener = new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    refreshHistory();
                    if (onPasteAction != null) {
                        onPasteAction.accept(entry);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    // Hover Look: Lighter background, lighter border
                    RoundedPanel.applyBorderWithColor(ClipboardItemCard.this, 15, Color.decode("#21262D"),
                            Color.decode("#0097b2"));
                    // Re-apply padding since setBorder in applyBorderWithColor overwrites it
                    setBorder(BorderFactory.createCompoundBorder(getBorder(), new EmptyBorder(10, 10, 10, 10)));
                    setForeground(Color.WHITE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // Normal Look
                    RoundedPanel.applyBorderWithColor(ClipboardItemCard.this, 15, Color.decode("#0D1117"),
                            Color.decode("#0097b2"));
                    // Re-apply padding
                    setBorder(BorderFactory.createCompoundBorder(getBorder(), new EmptyBorder(10, 10, 10, 10)));
                    setForeground(Color.WHITE);
                }
            };

            addMouseListener(clickListener);
            // addMouseListener(clickListener); // Removed duplicate call
            // contentText listener moved to creation block
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
