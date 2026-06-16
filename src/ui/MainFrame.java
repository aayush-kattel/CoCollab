package ui;

import components.*;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentArea;
    private CardLayout cardLayout;
    private Navbar navbar;

    public MainFrame() {
        setTitle("CoCollab — Collaborative Coding Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setPreferredSize(new Dimension(1280, 800));
        setBackground(Theme.BG_DARK);
        getContentPane().setBackground(Theme.BG_DARK);
        build();
        pack();
        setLocationRelativeTo(null);
    }

    private void build() {
        setLayout(new BorderLayout());

        navbar = new Navbar(this::switchPanel);
        add(navbar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG_DARK);

        // Add Home panel
        contentArea.add(new HomePanel(), "Home");

        // Add placeholder panels for other sections
        contentArea.add(createPlaceholderPanel("Rooms", "🏠", "Rooms section coming soon"), "Rooms");
        contentArea.add(createPlaceholderPanel("Leaderboard", "🏆", "Leaderboard section coming soon"), "Leaderboard");
        contentArea.add(createPlaceholderPanel("Profile", "👤", "Profile section coming soon"), "Profile");

        add(contentArea, BorderLayout.CENTER);
        cardLayout.show(contentArea, "Home");
    }

    private JPanel createPlaceholderPanel(String title, String icon, String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JPanel card = new CardPanel(Theme.BG_CARD, 16, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));
        card.setMaximumSize(new Dimension(400, 250));

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 56));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(16));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_H2);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));

        // Message
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(Theme.FONT_BODY);
        msgLabel.setForeground(Theme.TEXT_SECONDARY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(msgLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(card, gbc);

        return panel;
    }

    private void switchPanel(String section) {
        cardLayout.show(contentArea, section);
        navbar.setActiveSection(section);
    }

    public void navigateTo(String section) {
        switchPanel(section);
    }
}