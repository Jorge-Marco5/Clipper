package com.mycompany.clipper.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.clipper.controllers.Portapapeles;
import com.mycompany.clipper.models.ClipboardEntry;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ClipperUI extends JFrame {

    private Portapapeles portapapelesController;
    private List<ClipboardEntry> historyList; // Still needed for the monitor logic check
    private String lastCopiedText = "";
    private volatile boolean monitorPaused = false;

    // UI Components
    private ClipboardHistoryPanel clipboardHistoryPanel;
    private EmojiPanel emojiPanel;

    public ClipperUI() {
        portapapelesController = new Portapapeles();
        historyList = new ArrayList<>();

        initUI();

        // Initial load
        refreshData();

        // Start monitor
        startClipboardMonitor();
    }

    private void initUI() {
        setTitle("Clipper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Size similar to Windows side panel
        setSize(400, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        // Cargar el icono desde resources
        try {
            // Usar una ruta relativa absoluta desde el classpath
            URL iconURL = getClass().getResource("/images/logo.png");

            if (iconURL != null) {
                // Usar ImageIO para una carga síncrona y segura
                java.awt.image.BufferedImage icon = javax.imageio.ImageIO.read(iconURL);

                if (icon != null) {
                    // 1. Asignar icono a la ventana
                    setIconImage(icon);

                    // 2. Intentar asignar icono al Dock/Barra de tareas
                    if (java.awt.Taskbar.isTaskbarSupported() &&
                            java.awt.Taskbar.getTaskbar().isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                        try {
                            java.awt.Taskbar.getTaskbar().setIconImage(icon);
                        } catch (UnsupportedOperationException | SecurityException e) {
                            System.err
                                    .println("No se pudo establecer el icono en la barra de tareas: " + e.getMessage());
                        }
                    }
                }
            } else {
                System.err.println("CRÍTICO: No se encontró el icono en /images/logo.png");
            }
        } catch (java.io.IOException e) {
            System.err.println("Error IO al cargar el icono: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error general al establecer icono: " + e.getMessage());
            e.printStackTrace();
        }

        // Main Panel
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(mainContainer);

        // TabbedPane
        JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE,
                FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);

        // --- Tab 1: Clipboard History ---
        // Pass controller and the paste action
        clipboardHistoryPanel = new ClipboardHistoryPanel(portapapelesController, this::performPaste);
        mainTabbedPane.addTab("📋 Historial", clipboardHistoryPanel);

        // --- Tab 2: Emojis ---
        // Pass the paste action
        emojiPanel = new EmojiPanel(this::performPaste);
        mainTabbedPane.addTab("😀 Emojis", emojiPanel);

        mainContainer.add(mainTabbedPane, BorderLayout.CENTER);
    }

    private void refreshData() {
        // Keep local list updated for monitor check duplications
        historyList = portapapelesController.selectPortapapeles();
        // Update UI panel
        if (clipboardHistoryPanel != null) {
            clipboardHistoryPanel.refreshHistory();
        }
    }

    private void startClipboardMonitor() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    checkClipboard();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void checkClipboard() {
        if (monitorPaused)
            return;

        try {
            Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String) contents.getTransferData(DataFlavor.stringFlavor);

                if (text != null && !text.trim().isEmpty() && !text.equals(lastCopiedText)) {
                    // Avoid re-inserting if it matches the top of our known DB history
                    boolean alreadyInDbTop = !historyList.isEmpty() && historyList.get(0).getText().equals(text);

                    if (!text.equals(lastCopiedText) && !alreadyInDbTop) {
                        lastCopiedText = text;
                        portapapelesController.insertPortapapeles(text);
                        // Reload history
                        SwingUtilities.invokeLater(this::refreshData);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore clipboard access errors
        }
    }

    private void performPaste(String text) {
        try {
            // 1. Copy to clipboard
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

            // 2. Minimize window
            setState(Frame.ICONIFIED);

            // 3. Simulate Ctrl+V
            new Thread(() -> {
                try {
                    Thread.sleep(400);
                    Robot robot = new Robot();
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al pegar: " + e.getMessage());
        }
    }
}