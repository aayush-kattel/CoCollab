package ui;

import components.*;
import db.SubmissionDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class LeaderboardPanel extends JPanel {

    private SubmissionDAO submissionDAO = new SubmissionDAO();
    private DefaultTableModel model;

    public LeaderboardPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
        refresh();
    }

    private void build() {
        JPanel content = new JPanel();
        content.setBackground(Theme.BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        header.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("Leaderboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);
        header.add(title, BorderLayout.WEST);

        StyledButton refreshBtn = new StyledButton("Refresh", StyledButton.SECONDARY);
        refreshBtn.setPreferredSize(new Dimension(110, 36));
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);

        content.add(header);
        content.add(Box.createVerticalStrut(18));

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // 3 columns — MUST match what refresh() inserts below, in the same order.
        String[] cols = {"Room Name", "Room Code", "Total Score"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(Theme.FONT_NORMAL);
        table.setRowHeight(34);
        table.setGridColor(Theme.BORDER);
        table.setBackground(Theme.CARD_BG);
        table.setForeground(Theme.TEXT);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
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

        JTableHeader th = table.getTableHeader();
        th.setFont(Theme.FONT_LABEL);
        th.setBackground(Theme.BG);
        th.setForeground(Theme.TEXT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.CARD_BG);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        content.add(card);

        add(content, BorderLayout.CENTER);
    }

    public void refresh() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                return submissionDAO.getLeaderboard(50);
            }

            @Override
            protected void done() {
                try {
                    model.setRowCount(0);
                    for (Object[] row : get()) {
                        // row = { roomName, roomCode, totalScore } — matches the SQL SELECT order exactly.
                        model.addRow(row);
                    }
                } catch (Exception ex) {
                    System.out.println("LeaderboardPanel refresh failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}