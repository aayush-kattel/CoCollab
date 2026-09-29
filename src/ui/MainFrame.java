package ui;

import models.User;
import models.Room;
import components.*;
import auth.AuthService;
import room.CodingRoomPanel;

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
    private User currentUser;

    private LoginPanel loginPanel;
    private HomePanel homePanel;
    private RoomsPanel roomsPanel;
    private LeaderboardPanel leaderboardPanel;
    private ProfilePanel profilePanel;

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
                if (currentUser != null) {
                    new AuthService().markOffline(currentUser.getEmail());
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

        loginPanel = new LoginPanel(new LoginPanel.LoginListener() {
            public void onGoToRegister() {
                rootLayout.show(root, "Register");
            }

            public void onLoginSuccess(User user) {
                currentUser = user;
                onLoggedIn();
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
        contentArea.add(new JPanel(), "Home");
        contentArea.add(new JPanel(), "CodingRoom");

        container.add(contentArea, BorderLayout.CENTER);
    }

    private void onLoggedIn() {
        contentArea.removeAll();

        navbar.setUserName(currentUser.getName());

        homePanel = new HomePanel(
                currentUser,
                () -> switchPanel("Rooms"),
                () -> switchPanel("Leaderboard"),
                () -> switchPanel("Profile")
        );
        roomsPanel = new RoomsPanel(currentUser, this::openCodingRoom);
        leaderboardPanel = new LeaderboardPanel();
        profilePanel = new ProfilePanel(currentUser, this::logout);

        contentArea.add(homePanel, "Home");
        contentArea.add(roomsPanel, "Rooms");
        contentArea.add(leaderboardPanel, "Leaderboard");
        contentArea.add(profilePanel, "Profile");
        contentArea.add(new JPanel(), "CodingRoom");

        contentArea.revalidate();
        contentArea.repaint();
        switchPanel("Home");
    }

    private void logout() {
        final String email = currentUser != null ? currentUser.getEmail() : null;
        if (email != null) {
            new Thread(() -> new AuthService().markOffline(email)).start();
        }
        currentUser = null;
        homePanel = null;
        roomsPanel = null;
        leaderboardPanel = null;
        profilePanel = null;
        contentArea.removeAll();
        contentArea.add(new JPanel(), "Home");
        contentArea.add(new JPanel(), "CodingRoom");
        contentArea.revalidate();
        contentArea.repaint();

        // Back to the login screen
        loginPanel.reset();
        rootLayout.show(root, "Login");
    }

    private void openCodingRoom(Room room) {
        CodingRoomPanel coding = new CodingRoomPanel(currentUser, room, () -> switchPanel("Rooms"));
        contentArea.add(coding, "CodingRoom");
        cardLayout.show(contentArea, "CodingRoom");
    }

    private void switchPanel(String section) {
        if (cardLayout == null || contentArea == null) return;

        if ("Home".equals(section) && homePanel != null) {
            homePanel.refresh();
        } else if ("Profile".equals(section) && profilePanel != null) {
            profilePanel.refresh();
        } else if ("Leaderboard".equals(section) && leaderboardPanel != null) {
            leaderboardPanel.refresh();
        }

        cardLayout.show(contentArea, section);
        if (navbar != null) {
            navbar.setActiveSection(section);
        }
    }
}