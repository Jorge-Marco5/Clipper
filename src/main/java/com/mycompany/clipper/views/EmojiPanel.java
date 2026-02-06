package com.mycompany.clipper.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.clipper.controllers.FontLoader;
import com.mycompany.clipper.controllers.emojiTabsController;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class EmojiPanel extends JPanel {

    private Consumer<String> onEmojiSelected;

    public EmojiPanel(Consumer<String> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        JTabbedPane emojiTabs = new JTabbedPane(JTabbedPane.BOTTOM);
        emojiTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE,
                FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);

        emojiTabsController emojiController = new emojiTabsController();
        emojiTabs.setFont(new Font("Inter, SansSerif", Font.BOLD, 14));

        Map<String, List<emojiTabsController.Emoji>> groupedEmojis = emojiController.getEmojisByGroup();

        for (Map.Entry<String, List<emojiTabsController.Emoji>> entry : groupedEmojis.entrySet()) {
            String groupName = entry.getKey();
            List<emojiTabsController.Emoji> emojiList = entry.getValue();

            // Use the full name as tab title
            String tabTitle = groupName;
            emojiTabs.addTab(tabTitle, createEmojiGrid(emojiList));
        }

        add(emojiTabs, BorderLayout.CENTER);
    }

    private JScrollPane createEmojiGrid(List<emojiTabsController.Emoji> emojis) {
        JPanel gridPanel = new JPanel();
        gridPanel.setBackground(Color.decode("#000000")); // Pure Black for consistency
        int columns = 6;
        gridPanel.setLayout(new GridLayout(0, columns, 2, 2));
        gridPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        for (emojiTabsController.Emoji emoji : emojis) {
            String htmlEmoji = "<html><span style='font-size:20px; display: flex; align-items: center; text-align: center;'>"
                    + emoji.getCharacter() + "</span></html>";
            JButton emojiBtn = new JButton(htmlEmoji);
            emojiBtn.setForeground(Color.WHITE);

            emojiBtn.setFont(FontLoader.getEmojiFont(24));

            emojiBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE,
                    FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            Dimension squareSize = new Dimension(50, 50);
            emojiBtn.setPreferredSize(squareSize);
            emojiBtn.setMinimumSize(squareSize);

            emojiBtn.setToolTipText(emoji.getName());

            emojiBtn.addActionListener(e -> {
                if (onEmojiSelected != null) {
                    onEmojiSelected.accept(emoji.getCharacter());
                }
            });
            gridPanel.add(emojiBtn);
        }

        int emptySlots = columns - (emojis.size() % columns);
        if (emptySlots < columns) {
            for (int i = 0; i < emptySlots; i++) {
                gridPanel.add(new JPanel());
            }
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
}
