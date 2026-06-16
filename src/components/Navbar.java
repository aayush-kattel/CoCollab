package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class Navbar extends JPanel {

    private String activeSection = "Home";
    private NavClickListener listener;
    private JPanel centerPanel;

    public interface NavClickListener {
        void onNavClick(String section);
    }

    private String[] navItems = { "Home", "Rooms", "Leaderboard", "Profile" };

    public Navbar(NavClickListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(0, 64));
        setBackground(Theme.BG_NAVBAR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        build();
    }

    public void setActiveSection(String section) {
        this.activeSection = section;
        refreshNavColors();
    }

    private void build() {
        // Left — brand
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        JLabel brand = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int cx = 22, cy = 32, r = 14;
                int[] xs = new int[6], ys = new int[6];
                for (int i = 0; i < 6; i++) {
                    xs[i] = (int)(cx + r * Math.cos(Math.PI / 6 + i * Math.PI / 3));
                    ys[i] = (int)(cy + r * Math.sin(Math.PI / 6 + i * Math.PI / 3));
                }
                g2.setColor(Theme.ACCENT);
                g2.fillPolygon(xs, ys, 6);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("ARS", cx - fm.stringWidth("ARS") / 2, cy + fm.getAscent() / 2 - 1);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.drawString("Co", 44, 37);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
                g2.setColor(Theme.ACCENT_BRIGHT);
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString("Collab", 44 + fm2.stringWidth("Co"), 37);

                g2.dispose();
            }
        };
        brand.setPreferredSize(new Dimension(170, 64));
        left.add(brand);

        // Center — nav items
        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        centerPanel.setOpaque(false);

        for (String item : navItems) {
            JLabel nav = new JLabel(item) {
                private boolean hov = false;
                {
                    setFont(Theme.FONT_NAV);
                    setForeground(item.equals(activeSection) ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                    setPreferredSize(new Dimension(getPreferredSize().width + 32, 64));
                    setHorizontalAlignment(SwingConstants.CENTER);

                    addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                            hov = true;
                            if (!item.equals(activeSection))
                                setForeground(Theme.TEXT_PRIMARY);
                            repaint();
                        }
                        public void mouseExited(MouseEvent e) {
                            hov = false;
                            if (!item.equals(activeSection))
                                setForeground(Theme.TEXT_SECONDARY);
                            repaint();
                        }
                        public void mouseClicked(MouseEvent e) {
                            activeSection = item;
                            if (listener != null) {
                                listener.onNavClick(item);
                            }
                            refreshNavColors();
                        }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (item.equals(activeSection)) {
                        g2.setColor(Theme.ACCENT_GLOW);
                        g2.fillRect(0, 0, getWidth(), getHeight());

                        g2.setColor(Theme.ACCENT);
                        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        int y = getHeight() - 1;
                        int pad = 16;
                        g2.drawLine(pad, y, getWidth() - pad, y);
                    }

                    if (hov && !item.equals(activeSection)) {
                        g2.setColor(new Color(255, 255, 255, 8));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }

                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            centerPanel.add(nav);
        }

        // Right — user profile area (FIXED: Properly centered)
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.anchor = GridBagConstraints.CENTER;

        // Online badge
        JLabel badge = new JLabel("● Online");
        badge.setFont(Theme.FONT_SMALL);
        badge.setForeground(Theme.GREEN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        right.add(badge, gbc);

        // Separator
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setForeground(Theme.BORDER_BRIGHT);
        sep.setPreferredSize(new Dimension(1, 28));
        gbc.gridx = 1;
        gbc.gridy = 0;
        right.add(sep, gbc);

        // Username
        JLabel username = new JLabel("User");
        username.setFont(Theme.FONT_BOLD);
        username.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridx = 2;
        gbc.gridy = 0;
        right.add(username, gbc);

        // Avatar
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT, getWidth(), getHeight(),
                        new Color(168, 85, 247));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String init = "U";
                g2.drawString(init,
                        (getWidth() - fm.stringWidth(init)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setOpaque(false);
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 3;
        gbc.gridy = 0;
        right.add(avatar, gbc);

        add(left, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    private void refreshNavColors() {
        if (centerPanel == null) return;
        for (Component c : centerPanel.getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                lbl.setForeground(lbl.getText().equals(activeSection)
                        ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
                lbl.repaint();
            }
        }
    }
}