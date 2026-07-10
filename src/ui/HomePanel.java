package ui;

import components.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class HomePanel extends JPanel {

    public HomePanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        JPanel content = new JPanel();
        content.setBackground(Theme.BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));
        content.setAlignmentX(LEFT_ALIGNMENT);

        JLabel welcome = new JLabel("Welcome back, User");
        welcome.setFont(Theme.FONT_TITLE);
        welcome.setForeground(Theme.TEXT);
        welcome.setAlignmentX(LEFT_ALIGNMENT);
        content.add(welcome);
        content.add(Box.createVerticalStrut(24));

        content.add(buildStatRow());
        content.add(Box.createVerticalStrut(28));

        content.add(sectionTitle("Quick Actions"));
        content.add(Box.createVerticalStrut(12));
        content.add(buildQuickActions());
        content.add(Box.createVerticalStrut(28));

        content.add(sectionTitle("Recent Activity"));
        content.add(Box.createVerticalStrut(12));
        content.add(buildActivityTable());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_HEADING);
        lbl.setForeground(Theme.TEXT);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setAlignmentX(LEFT_ALIGNMENT);

        row.add(statCard("1240", "Total Score"));
        row.add(statCard("8", "Challenges Solved"));
        row.add(statCard("#3", "Global Rank"));
        row.add(statCard("12", "Rooms Joined"));
        return row;
    }

    private CardPanel statCard(String value, String label) {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 22));
        val.setForeground(Theme.ORANGE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_GRAY);

        card.add(val);
        card.add(Box.createVerticalStrut(4));
        card.add(lbl);
        return card;
    }

    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setAlignmentX(LEFT_ALIGNMENT);

        row.add(actionCard("Create Room", "Start a new session with your team"));
        row.add(actionCard("Join Room", "Enter a room code shared by a teammate"));
        row.add(actionCard("Practice Solo", "Try a challenge on your own"));
        return row;
    }

    private CardPanel actionCard(String title, String desc) {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_LABEL);
        t.setForeground(Theme.TEXT);

        JLabel d = new JLabel("<html><body style='width:180px'>" + desc + "</body></html>");
        d.setFont(Theme.FONT_SMALL);
        d.setForeground(Theme.TEXT_GRAY);

        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(d);
        return card;
    }

    private CardPanel buildActivityTable() {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Room ID", "Topic", "Language", "Difficulty", "Result", "Score"};
        Object[][] data = {
                {"AX92K", "Arrays", "Java", "Medium", "Pass", "+150"},
                {"BT45M", "Strings", "Python", "Easy", "Pass", "+100"},
                {"CK78P", "Recursion", "JavaScript", "Hard", "Fail", "0"},
                {"DM23Q", "OOP", "Java", "Medium", "Pass", "+150"},
        };

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(Theme.FONT_NORMAL);
        table.setRowHeight(32);
        table.setGridColor(Theme.BORDER);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                c.setBackground(row % 2 == 0 ? Theme.CARD_BG : new Color(42, 47, 57));
                c.setForeground(Theme.TEXT);
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_LABEL);
        header.setBackground(Theme.BG);
        header.setForeground(Theme.TEXT);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }
}