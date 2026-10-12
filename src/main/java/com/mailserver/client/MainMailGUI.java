package com.mailserver.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.mailserver.ServerConfig;
import com.mailserver.dao.EmailDAO;
import com.mailserver.model.Email;

public class MainMailGUI extends JFrame {

        private final EmailDAO emailDAO = new EmailDAO();

        // =========================================================
        // COLORS
        // =========================================================

        private static final Color SIDEBAR = new Color(15, 23, 42);
        private static final Color MUTED = new Color(100, 116, 139);
        private static final Color SIDEBAR_HOVER = new Color(30, 41, 59);
        private static final Color PRIMARY = new Color(37, 99, 235);
        private static final Color PRIMARY_HOVER = new Color(29, 78, 216);
        private static final Color BACKGROUND = new Color(245, 247, 251);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(30, 41, 59);
        private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
        private static final Color BORDER = new Color(226, 232, 240);

        // =========================================================
        // FIELDS
        // =========================================================

        private final String username;
        private final String password;

        private JPanel contentPanel;
        private JLabel pageTitle;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public MainMailGUI(String username, String password) {

                this.username = username;
                this.password = password;

                setTitle("Mail Client - " + username);
                setSize(1150, 720);
                setMinimumSize(new Dimension(950, 600));
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLocationRelativeTo(null);

                createUI();
        }

        // =========================================================
        // CREATE UI
        // =========================================================

        private void createUI() {

                JPanel root = new JPanel(new BorderLayout());
                root.setBackground(BACKGROUND);

                JPanel sidebar = createSidebar();
                root.add(sidebar, BorderLayout.WEST);

                JPanel rightPanel = new JPanel(new BorderLayout());
                rightPanel.setBackground(BACKGROUND);

                JPanel header = createHeader();
                rightPanel.add(header, BorderLayout.NORTH);

                contentPanel = new JPanel(new BorderLayout());
                contentPanel.setBackground(BACKGROUND);
                contentPanel.setBorder(new EmptyBorder(25, 30, 30, 30));

                rightPanel.add(contentPanel, BorderLayout.CENTER);
                root.add(rightPanel, BorderLayout.CENTER);

                add(root);

                showInbox();
        }

        // =========================================================
        // SIDEBAR
        // =========================================================

