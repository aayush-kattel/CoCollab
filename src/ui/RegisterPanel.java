package ui;

import components.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import auth.AuthService;

public class RegisterPanel extends JPanel {

    public interface RegisterListener {
        void onGoToLogin();
    }

    private RegisterListener listener;
    private JTextField regFullName;
    private JTextField regEmail;
    private JPasswordField regPassword;

    public RegisterPanel(RegisterListener listener) {
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
        card.setPreferredSize(new Dimension(380, 420));
        card.setMaximumSize(new Dimension(380, 420));
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 30, 36));

        JLabel title = new JLabel("Create account");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(20));

        card.add(fieldLabel("Full Name"));
        regFullName = new JTextField();
        styleField(regFullName);
        card.add(regFullName);
        card.add(Box.createVerticalStrut(14));

        card.add(fieldLabel("Email"));
        regEmail = new JTextField();
        styleField(regEmail);
        card.add(regEmail);
        card.add(Box.createVerticalStrut(14));

        card.add(fieldLabel("Password"));
        regPassword = new JPasswordField();
        styleField(regPassword);
        card.add(regPassword);
        card.add(Box.createVerticalStrut(22));

        StyledButton regBtn = new StyledButton("Create Account");
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        regBtn.addActionListener(e -> {
            String fullName = regFullName.getText().trim();
            String email = regEmail.getText().trim();
            String password = new String(regPassword.getPassword());

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            AuthService authService = new AuthService();
            if (authService.register(fullName, email, password)) {
                JOptionPane.showMessageDialog(this, "Account created, please sign in.");
                if (listener != null) listener.onGoToLogin();
            } else {
                JOptionPane.showMessageDialog(this, "Email already taken.");
            }
        });
        card.add(regBtn);
        card.add(Box.createVerticalStrut(14));

        card.add(loginLink());

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

    private JPanel loginLink() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel text = new JLabel("Already have an account? ");
        text.setFont(Theme.FONT_NORMAL);
        text.setForeground(Theme.TEXT_GRAY);

        JLabel link = new JLabel("Sign In");
        link.setFont(Theme.FONT_LABEL);
        link.setForeground(Theme.TEAL);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (listener != null) listener.onGoToLogin();
            }
        });

        row.add(text);
        row.add(link);
        return row;
    }
}