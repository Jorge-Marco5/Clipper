package com.mycompany.clipper.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.clipper.controllers.Portapapeles;
import com.mycompany.clipper.models.ClipboardEntry;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ClipperUI extends JFrame {

    private Portapapeles portapapelesController;
    private List<ClipboardEntry> historyList; // Still needed for the monitor logic check
    private String lastCopiedText = "";
    private int lastCopiedImageHash = 0; // Simple hash to avoid immediate duplicates
    private volatile boolean monitorPaused = false;

    // UI Components
    private ClipboardHistoryPanel clipboardHistoryPanel;
    private EmojiPanel emojiPanel;

    public ClipperUI() {
        portapapelesController = new Portapapeles();
        historyList = new ArrayList<>();

        // Registrar esta ventana para que SingleInstanceLock pueda restaurarla
        com.mycompany.clipper.SingleInstanceLock.setMainFrame(this);

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
            URL iconURL = getClass().getResource("/assets/clipper.png");

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
        mainContainer.setBackground(Color.decode("#000000"));
        setContentPane(mainContainer);

        // TabbedPane
        JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE,
                FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);

        // Custom styling for selected tab
        Color accentColor = Color.decode("#0097b2");
        mainTabbedPane.setFont(new Font("Inter, SansSerif", Font.BOLD, 14));
        mainTabbedPane.setBackground(Color.decode("#0D1117"));
        mainTabbedPane.setForeground(Color.decode("#FFFFFF"));
        UIManager.put("TabbedPane.selectedBackground", accentColor);
        UIManager.put("TabbedPane.selectedForeground", Color.decode("#FFFFFF"));
        UIManager.put("TabbedPane.focusColor", accentColor);
        UIManager.put("TabbedPane.hoverColor", Color.decode("#161B22"));
        UIManager.put("TabbedPane.underlineColor", accentColor);

        mainTabbedPane.putClientProperty("TabbedPane.selectedBackground", accentColor);
        mainTabbedPane.putClientProperty("TabbedPane.underlineColor", accentColor);

        // --- Tab 1: Clipboard History ---
        // Pass controller and the paste action
        clipboardHistoryPanel = new ClipboardHistoryPanel(portapapelesController, this::performPaste);
        mainTabbedPane.addTab("📋 Historial", clipboardHistoryPanel);

        // --- Tab 2: Emojis ---
        // Pass the paste action
        emojiPanel = new EmojiPanel(text -> performPaste(new ClipboardEntry(0, text, null)));
        mainTabbedPane.addTab("😀 Emojis", emojiPanel);

        mainContainer.add(mainTabbedPane, BorderLayout.CENTER);
    }

    private void refreshData() {
        new SwingWorker<List<ClipboardEntry>, Void>() {
            @Override
            protected List<ClipboardEntry> doInBackground() throws Exception {
                // Background DB fetch
                return portapapelesController.selectPortapapeles();
            }

            @Override
            protected void done() {
                try {
                    // Update local reference on EDT
                    historyList = get();
                    // Update UI panel directly
                    if (clipboardHistoryPanel != null) {
                        clipboardHistoryPanel.setHistoryList(historyList);
                    }
                } catch (InterruptedException e) {
                    // Ignored
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
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
            if (contents != null) {
                if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String text = (String) contents.getTransferData(DataFlavor.stringFlavor);

                    if (text != null && !text.trim().isEmpty() && !text.equals(lastCopiedText)) {
                        // Avoid re-inserting if it matches the top of our known DB history
                        boolean alreadyInDbTop = !historyList.isEmpty() && historyList.get(0).getText() != null
                                && historyList.get(0).getText().equals(text);

                        if (!text.equals(lastCopiedText) && !alreadyInDbTop) {
                            lastCopiedText = text;
                            portapapelesController.insertPortapapeles(text);
                            // Reload history
                            SwingUtilities.invokeLater(this::refreshData);
                        }
                    }
                } else if (contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    // Image detected
                    try {
                        Image image = (Image) contents.getTransferData(DataFlavor.imageFlavor);
                        BufferedImage bImage = toBufferedImage(image);

                        // Simple hash check
                        int currentHash = System.identityHashCode(image) + bImage.getWidth() + bImage.getHeight();
                        // Note: converting to bytes every time to check exact content is expensive.
                        // For this lightweight app, checking if it's "new" by assuming change of focus
                        // or check simply if latest entry wasn't an image with same size etc (omitted
                        // for now, relying on basic check)

                        // Convert to byte[]
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ImageIO.write(bImage, "png", bos);
                        byte[] data = bos.toByteArray();

                        // Avoid immediate duplicate insertion based on simple hash or last DB check
                        // For simplicity: just insert if list is empty or last entry is NOT an image
                        // (or logic could be better)
                        // This is basic.
                        if (data.length > 0) {
                            if (historyList.isEmpty() || !historyList.get(0).hasImage()) {
                                portapapelesController.insertPortapapeles(data);
                                SwingUtilities.invokeLater(this::refreshData);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore clipboard access errors
        }
    }

    private void performPaste(ClipboardEntry entry) {
        try {
            // 1. Copy to clipboard
            if (entry.hasImage()) {
                byte[] imgData = entry.getImageData();
                if (imgData != null) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imgData);
                    BufferedImage bImage = ImageIO.read(bis);
                    ImageSelection imgSel = new ImageSelection(bImage);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
                }
            } else {
                StringSelection selection = new StringSelection(entry.getText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            }

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

    // Helper for Image monitoring
    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    // Inner class for Image Selection
    public class ImageSelection implements Transferable {
        private Image image;

        public ImageSelection(Image image) {
            this.image = image;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}