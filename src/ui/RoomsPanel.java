package ui;

import components.*;
import db.RoomDAO;
import models.Room;
import models.User;
import room.RoomService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class RoomsPanel extends JPanel {

    private User currentUser;
    private RoomDAO roomDAO = new RoomDAO();
    private RoomService roomService = new RoomService();
    private DefaultTableModel tableModel;
    private JTable table;
    private Consumer<Room> onOpenRoom;
    private StyledButton refreshBtn, joinBtn, createBtn;

    public RoomsPanel(User currentUser, Consumer<Room> onOpenRoom) {
        this.currentUser = currentUser;
        this.onOpenRoom = onOpenRoom;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
        refreshTable();
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

        JLabel title = new JLabel("Rooms");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);
        header.add(title, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        refreshBtn = new StyledButton("Refresh", StyledButton.SECONDARY);
        refreshBtn.setPreferredSize(new Dimension(110, 36));
        refreshBtn.addActionListener(e -> refreshTable());

        joinBtn = new StyledButton("Join Room", StyledButton.SECONDARY);
        joinBtn.setPreferredSize(new Dimension(120, 36));
        joinBtn.addActionListener(e -> showJoinDialog());

        createBtn = new StyledButton("Create Room", StyledButton.PRIMARY);
        createBtn.setPreferredSize(new Dimension(130, 36));
        createBtn.addActionListener(e -> showCreateDialog());

        btnRow.add(refreshBtn);
        btnRow.add(joinBtn);
        btnRow.add(createBtn);
        header.add(btnRow, BorderLayout.EAST);

        content.add(header);
        content.add(Box.createVerticalStrut(18));

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 480));

        String[] cols = {"Code", "Name", "Topic", "Language", "Difficulty", "Status", "Members", "Owner"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(Theme.FONT_NORMAL);
        table.setRowHeight(34);
        table.setGridColor(Theme.BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(Theme.CARD_BG);
        table.setForeground(Theme.TEXT);
        table.setSelectionBackground(Theme.ORANGE);
        table.setSelectionForeground(Color.WHITE);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.CARD_BG : new Color(42, 47, 57));
                    c.setForeground(Theme.TEXT);
                }
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
        content.add(Box.createVerticalStrut(12));

        JLabel hint = new JLabel("Double-click a room to open the coding room.");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_GRAY);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        content.add(hint);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        String code = (String) tableModel.getValueAt(row, 0);
                        openRoomByCode(code);
                    }
                }
            }
        });

        JScrollPane outer = new JScrollPane(content);
        outer.setBorder(null);
        outer.getViewport().setBackground(Theme.BG);
        outer.getVerticalScrollBar().setUnitIncrement(16);
        add(outer, BorderLayout.CENTER);
    }

    // Opening a room does a DB lookup — off the EDT, same pattern as everything else here.
    private void openRoomByCode(String code) {
        setButtonsEnabled(false);
        SwingWorker<Room, Void> worker = new SwingWorker<>() {
            @Override
            protected Room doInBackground() {
                return roomDAO.findByCode(code);
            }
            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    Room room = get();
                    if (room != null && onOpenRoom != null) {
                        onOpenRoom.accept(room);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RoomsPanel.this, "Could not open room. Check your connection.");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void refreshTable() {
        setButtonsEnabled(false);
        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Room> doInBackground() {
                List<Room> mine = roomDAO.findByUser(currentUser.getId());
                return !mine.isEmpty() ? mine : roomDAO.findActiveRooms();
            }
            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    tableModel.setRowCount(0);
                    for (Room r : get()) {
                        addRow(r);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RoomsPanel.this, "Could not load rooms. Check your connection.");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        refreshBtn.setEnabled(enabled);
        joinBtn.setEnabled(enabled);
        createBtn.setEnabled(enabled);
    }

    private void addRow(Room r) {
        tableModel.addRow(new Object[]{
                r.getCode(), r.getName(), r.getTopic(), r.getLanguage(),
                r.getDifficulty(), r.getStatus(), r.getMemberCount(), r.getOwnerName()
        });
    }

    private void showCreateDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Create Room",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 500);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG);

        JPanel form = new JPanel();
        form.setBackground(Theme.BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JTextField nameField = field();
        JComboBox<String> topicBox = combo("Arrays", "Strings", "Recursion", "OOP", "Sorting", "Dynamic Programming", "Graphs");
        JComboBox<String> langBox = combo("Java", "Python", "JavaScript", "C++", "C");
        JComboBox<String> diffBox = combo("Easy", "Medium", "Hard");
        JComboBox<String> countBox = combo("3", "5", "7", "10");
        countBox.setSelectedItem("5"); // smaller default — more reliable than 10 in one Groq call

        form.add(label("Room Name"));
        form.add(nameField);
        form.add(Box.createVerticalStrut(12));
        form.add(label("Topic"));
        form.add(topicBox);
        form.add(Box.createVerticalStrut(12));
        form.add(label("Language"));
        form.add(langBox);
        form.add(Box.createVerticalStrut(12));
        form.add(label("Difficulty"));
        form.add(diffBox);
        form.add(Box.createVerticalStrut(12));
        form.add(label("Number of Questions"));
        form.add(countBox);
        form.add(Box.createVerticalStrut(20));

        StyledButton create = new StyledButton("Create Room", StyledButton.PRIMARY);
        create.setAlignmentX(LEFT_ALIGNMENT);
        create.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        create.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Enter a room name.");
                return;
            }
            int questionCount = Integer.parseInt((String) countBox.getSelectedItem());

            create.setEnabled(false);
            create.setText("Creating...");

            SwingWorker<Room, Void> worker = new SwingWorker<>() {
                @Override
                protected Room doInBackground() {
                    return roomService.createRoom(
                            currentUser.getId(),
                            name,
                            (String) topicBox.getSelectedItem(),
                            (String) langBox.getSelectedItem(),
                            (String) diffBox.getSelectedItem(),
                            questionCount
                    );
                }
                @Override
                protected void done() {
                    create.setEnabled(true);
                    create.setText("Create Room");
                    try {
                        Room room = get();
                        if (room == null) {
                            JOptionPane.showMessageDialog(dialog, "Failed to create room.");
                            return;
                        }
                        dialog.dispose();
                        JOptionPane.showMessageDialog(RoomsPanel.this,
                                "Room created!\nCode: " + room.getCode() + "\nShare this with your team.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Could not create room. Check your connection.");
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        });
        form.add(create);

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void showJoinDialog() {
        String code = JOptionPane.showInputDialog(this, "Enter room code:", "Join Room",
                JOptionPane.QUESTION_MESSAGE);
        if (code == null || code.trim().isEmpty()) return;
        final String finalCode = code;

        setButtonsEnabled(false);
        SwingWorker<Room, Void> worker = new SwingWorker<>() {
            @Override
            protected Room doInBackground() {
                return roomService.joinRoom(finalCode, currentUser.getId());
            }
            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    Room room = get();
                    if (room == null) {
                        JOptionPane.showMessageDialog(RoomsPanel.this, "Invalid or finished room code.");
                        return;
                    }
                    JOptionPane.showMessageDialog(RoomsPanel.this,
                            "Joined \"" + room.getName() + "\" (" + room.getCode() + ")");
                    refreshTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RoomsPanel.this, "Could not join room. Check your connection.");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_GRAY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(Theme.FONT_NORMAL);
        f.setForeground(Theme.TEXT);
        f.setBackground(new Color(58, 64, 76));
        f.setCaretColor(Theme.TEXT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(Theme.FONT_NORMAL);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setBackground(new Color(58, 64, 76));
        c.setForeground(Theme.TEXT);
        return c;
    }
}