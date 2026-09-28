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

public class HomePanel extends JPanel {

    private User currentUser;
    private UserDAO userDAO = new UserDAO();
    private SubmissionDAO submissionDAO = new SubmissionDAO();
    private Runnable onGoRooms, onGoLeaderboard, onGoProfile;

    private JLabel scoreVal, solvedVal, rankVal, roomsVal;
    private DefaultTableModel activityModel;

    public HomePanel(User currentUser, Runnable onGoRooms, Runnable onGoLeaderboard, Runnable onGoProfile) {
        this.currentUser = currentUser;
        this.onGoRooms = onGoRooms;
        this.onGoLeaderboard = onGoLeaderboard;
        this.onGoProfile = onGoProfile;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
        refresh();
    }

    private void build() {
        JPanel content = new JPanel();
        content.setBackground(Theme.BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));
        content.setAlignmentX(LEFT_ALIGNMENT);

        String name = currentUser != null ? currentUser.getName() : "User";
        JLabel welcome = new JLabel("Welcome back, " + name);
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

    public void refresh() {
        if (currentUser == null) return;

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() {
                int score = userDAO.getTotalScore(currentUser.getId());
                int solved = userDAO.getSolvedCount(currentUser.getId());
                int rank = userDAO.getGlobalRank(currentUser.getId());
                int rooms = userDAO.getRoomsJoined(currentUser.getId());
                List<Submission> activity = submissionDAO.findByUser(currentUser.getId());
                return new Object[]{score, solved, rank, rooms, activity};
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

                    activityModel.setRowCount(0);
                    @SuppressWarnings("unchecked")
                    List<Submission> list = (List<Submission>) r[4];
                    int n = 0;
                    for (Submission s : list) {
                        if (n++ >= 10) break;
                        activityModel.addRow(new Object[]{
                                s.getRoomCode(),
                                s.getTopic() != null ? s.getTopic() : "—",
                                s.getLanguageName(),
                                s.getResult(),
                                s.getScore()
                        });
                    }
                } catch (Exception ex) {
                    System.out.println("HomePanel refresh failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
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

        CardPanel scoreCard = statCard("—");
        CardPanel solvedCard = statCard("—");
        CardPanel rankCard = statCard("—");
        CardPanel roomsCard = statCard("—");

        scoreVal = (JLabel) scoreCard.getComponent(0);
        solvedVal = (JLabel) solvedCard.getComponent(0);
        rankVal = (JLabel) rankCard.getComponent(0);
        roomsVal = (JLabel) roomsCard.getComponent(0);

        row.add(wrapLabeled(scoreCard, "Total Score"));
        row.add(wrapLabeled(solvedCard, "Challenges Solved"));
        row.add(wrapLabeled(rankCard, "Best Room Rank"));
        row.add(wrapLabeled(roomsCard, "Rooms Joined"));
        return row;
    }

    private CardPanel statCard(String initialValue) {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel v = new JLabel(initialValue);
        v.setFont(Theme.FONT_TITLE);
        v.setForeground(Theme.ORANGE);
        v.setAlignmentX(LEFT_ALIGNMENT);
        card.add(v);
        return card;
    }

    private CardPanel wrapLabeled(CardPanel valueCard, String label) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_GRAY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        valueCard.add(Box.createVerticalStrut(4));
        valueCard.add(l);
        return valueCard;
    }

    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.setAlignmentX(LEFT_ALIGNMENT);

        row.add(actionCard("Create / Join Room", "Open rooms section", () -> {
            if (onGoRooms != null) onGoRooms.run();
        }));
        row.add(actionCard("View Leaderboard", "See top ranked users", () -> {
            if (onGoLeaderboard != null) onGoLeaderboard.run();
        }));
        row.add(actionCard("Your Profile", "Scores and history", () -> {
            if (onGoProfile != null) onGoProfile.run();
        }));
        return row;
    }

    private CardPanel actionCard(String title, String desc, Runnable action) {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        if (action != null) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    action.run();
                }
            });
        }

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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Room", "Topic", "Language", "Result", "Score"};
        activityModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(activityModel);
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