package ui;

import components.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginPanel extends JPanel {

    public interface LoginListener {
        void onGuestLogin();
    }

    private LoginListener listener;
    private CardLayout formLayout;
    private JPanel formStack;

    // Toggle buttons
    private JPanel toggleBar;
    private JLabel loginToggle;
    private JLabel registerToggle;
    private boolean showingLogin = true;

    // Login fields
    private JTextField loginIdentifier;
    private JPasswordField loginPassword;

    // Register fields
    private JTextField regUsername;
    private JTextField regEmail;
    private JPasswordField regPassword;

    public LoginPanel(LoginListener listener) {
        this.listener = listener;
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        build();
    }

    // ─────────────────────────────────────────
    //  Root
    // ─────────────────────────────────────────
    private void build() {
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                GradientPaint gp = new GradientPaint(0, 0, new Color(8, 10, 18),
                        getWidth(), getHeight(), new Color(14, 18, 34));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Dot grid
                g2.setColor(new Color(99, 102, 241, 16));
                int s = 28;
                for (int x = 0; x < getWidth(); x += s)
                    for (int y = 0; y < getHeight(); y += s)
                        g2.fillOval(x - 1, y - 1, 2, 2);

                // Glow blobs
                RadialGradientPaint b1 = new RadialGradientPaint(new Point(getWidth() / 3, getHeight() / 3),
                        320, new float[]{0f, 1f},
                        new Color[]{new Color(99, 102, 241, 28), new Color(99, 102, 241, 0)});
                g2.setPaint(b1);
                g2.fillOval(getWidth() / 3 - 320, getHeight() / 3 - 320, 640, 640);

                RadialGradientPaint b2 = new RadialGradientPaint(new Point(getWidth() * 2 / 3, getHeight() * 2 / 3),
                        260, new float[]{0f, 1f},
                        new Color[]{new Color(168, 85, 247, 22), new Color(168, 85, 247, 0)});
                g2.setPaint(b2);
                g2.fillOval(getWidth() * 2 / 3 - 260, getHeight() * 2 / 3 - 260, 520, 520);

                g2.dispose();
            }
        };
        bg.setOpaque(false);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        // Brand header
        JPanel brand = buildBrand();
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(brand);
        col.add(Box.createVerticalStrut(32));

        // Card
        JPanel card = buildCard();
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(card);

        bg.add(col, new GridBagConstraints());
        add(bg, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────
    //  Brand
    // ─────────────────────────────────────────
    private JPanel buildBrand() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        logoRow.setOpaque(false);

        // Hex logo
        JPanel hex = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = 22, cy = 22, r = 18;
                int[] xs = new int[6], ys = new int[6];
                for (int i = 0; i < 6; i++) {
                    xs[i] = (int)(cx + r * Math.cos(Math.PI / 6 + i * Math.PI / 3));
                    ys[i] = (int)(cy + r * Math.sin(Math.PI / 6 + i * Math.PI / 3));
                }
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT, 44, 44, Theme.PURPLE);
                g2.setPaint(gp);
                g2.fillPolygon(xs, ys, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                String t = "CC";
                g2.drawString(t, cx - fm.stringWidth(t) / 2, cy + fm.getAscent() / 2 - 1);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(44, 44); }
        };
        hex.setOpaque(false);

        JLabel brandTxt = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Font f = new Font("Segoe UI", Font.BOLD, 30);
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.drawString("Co", 0, fm.getAscent());
                g2.setColor(Theme.ACCENT_BRIGHT);
                g2.drawString("Collab", fm.stringWidth("Co"), fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(new Font("Segoe UI", Font.BOLD, 30));
                return new Dimension(fm.stringWidth("CoCollab") + 4, fm.getHeight());
            }
        };

        logoRow.add(hex);
        logoRow.add(brandTxt);
        p.add(logoRow);
        p.add(Box.createVerticalStrut(8));

        JLabel tagline = new JLabel("Collaborative Coding Platform");
        tagline.setFont(Theme.FONT_BODY);
        tagline.setForeground(Theme.TEXT_SECONDARY);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(tagline);

        return p;
    }

    // ─────────────────────────────────────────
    //  Main card
    // ─────────────────────────────────────────
    private JPanel buildCard() {
        CardPanel card = new CardPanel(Theme.BG_CARD, 20, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 36, 40));
        card.setMaximumSize(new Dimension(440, 2000));

        // Toggle switcher
        JPanel toggle = buildToggle();
        toggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(toggle);
        card.add(Box.createVerticalStrut(32));

        // Form switcher (CardLayout)
        formLayout = new CardLayout();
        formStack = new JPanel(formLayout) {
            @Override public Dimension getPreferredSize() { return new Dimension(360, 380); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
        };
        formStack.setOpaque(false);
        formStack.setAlignmentX(Component.CENTER_ALIGNMENT);
        formStack.add(buildLoginForm(), "Login");
        formStack.add(buildRegisterForm(), "Register");
        formLayout.show(formStack, "Login");

        card.add(formStack);

        return card;
    }

    // ─────────────────────────────────────────
    //  Toggle bar
    // ─────────────────────────────────────────
    private JPanel buildToggle() {
        // Pill container
        JPanel pill = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Pill background
                g2.setColor(new Color(13, 16, 28));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                // Border
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
                // Active tab highlight
                int tabW = getWidth() / 2;
                int activeX = showingLogin ? 0 : tabW;
                g2.setColor(Theme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(activeX + 3, 3, tabW - 6, getHeight() - 6, 10, 10));
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(360, 44); }
        };
        pill.setOpaque(false);

        loginToggle = new JLabel("Login", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                setForeground(showingLogin ? Color.WHITE : Theme.TEXT_SECONDARY);
                super.paintComponent(g);
            }
        };
        loginToggle.setFont(Theme.FONT_BOLD);
        loginToggle.setBounds(0, 0, 180, 44);
        loginToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginToggle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTo(true, pill); }
        });

        registerToggle = new JLabel("Register", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                setForeground(!showingLogin ? Color.WHITE : Theme.TEXT_SECONDARY);
                super.paintComponent(g);
            }
        };
        registerToggle.setFont(Theme.FONT_BOLD);
        registerToggle.setBounds(180, 0, 180, 44);
        registerToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerToggle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTo(false, pill); }
        });

        pill.add(loginToggle);
        pill.add(registerToggle);

        return pill;
    }

    private void switchTo(boolean login, JPanel pill) {
        showingLogin = login;
        formLayout.show(formStack, login ? "Login" : "Register");
        pill.repaint();
        loginToggle.repaint();
        registerToggle.repaint();
    }

    // ─────────────────────────────────────────
    //  Login form
    // ─────────────────────────────────────────
    private JPanel buildLoginForm() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(buildLabel("Email or Username"));
        p.add(Box.createVerticalStrut(7));
        loginIdentifier = buildTextField("Enter your email or username");
        loginIdentifier.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(loginIdentifier);
        p.add(Box.createVerticalStrut(18));

        p.add(buildLabel("Password"));
        p.add(Box.createVerticalStrut(7));
        loginPassword = buildPasswordField("Enter your password");
        loginPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(loginPassword);
        p.add(Box.createVerticalStrut(28));

        StyledButton btn = new StyledButton("Sign In", StyledButton.Style.PRIMARY);
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setRadius(12);
        btn.addActionListener(e -> showError());
        p.add(btn);
        p.add(Box.createVerticalStrut(18));

        p.add(buildGuestLink());

        return p;
    }

    // ─────────────────────────────────────────
    //  Register form
    // ─────────────────────────────────────────
    private JPanel buildRegisterForm() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(buildLabel("Username"));
        p.add(Box.createVerticalStrut(7));
        regUsername = buildTextField("Choose a username");
        regUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(regUsername);
        p.add(Box.createVerticalStrut(18));

        p.add(buildLabel("Email"));
        p.add(Box.createVerticalStrut(7));
        regEmail = buildTextField("Enter your email");
        regEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(regEmail);
        p.add(Box.createVerticalStrut(18));

        p.add(buildLabel("Password"));
        p.add(Box.createVerticalStrut(7));
        regPassword = buildPasswordField("Create a password");
        regPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(regPassword);
        p.add(Box.createVerticalStrut(28));

        StyledButton btn = new StyledButton("Create Account", StyledButton.Style.PRIMARY);
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setRadius(12);
        btn.addActionListener(e -> showError());
        p.add(btn);
        p.add(Box.createVerticalStrut(18));

        p.add(buildGuestLink());

        return p;
    }

    // ─────────────────────────────────────────
    //  Shared helpers
    // ─────────────────────────────────────────
    private JLabel buildLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        applyFieldStyle(f, placeholder);
        return f;
    }

    private JPasswordField buildPasswordField(String placeholder) {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        applyFieldStyle(f, placeholder);
        return f;
    }

    private void applyFieldStyle(JTextField f, String placeholder) {
        f.setOpaque(false);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setCaretColor(Theme.ACCENT_BRIGHT);
        f.setFont(Theme.FONT_BODY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        f.setPreferredSize(new Dimension(0, 46));
        f.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(12, Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(12, Theme.ACCENT),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(12, Theme.BORDER),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
        });
        f.setUI(new PlaceholderUI(placeholder));
    }

    private JPanel buildGuestLink() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel text = new JLabel("— or continue as");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        text.setForeground(Theme.TEXT_MUTED);

        JLabel link = new JLabel("Guest") {
            {
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(Theme.ACCENT_BRIGHT);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { setForeground(Color.WHITE); }
                    public void mouseExited(MouseEvent e)  { setForeground(Theme.ACCENT_BRIGHT); }
                    public void mouseClicked(MouseEvent e) {
                        if (listener != null) listener.onGuestLogin();
                    }
                });
            }
        };

        row.add(text);
        row.add(link);
        return row;
    }

    private void showError() {
        JDialog dlg = new JDialog();
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        CardPanel card = new CardPanel(Theme.BG_CARD, 16, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        card.setBorderColor(Theme.RED);

        JLabel icon = new JLabel("⚠", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        icon.setForeground(Theme.RED);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(12));

        JLabel msg = new JLabel("Please try again later.", SwingConstants.CENTER);
        msg.setFont(Theme.FONT_H3);
        msg.setForeground(Theme.TEXT_PRIMARY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(msg);
        card.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Database not connected yet.", SwingConstants.CENTER);
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(20));

        StyledButton ok = new StyledButton("OK", StyledButton.Style.GHOST);
        ok.setPreferredSize(new Dimension(120, 38));
        ok.setMaximumSize(new Dimension(120, 38));
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        ok.addActionListener(e -> dlg.dispose());
        card.add(ok);

        dlg.setContentPane(card);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setModal(true);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────
    //  Inner helpers
    // ─────────────────────────────────────────
    private static class RoundBorder implements Border {
        private int r; private Color c;
        RoundBorder(int r, Color c) { this.r = r; this.c = c; }
        public Insets getBorderInsets(Component comp) { return new Insets(1,1,1,1); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x+0.75f, y+0.75f, w-1.5f, h-1.5f, r, r));
            g2.dispose();
        }
    }

    private static class PlaceholderUI extends javax.swing.plaf.basic.BasicTextFieldUI {
        private String ph;
        PlaceholderUI(String ph) { this.ph = ph; }
        @Override protected void paintSafely(Graphics g) {
            super.paintSafely(g);
            JTextComponent c = getComponent();
            if (c.getText().isEmpty() && !c.hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(c.getFont());
                Insets ins = c.getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ph, ins.left, (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        }
    }
}