package ui;

import components.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class HomePanel extends JPanel {

    public HomePanel() {
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        JPanel content = buildContent();

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scroll.getVerticalScrollBar().setBackground(Theme.BG_DARK);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setBackground(Theme.BG_DARK);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(36, 40, 40, 40));
        content.setAlignmentX(LEFT_ALIGNMENT);

        JPanel welcomeHeader = buildWelcomeHeader();
        welcomeHeader.setAlignmentX(LEFT_ALIGNMENT);
        content.add(welcomeHeader);
        content.add(Box.createVerticalStrut(28));

        JPanel statCards = buildStatCards();
        statCards.setAlignmentX(LEFT_ALIGNMENT);
        content.add(statCards);
        content.add(Box.createVerticalStrut(32));

        JLabel quickTitle = buildSectionTitle("Quick Actions");
        quickTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(quickTitle);
        content.add(Box.createVerticalStrut(14));

        JPanel quickActions = buildQuickActions();
        quickActions.setAlignmentX(LEFT_ALIGNMENT);
        content.add(quickActions);
        content.add(Box.createVerticalStrut(32));

        JLabel recentTitle = buildSectionTitle("Recent Activity");
        recentTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(recentTitle);
        content.add(Box.createVerticalStrut(14));

        JPanel recentActivity = buildRecentActivity();
        recentActivity.setAlignmentX(LEFT_ALIGNMENT);
        content.add(recentActivity);

        return content;
    }

    private JPanel buildWelcomeHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome back, User 👋");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcome.setForeground(Theme.TEXT_PRIMARY);
        welcome.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Ready to code with your team today?");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        left.add(welcome);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row.setAlignmentX(LEFT_ALIGNMENT);

        String[][] stats = {
                { "1,240", "Total Score",       "↑ +150 this week", "#6366f1" },
                { "8",     "Challenges Solved", "↑ +2 this week",   "#22c55e" },
                { "#3",    "Global Rank",       "↑ Up 2 positions", "#f59e0b" },
                { "12",    "Rooms Joined",      "3 active rooms",   "#06b6d4" },
        };

        for (String[] s : stats) {
            row.add(buildStatCard(s[0], s[1], s[2], Color.decode(s[3])));
        }
        return row;
    }

    private JPanel buildStatCard(String value, String label, String sub, Color accent) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JPanel accentBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, accent, 0, getHeight(),
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        accentBar.setPreferredSize(new Dimension(3, 0));
        accentBar.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(Theme.FONT_SMALL);
        lblLabel.setForeground(Theme.TEXT_SECONDARY);

        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLabel.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));

        textPanel.add(valLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subLabel);

        card.add(accentBar, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row.setAlignmentX(LEFT_ALIGNMENT);

        row.add(buildActionCard("Create Room",
                "Start a new session with your team and solve an AI challenge",
                Theme.ACCENT, new Color(99, 102, 241, 25), "+"));
        row.add(buildActionCard("Join Room",
                "Enter a room code shared by your teammate to join their session",
                Theme.GREEN, Theme.GREEN_DIM, "→"));
        row.add(buildActionCard("Practice Solo",
                "Sharpen your skills alone with an AI-generated challenge",
                Theme.YELLOW, Theme.YELLOW_DIM, "⚡"));

        return row;
    }

    private JPanel buildActionCard(String title, String desc, Color accent, Color bg, String icon) {
        CardPanel card = new CardPanel(Theme.BG_CARD, 14, true);
        card.setBorderColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(accent);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        iconCircle.setPreferredSize(new Dimension(44, 44));
        iconCircle.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_H3);
        t.setForeground(Theme.TEXT_PRIMARY);

        JLabel d = new JLabel("<html><body style='width:200px'>" + desc + "</body></html>");
        d.setFont(Theme.FONT_SMALL);
        d.setForeground(Theme.TEXT_SECONDARY);

        text.add(t);
        text.add(Box.createVerticalStrut(4));
        text.add(d);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setCardBackground(Theme.BG_HOVER); card.repaint(); }
            public void mouseExited(MouseEvent e)  { card.setCardBackground(Theme.BG_CARD);  card.repaint(); }
        });

        JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        leftWrap.setOpaque(false);
        leftWrap.add(iconCircle);

        card.add(leftWrap, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecentActivity() {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // Updated columns with Language
        String[] cols = { "Room ID", "Topic", "Language", "Difficulty", "Result", "Score", "Date" };
        Object[][] data = {
                { "AX92K", "Arrays",      "Java",       "Medium", "✅ Pass", "+150 pts", "Jun 10" },
                { "BT45M", "Strings",     "Python",     "Easy",   "✅ Pass", "+100 pts", "Jun 9"  },
                { "CK78P", "Recursion",   "JavaScript", "Hard",   "❌ Fail", "0 pts",    "Jun 8"  },
                { "DM23Q", "OOP",         "Java",       "Medium", "✅ Pass", "+150 pts", "Jun 7"  },
                { "EX11R", "Linked List", "C++",        "Hard",   "❌ Fail", "0 pts",    "Jun 6"  },
        };

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? Theme.BG_CARD : new Color(20, 25, 42));
                c.setForeground(Theme.TEXT_PRIMARY);
                if (c instanceof JComponent)
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                // Color coding for Language column
                if (col == 2) {
                    String v = (String) getValueAt(row, col);
                    if (v.equals("Java")) {
                        c.setForeground(new Color(255, 152, 0)); // Orange
                    } else if (v.equals("Python")) {
                        c.setForeground(new Color(52, 152, 219)); // Blue
                    } else if (v.equals("JavaScript")) {
                        c.setForeground(new Color(241, 196, 15)); // Yellow
                    } else if (v.equals("C++")) {
                        c.setForeground(new Color(52, 73, 94)); // Dark blue-gray
                    }
                }

                // Color coding for Difficulty column
                if (col == 3) {
                    String v = (String) getValueAt(row, col);
                    c.setForeground(v.equals("Easy") ? Theme.GREEN :
                            v.equals("Medium") ? Theme.YELLOW : Theme.RED);
                }

                // Color coding for Result column
                if (col == 4) {
                    String v = (String) getValueAt(row, col);
                    c.setForeground(v.contains("Pass") ? Theme.GREEN : Theme.RED);
                }

                // Color coding for Score column
                if (col == 5) {
                    String v = (String) getValueAt(row, col);
                    c.setForeground(v.startsWith("+") ? Theme.GREEN : Theme.TEXT_MUTED);
                }
                return c;
            }
        };

        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.BG_HOVER);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setBorder(null);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 19, 32));
        header.setForeground(Theme.TEXT_SECONDARY);
        header.setFont(Theme.FONT_SMALL);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Updated column widths with Language column
        int[] widths = { 90, 100, 90, 100, 90, 100, 100 };
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JLabel buildSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_H3);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }
}