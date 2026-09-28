package room;

import api.Judge0Service;
import components.*;
import db.ChallengeDAO;
import db.LanguageDAO;
import db.RoomDAO;
import db.RoomMessageDAO;
import db.SubmissionDAO;
import models.*;
import socket.SocketProtocol;
import utils.AppConfig;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodingRoomPanel extends JPanel {

    private User currentUser;
    private Room room;
    private Challenge challenge;
    private JLabel problemTitleLabel;
    private JTextArea problemArea;
    private JTextArea codeArea;
    private JLabel statusLabel;
    private Runnable onBack;
    private int totalQuestions = 1;
    private JLabel questionLabel;
    private StyledButton prevBtn, nextBtn;

    // Keeps whatever you've typed for each question while this room screen is open.
    // Cleared when the room is closed — this is intentional, not a bug: a room is one
    // working session, not a permanent save file.
    private Map<Integer, String> codeDrafts = new HashMap<>();

    // Chat / turn control
    private JTextArea chatArea;
    private JTextField chatInput;
    private StyledButton takeControlBtn, releaseControlBtn;
    private JLabel controlStatusLabel;
    private boolean hasControl = false;

    // Socket
    private Socket socket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private volatile boolean socketConnected = false;
    private boolean suppressCodeChangeEvent = false;

    public CodingRoomPanel(User user, Room room, Runnable onBack) {
        this.currentUser = user;
        this.room = room;
        this.onBack = onBack;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
        loadChallenge();
        loadChatHistory();
        connectSocket();
    }

    // ================= Challenge loading =================

    private void loadChallenge() {
        problemArea.setText("Loading challenge...");

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() {
                ChallengeDAO dao = new ChallengeDAO();
                int total = dao.countByRoomId(room.getId());
                Challenge c = dao.findByRoomIdAndSequence(room.getId(), room.getCurrentQuestion());
                return new Object[]{c, total};
            }

            @Override
            protected void done() {
                try {
                    Object[] r = get();
                    challenge = (Challenge) r[0];
                    totalQuestions = (int) r[1];
                } catch (Exception ex) {
                    challenge = null;
                    System.out.println("Failed to load challenge: " + ex.getMessage());
                }
                renderChallenge();
            }
        };
        worker.execute();
    }

    private void goToQuestion(int questionNo) {
        if (questionNo < 1 || questionNo > totalQuestions) return;

        // Save whatever's in the editor right now before switching away from this question.
        if (challenge != null) {
            codeDrafts.put(challenge.getId(), codeArea.getText());
        }

        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);

        SwingWorker<Challenge, Void> worker = new SwingWorker<>() {
            @Override
            protected Challenge doInBackground() {
                new RoomDAO().updateCurrentQuestion(room.getId(), questionNo);
                return new ChallengeDAO().findByRoomIdAndSequence(room.getId(), questionNo);
            }

            @Override
            protected void done() {
                try {
                    room.setCurrentQuestion(questionNo);
                    challenge = get();
                } catch (Exception ex) {
                    System.out.println("goToQuestion failed: " + ex.getMessage());
                }
                renderChallenge();
            }
        };
        worker.execute();
    }

    private void renderChallenge() {
        questionLabel.setText("Question " + room.getCurrentQuestion() + " of " + totalQuestions);
        prevBtn.setEnabled(room.getCurrentQuestion() > 1);
        nextBtn.setEnabled(room.getCurrentQuestion() < totalQuestions);

        if (challenge != null) {
            problemTitleLabel.setText(challenge.getTitle());

            StringBuilder sb = new StringBuilder();
            sb.append(nullSafe(challenge.getProblem()));
            sb.append("\n\n---------- Sample Input ----------\n");
            sb.append(nullSafe(challenge.getSampleInput()));
            sb.append("\n\n---------- Sample Output ----------\n");
            sb.append(nullSafe(challenge.getSampleOutput()));
            sb.append("\n\n---------- Constraints ----------\n");
            sb.append(nullSafe(challenge.getConstraints()));
            problemArea.setText(sb.toString());
            problemArea.setCaretPosition(0);

            // Restore this question's draft if we already visited it this session,
            // otherwise show the default starter code for the room's language.
            suppressCodeChangeEvent = true;
            String draft = codeDrafts.get(challenge.getId());
            codeArea.setText(draft != null ? draft : defaultCode(room.getLanguage() != null ? room.getLanguage() : "Java"));
            suppressCodeChangeEvent = false;
        } else {
            problemTitleLabel.setText("No challenge");
            problemArea.setText("No challenge found for this question.");
        }
    }

    // ================= UI =================

    private void build() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG);
        top.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        StyledButton back = new StyledButton("Back", StyledButton.SECONDARY);
        back.setPreferredSize(new Dimension(90, 32));
        back.addActionListener(e -> {
            disconnectSocket();
            if (onBack != null) onBack.run();
        });

        String titleText = room.getName() + " [" + room.getCode() + "]";
        if (room.getTopic() != null) titleText += " · " + room.getTopic();
        if (room.getDifficulty() != null) titleText += " / " + room.getDifficulty();
        if (room.getLanguage() != null) titleText += " (" + room.getLanguage() + ")";

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);

        prevBtn = new StyledButton("< Prev", StyledButton.SECONDARY);
        prevBtn.setPreferredSize(new Dimension(90, 32));
        prevBtn.addActionListener(e -> goToQuestion(room.getCurrentQuestion() - 1));

        questionLabel = new JLabel("Question 1 of 1");
        questionLabel.setFont(Theme.FONT_LABEL);
        questionLabel.setForeground(Theme.TEXT_GRAY);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        nextBtn = new StyledButton("Next >", StyledButton.SECONDARY);
        nextBtn.setPreferredSize(new Dimension(90, 32));
        nextBtn.addActionListener(e -> goToQuestion(room.getCurrentQuestion() + 1));

        nav.add(prevBtn);
        nav.add(questionLabel);
        nav.add(nextBtn);

        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(nav, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.4);
        mainSplit.setBorder(null);
        mainSplit.setBackground(Theme.BG);

        mainSplit.setLeftComponent(buildProblemPanel());
        mainSplit.setRightComponent(buildRightSide());

        add(mainSplit, BorderLayout.CENTER);
    }

    private CardPanel buildProblemPanel() {
        CardPanel left = new CardPanel();
        left.setLayout(new BorderLayout(0, 8));
        left.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        problemTitleLabel = new JLabel("Loading...");
        problemTitleLabel.setFont(Theme.FONT_HEADING);
        problemTitleLabel.setForeground(Theme.ORANGE);

        problemArea = new JTextArea();
        problemArea.setEditable(false);
        problemArea.setLineWrap(true);
        problemArea.setWrapStyleWord(true);
        problemArea.setFont(Theme.FONT_NORMAL);
        problemArea.setBackground(new Color(42, 47, 57));
        problemArea.setForeground(Theme.TEXT);
        problemArea.setCaretColor(Theme.TEXT);
        problemArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        problemArea.setText("Loading challenge...");

        JScrollPane problemScroll = new JScrollPane(problemArea);
        problemScroll.setBorder(null);
        problemScroll.getViewport().setBackground(new Color(42, 47, 57));
        left.add(problemTitleLabel, BorderLayout.NORTH);
        left.add(problemScroll, BorderLayout.CENTER);
        return left;
    }

    private JSplitPane buildRightSide() {
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setResizeWeight(0.7);
        rightSplit.setBorder(null);
        rightSplit.setBackground(Theme.BG);

        rightSplit.setTopComponent(buildEditorPanel());
        rightSplit.setBottomComponent(buildChatPanel());
        return rightSplit;
    }

    private JPanel buildEditorPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(Theme.BG);
        right.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel editorLabel = new JLabel("Code Editor");
        editorLabel.setFont(Theme.FONT_LABEL);
        editorLabel.setForeground(Theme.TEXT_GRAY);

        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlRow.setOpaque(false);

        controlStatusLabel = new JLabel("Editor is locked — take control to write code");
        controlStatusLabel.setFont(Theme.FONT_SMALL);
        controlStatusLabel.setForeground(Theme.TEXT_GRAY);

        takeControlBtn = new StyledButton("Take Control", StyledButton.PRIMARY);
        takeControlBtn.setPreferredSize(new Dimension(120, 30));
        takeControlBtn.addActionListener(e -> takeControl());

        releaseControlBtn = new StyledButton("Release", StyledButton.SECONDARY);
        releaseControlBtn.setPreferredSize(new Dimension(90, 30));
        releaseControlBtn.setVisible(false);
        releaseControlBtn.addActionListener(e -> releaseControl());

        controlRow.add(controlStatusLabel);
        controlRow.add(takeControlBtn);
        controlRow.add(releaseControlBtn);

        headerRow.add(editorLabel, BorderLayout.WEST);
        headerRow.add(controlRow, BorderLayout.EAST);

        codeArea = new JTextArea();
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        codeArea.setBackground(new Color(30, 34, 42));
        codeArea.setForeground(Theme.TEXT);
        codeArea.setCaretColor(Theme.TEXT);
        codeArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        codeArea.setTabSize(4);
        codeArea.setEditable(false);

        String lang = room.getLanguage() != null ? room.getLanguage() : "Java";
        codeArea.setText(defaultCode(lang));

        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onCodeChanged(); }
            public void removeUpdate(DocumentEvent e) { onCodeChanged(); }
            public void changedUpdate(DocumentEvent e) { onCodeChanged(); }
        });

        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        codeScroll.getViewport().setBackground(new Color(30, 34, 42));

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.TEXT_GRAY);

        StyledButton submit = new StyledButton("Submit to Judge0", StyledButton.PRIMARY);
        submit.setPreferredSize(new Dimension(180, 36));
        submit.addActionListener(e -> submitCode(submit));

        bottom.add(statusLabel, BorderLayout.CENTER);
        bottom.add(submit, BorderLayout.EAST);

        right.add(headerRow, BorderLayout.NORTH);
        right.add(codeScroll, BorderLayout.CENTER);
        right.add(bottom, BorderLayout.SOUTH);
        return right;
    }

    private JPanel buildChatPanel() {
        CardPanel chatCard = new CardPanel();
        chatCard.setLayout(new BorderLayout(0, 6));
        chatCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel chatLabel = new JLabel("Team Chat");
        chatLabel.setFont(Theme.FONT_LABEL);
        chatLabel.setForeground(Theme.TEXT_GRAY);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(Theme.FONT_SMALL);
        chatArea.setBackground(new Color(42, 47, 57));
        chatArea.setForeground(Theme.TEXT);
        chatArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.getViewport().setBackground(new Color(42, 47, 57));
        chatScroll.setBorder(null);

        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setOpaque(false);

        chatInput = new JTextField();
        chatInput.setFont(Theme.FONT_NORMAL);
        chatInput.setBackground(new Color(58, 64, 76));
        chatInput.setForeground(Theme.TEXT);
        chatInput.setCaretColor(Theme.TEXT);
        chatInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        chatInput.addActionListener(e -> sendChatMessage());

        StyledButton sendBtn = new StyledButton("Send", StyledButton.PRIMARY);
        sendBtn.setPreferredSize(new Dimension(80, 32));
        sendBtn.addActionListener(e -> sendChatMessage());

        inputRow.add(chatInput, BorderLayout.CENTER);
        inputRow.add(sendBtn, BorderLayout.EAST);

        chatCard.add(chatLabel, BorderLayout.NORTH);
        chatCard.add(chatScroll, BorderLayout.CENTER);
        chatCard.add(inputRow, BorderLayout.SOUTH);
        return chatCard;
    }

    // ================= Chat history (persisted) =================

    private void loadChatHistory() {
        SwingWorker<List<RoomMessage>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RoomMessage> doInBackground() {
                return new RoomMessageDAO().findHistoryByRoomId(room.getId());
            }
            @Override
            protected void done() {
                try {
                    for (RoomMessage rm : get()) {
                        appendChat(rm.getSenderName() + ": " + rm.getMessage());
                    }
                } catch (Exception ex) {
                    System.out.println("loadChatHistory failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================= Socket connection =================

    private void connectSocket() {
        String host = AppConfig.get("socket.host");
        if (host == null || host.isEmpty()) host = "localhost";
        String portStr = AppConfig.get("socket.port");
        int port = 8081;
        try {
            if (portStr != null && !portStr.isEmpty()) port = Integer.parseInt(portStr);
        } catch (NumberFormatException ignored) {
        }
        final String finalHost = host;
        final int finalPort = port;

        new Thread(() -> {
            try {
                socket = new Socket(finalHost, finalPort);
                socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                socketConnected = true;

                socketOut.println(SocketProtocol.buildMessage(
                        "USER_JOINED", room.getCode(), currentUser.getId(), currentUser.getName(), ""));

                SwingUtilities.invokeLater(() -> appendChat("You joined the room."));

                String line;
                while ((line = socketIn.readLine()) != null) {
                    final String msg = line;
                    SwingUtilities.invokeLater(() -> handleIncoming(msg));
                }
            } catch (IOException e) {
                socketConnected = false;
                SwingUtilities.invokeLater(() ->
                        appendChat("(Could not connect to live session — chat and code sync unavailable. You can still write code and submit.)"));
            }
        }).start();
    }

    private void disconnectSocket() {
        if (socketConnected && socketOut != null) {
            socketOut.println(SocketProtocol.buildMessage(
                    "USER_LEFT", room.getCode(), currentUser.getId(), currentUser.getName(), ""));
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
        socketConnected = false;
    }

    private void handleIncoming(String json) {
        String type = SocketProtocol.getField(json, "type");
        String userName = SocketProtocol.getField(json, "userName");
        String payload = SocketProtocol.getField(json, "payload");
        if (type == null) return;

        switch (type) {
            case "USER_JOINED":
                appendChat(userName + " joined the room.");
                break;
            case "USER_LEFT":
                appendChat(userName + " left the room.");
                break;
            case "CHAT_MESSAGE":
                appendChat(userName + ": " + payload);
                break;
            case "CODE_LOCK":
                hasControl = false;
                codeArea.setEditable(false);
                takeControlBtn.setEnabled(false);
                releaseControlBtn.setVisible(false);
                controlStatusLabel.setText(userName + " is coding...");
                break;
            case "CODE_RELEASE":
                takeControlBtn.setEnabled(true);
                controlStatusLabel.setText("Editor is unlocked — take control to write code");
                break;
            case "CODE_CHANGE":
                suppressCodeChangeEvent = true;
                int caret = codeArea.getCaretPosition();
                codeArea.setText(payload);
                try {
                    codeArea.setCaretPosition(Math.min(caret, codeArea.getText().length()));
                } catch (Exception ignored) {
                }
                suppressCodeChangeEvent = false;
                break;
        }
    }

    private void appendChat(String line) {
        chatArea.append(line + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void sendChatMessage() {
        String text = chatInput.getText().trim();
        if (text.isEmpty()) return;
        chatInput.setText("");
        appendChat("You: " + text);

        if (socketConnected) {
            socketOut.println(SocketProtocol.buildMessage(
                    "CHAT_MESSAGE", room.getCode(), currentUser.getId(), currentUser.getName(), text));
        }

        new Thread(() -> new RoomMessageDAO().create(room.getId(), currentUser.getId(), text)).start();
    }

    private void takeControl() {
        hasControl = true;
        codeArea.setEditable(true);
        codeArea.requestFocus();
        takeControlBtn.setEnabled(false);
        releaseControlBtn.setVisible(true);
        controlStatusLabel.setText("You have control");

        if (socketConnected) {
            socketOut.println(SocketProtocol.buildMessage(
                    "CODE_LOCK", room.getCode(), currentUser.getId(), currentUser.getName(), ""));
        }
    }

    private void releaseControl() {
        hasControl = false;
        codeArea.setEditable(false);
        takeControlBtn.setEnabled(true);
        releaseControlBtn.setVisible(false);
        controlStatusLabel.setText("Editor is unlocked — take control to write code");

        if (socketConnected) {
            socketOut.println(SocketProtocol.buildMessage(
                    "CODE_RELEASE", room.getCode(), currentUser.getId(), currentUser.getName(), ""));
        }
    }

    private void onCodeChanged() {
        if (suppressCodeChangeEvent || !hasControl || !socketConnected) return;
        SwingUtilities.invokeLater(() ->
                socketOut.println(SocketProtocol.buildMessage(
                        "CODE_CHANGE", room.getCode(), currentUser.getId(), currentUser.getName(), codeArea.getText()))
        );
    }

    // ================= Submission =================

    private void submitCode(StyledButton submitBtn) {
        if (challenge == null) {
            JOptionPane.showMessageDialog(this, "No challenge in this room.");
            return;
        }

        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Write some code first.");
            return;
        }

        String langName = room.getLanguage() != null ? room.getLanguage() : "Java";

        submitBtn.setEnabled(false);
        statusLabel.setText("Running on Judge0...");
        statusLabel.setForeground(Theme.TEAL);

        new Thread(() -> {
            Judge0Service judge = new Judge0Service();
            Judge0Service.Result r = judge.evaluateAll(code, langName, challenge.getTestCases());

            int langId = resolveLanguageId(langName);

            SubmissionDAO subDAO = new SubmissionDAO();
            boolean alreadyPassed = subDAO.hasUserPassed(currentUser.getId(), challenge.getId());

            Submission s = new Submission();
            s.setChallengeId(challenge.getId());
            s.setUserId(currentUser.getId());
            s.setCode(code);
            s.setLanguageId(langId);
            s.setResult(r.passed ? "Pass" : "Fail");
            s.setScore(r.passed && !alreadyPassed ? r.score : 0);

            int subId = subDAO.create(s);
            System.out.println("Submission saved id=" + subId
                    + " result=" + s.getResult()
                    + " score=" + s.getScore()
                    + " langId=" + langId
                    + " userId=" + currentUser.getId()
                    + " challengeId=" + challenge.getId());

            SwingUtilities.invokeLater(() -> {
                submitBtn.setEnabled(true);
                statusLabel.setText(r.status + " | Score: " + s.getScore());
                statusLabel.setForeground(r.passed ? Theme.GREEN : Theme.RED);

                String msg = r.status + "\nScore: " + s.getScore() + "\n";
                if (r.stdout != null && !r.stdout.isEmpty()) msg += "\n" + r.stdout;
                if (r.message != null && !r.message.isEmpty()) msg += "\n\n" + r.message;
                if (r.passed && alreadyPassed) msg += "\n\n(Already solved earlier — no additional points.)";
                if (subId < 0) msg += "\n\n(Warning: could not save submission to database.)";

                JOptionPane.showMessageDialog(this, msg, "Judge0 Result",
                        r.passed ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            });
        }).start();
    }

    private int resolveLanguageId(String langName) {
        LanguageDAO langDAO = new LanguageDAO();
        Language lang = langDAO.findByName(langName);
        if (lang != null) return lang.getId();

        List<Language> all = langDAO.findAll();
        if (all != null) {
            for (Language l : all) {
                if (l.getName() != null && l.getName().equalsIgnoreCase(langName)) return l.getId();
            }
            if (!all.isEmpty()) return all.get(0).getId();
        }
        return 1;
    }

    private String defaultCode(String language) {
        if (language == null) language = "Java";
        switch (language) {
            case "Python": return "# write your solution\n\n";
            case "JavaScript": return "// write your solution\n\n";
            case "C++": return "#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // your code\n    return 0;\n}\n";
            case "C": return "#include <stdio.h>\n\nint main() {\n    // your code\n    return 0;\n}\n";
            case "Java":
            default: return "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // your code\n    }\n}\n";
        }
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}