package ui;

import components.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import auth.AuthService;

public class LoginPanel extends JPanel {

    public interface LoginListener {
        void onGoToRegister();
        void onLoginSuccess(String email);
    }

    private LoginListener listener;
    private JTextField loginIdentifier;
    private JPasswordField loginPassword;

    public LoginPanel(LoginListener listener) {
        this.listener = listener;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        JPanel logoArea = new JPanel();
        logoArea.setOpaque(false);
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setBorder(BorderFactory.createEmptyBorder(50, 0, 20, 0));
        logoArea.add(buildLogo());
        add(logoArea, BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(buildCard());
        add(centerWrap, BorderLayout.CENTER);
    }

    private JPanel buildLogo() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        File logoFile = new File("assets/logo_horizontal.png");
        if (logoFile.exists()) {
            ImageIcon icon = new ImageIcon("assets/logo_horizontal.png");
            Image scaled = icon.getImage().getScaledInstance(260, -1, Image.SCALE_SMOOTH);
            JLabel logoImg = new JLabel(new ImageIcon(scaled));
            logoImg.setAlignmentX(Component.CENTER_ALIGNMENT);
            wrap.add(logoImg);
        } else {
            JLabel brand = new JLabel("CoCollab");
            brand.setFont(Theme.FONT_TITLE);
            brand.setForeground(Theme.TEAL);
            brand.setAlignmentX(Component.CENTER_ALIGNMENT);
            wrap.add(brand);
        }

        JLabel tagline = new JLabel("Collaborative Coding Platform");
        tagline.setFont(Theme.FONT_NORMAL);
        tagline.setForeground(Theme.TEXT_GRAY);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrap.add(tagline);

        return wrap;
    }

    private CardPanel buildCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 380));
        card.setMaximumSize(new Dimension(380, 380));
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 30, 36));

        JLabel title = new JLabel("Welcome back");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(20));

        card.add(fieldLabel("Email"));
        loginIdentifier = new JTextField();
        styleField(loginIdentifier);
        card.add(loginIdentifier);
        card.add(Box.createVerticalStrut(14));

        card.add(fieldLabel("Password"));
        loginPassword = new JPasswordField();
        styleField(loginPassword);
        card.add(loginPassword);
        card.add(Box.createVerticalStrut(22));

        StyledButton loginBtn = new StyledButton("Sign In");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginBtn.addActionListener(e -> {
            String email = loginIdentifier.getText().trim();
            String password = new String(loginPassword.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in both fields.");
                return;
            }

            AuthService authService = new AuthService();
            if (authService.login(email, password)) {
                JOptionPane.showMessageDialog(this, "Login successful");
                if (listener != null) listener.onLoginSuccess(email);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password.");
            }
        });
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(14));

        card.add(signUpLink());

        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.TEXT_GRAY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setFont(Theme.FONT_NORMAL);
        field.setForeground(Theme.TEXT);
        field.setBackground(new Color(58, 64, 76));
        field.setCaretColor(Theme.TEXT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private JPanel signUpLink() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel text = new JLabel("Don't have an account? ");
        text.setFont(Theme.FONT_NORMAL);
        text.setForeground(Theme.TEXT_GRAY);

        JLabel link = new JLabel("Sign Up");
        link.setFont(Theme.FONT_LABEL);
        link.setForeground(Theme.TEAL);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (listener != null) listener.onGoToRegister();
            }
        });

        row.add(text);
        row.add(link);
        return row;
    }
}