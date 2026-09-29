package ui;

import components.*;
import db.SubmissionDAO;
import db.UserDAO;
import models.Submission;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ProfilePanel extends JPanel {

    private final User currentUser;
    private final Runnable onLogout;   // called by MainFrame to go back to the login screen
    private UserDAO userDAO = new UserDAO();
    private SubmissionDAO submissionDAO = new SubmissionDAO();

    private JLabel scoreVal, solvedVal, rankVal, roomsVal;
    private DefaultTableModel historyModel;

    public ProfilePanel(User currentUser, Runnable onLogout) {
        this.currentUser = currentUser;
        this.onLogout = onLogout;
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
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel title = new JLabel("Profile");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Log out");
        logoutBtn.setFont(Theme.FONT_LABEL);
        logoutBtn.setBackground(Theme.RED);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setOpaque(true);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());
        header.add(logoutBtn, BorderLayout.EAST);

        content.add(header);
        content.add(Box.createVerticalStrut(20));

        // User info card
        CardPanel info = new CardPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        info.setAlignmentX(LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        info.add(infoLine("Name", currentUser.getName()));
        info.add(Box.createVerticalStrut(8));
        info.add(infoLine("Email", currentUser.getEmail()));
        info.add(Box.createVerticalStrut(8));
        info.add(infoLine("Role", currentUser.getRole()));
        content.add(info);
        content.add(Box.createVerticalStrut(22));

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        stats.setAlignmentX(LEFT_ALIGNMENT);

        CardPanel scoreCard = statCard("Total Score");
        CardPanel solvedCard = statCard("Solved");
        CardPanel rankCard = statCard("Best Room Rank");
        CardPanel roomsCard = statCard("Rooms Joined");
        scoreVal = (JLabel) scoreCard.getComponent(0);
        solvedVal = (JLabel) solvedCard.getComponent(0);
        rankVal = (JLabel) rankCard.getComponent(0);
        roomsVal = (JLabel) roomsCard.getComponent(0);

        stats.add(scoreCard);
        stats.add(solvedCard);
        stats.add(rankCard);
        stats.add(roomsCard);
        content.add(stats);
        content.add(Box.createVerticalStrut(24));

        // History
        JLabel hist = new JLabel("Challenge History");
        hist.setFont(Theme.FONT_HEADING);
        hist.setForeground(Theme.TEXT);
        hist.setAlignmentX(LEFT_ALIGNMENT);
        content.add(hist);
        content.add(Box.createVerticalStrut(10));
        content.add(buildHistoryTable());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Log out",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION && onLogout != null) {
            onLogout.run();
        }
    }

    public void refresh() {
        if (currentUser == null) return;
        final int userId = currentUser.getId();

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() {
                int score = userDAO.getTotalScore(userId);
                int solved = userDAO.getSolvedCount(userId);
                int rank = userDAO.getGlobalRank(userId);
                int rooms = userDAO.getRoomsJoined(userId);
                List<Submission> history = submissionDAO.findByUser(userId);
                return new Object[]{score, solved, rank, rooms, history};
            }

            @Override
            protected void done() {
                try {
                    Object[] r = get();
                    scoreVal.setText(String.valueOf(r[0]));
                    solvedVal.setText(String.valueOf(r[1]));
                    int rank = (int) r[2];
                    rankVal.setText(rank > 0 ? "#" + rank : "—");
                    roomsVal.setText(String.valueOf(r[3]));

                    historyModel.setRowCount(0);
                    @SuppressWarnings("unchecked")
                    List<Submission> list = (List<Submission>) r[4];
                    for (Submission s : list) {
                        String time = s.getSubmittedAt() != null ? s.getSubmittedAt().toString() : "";
                        historyModel.addRow(new Object[]{
                                s.getRoomCode(), s.getTopic(), s.getLanguageName(),
                                s.getResult(), s.getScore(), time
                        });
                    }
                } catch (Exception ex) {
                    System.out.println("ProfilePanel refresh failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel infoLine(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label + ":  ");
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_GRAY);
        JLabel v = new JLabel(value != null ? value : "—");
        v.setFont(Theme.FONT_NORMAL);
        v.setForeground(Theme.TEXT);
        row.add(l);
        row.add(v);
        return row;
    }

    private CardPanel statCard(String label) {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel v = new JLabel("—");
        v.setFont(Theme.FONT_TITLE);
        v.setForeground(Theme.ORANGE);
        v.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_GRAY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        card.add(v);
        card.add(Box.createVerticalStrut(4));
        card.add(l);
        return card;
    }

    private CardPanel buildHistoryTable() {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"Room", "Topic", "Language", "Result", "Score", "Submitted"};
        historyModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(historyModel);
        table.setFont(Theme.FONT_NORMAL);
        table.setRowHeight(32);
        table.setGridColor(Theme.BORDER);
        table.setBackground(Theme.CARD_BG);
        table.setForeground(Theme.TEXT);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                c.setBackground(row % 2 == 0 ? Theme.CARD_BG : new Color(42, 47, 57));
                c.setForeground(Theme.TEXT);
                if (col == 3 && value != null) {
                    if ("Pass".equalsIgnoreCase(value.toString())) c.setForeground(Theme.GREEN);
                    else if ("Fail".equalsIgnoreCase(value.toString())) c.setForeground(Theme.RED);
                }
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

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Theme.CARD_BG);
        sp.setBorder(null);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }
}