        private JPanel createSidebar() {

                JPanel sidebar = new JPanel();
                sidebar.setPreferredSize(new Dimension(245, 720));
                sidebar.setBackground(SIDEBAR);
                sidebar.setBorder(new EmptyBorder(25, 18, 20, 18));
                sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
                sidebar.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                logoPanel.setOpaque(false);
                logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                logoPanel.setPreferredSize(new Dimension(200, 40));
                logoPanel.setMinimumSize(new Dimension(200, 40));
                logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                MailLogo logo = new MailLogo();
                logoPanel.add(logo);

                JLabel logoText = new JLabel("EMAIL");
                logoText.setForeground(Color.WHITE);
                logoText.setFont(new Font("Segoe UI", Font.BOLD, 28));
                logoPanel.add(logoText);

                sidebar.add(logoPanel);
                sidebar.add(Box.createVerticalStrut(12));

                JPanel userCard = new JPanel(new BorderLayout(8, 0));
                userCard.setBackground(new Color(30, 41, 59));
                userCard.setBorder(new EmptyBorder(8, 10, 8, 10));
                userCard.setPreferredSize(new Dimension(261, 62));
                userCard.setMinimumSize(new Dimension(261, 62));
                userCard.setMaximumSize(new Dimension(261, 62));
                userCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                UserAvatar avatar = new UserAvatar();
                avatar.setPreferredSize(new Dimension(40, 40));
                avatar.setMinimumSize(new Dimension(40, 40));
                avatar.setMaximumSize(new Dimension(40, 40));
                userCard.add(avatar, BorderLayout.WEST);

                JPanel userInfo = new JPanel();
                userInfo.setOpaque(false);
                userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
                userInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel name = new JLabel(username);
                name.setForeground(Color.WHITE);
                name.setFont(new Font("Segoe UI", Font.BOLD, 14));

                JLabel status = new JLabel("Online");
                status.setForeground(new Color(134, 239, 172));
                status.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                userInfo.add(name);
                userInfo.add(Box.createVerticalStrut(2));
                userInfo.add(status);

                userCard.add(userInfo, BorderLayout.CENTER);

                sidebar.add(userCard);
                sidebar.add(Box.createVerticalStrut(10));

                MailMenuButton inbox = new MailMenuButton("Inbox", MailMenuButton.INBOX);
                MailMenuButton starred = new MailMenuButton("Starred", MailMenuButton.STARRED);
                MailMenuButton compose = new MailMenuButton("Compose", MailMenuButton.COMPOSE);
                MailMenuButton sent = new MailMenuButton("Sent", MailMenuButton.SENT);
                MailMenuButton trash = new MailMenuButton("Trash", MailMenuButton.TRASH);

                inbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                starred.setAlignmentX(Component.LEFT_ALIGNMENT);
                compose.setAlignmentX(Component.LEFT_ALIGNMENT);
                sent.setAlignmentX(Component.LEFT_ALIGNMENT);
                trash.setAlignmentX(Component.LEFT_ALIGNMENT);

                sidebar.add(inbox);
                sidebar.add(Box.createVerticalStrut(6));
                sidebar.add(starred);
                sidebar.add(Box.createVerticalStrut(6));
                sidebar.add(compose);
                sidebar.add(Box.createVerticalStrut(6));
                sidebar.add(sent);
                sidebar.add(Box.createVerticalStrut(6));
                sidebar.add(trash);

                sidebar.add(Box.createVerticalGlue());

                MailMenuButton logout = new MailMenuButton("Logout", MailMenuButton.LOGOUT);
                logout.setAlignmentX(Component.LEFT_ALIGNMENT);
                sidebar.add(logout);

                inbox.addActionListener(e -> showInbox());
                starred.addActionListener(e -> showStarred());
                compose.addActionListener(e -> showCompose());
                sent.addActionListener(e -> showSent());
                trash.addActionListener(e -> showTrash());

                logout.addActionListener(e -> {
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                                new MailClientGUI().setVisible(true);
                        });
                });

                return sidebar;
        }

        // =========================================================
        // HEADER
        // =========================================================

        private JPanel createHeader() {

                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(WHITE);
                header.setBorder(new EmptyBorder(20, 30, 20, 30));

                pageTitle = new JLabel("Inbox");
                pageTitle.setForeground(TEXT);
                pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));

                header.add(pageTitle, BorderLayout.WEST);

                JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                accountPanel.setOpaque(false);

                JLabel account = new JLabel(username);
                account.setForeground(TEXT_SECONDARY);
                account.setFont(new Font("Segoe UI", Font.BOLD, 14));

                StatusDot dot = new StatusDot();

                accountPanel.add(account);
                accountPanel.add(dot);

                header.add(accountPanel, BorderLayout.EAST);

                return header;
        }

        // =========================================================
        // INBOX
        // =========================================================

        private void showInbox() {

                setPageTitle("Inbox");
                contentPanel.removeAll();
                contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

                JPanel card = createCard();

                JPanel cardHeader = new JPanel(new BorderLayout());
                cardHeader.setOpaque(false);

                JLabel title = new JLabel("Inbox");
                title.setForeground(TEXT);
                title.setFont(new Font("Segoe UI", Font.BOLD, 24));

                RoundedButton refresh = new RoundedButton(
                                "REFRESH",
                                new Color(239, 246, 255),
                                new Color(219, 234, 254));
                refresh.setForeground(PRIMARY);
                refresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
                refresh.setPreferredSize(new Dimension(110, 38));
                refresh.setMaximumSize(new Dimension(110, 38));
                refresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

                cardHeader.add(title, BorderLayout.WEST);
                cardHeader.add(refresh, BorderLayout.EAST);
                card.add(cardHeader, BorderLayout.NORTH);

                JPanel emailList = new JPanel();
                emailList.setOpaque(false);
                emailList.setLayout(new BoxLayout(emailList, BoxLayout.Y_AXIS));

                try {
                        List<Email> emails = fetchInboxViaPOP3();

                        if (emails == null || emails.isEmpty()) {
                                JPanel empty = createEmptyPanel(
                                                "Your inbox is empty",
                                                "Received emails will appear here.");
                                emailList.add(empty);
                        } else {
                                for (Email email : emails) {
                                        JPanel emailItem = createEmailItem(email);
                                        emailList.add(emailItem);
                                        emailList.add(Box.createVerticalStrut(8));
                                }
                        }
                } catch (Exception e) {
                        JLabel error = new JLabel(
                                        "Cannot load inbox: " + e.getMessage());
                        error.setForeground(new Color(220, 38, 38));
                        error.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        emailList.add(error);
                        e.printStackTrace();
                }

                JScrollPane scroll = new JScrollPane(emailList);
                scroll.setBorder(null);
                scroll.setBackground(WHITE);
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                card.add(scroll, BorderLayout.CENTER);

                refresh.addActionListener(e -> showInbox());

                contentPanel.add(card, BorderLayout.CENTER);
                refreshUI();
        }

        private String getUserEmail() {

                if (username.equalsIgnoreCase("thanh")) {
                        return "thanh@mail.local";
                }

                if (username.equalsIgnoreCase("admin")) {
                        return "admin@mail.local";
                }

                if (username.contains("@")) {
                        return username;
                }

                return username + "@mail.local";
        }

        private JPanel createEmailItem(Email email) {

                JPanel item = new JPanel(new BorderLayout(15, 0));

                boolean read = emailDAO.isRead(
                                email.getId(),
                                getUserEmail());

                final Color NORMAL_BG = read
                                ? Color.WHITE
                                : new Color(239, 246, 255);
                final Color HOVER_BG = new Color(226, 232, 240);

                item.setBackground(NORMAL_BG);
                item.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER),
                                new EmptyBorder(15, 15, 15, 15)));
                item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

                UserAvatar avatar = new UserAvatar();
                item.add(avatar, BorderLayout.WEST);

                JPanel information = new JPanel();
                information.setOpaque(false);
                information.setLayout(new BoxLayout(information, BoxLayout.Y_AXIS));

                JLabel sender = new JLabel(email.getSender());
                sender.setFont(new Font(
                                "Segoe UI",
                                read ? Font.PLAIN : Font.BOLD,
                                14));
                sender.setForeground(read
                                ? new Color(148, 163, 184)
                                : new Color(30, 41, 59));

                String subjectText = email.getSubject();
                if (subjectText == null || subjectText.trim().isEmpty()) {
                        subjectText = "(No subject)";
                }

                JLabel subject = new JLabel(subjectText);
                subject.setFont(new Font(
                                "Segoe UI",
                                read ? Font.PLAIN : Font.BOLD,
                                13));
                subject.setForeground(read
                                ? new Color(148, 163, 184)
                                : new Color(37, 99, 235));

                String content = email.getContent();
                if (content == null)
                        content = "";
                content = content.replace("\n", " ");
                if (content.length() > 70) {
                        content = content.substring(0, 70) + "...";
                }

                JLabel preview = new JLabel(content);
                preview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                preview.setForeground(read
                                ? new Color(203, 213, 225)
                                : new Color(100, 116, 139));

                information.add(sender);
                information.add(Box.createVerticalStrut(3));
                information.add(subject);
                information.add(Box.createVerticalStrut(3));
                information.add(preview);

                item.add(information, BorderLayout.CENTER);

                JPanel right = new JPanel();
                right.setOpaque(false);
                right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

                boolean starred = emailDAO.isStarred(
                                email.getId(),
                                getUserEmail());

                JButton starButton = new JButton(
                                starred ? "★" : "☆");

                starButton.setFont(
                                new Font("Segoe UI Symbol", Font.PLAIN, 22));

                starButton.setForeground(
                                starred
                                                ? new Color(234, 179, 8)
                                                : new Color(148, 163, 184));

                starButton.setFocusPainted(false);
                starButton.setBorderPainted(false);
                starButton.setContentAreaFilled(false);
                starButton.setCursor(
                                new Cursor(Cursor.HAND_CURSOR));

                starButton.addActionListener(e -> {

                        boolean success;

                        if (emailDAO.isStarred(
                                        email.getId(),
                                        getUserEmail())) {

                                success = emailDAO.unstarEmail(
                                                email.getId(),
                                                getUserEmail());

                                if (success) {
                                        starButton.setText("☆");
                                        starButton.setForeground(
                                                        new Color(148, 163, 184));
                                }

                        } else {

                                success = emailDAO.starEmail(
                                                email.getId(),
                                                getUserEmail());

                                if (success) {
                                        starButton.setText("★");
                                        starButton.setForeground(
                                                        new Color(234, 179, 8));
                                }
                        }
                });

                right.add(starButton);
                right.add(Box.createVerticalStrut(5));

                JLabel id = new JLabel("#" + email.getId());
                id.setForeground(TEXT_SECONDARY);
                id.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                right.add(id);

                item.add(right, BorderLayout.EAST);

                item.setCursor(new Cursor(Cursor.HAND_CURSOR));

                MouseAdapter openEmailAdapter = new MouseAdapter() {

                        @Override
                        public void mouseEntered(MouseEvent e) {
                                item.setBackground(HOVER_BG);
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                                item.setBackground(NORMAL_BG);
                        }

                        @Override
                        public void mouseClicked(MouseEvent e) {
                                emailDAO.markAsRead(
                                                email.getId(),
                                                getUserEmail());

                                showEmailDetail(email);
                        }
                };

                information.addMouseListener(openEmailAdapter);
                sender.addMouseListener(openEmailAdapter);
                subject.addMouseListener(openEmailAdapter);
                preview.addMouseListener(openEmailAdapter);
                avatar.addMouseListener(openEmailAdapter);

                item.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mouseEntered(MouseEvent e) {
                                item.setBackground(HOVER_BG);
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                                item.setBackground(NORMAL_BG);
                        }
                });

                return item;
        }

        // =========================================================
        // SHOW EMAIL / CONVERSATION
        // =========================================================

        private void showEmailDetail(Email email) {

                List<Email> conversation = new ArrayList<>();

                try {
                        int threadId = email.getThreadId();

                        if (threadId > 0) {
                                POP3Client client = new POP3Client(username, password);
                                List<POP3Client.RawMessage> raws = client.fetchConversation(threadId);

                                for (POP3Client.RawMessage raw : raws) {
                                        conversation.add(rawToEmail(raw));
                                }
                        }
                } catch (Exception ex) {
                        System.err.println("[Conversation] " + ex.getMessage());
                        ex.printStackTrace();
                }

                if (conversation.isEmpty()) {
                        conversation.add(email);
                }

                contentPanel.removeAll();
                contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

                String myEmail = getUserEmail();

                JPanel main = new JPanel(new BorderLayout());
                main.setOpaque(false);

                JPanel header = new JPanel(new BorderLayout(15, 0));
                header.setOpaque(false);
                header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

                RoundedButton back = new RoundedButton(
                                "← BACK",
                                new Color(241, 245, 249),
                                new Color(226, 232, 240));
                back.setForeground(new Color(51, 65, 85));
                back.setFont(new Font("Segoe UI", Font.BOLD, 12));
                back.setPreferredSize(new Dimension(90, 38));
                back.setCursor(new Cursor(Cursor.HAND_CURSOR));
                back.addActionListener(e -> showInbox());

                String conversationSubject = conversation.get(0).getSubject() == null
                                || conversation.get(0).getSubject().isEmpty()
                                                ? "(No subject)"
                                                : conversation.get(0).getSubject();

                JLabel title = new JLabel(conversationSubject);
                title.setFont(new Font("Segoe UI", Font.BOLD, 22));
                title.setForeground(TEXT);

                JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
                leftHeader.setOpaque(false);
                leftHeader.add(back);
                leftHeader.add(title);
                header.add(leftHeader, BorderLayout.WEST);

                JPanel chatPanel = new JPanel();
                chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
                chatPanel.setOpaque(false);
                chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

                for (Email message : conversation) {
                        boolean isMine = message.getSender() != null
                                        && message.getSender().equalsIgnoreCase(myEmail);
                        JPanel bubble = createChatBubble(message, isMine);
                        chatPanel.add(bubble);

                        // ⭐ HIỂN THỊ ATTACHMENTS
                        if (message.getAttachments() != null
                                        && !message.getAttachments().isEmpty()) {
                                JPanel attPanel = createAttachmentsPanel(
                                                message.getAttachments(), isMine);
                                chatPanel.add(attPanel);
                        }

                        chatPanel.add(Box.createVerticalStrut(14));
                }

                JScrollPane scroll = new JScrollPane(chatPanel);
                scroll.setBorder(null);
                scroll.setOpaque(false);
                scroll.getViewport().setOpaque(false);
                scroll.getVerticalScrollBar().setUnitIncrement(16);

                JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                bottom.setOpaque(false);
                bottom.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

                Email replyTarget = conversation.get(conversation.size() - 1);

                RoundedButton reply = new RoundedButton("REPLY", PRIMARY, PRIMARY_HOVER);
                reply.setFont(new Font("Segoe UI", Font.BOLD, 12));
                reply.setPreferredSize(new Dimension(110, 40));
                reply.addActionListener(e -> showReplyCompose(replyTarget));

                RoundedButton delete = new RoundedButton(
                                "DELETE",
                                new Color(220, 38, 38),
                                new Color(185, 28, 28));
                delete.setFont(new Font("Segoe UI", Font.BOLD, 12));
                delete.setPreferredSize(new Dimension(110, 40));

                delete.addActionListener(e -> {
                        int result = JOptionPane.showConfirmDialog(
                                        this,
                                        "Delete this email?",
                                        "Confirm Delete",
                                        JOptionPane.YES_NO_OPTION);

                        if (result != JOptionPane.YES_OPTION)
                                return;

                        new Thread(() -> {
                                boolean success = false;
                                String errorMsg = null;
                                try {
                                        success = emailDAO.deleteMail(
                                                        email.getId(),
                                                        getUserEmail());
                                } catch (Exception ex) {
                                        errorMsg = ex.getMessage();
                                        ex.printStackTrace();
                                }

                                final boolean ok = success;
                                final String err = errorMsg;

                                SwingUtilities.invokeLater(() -> {
                                        if (ok) {
                                                JOptionPane.showMessageDialog(this, "Email deleted.");
                                                showInbox();
                                        } else {
                                                JOptionPane.showMessageDialog(
                                                                this,
                                                                "Cannot delete email."
                                                                                + (err != null ? "\n" + err : ""),
                                                                "Error",
                                                                JOptionPane.ERROR_MESSAGE);
                                        }
                                });
                        }).start();
                });

                bottom.add(reply);
                bottom.add(delete);

                JPanel centerWrapper = new JPanel(new BorderLayout());
                centerWrapper.setOpaque(false);
                centerWrapper.add(scroll, BorderLayout.CENTER);
                centerWrapper.add(bottom, BorderLayout.SOUTH);

                main.add(header, BorderLayout.NORTH);
                main.add(centerWrapper, BorderLayout.CENTER);

                contentPanel.add(main, BorderLayout.CENTER);
                refreshUI();
        }

        private JPanel createChatBubble(Email email, boolean isMine) {

                JPanel wrapper = new JPanel(new FlowLayout(
                                isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
                wrapper.setOpaque(false);
                wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel column = new JPanel();
                column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
                column.setOpaque(false);

                String headText;
                if (isMine) {
                        headText = "You → " + safeText(email.getReceiver());
                } else {
                        headText = safeText(email.getSender());
                }

                JLabel headLabel = new JLabel(headText);
                headLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                headLabel.setForeground(isMine
                                ? new Color(30, 64, 175)
                                : new Color(71, 85, 105));
                headLabel.setAlignmentX(isMine
                                ? Component.RIGHT_ALIGNMENT
                                : Component.LEFT_ALIGNMENT);
                headLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 4, 6));

                JPanel bubble = new JPanel();
                bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
                bubble.setBackground(isMine
                                ? new Color(219, 234, 254)
                                : Color.WHITE);
                bubble.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                                isMine ? new Color(191, 219, 254) : new Color(226, 232, 240),
                                                1, true),
                                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
                bubble.setAlignmentX(isMine
                                ? Component.RIGHT_ALIGNMENT
                                : Component.LEFT_ALIGNMENT);

                String subject = email.getSubject();
                if (subject != null && !subject.trim().isEmpty()
                                && !subject.equalsIgnoreCase("(No subject)")) {
                        JLabel subjectLabel = new JLabel(subject);
                        subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        subjectLabel.setForeground(isMine
                                        ? new Color(30, 64, 175)
                                        : new Color(15, 23, 42));
                        subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        bubble.add(subjectLabel);
                        bubble.add(Box.createVerticalStrut(6));
                }

                String content = email.getContent() == null ? "" : email.getContent();
                JTextArea body = new JTextArea(content);
                body.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                body.setForeground(new Color(30, 41, 59));
                body.setOpaque(false);
                body.setEditable(false);
                body.setLineWrap(true);
                body.setWrapStyleWord(true);
                body.setAlignmentX(Component.LEFT_ALIGNMENT);
                body.setBorder(null);
                body.setColumns(35);

                bubble.add(body);

                JLabel timeLabel = new JLabel("#" + email.getId());
                timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                timeLabel.setForeground(new Color(148, 163, 184));
                timeLabel.setAlignmentX(isMine
                                ? Component.RIGHT_ALIGNMENT
                                : Component.LEFT_ALIGNMENT);
                timeLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));

                column.add(headLabel);
                column.add(bubble);
                column.add(timeLabel);

                wrapper.add(column);
                return wrapper;
        }

        private String safeText(String value) {
                return value == null || value.isEmpty() ? "-" : value;
        }

        private Email rawToEmail(POP3Client.RawMessage raw) {
                Email e = new Email();
                e.setSender(raw.sender);
                e.setReceiver(raw.receiver);
                e.setSubject(raw.subject);
                e.setContent(raw.content);
                e.setId(raw.messageId > 0 ? raw.messageId : raw.number);
                e.setThreadId(raw.threadId);
                e.setInReplyTo(raw.inReplyTo);
                e.setRead(raw.isRead);
                e.setAttachments(raw.attachments);
                return e;
        }

        // =========================================================
        // REPLY COMPOSE
        // =========================================================

        private void showReplyCompose(Email target) {

                setPageTitle("Reply");
                contentPanel.removeAll();

                JPanel card = createCard();
                card.setLayout(new BorderLayout(0, 20));

                JLabel title = new JLabel("Reply");
                title.setForeground(TEXT);
                title.setFont(new Font("Segoe UI", Font.BOLD, 21));
                card.add(title, BorderLayout.NORTH);

                JPanel form = new JPanel();
                form.setOpaque(false);
                form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

                JLabel receiverLabel = createLabel("To");
                receiverLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextField receiver = createTextField();
                receiver.setText(safeText(target.getSender()));
                receiver.setAlignmentX(Component.LEFT_ALIGNMENT);
                receiver.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                JLabel subjectLabel = createLabel("Subject");
                subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextField subject = createTextField();
                String originalSubject = target.getSubject() == null ? "" : target.getSubject().trim();
                if (originalSubject.toLowerCase().startsWith("re:")) {
                        subject.setText(originalSubject);
                } else {
                        subject.setText("Re: " + originalSubject);
                }
                subject.setAlignmentX(Component.LEFT_ALIGNMENT);
                subject.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                JLabel messageLabel = createLabel("Message");
                messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextArea message = new JTextArea();
                message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                message.setForeground(TEXT);
                message.setLineWrap(true);
                message.setWrapStyleWord(true);
                message.setBorder(new EmptyBorder(12, 12, 12, 12));

                JScrollPane messageScroll = new JScrollPane(message);
                messageScroll.setBorder(BorderFactory.createLineBorder(BORDER));
                messageScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                messageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

                form.add(receiverLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(receiver);
                form.add(Box.createVerticalStrut(15));
                form.add(subjectLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(subject);
                form.add(Box.createVerticalStrut(15));
                form.add(messageLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(messageScroll);
                form.add(Box.createVerticalStrut(18));

                RoundedButton send = new RoundedButton("SEND REPLY", PRIMARY, PRIMARY_HOVER);
                send.setPreferredSize(new Dimension(150, 44));
                send.setMaximumSize(new Dimension(150, 44));
                send.setAlignmentX(Component.LEFT_ALIGNMENT);
                form.add(send);

                send.addActionListener(e -> {

                        String to = receiver.getText().trim();
                        String sub = subject.getText().trim();
                        String msg = message.getText().trim();

                        if (to.isEmpty() || to.equals("-")) {
                                JOptionPane.showMessageDialog(this, "Cannot determine receiver.");
                                return;
                        }

                        if (sub.isEmpty()) {
                                JOptionPane.showMessageDialog(this, "Please enter subject.");
                                return;
                        }

                        if (msg.isEmpty()) {
                                JOptionPane.showMessageDialog(this, "Please enter message.");
                                return;
                        }

                        send.setEnabled(false);
                        send.setText("SENDING...");

                        new Thread(() -> {

                                boolean success = sendSMTPMail(
                                                to,
                                                sub,
                                                msg,
                                                target.getThreadId(),
                                                target.getId());

                                SwingUtilities.invokeLater(() -> {
                                        send.setEnabled(true);
                                        send.setText("SEND REPLY");

                                        if (success) {
                                                JOptionPane.showMessageDialog(
                                                                this,
                                                                "Reply sent successfully!",
                                                                "SMTP",
                                                                JOptionPane.INFORMATION_MESSAGE);

                                                showEmailDetail(target);
                                        } else {
                                                JOptionPane.showMessageDialog(
                                                                this,
                                                                "Cannot send reply.Please check SMTP Server.",
                                                                "SMTP Error",
                                                                JOptionPane.ERROR_MESSAGE);
                                        }
                                });
                        }).start();
                });

                card.add(form, BorderLayout.CENTER);
                contentPanel.add(card, BorderLayout.CENTER);
                refreshUI();
        }

        // =========================================================
        // COMPOSE
        // =========================================================

        private void showCompose() {

                setPageTitle("Compose");
                contentPanel.removeAll();

                JPanel card = createCard();
                card.setLayout(new BorderLayout(0, 20));

                JLabel title = new JLabel("New Message");
                title.setForeground(TEXT);
                title.setFont(new Font("Segoe UI", Font.BOLD, 21));
                card.add(title, BorderLayout.NORTH);

                JPanel form = new JPanel();
                form.setOpaque(false);
                form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

                JLabel receiverLabel = createLabel("To (multiple recipients: comma or semicolon)");
                receiverLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextField receiver = createTextField();
                receiver.setAlignmentX(Component.LEFT_ALIGNMENT);
                receiver.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                form.add(receiverLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(receiver);
                form.add(Box.createVerticalStrut(15));

                JLabel subjectLabel = createLabel("Subject");
                subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextField subject = createTextField();
                subject.setAlignmentX(Component.LEFT_ALIGNMENT);
                subject.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                form.add(subjectLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(subject);
                form.add(Box.createVerticalStrut(15));

                JLabel messageLabel = createLabel("Message");
                messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextArea message = new JTextArea();
                Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);
                if (emojiFont.getFamily().equals("Segoe UI Emoji")) {
                        message.setFont(emojiFont);
                } else {
                        message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                }
                message.setForeground(TEXT);
                message.setLineWrap(true);
                message.setWrapStyleWord(true);
                message.setBorder(new EmptyBorder(12, 12, 12, 12));

                JScrollPane messageScroll = new JScrollPane(message);
                messageScroll.setBorder(BorderFactory.createLineBorder(BORDER));
                messageScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                messageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

                form.add(messageLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(messageScroll);
                form.add(Box.createVerticalStrut(18));

                // ============ ATTACHMENT PANEL ============
                JLabel attachLabel = createLabel("Attachments");
                attachLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                form.add(attachLabel);
                form.add(Box.createVerticalStrut(7));

                JPanel attachPanel = new JPanel();
                attachPanel.setLayout(new BoxLayout(attachPanel, BoxLayout.Y_AXIS));
                attachPanel.setOpaque(false);
                attachPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel fileListPanel = new JPanel();
                fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));
                fileListPanel.setOpaque(false);
                fileListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JScrollPane fileScroll = new JScrollPane(fileListPanel);
                fileScroll.setBorder(BorderFactory.createLineBorder(BORDER));
                fileScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                fileScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
                fileScroll.setPreferredSize(new Dimension(400, 100));

                List<java.io.File> selectedFiles = new ArrayList<>();

                RoundedButton attachButton = new RoundedButton(
                                "ATTACH FILE", new Color(241, 245, 249), new Color(226, 232, 240));
                attachButton.setForeground(TEXT);
                attachButton.setPreferredSize(new Dimension(160, 40));
                attachButton.setMaximumSize(new Dimension(160, 40));
                attachButton.setAlignmentX(Component.LEFT_ALIGNMENT);

                attachButton.addActionListener(e -> {
                        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                        chooser.setMultiSelectionEnabled(true);
                        chooser.setDialogTitle("Chọn file đính kèm");

                        int result = chooser.showOpenDialog(this);
                        if (result != javax.swing.JFileChooser.APPROVE_OPTION)
                                return;

                        java.io.File[] files = chooser.getSelectedFiles();

                        for (java.io.File f : files) {
                                if (f.length() > 10 * 1024 * 1024) {
                                        JOptionPane.showMessageDialog(this,
                                                        "File quá lớn (max 10MB): " + f.getName());
                                        continue;
                                }

                                if (selectedFiles.contains(f))
                                        continue;

                                selectedFiles.add(f);

                                JPanel row = new JPanel(new BorderLayout(8, 0));
                                row.setOpaque(false);
                                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                                JLabel nameLbl = new JLabel("" + f.getName()
                                                + "  (" + (f.length() / 1024) + " KB)");
                                nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                                nameLbl.setForeground(TEXT);

                                JButton removeBtn = new JButton("X");
                                removeBtn.setForeground(new Color(220, 38, 38));
                                removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                                removeBtn.setFocusPainted(false);
                                removeBtn.setBorderPainted(false);
                                removeBtn.setContentAreaFilled(false);
                                removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                                removeBtn.addActionListener(ev -> {
                                        selectedFiles.remove(f);
                                        fileListPanel.remove(row);
                                        fileListPanel.revalidate();
                                        fileListPanel.repaint();
                                });

                                row.add(nameLbl, BorderLayout.CENTER);
                                row.add(removeBtn, BorderLayout.EAST);
                                fileListPanel.add(row);
                        }

                        fileListPanel.revalidate();
                        fileListPanel.repaint();
                });

                attachPanel.add(attachButton);
                attachPanel.add(Box.createVerticalStrut(8));
                attachPanel.add(fileScroll);

                form.add(attachPanel);
                form.add(Box.createVerticalStrut(18));

                RoundedButton send = new RoundedButton("SEND MAIL", PRIMARY, PRIMARY_HOVER);
                send.setPreferredSize(new Dimension(150, 44));
                send.setMaximumSize(new Dimension(150, 44));
                send.setAlignmentX(Component.LEFT_ALIGNMENT);

                form.add(send);

                send.addActionListener(e -> {
                        List<String> recipients = parseRecipients(receiver.getText());
                        String sub = subject.getText().trim();
                        String msg = message.getText().trim();

                        if (recipients.isEmpty()) {
                                JOptionPane.showMessageDialog(this,
                                                "Please enter at least one receiver email.\n"
                                                                + "Separate multiple addresses with comma (,) or semicolon (;).",
                                                "Missing receiver",
                                                JOptionPane.WARNING_MESSAGE);
                                receiver.requestFocus();
                                return;
                        }

                        if (sub.isEmpty()) {
                                JOptionPane.showMessageDialog(this,
                                                "Please enter subject.",
                                                "Missing subject",
                                                JOptionPane.WARNING_MESSAGE);
                                subject.requestFocus();
                                return;
                        }

                        if (msg.isEmpty()) {
                                JOptionPane.showMessageDialog(this,
                                                "Please enter message.",
                                                "Missing message",
                                                JOptionPane.WARNING_MESSAGE);
                                message.requestFocus();
                                return;
                        }

                        send.setEnabled(false);
                        send.setText("SENDING...");

                        List<java.io.File> filesToSend = new ArrayList<>(selectedFiles);

                        new Thread(() -> {

                                boolean success = sendSMTPMailWithAttachments(
                                                recipients, sub, msg, filesToSend);

                                SwingUtilities.invokeLater(() -> {
                                        send.setEnabled(true);
                                        send.setText("SEND MAIL");

                                        if (success) {
                                                JOptionPane.showMessageDialog(this, "Email sent successfully!");
                                                receiver.setText("");
                                                subject.setText("");
                                                message.setText("");
                                                selectedFiles.clear();
                                                fileListPanel.removeAll();
                                                fileListPanel.revalidate();
                                                fileListPanel.repaint();
                                        } else {
                                                JOptionPane.showMessageDialog(this,
                                                                "Cannot send email. Please check SMTP Server.",
                                                                "SMTP Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                });

                        }).start();
                });
                card.add(form, BorderLayout.CENTER);
                contentPanel.add(card, BorderLayout.CENTER);
                refreshUI();
        }

        // =========================================================
        // SMTP SEND - SINGLE RECIPIENT
        // =========================================================

        private boolean sendSMTPMail(String receiver, String subject, String content) {
                if (receiver == null || receiver.trim().isEmpty()) {
                        return false;
                }

                return sendSMTPMail(
                                List.of(receiver.trim()),
                                subject,
                                content,
                                0,
                                null);
        }

        // =========================================================
        // SMTP SEND - MULTIPLE RECIPIENTS
        // =========================================================

        private boolean sendSMTPMail(
                        List<String> receivers,
                        String subject,
                        String content) {

                return sendSMTPMail(
                                receivers,
                                subject,
                                content,
                                0,
                                null);
        }

        // =========================================================
        // SMTP SEND - WITH ATTACHMENTS (⭐ FIX: Attachment TRƯỚC blank line)
        // =========================================================

        private boolean sendSMTPMailWithAttachments(
                        List<String> receivers,
                        String subject,
                        String content,
                        List<File> files) {

                if (receivers == null || receivers.isEmpty())
                        return false;

                final String SMTP_HOST = ServerConfig.getServerHost();
                final int SMTP_PORT = ServerConfig.SMTP_PORT;
                String sender = getUserEmail();

                // ============ BƯỚC 1: UPLOAD TẤT CẢ FILE ============
                List<POP3Client.AttachmentMeta> uploaded = new ArrayList<>();
                try {
                        POP3Client uploader = new POP3Client(username, password);
                        for (File f : files) {
                                String ct = Files.probeContentType(f.toPath());
                                if (ct == null)
                                        ct = "application/octet-stream";

                                byte[] data = Files.readAllBytes(f.toPath());
                                int attId = uploader.uploadAttachment(f.getName(), ct, data);

                                uploaded.add(new POP3Client.AttachmentMeta(
                                                attId, f.getName(), ct, data.length));
                        }
                } catch (Exception ex) {
                        System.err.println("Upload attachment failed: " + ex.getMessage());
                        ex.printStackTrace();
                        return false;
                }

                // ============ BƯỚC 2: GỬI SMTP ============
                try (
                                Socket socket = new Socket(SMTP_HOST, SMTP_PORT);
                                BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(socket.getInputStream()));
                                PrintWriter writer = new PrintWriter(
                                                socket.getOutputStream(), true)) {

                        String response = reader.readLine();
                        if (response == null || !response.startsWith("220"))
                                return false;

                        writer.println("HELO localhost");
                        response = reader.readLine();
                        if (response == null || !response.startsWith("250"))
                                return false;

                        writer.println("MAIL FROM: " + sender);
                        response = reader.readLine();
                        if (response == null || !response.startsWith("250"))
                                return false;

                        for (String receiver : receivers) {
                                writer.println("RCPT TO: " + receiver);
                                response = reader.readLine();
                                if (response == null || !response.startsWith("250"))
                                        return false;
                        }

                        writer.println("DATA");
                        response = reader.readLine();
                        if (response == null || !response.startsWith("354"))
                                return false;

                        // ⭐⭐ HEADERS — GỬI TRƯỚC BLANK LINE ⭐⭐
                        writer.println("Subject: " + subject);

                        for (POP3Client.AttachmentMeta att : uploaded) {
                                writer.println("Attachment: " + att.id
                                                + "|" + att.filename
                                                + "|" + att.contentType
                                                + "|" + att.size);
                        }

                        writer.println(); // ← blank line SAU KHI đã gửi hết header

                        // ============ BODY ============
                        String[] lines = content.split("\\R", -1);
                        for (String line : lines) {
                                writer.println(line.startsWith(".") ? "." + line : line);
                        }

                        writer.println("<END>");
                        response = reader.readLine();
                        if (response == null || !response.startsWith("250"))
                                return false;

                        writer.println("QUIT");
                        response = reader.readLine();
                        return response != null && response.startsWith("221");

                } catch (Exception e) {
                        System.out.println("SMTP connection error: " + e.getMessage());
                        e.printStackTrace();
                        return false;
                }
        }

        // =========================================================
        // SMTP SEND - THREAD / REPLY
        // =========================================================

        private boolean sendSMTPMail(
                        String receiver,
                        String subject,
                        String content,
                        int threadId,
                        Integer inReplyTo) {

                if (receiver == null || receiver.trim().isEmpty()) {
                        return false;
                }

                return sendSMTPMail(
                                List.of(receiver.trim()),
                                subject,
                                content,
                                threadId,
                                inReplyTo);
        }

        private boolean sendSMTPMail(
                        List<String> receivers,
                        String subject,
                        String content,
                        int threadId,
                        Integer inReplyTo) {

                if (receivers == null || receivers.isEmpty()) {
                        return false;
                }

                final String SMTP_HOST = ServerConfig.getServerHost();
                final int SMTP_PORT = ServerConfig.SMTP_PORT;
                String sender = getUserEmail();

                try (
                                Socket socket = new Socket(SMTP_HOST, SMTP_PORT);
                                BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(socket.getInputStream()));
                                PrintWriter writer = new PrintWriter(
                                                socket.getOutputStream(), true)) {

                        String response = reader.readLine();
                        if (response == null || !response.startsWith("220"))
                                return false;

                        writer.println("HELO localhost");
                        response = reader.readLine();
                        if (response == null || !response.startsWith("250"))
                                return false;

                        writer.println("MAIL FROM: " + sender);
                        response = reader.readLine();
                        if (response == null || !response.startsWith("250"))
                                return false;

                        for (String receiver : receivers) {
                                writer.println("RCPT TO: " + receiver);
                                response = reader.readLine();
                                if (response == null || !response.startsWith("250")) {
                                        return false;
                                }
                        }

                        writer.println("DATA");
                        response = reader.readLine();
                        if (response == null || !response.startsWith("354"))
                                return false;

                        writer.println("Subject: " + subject);

                        if (threadId > 0) {
                                writer.println("Thread-ID: " + threadId);
                        }

                        if (inReplyTo != null && inReplyTo > 0) {
                                writer.println("In-Reply-To: " + inReplyTo);
                        }

                        writer.println();

                        String[] lines = content.split("\\R", -1);
                        for (String line : lines) {
                                if (line.startsWith(".")) {
                                        writer.println("." + line);
                                } else {
                                        writer.println(line);
                                }
                        }

                        writer.println("<END>");
                        response = reader.readLine();
                        if (response == null || !response.startsWith("250"))
                                return false;

                        writer.println("QUIT");
                        response = reader.readLine();
                        return response != null && response.startsWith("221");

                } catch (Exception e) {
                        System.out.println("SMTP connection error: " + e.getMessage());
                        e.printStackTrace();
                        return false;
                }
        }

        // =========================================================
        // PARSE MULTIPLE RECIPIENTS
        // =========================================================

        private List<String> parseRecipients(String text) {

                List<String> recipients = new ArrayList<>();

                if (text == null || text.trim().isEmpty()) {
                        return recipients;
                }

                String[] parts = text.split("[,;]+");

                for (String part : parts) {
                        String email = part.trim();

                        if (!email.isEmpty() && !recipients.contains(email)) {
                                recipients.add(email);
                        }
                }

                return recipients;
        }

        private List<Email> fetchInboxViaPOP3() {
                List<Email> result = new ArrayList<>();

                try {
                        POP3Client client = new POP3Client(username, password);
                        List<POP3Client.RawMessage> raws = client.fetchInbox();

                        for (POP3Client.RawMessage raw : raws) {
                                result.add(rawToEmail(raw));
                        }
                } catch (Exception ex) {
                        System.err.println("[POP3 Client] " + ex.getMessage());
                        ex.printStackTrace();
                }

                return result;
        }

        private List<Email> fetchSentViaPOP3() {
                List<Email> result = new ArrayList<>();

                try {
                        POP3Client client = new POP3Client(username, password);
                        List<POP3Client.RawMessage> raws = client.fetchSent();

                        for (POP3Client.RawMessage raw : raws) {
                                result.add(rawToEmail(raw));
                        }
                } catch (Exception ex) {
                        System.err.println("[POP3 Client - Sent] " + ex.getMessage());
                        ex.printStackTrace();
                }

                return result;
        }

        private void showStarred() {

                setPageTitle("Starred");
                contentPanel.removeAll();

                JPanel card = createCard();

                JPanel cardHeader = new JPanel(new BorderLayout());
                cardHeader.setOpaque(false);

                JLabel title = new JLabel("Starred");
                title.setForeground(TEXT);
                title.setFont(new Font("Segoe UI", Font.BOLD, 21));

                RoundedButton refresh = new RoundedButton(
                                "REFRESH",
                                new Color(239, 246, 255),
                                new Color(219, 234, 254));

                refresh.setForeground(PRIMARY);
                refresh.setPreferredSize(new Dimension(100, 36));
                refresh.setMaximumSize(new Dimension(100, 36));

                cardHeader.add(title, BorderLayout.WEST);
                cardHeader.add(refresh, BorderLayout.EAST);

                card.add(cardHeader, BorderLayout.NORTH);

                JPanel emailList = new JPanel();
                emailList.setOpaque(false);
                emailList.setLayout(new BoxLayout(emailList, BoxLayout.Y_AXIS));

                try {

                        List<Email> emails = fetchStarredEmails();

                        if (emails == null || emails.isEmpty()) {

                                JPanel empty = createEmptyPanel(
                                                "No starred emails",
                                                "Emails you star will appear here.");

                                emailList.add(empty);

                        } else {

                                for (Email email : emails) {

                                        JPanel item = createStarredEmailItem(email);

                                        emailList.add(item);
                                        emailList.add(Box.createVerticalStrut(8));
                                }
                        }

                } catch (Exception e) {

                        JLabel error = new JLabel(
                                        "Cannot load starred emails: " + e.getMessage());

                        error.setForeground(new Color(220, 38, 38));
                        error.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                        emailList.add(error);

                        e.printStackTrace();
                }

                JScrollPane scroll = new JScrollPane(emailList);

                scroll.setBorder(null);
                scroll.setBackground(WHITE);
                scroll.getVerticalScrollBar().setUnitIncrement(16);

                card.add(scroll, BorderLayout.CENTER);

                refresh.addActionListener(e -> showStarred());

                contentPanel.add(card, BorderLayout.CENTER);

                refreshUI();
        }

        private JPanel createStarredEmailItem(Email email) {

                JPanel panel = new JPanel(new BorderLayout(15, 0));

                panel.setBackground(Color.WHITE);

                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER),
                                BorderFactory.createEmptyBorder(
                                                15, 15, 15, 15)));

                panel.setMaximumSize(
                                new Dimension(Integer.MAX_VALUE, 90));

                JButton starButton = new JButton("★");

                starButton.setFont(
                                new Font("Segoe UI Symbol", Font.PLAIN, 24));

                starButton.setForeground(
                                new Color(234, 179, 8));

                starButton.setFocusPainted(false);
                starButton.setBorderPainted(false);
                starButton.setContentAreaFilled(false);
                starButton.setCursor(
                                new Cursor(Cursor.HAND_CURSOR));

                starButton.addActionListener(e -> {

                        boolean success = emailDAO.unstarEmail(
                                        email.getId(),
                                        getUserEmail());

                        if (success) {
                                showStarred();
                        }
                });

                panel.add(starButton, BorderLayout.WEST);

                JPanel info = new JPanel();

                info.setOpaque(false);
                info.setLayout(
                                new BoxLayout(info, BoxLayout.Y_AXIS));

                JLabel sender = new JLabel(
                                email.getSender());

                sender.setFont(
                                new Font("Segoe UI", Font.BOLD, 14));

                sender.setForeground(TEXT);

                String subjectText = email.getSubject();

                if (subjectText == null ||
                                subjectText.trim().isEmpty()) {

                        subjectText = "(No subject)";
                }

                JLabel subject = new JLabel(
                                subjectText);

                subject.setFont(
                                new Font("Segoe UI", Font.BOLD, 13));

                subject.setForeground(PRIMARY);

                String preview = email.getContent() == null
                                ? ""
                                : email.getContent()
                                                .replace("\n", " ");

                if (preview.length() > 70) {
                        preview = preview.substring(0, 70)
                                        + "...";
                }

                JLabel content = new JLabel(preview);

                content.setFont(
                                new Font("Segoe UI", Font.PLAIN, 12));

                content.setForeground(TEXT_SECONDARY);

                info.add(sender);
                info.add(Box.createVerticalStrut(4));
                info.add(subject);
                info.add(Box.createVerticalStrut(4));
                info.add(content);

                panel.add(info, BorderLayout.CENTER);

                JLabel id = new JLabel(
                                "#" + email.getId());

                id.setFont(
                                new Font("Segoe UI", Font.PLAIN, 11));

                id.setForeground(
                                new Color(148, 163, 184));

                panel.add(id, BorderLayout.EAST);

                MouseAdapter adapter = new MouseAdapter() {

                        @Override
                        public void mouseEntered(MouseEvent e) {
                                panel.setBackground(
                                                new Color(248, 250, 252));
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                                panel.setBackground(Color.WHITE);
                        }

                        @Override
                        public void mouseClicked(MouseEvent e) {

                                if (e.getSource() != starButton) {
                                        showEmailDetail(email);
                                }
                        }
                };

                panel.addMouseListener(adapter);
                info.addMouseListener(adapter);
                sender.addMouseListener(adapter);
                subject.addMouseListener(adapter);
                content.addMouseListener(adapter);
                id.addMouseListener(adapter);

                return panel;
        }

        private List<Email> fetchStarredEmails() {

                try {
                        return emailDAO.getStarredEmails(getUserEmail());
                } catch (Exception e) {
                        e.printStackTrace();
                        return new ArrayList<>();
                }
        }

        // =========================================================
        // SENT
        // =========================================================

        private void showSent() {

                setPageTitle("Sent");
                contentPanel.removeAll();

                JPanel card = createCard();

                JPanel cardHeader = new JPanel(new BorderLayout());
                cardHeader.setOpaque(false);

                JLabel title = new JLabel("Sent Mail");
                title.setForeground(TEXT);
                title.setFont(new Font("Segoe UI", Font.BOLD, 21));

                RoundedButton refresh = new RoundedButton(
                                "REFRESH",
                                new Color(239, 246, 255),
                                new Color(219, 234, 254));
                refresh.setForeground(PRIMARY);
                refresh.setPreferredSize(new Dimension(100, 36));
                refresh.setMaximumSize(new Dimension(100, 36));

                cardHeader.add(title, BorderLayout.WEST);
                cardHeader.add(refresh, BorderLayout.EAST);
                card.add(cardHeader, BorderLayout.NORTH);

                JPanel emailList = new JPanel();
                emailList.setOpaque(false);
                emailList.setLayout(new BoxLayout(emailList, BoxLayout.Y_AXIS));

                try {
                        List<Email> emails = fetchSentViaPOP3();

                        if (emails == null || emails.isEmpty()) {
                                JPanel empty = createEmptyPanel(
                                                "No sent emails",
                                                "Emails you send will appear here.");
                                emailList.add(empty);
                        } else {
                                for (Email email : emails) {
                                        JPanel emailItem = createSentEmailItem(email);
                                        emailList.add(emailItem);
                                        emailList.add(Box.createVerticalStrut(8));
                                }
                        }

                } catch (Exception e) {
                        JLabel error = new JLabel(
                                        "Cannot load sent emails: " + e.getMessage());
                        error.setForeground(new Color(220, 38, 38));
                        error.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        emailList.add(error);
                        e.printStackTrace();
                }

                JScrollPane scroll = new JScrollPane(emailList);
                scroll.setBorder(null);
                scroll.setBackground(WHITE);
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                card.add(scroll, BorderLayout.CENTER);

                refresh.addActionListener(e -> showSent());

                contentPanel.add(card, BorderLayout.CENTER);
                refreshUI();
        }

        private JPanel createSentEmailItem(Email email) {
                JPanel panel = new JPanel(new BorderLayout(20, 0));
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                                BorderFactory.createEmptyBorder(15, 18, 15, 18)));
                panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setOpaque(false);

                JLabel receiverLabel = new JLabel("To: " + email.getReceiver());
                receiverLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                receiverLabel.setForeground(new Color(15, 23, 42));

                JLabel subjectLabel = new JLabel(
                                email.getSubject() == null || email.getSubject().isEmpty()
                                                ? "(No subject)"
                                                : email.getSubject());
                subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                subjectLabel.setForeground(new Color(37, 99, 235));

                String preview = email.getContent() == null
                                ? ""
                                : email.getContent().replace("\n", " ");

                if (preview.length() > 70) {
                        preview = preview.substring(0, 70) + "...";
                }

                JLabel contentLabel = new JLabel(preview);
                contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                contentLabel.setForeground(new Color(100, 116, 139));

                info.add(receiverLabel);
                info.add(Box.createVerticalStrut(4));
                info.add(subjectLabel);
                info.add(Box.createVerticalStrut(4));
                info.add(contentLabel);

                JLabel idLabel = new JLabel("#" + email.getId());
                idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                idLabel.setForeground(new Color(148, 163, 184));

                panel.add(info, BorderLayout.CENTER);
                panel.add(idLabel, BorderLayout.EAST);

                MouseAdapter adapter = new MouseAdapter() {

                        @Override
                        public void mouseEntered(MouseEvent e) {
                                panel.setBackground(new Color(248, 250, 252));
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                                panel.setBackground(Color.WHITE);
                        }

                        @Override
                        public void mouseClicked(MouseEvent e) {
                                showSentEmailDetail(email);
                        }
                };

                panel.addMouseListener(adapter);
                info.addMouseListener(adapter);
                receiverLabel.addMouseListener(adapter);
                subjectLabel.addMouseListener(adapter);
                contentLabel.addMouseListener(adapter);
                idLabel.addMouseListener(adapter);

                return panel;
        }

        private void showSentEmailDetail(Email email) {

                setPageTitle("Sent Mail");
                contentPanel.removeAll();

                JPanel card = createCard();

                String subjectText = email.getSubject();
                if (subjectText == null || subjectText.trim().isEmpty()) {
                        subjectText = "(No subject)";
                }

                JLabel subject = new JLabel(subjectText);
                subject.setForeground(TEXT);
                subject.setFont(new Font("Segoe UI", Font.BOLD, 22));
                card.add(subject, BorderLayout.NORTH);

                JPanel body = new JPanel();
                body.setOpaque(false);
                body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

                JLabel from = new JLabel("From: " + email.getSender());
                from.setForeground(TEXT_SECONDARY);
                from.setFont(new Font("Segoe UI", Font.BOLD, 13));

                JLabel to = new JLabel("To: " + email.getReceiver());
                to.setForeground(TEXT_SECONDARY);
                to.setFont(new Font("Segoe UI", Font.BOLD, 13));

                body.add(from);
                body.add(Box.createVerticalStrut(5));
                body.add(to);
                body.add(Box.createVerticalStrut(20));

                JTextArea content = new JTextArea(email.getContent());
                content.setEditable(false);
                content.setLineWrap(true);
                content.setWrapStyleWord(true);
                content.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                content.setForeground(TEXT);
                content.setBackground(WHITE);
                content.setBorder(new EmptyBorder(5, 0, 5, 0));

                body.add(content);
                card.add(body, BorderLayout.CENTER);

                RoundedButton back = new RoundedButton(
                                "BACK TO SENT",
                                PRIMARY,
                                PRIMARY_HOVER);
                back.setPreferredSize(new Dimension(150, 40));
                back.addActionListener(e -> showSent());

                card.add(back, BorderLayout.SOUTH);

                contentPanel.add(card, BorderLayout.CENTER);
                refreshUI();
        }

        // =========================================================
        // TRASH
        // =========================================================

        private void showTrash() {
                setPageTitle("Trash");
                contentPanel.removeAll();
                contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

                JPanel header = new JPanel(new BorderLayout());
                header.setOpaque(false);
                header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

                JLabel title = new JLabel("Trash");
                title.setFont(new Font("Segoe UI", Font.BOLD, 28));
                title.setForeground(TEXT);

                RoundedButton refresh = new RoundedButton("REFRESH", PRIMARY, Color.WHITE);
                refresh.setPreferredSize(new Dimension(100, 35));
                refresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
                refresh.addActionListener(e -> showTrash());

                header.add(title, BorderLayout.WEST);
                header.add(refresh, BorderLayout.EAST);

                JPanel emailList = new JPanel();
                emailList.setLayout(new BoxLayout(emailList, BoxLayout.Y_AXIS));
                emailList.setBackground(BACKGROUND);

                try {
                        List<Email> emails = emailDAO.getTrash(getUserEmail());

                        if (emails.isEmpty()) {
                                JPanel emptyPanel = new JPanel();
                                emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
                                emptyPanel.setOpaque(false);
                                emptyPanel.add(Box.createVerticalStrut(50));

                                JLabel emptyIcon = new JLabel("Trash is empty");
                                emptyIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
                                emptyIcon.setForeground(new Color(148, 163, 184));
                                emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
                                emptyPanel.add(emptyIcon);

                                emailList.add(emptyPanel);
                        } else {
                                for (Email email : emails) {
                                        JPanel item = createTrashEmailItem(email);
                                        emailList.add(item);
                                        emailList.add(Box.createVerticalStrut(12));
                                }
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                        JLabel error = new JLabel("Cannot load Trash: " + e.getMessage());
                        error.setForeground(Color.RED);
                        emailList.add(error);
                }

                JScrollPane scrollPane = new JScrollPane(emailList);
                scrollPane.setBorder(null);
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.getViewport().setBackground(BACKGROUND);

                contentPanel.add(header, BorderLayout.NORTH);
                contentPanel.add(scrollPane, BorderLayout.CENTER);

                refreshUI();
                contentPanel.revalidate();
                contentPanel.repaint();
        }

        private JPanel createTrashEmailItem(Email email) {

                JPanel panel = new JPanel(new BorderLayout(15, 0));
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                                BorderFactory.createEmptyBorder(15, 18, 15, 18)));
                panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setOpaque(false);

                JLabel sender = new JLabel("From: " + email.getSender());
                sender.setFont(new Font("Segoe UI", Font.BOLD, 14));
                sender.setForeground(new Color(15, 23, 42));

                JLabel subject = new JLabel(
                                email.getSubject() == null || email.getSubject().isEmpty()
                                                ? "(No subject)"
                                                : email.getSubject());
                subject.setFont(new Font("Segoe UI", Font.BOLD, 13));
                subject.setForeground(new Color(100, 116, 139));

                String preview = email.getContent() == null
                                ? ""
                                : email.getContent().replace("\n", " ");

                if (preview.length() > 70) {
                        preview = preview.substring(0, 70) + "...";
                }

                JLabel content = new JLabel(preview);
                content.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                content.setForeground(new Color(148, 163, 184));

                info.add(sender);
                info.add(Box.createVerticalStrut(4));
                info.add(subject);
                info.add(Box.createVerticalStrut(4));
                info.add(content);

                RoundedButton restore = new RoundedButton(
                                "RESTORE",
                                new Color(22, 163, 74),
                                Color.WHITE);

                restore.addActionListener(e -> {
                        try {
                                boolean success = emailDAO.restoreMail(
                                                email.getId(),
                                                getUserEmail());

                                if (success) {
                                        JOptionPane.showMessageDialog(this, "Email restored.");
                                        showTrash();
                                } else {
                                        JOptionPane.showMessageDialog(this,
                                                        "Cannot restore email.",
                                                        "Error",
                                                        JOptionPane.ERROR_MESSAGE);
                                }
                        } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(this,
                                                "Error: " + ex.getMessage(),
                                                "Database Error",
                                                JOptionPane.ERROR_MESSAGE);
                        }
                });

                panel.add(info, BorderLayout.CENTER);
                panel.add(restore, BorderLayout.EAST);

                return panel;
        }

        // =========================================================
        // EMPTY PANEL
        // =========================================================

        private JPanel createEmptyPanel(String titleText, String description) {

                JPanel panel = new JPanel();
                panel.setOpaque(false);
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                MailLargeIcon icon = new MailLargeIcon();
                icon.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel title = new JLabel(titleText);
                title.setForeground(TEXT);
                title.setFont(new Font("Segoe UI", Font.BOLD, 19));
                title.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel text = new JLabel(description);
                text.setForeground(TEXT_SECONDARY);
                text.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                text.setAlignmentX(Component.CENTER_ALIGNMENT);

                panel.add(Box.createVerticalGlue());
                panel.add(icon);
                panel.add(Box.createVerticalStrut(15));
                panel.add(title);
                panel.add(Box.createVerticalStrut(7));
                panel.add(text);
                panel.add(Box.createVerticalGlue());

                return panel;
        }

        // =========================================================
        // CARD
        // =========================================================

        private JPanel createCard() {

                JPanel card = new JPanel(new BorderLayout(0, 15));
                card.setBackground(WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER),
                                new EmptyBorder(25, 25, 25, 25)));

                return card;
        }

        // =========================================================
        // LABEL
        // =========================================================

        private JLabel createLabel(String text) {

                JLabel label = new JLabel(text);
                label.setForeground(TEXT);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));

                return label;
        }

        // =========================================================
        // TEXT FIELD
        // =========================================================

        private JTextField createTextField() {

                JTextField field = new JTextField();
                field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                field.setForeground(TEXT);
                field.setBackground(new Color(248, 250, 252));
                field.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER),
                                new EmptyBorder(9, 12, 9, 12)));
                field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

                return field;
        }

        // =========================================================
        // SMALL BUTTON
        // =========================================================

        private JButton createSmallButton(String text) {

                JButton button = new JButton(text);
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setBackground(new Color(239, 246, 255));
                button.setForeground(PRIMARY);
                button.setFont(new Font("Segoe UI", Font.BOLD, 12));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));

                return button;
        }

        // =========================================================
        // PAGE TITLE
        // =========================================================

        private void setPageTitle(String title) {
                if (pageTitle != null) {
                        pageTitle.setText(title);
                }
        }

        // =========================================================
        // REFRESH UI
        // =========================================================

        private void refreshUI() {
                contentPanel.revalidate();
                contentPanel.repaint();
        }

        // =========================================================
        // LOGOUT
        // =========================================================

        private void logout() {

                int result = JOptionPane.showConfirmDialog(
                                this,
                                "Do you want to logout?",
                                "Logout",
                                JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                        new MailClientGUI().setVisible(true);
                        dispose();
                }
        }

        // =========================================================
        // ATTACHMENTS PANEL
        // =========================================================

        private JPanel createAttachmentsPanel(
                        List<POP3Client.AttachmentMeta> attachments,
                        boolean isMine) {

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setOpaque(false);
                panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

                for (POP3Client.AttachmentMeta att : attachments) {

                        JPanel row = new JPanel(new BorderLayout(10, 0));
                        row.setBackground(new Color(248, 250, 252));
                        row.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(BORDER),
                                        new EmptyBorder(8, 12, 8, 12)));
                        row.setMaximumSize(new Dimension(500, 50));
                        row.setAlignmentX(isMine
                                        ? Component.RIGHT_ALIGNMENT
                                        : Component.LEFT_ALIGNMENT);

                        String icon = getFileIcon(att.contentType);

                        JLabel iconLbl = new JLabel(icon);
                        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

                        JLabel nameLbl = new JLabel(
                                        att.filename + "  (" + att.getReadableSize() + ")");
                        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        nameLbl.setForeground(TEXT);

                        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                        left.setOpaque(false);
                        left.add(iconLbl);
                        left.add(nameLbl);

                        RoundedButton downloadBtn = new RoundedButton(
                                        "⬇ DOWNLOAD",
                                        PRIMARY,
                                        PRIMARY_HOVER);
                        downloadBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        downloadBtn.setPreferredSize(new Dimension(120, 32));
                        downloadBtn.setMaximumSize(new Dimension(120, 32));

                        downloadBtn.addActionListener(e -> downloadAttachment(att));

                        row.add(left, BorderLayout.CENTER);
                        row.add(downloadBtn, BorderLayout.EAST);

                        panel.add(row);
                        panel.add(Box.createVerticalStrut(6));
                }

                return panel;
        }

        // =========================================================
        // ICON THEO LOẠI FILE
        // =========================================================

        private String getFileIcon(String contentType) {

                if (contentType == null)
                        return "📎";

                if (contentType.startsWith("image/"))
                        return "🖼";

                if (contentType.startsWith("video/"))
                        return "🎬";

                if (contentType.startsWith("audio/"))
                        return "🎵";

                if (contentType.contains("pdf"))
                        return "📕";

                if (contentType.contains("zip")
                                || contentType.contains("rar")
                                || contentType.contains("tar"))
                        return "🗜";

                if (contentType.contains("word")
                                || contentType.contains("document"))
                        return "📘";

                if (contentType.contains("excel")
                                || contentType.contains("spreadsheet"))
                        return "📗";

                if (contentType.contains("powerpoint")
                                || contentType.contains("presentation"))
                        return "📙";

                if (contentType.startsWith("text/"))
                        return "📄";

                return "📎";
        }

        // =========================================================
        // DOWNLOAD ATTACHMENT
        // =========================================================

        private void downloadAttachment(POP3Client.AttachmentMeta att) {

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Lưu file: " + att.filename);
                chooser.setSelectedFile(new File(att.filename));

                int result = chooser.showSaveDialog(this);
                if (result != JFileChooser.APPROVE_OPTION)
                        return;

                File target = chooser.getSelectedFile();

                new Thread(() -> {

                        try {
                                POP3Client client = new POP3Client(username, password);
                                byte[] data = client.downloadAttachment(att.id);

                                Files.write(target.toPath(), data);

                                SwingUtilities.invokeLater(() -> {
                                        JOptionPane.showMessageDialog(
                                                        this,
                                                        "Đã lưu file:\n" + target.getAbsolutePath(),
                                                        "Download thành công",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                });

                        } catch (Exception ex) {

                                ex.printStackTrace();

                                SwingUtilities.invokeLater(() -> {
                                        JOptionPane.showMessageDialog(
                                                        this,
                                                        "Lỗi tải file:\n" + ex.getMessage(),
                                                        "Download lỗi",
                                                        JOptionPane.ERROR_MESSAGE);
                                });
                        }

                }).start();
        }

        // =========================================================
        // ROUNDED BUTTON
        // =========================================================

        static class RoundedButton extends JButton {

                private final Color normalColor;
                private final Color hoverColor;

                public RoundedButton(String text, Color normalColor, Color hoverColor) {

                        super(text);
                        this.normalColor = normalColor;
                        this.hoverColor = hoverColor;

                        setForeground(Color.WHITE);
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                        setFocusPainted(false);
                        setBorderPainted(false);
                        setContentAreaFilled(false);
                        setOpaque(false);
                        setBackground(normalColor);
                        setCursor(new Cursor(Cursor.HAND_CURSOR));

                        addMouseListener(new MouseAdapter() {

                                @Override
                                public void mouseEntered(MouseEvent e) {
                                        setBackground(hoverColor);
                                        repaint();
                                }

                                @Override
                                public void mouseExited(MouseEvent e) {
                                        setBackground(normalColor);
                                        repaint();
                                }
                        });
                }

                @Override
                protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                        g2.setColor(getForeground());

                        FontMetrics fm = g2.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(getText())) / 2;
                        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                        g2.drawString(getText(), x, y);

                        g2.dispose();
                }
        }

        // =========================================================
        // SIDEBAR MENU BUTTON
        // =========================================================

        static class MailMenuButton extends JButton {

                public static final int INBOX = 1;
                public static final int STARRED = 2;
                public static final int COMPOSE = 3;
                public static final int SENT = 4;
                public static final int TRASH = 5;
                public static final int LOGOUT = 6;

                private final int iconType;
                private boolean hovered = false;

                public MailMenuButton(String text, int iconType) {

                        super(text);
                        this.iconType = iconType;

                        setForeground(new Color(226, 232, 240));
                        setFont(new Font("Segoe UI", Font.BOLD, 14));
                        setHorizontalAlignment(SwingConstants.LEFT);
                        setBorderPainted(false);
                        setFocusPainted(false);
                        setContentAreaFilled(false);
                        setOpaque(false);
                        setPreferredSize(new Dimension(205, 44));
                        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                        setCursor(new Cursor(Cursor.HAND_CURSOR));

                        addMouseListener(new MouseAdapter() {

                                @Override
                                public void mouseEntered(MouseEvent e) {
                                        hovered = true;
                                        repaint();
                                }

                                @Override
                                public void mouseExited(MouseEvent e) {
                                        hovered = false;
                                        repaint();
                                }
                        });
                }

                @Override
                protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        if (hovered) {
                                g2.setColor(SIDEBAR_HOVER);
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        }

                        drawIcon(g2, iconType, 17, getHeight() / 2);

                        g2.setColor(getForeground());
                        g2.setFont(getFont());

                        FontMetrics fm = g2.getFontMetrics();
                        int textX = 47;
                        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                        g2.drawString(getText(), textX, textY);

                        g2.dispose();
                }

                private void drawIcon(Graphics2D g2, int type, int x, int y) {

                        Color iconColor = new Color(203, 213, 225);
                        g2.setColor(iconColor);
                        g2.setStroke(new java.awt.BasicStroke(
                                        2.0f,
                                        java.awt.BasicStroke.CAP_ROUND,
                                        java.awt.BasicStroke.JOIN_ROUND));

                        if (type == INBOX) {
                                g2.drawRoundRect(x, y - 7, 18, 14, 3, 3);
                                g2.drawLine(x, y - 6, x + 9, y + 1);
                                g2.drawLine(x + 18, y - 6, x + 9, y + 1);
                        } else if (type == STARRED) {
                                g2.drawOval(x + 3, y - 9, 16, 16);
                                g2.drawLine(x + 11, y - 12, x + 13, y - 9);
                                g2.drawLine(x + 15, y - 8, x + 18, y - 6);
                        } else if (type == COMPOSE) {
                                g2.drawLine(x + 3, y + 7, x + 16, y - 6);
                                g2.drawLine(x + 2, y + 9, x + 7, y + 8);
                                g2.drawLine(x + 2, y + 9, x + 3, y + 4);
                                g2.drawLine(x + 13, y - 9, x + 17, y - 5);
                        } else if (type == SENT) {
                                g2.drawLine(x + 2, y, x + 17, y - 7);
                                g2.drawLine(x + 17, y - 7, x + 13, y + 8);
                                g2.drawLine(x + 13, y + 8, x + 9, y + 3);
                                g2.drawLine(x + 9, y + 3, x + 2, y);
                        } else if (type == TRASH) {
                                g2.drawRoundRect(x + 3, y - 5, 13, 15, 2, 2);
                                g2.drawLine(x + 1, y - 8, x + 18, y - 8);
                                g2.drawLine(x + 7, y - 11, x + 13, y - 11);
                                g2.drawLine(x + 7, y - 2, x + 7, y + 6);
                                g2.drawLine(x + 12, y - 2, x + 12, y + 6);
                        } else if (type == LOGOUT) {
                                g2.drawRoundRect(x + 1, y - 9, 10, 18, 2, 2);
                                g2.drawLine(x + 8, y, x + 18, y);
                                g2.drawLine(x + 14, y - 4, x + 18, y);
                                g2.drawLine(x + 14, y + 4, x + 18, y);
                        }
                }
        }

        // =========================================================
        // MAIL LOGO
        // =========================================================

        static class MailLogo extends JComponent {

                public MailLogo() {
                        setPreferredSize(new Dimension(35, 35));
                }

                @Override
                protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(new Color(59, 130, 246));
                        g2.fillRoundRect(2, 5, 30, 23, 6, 6);

                        g2.setColor(Color.WHITE);
                        g2.setStroke(new java.awt.BasicStroke(
                                        2.2f,
                                        java.awt.BasicStroke.CAP_ROUND,
                                        java.awt.BasicStroke.JOIN_ROUND));
                        g2.drawLine(5, 8, 17, 18);
                        g2.drawLine(17, 18, 29, 8);

                        g2.dispose();
                }
        }

        // =========================================================
        // USER AVATAR
        // =========================================================

        static class UserAvatar extends JComponent {

                public UserAvatar() {
                        setPreferredSize(new Dimension(38, 38));
                }

                @Override
                protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(new Color(59, 130, 246));
                        g2.fillOval(1, 1, 36, 36);

                        g2.setColor(Color.WHITE);
                        g2.fillOval(13, 7, 12, 12);
                        g2.fillRoundRect(9, 20, 20, 12, 8, 8);

                        g2.dispose();
                }
        }

        // =========================================================
        // STATUS DOT
        // =========================================================

        static class StatusDot extends JComponent {

                public StatusDot() {
                        setPreferredSize(new Dimension(10, 10));
                }

                @Override
                protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(new Color(34, 197, 94));
                        g2.fillOval(1, 1, 8, 8);

                        g2.dispose();
                }
        }

        // =========================================================
        // LARGE MAIL ICON
        // =========================================================

        static class MailLargeIcon extends JComponent {

                public MailLargeIcon() {
                        setPreferredSize(new Dimension(80, 70));
                        setMaximumSize(new Dimension(80, 70));
                }

                @Override
                protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        int x = 10;
                        int y = 12;

                        g2.setColor(new Color(219, 234, 254));
                        g2.fillRoundRect(x, y, 60, 45, 10, 10);

                        g2.setColor(PRIMARY);
                        g2.setStroke(new java.awt.BasicStroke(
                                        2.5f,
                                        java.awt.BasicStroke.CAP_ROUND,
                                        java.awt.BasicStroke.JOIN_ROUND));

                        g2.drawRoundRect(x + 8, y + 10, 44, 28, 5, 5);
                        g2.drawLine(x + 8, y + 11, x + 30, y + 27);
                        g2.drawLine(x + 52, y + 11, x + 30, y + 27);

                        g2.dispose();
                }
        }

        // =========================================================
        // MAIN
        // =========================================================

        public static void main(String[] args) {

                SwingUtilities.invokeLater(() -> {
                        MainMailGUI gui = new MainMailGUI("thanh", "123456");
                        gui.setVisible(true);
                });
        }
}