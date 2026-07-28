package ui;
import components.*;
import auth.AuthService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private JPanel root;
    private CardLayout rootLayout;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private Navbar navbar;
    private String currentUserEmail;

    public MainFrame() {
        setTitle("CoCollab - Collaborative Coding Platform");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setPreferredSize(new Dimension(1200, 750));
        getContentPane().setBackground(Theme.BG);
        build();
        attachShutdownHook();
        pack();
        setLocationRelativeTo(null);
    }

    private void attachShutdownHook() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (currentUserEmail != null) {
                    new AuthService().markOffline(currentUserEmail);
                }
                dispose();
                System.exit(0);
            }
        });
    }

    private void build() {
        rootLayout = new CardLayout();
        root = new JPanel(rootLayout);
        root.setBackground(Theme.BG);
        LoginPanel loginPanel = new LoginPanel(new LoginPanel.LoginListener() {
            public void onGoToRegister() {
                rootLayout.show(root, "Register");
            }
            public void onLoginSuccess(String email) {
                currentUserEmail = email;
                rootLayout.show(root, "App");
            }
        });
        root.add(loginPanel, "Login");
        RegisterPanel registerPanel = new RegisterPanel(() -> rootLayout.show(root, "Login"));
        root.add(registerPanel, "Register");
        JPanel appWrapper = new JPanel(new BorderLayout());
        appWrapper.setBackground(Theme.BG);
        buildApp(appWrapper);
        root.add(appWrapper, "App");
        setContentPane(root);
        rootLayout.show(root, "Login");
    }

    private void buildApp(JPanel container) {
        navbar = new Navbar(this::switchPanel);
        container.add(navbar, BorderLayout.NORTH);
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG);
        contentArea.add(new HomePanel(), "Home");
        contentArea.add(placeholderPanel("Rooms", "Rooms section coming soon"), "Rooms");
        contentArea.add(placeholderPanel("Leaderboard", "Leaderboard section coming soon"), "Leaderboard");
        contentArea.add(placeholderPanel("Profile", "Profile section coming soon"), "Profile");
        container.add(contentArea, BorderLayout.CENTER);
        cardLayout.show(contentArea, "Home");
    }

    private JPanel placeholderPanel(String title, String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG);
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(50, 70, 50, 70));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(Theme.FONT_NORMAL);
        msgLabel.setForeground(Theme.TEXT_GRAY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(msgLabel);
        panel.add(card);
        return panel;
    }

    private void switchPanel(String section) {
        cardLayout.show(contentArea, section);
        navbar.setActiveSection(section);
    }
}