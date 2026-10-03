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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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

import com.mailserver.dao.EmailDAO;
import com.mailserver.model.Email;

/**
 * ============================================================
 * MAIL SERVER - MAIN GUI
 * ============================================================
 *
 * Giao diện chính của Mail Client.
 *
 * Chức năng giao diện:
 * - Inbox
 * - Compose
 * - Sent
 * - Trash
 * - Logout
 *
 * Hiện tại đây là phần GUI.
 * Các chức năng SMTP / POP3 / IMAP sẽ được nối vào sau.
 */
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

        private JPanel contentPanel;

        private JLabel pageTitle;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public MainMailGUI(String username) {

                this.username = username;

                setTitle(
                                "Mail Client - " + username);

                setSize(1150, 720);

                setMinimumSize(
                                new Dimension(950, 600));

                setDefaultCloseOperation(
                                JFrame.EXIT_ON_CLOSE);

                setLocationRelativeTo(null);

                createUI();
        }

        // =========================================================
        // CREATE UI
        // =========================================================

        private void createUI() {

                JPanel root = new JPanel(new BorderLayout());

                root.setBackground(BACKGROUND);

                // =====================================================
                // SIDEBAR
                // =====================================================

                JPanel sidebar = createSidebar();

                root.add(
                                sidebar,
                                BorderLayout.WEST);

                // =====================================================
                // RIGHT SIDE
                // =====================================================

                JPanel rightPanel = new JPanel(new BorderLayout());

                rightPanel.setBackground(
                                BACKGROUND);

                // Header

                JPanel header = createHeader();

                rightPanel.add(
                                header,
                                BorderLayout.NORTH);

                // Content

                contentPanel = new JPanel(
                                new BorderLayout());

                contentPanel.setBackground(
                                BACKGROUND);

                contentPanel.setBorder(
                                new EmptyBorder(
                                                25,
                                                30,
                                                30,
                                                30));

                rightPanel.add(
                                contentPanel,
                                BorderLayout.CENTER);

                root.add(
                                rightPanel,
                                BorderLayout.CENTER);

                add(root);

                // Default page

                showInbox();
        }

        // =========================================================
        // SIDEBAR
        // =========================================================

        private JPanel createSidebar() {

                JPanel sidebar = new JPanel();

                sidebar.setPreferredSize(
                                new Dimension(245, 720));

                sidebar.setBackground(
                                SIDEBAR);

                sidebar.setBorder(
                                new EmptyBorder(
                                                25,
                                                18,
                                                20,
                                                18));

                sidebar.setLayout(
                                new BoxLayout(
                                                sidebar,
                                                BoxLayout.Y_AXIS));

                sidebar.setAlignmentX(Component.LEFT_ALIGNMENT);

                // =====================================================
                // LOGO
                // =====================================================

                JPanel logoPanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                5,
                                                0));

                logoPanel.setOpaque(false);

                logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                logoPanel.setPreferredSize(
                                new Dimension(
                                                200,
                                                40));

                logoPanel.setMinimumSize(
                                new Dimension(
                                                200,
                                                40));

                logoPanel.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                40));

                MailLogo logo = new MailLogo();

                logoPanel.add(logo);

                JLabel logoText = new JLabel("MAIL");

                logoText.setForeground(
                                Color.WHITE);

                logoText.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                28));

                logoPanel.add(logoText);

                sidebar.add(logoPanel);

                sidebar.add(
                                Box.createVerticalStrut(12));

                // =====================================================
                // USER CARD
                // =====================================================

                JPanel userCard = new JPanel(
                                new BorderLayout(
                                                8,
                                                0));

                userCard.setBackground(
                                new Color(30, 41, 59));

                userCard.setBorder(
                                new EmptyBorder(
                                                8,
                                                10,
                                                8,
                                                10));

                userCard.setPreferredSize(
                                new Dimension(
                                                261,
                                                62));

                userCard.setMinimumSize(
                                new Dimension(
                                                261,
                                                62));

                userCard.setMaximumSize(
                                new Dimension(
                                                261,
                                                62));

                userCard.setAlignmentX(
                                Component.LEFT_ALIGNMENT);
                UserAvatar avatar = new UserAvatar();

                avatar.setPreferredSize(
                                new Dimension(
                                                40,
                                                40));

                avatar.setMinimumSize(
                                new Dimension(
                                                40,
                                                40));

                avatar.setMaximumSize(
                                new Dimension(
                                                40,
                                                40));
                userCard.add(
                                avatar,
                                BorderLayout.WEST);

                JPanel userInfo = new JPanel();

                userInfo.setOpaque(false);

                userInfo.setLayout(
                                new BoxLayout(
                                                userInfo,
                                                BoxLayout.Y_AXIS));
                userInfo.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                JLabel name = new JLabel(username);

                name.setForeground(
                                Color.WHITE);

                name.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                JLabel status = new JLabel("Online");

                status.setForeground(
                                new Color(134, 239, 172));

                status.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                12));

                userInfo.add(name);

                userInfo.add(
                                Box.createVerticalStrut(2));

                userInfo.add(status);

                userCard.add(
                                userInfo,
                                BorderLayout.CENTER);

                sidebar.add(userCard);

                sidebar.add(
                                Box.createVerticalStrut(10));

                // =====================================================
                // MENU LABEL
                // =====================================================

                JLabel menuLabel = new JLabel("MAILBOX");
                menuLabel.setAlignmentX(
                                Component.LEFT_ALIGNMENT);
                menuLabel.setForeground(
                                new Color(100, 116, 139));

                menuLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                11));

                menuLabel.setBorder(
                                new EmptyBorder(
                                                0,
                                                8,
                                                10,
                                                0));

                sidebar.add(menuLabel);

                // =====================================================
                // MENU BUTTONS
                // =====================================================

                MailMenuButton inbox = new MailMenuButton(
                                "Inbox",
                                MailMenuButton.INBOX);

                MailMenuButton compose = new MailMenuButton(
                                "Compose",
                                MailMenuButton.COMPOSE);

                MailMenuButton sent = new MailMenuButton(
                                "Sent",
                                MailMenuButton.SENT);

                MailMenuButton trash = new MailMenuButton(
                                "Trash",
                                MailMenuButton.TRASH);

                inbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                compose.setAlignmentX(Component.LEFT_ALIGNMENT);
                sent.setAlignmentX(Component.LEFT_ALIGNMENT);
                trash.setAlignmentX(Component.LEFT_ALIGNMENT);

                sidebar.add(inbox);

                sidebar.add(
                                Box.createVerticalStrut(6));

                sidebar.add(compose);

                sidebar.add(
                                Box.createVerticalStrut(6));

                sidebar.add(sent);

                sidebar.add(
                                Box.createVerticalStrut(6));

                sidebar.add(trash);

                // =====================================================
                // SPACER
                // =====================================================

                sidebar.add(
                                Box.createVerticalGlue());

                // =====================================================
                // LOGOUT
                // =====================================================

                MailMenuButton logout = new MailMenuButton(
                                "Logout",
                                MailMenuButton.LOGOUT);

                logout.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                sidebar.add(logout);

                // =====================================================
                // EVENTS
                // =====================================================

                inbox.addActionListener(
                                e -> showInbox());

                compose.addActionListener(
                                e -> showCompose());

                sent.addActionListener(
                                e -> showSent());

                trash.addActionListener(
                                e -> showTrash());

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

                JPanel header = new JPanel(
                                new BorderLayout());

                header.setBackground(
                                WHITE);

                header.setBorder(
                                new EmptyBorder(
                                                20,
                                                30,
                                                20,
                                                30));

                pageTitle = new JLabel("Inbox");

                pageTitle.setForeground(
                                TEXT);

                pageTitle.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                25));

                header.add(
                                pageTitle,
                                BorderLayout.WEST);

                // Right side

                JPanel accountPanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                8,
                                                0));

                accountPanel.setOpaque(false);

                JLabel account = new JLabel(username);

                account.setForeground(
                                TEXT_SECONDARY);

                account.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                StatusDot dot = new StatusDot();

                accountPanel.add(account);
                accountPanel.add(dot);

                header.add(
                                accountPanel,
                                BorderLayout.EAST);

                return header;
        }

        // =========================================================
        // INBOX
        // =========================================================

        private void showInbox() {

                setPageTitle("Inbox");

                contentPanel.removeAll();

                JPanel card = createCard();

                // =====================================================
                // HEADER
                // =====================================================

                JPanel cardHeader = new JPanel(new BorderLayout());

                cardHeader.setOpaque(false);

                JLabel title = new JLabel("Inbox");

                title.setForeground(TEXT);

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                21));

                RoundedButton refresh = new RoundedButton(
                                "REFRESH",
                                new Color(239, 246, 255),
                                new Color(219, 234, 254));

                refresh.setForeground(PRIMARY);

                refresh.setPreferredSize(
                                new Dimension(100, 36));

                refresh.setMaximumSize(
                                new Dimension(100, 36));

                cardHeader.add(
                                title,
                                BorderLayout.WEST);

                cardHeader.add(
                                refresh,
                                BorderLayout.EAST);

                card.add(
                                cardHeader,
                                BorderLayout.NORTH);

                // =====================================================
                // EMAIL LIST
                // =====================================================

                JPanel emailList = new JPanel();

                emailList.setOpaque(false);

                emailList.setLayout(
                                new BoxLayout(
                                                emailList,
                                                BoxLayout.Y_AXIS));

                EmailDAO emailDAO = new EmailDAO();

                try {

                        /*
                         * Lấy email thật từ database.
                         */
                        List<Email> emails = emailDAO.getInbox(
                                        getUserEmail());

                        if (emails == null || emails.isEmpty()) {

                                JPanel empty = createEmptyPanel(
                                                "Your inbox is empty",
                                                "Received emails will appear here.");

                                emailList.add(empty);

                        } else {

                                for (Email email : emails) {

                                        JPanel emailItem = createEmailItem(email);

                                        emailList.add(emailItem);

                                        emailList.add(
                                                        Box.createVerticalStrut(8));
                                }
                        }

                } catch (Exception e) {

                        JLabel error = new JLabel(
                                        "Cannot load inbox: "
                                                        + e.getMessage());

                        error.setForeground(
                                        new Color(
                                                        220,
                                                        38,
                                                        38));

                        error.setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        Font.PLAIN,
                                                        14));

                        emailList.add(error);

                        e.printStackTrace();
                }

                JScrollPane scroll = new JScrollPane(
                                emailList);

                scroll.setBorder(null);

                scroll.setBackground(WHITE);

                scroll.getVerticalScrollBar()
                                .setUnitIncrement(16);

                card.add(
                                scroll,
                                BorderLayout.CENTER);

                // =====================================================
                // REFRESH
                // =====================================================

                refresh.addActionListener(
                                e -> showInbox());

                contentPanel.add(
                                card,
                                BorderLayout.CENTER);

                refreshUI();
        }

        private String getUserEmail() {

                /*
                 * Tạm thời ánh xạ username -> email.
                 *
                 * Sau này có thể lấy trực tiếp từ UserDAO.
                 */

                if (username.equalsIgnoreCase("thanh")) {
                        return "thanh@mail.local";
                }

                if (username.equalsIgnoreCase("admin")) {
                        return "admin@mail.local";
                }

                /*
                 * Nếu username đã là email
                 */
                if (username.contains("@")) {
                        return username;
                }

                return username + "@mail.local";
        }

        private JPanel createEmailItem(
                        Email email) {

                JPanel item = new JPanel(
                                new BorderLayout(
                                                15,
                                                0));

                item.setBackground(
                                Color.WHITE);

                item.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                BORDER),
                                                new EmptyBorder(
                                                                15,
                                                                15,
                                                                15,
                                                                15)));

                item.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                90));

                // =====================================================
                // AVATAR
                // =====================================================

                UserAvatar avatar = new UserAvatar();

                item.add(
                                avatar,
                                BorderLayout.WEST);

                // =====================================================
                // EMAIL INFORMATION
                // =====================================================

                JPanel information = new JPanel();

                information.setOpaque(false);

                information.setLayout(
                                new BoxLayout(
                                                information,
                                                BoxLayout.Y_AXIS));

                // Sender

                JLabel sender = new JLabel(
                                email.getSender());

                sender.setForeground(TEXT);

                sender.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                // Subject

                String subjectText = email.getSubject();

                if (subjectText == null ||
                                subjectText.trim().isEmpty()) {

                        subjectText = "(No subject)";
                }

                JLabel subject = new JLabel(
                                subjectText);

                subject.setForeground(
                                TEXT_SECONDARY);

                subject.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                // Content preview

                String content = email.getContent();

                if (content == null) {
                        content = "";
                }

                content = content.replace(
                                "\n",
                                " ");

                if (content.length() > 70) {

                        content = content.substring(
                                        0,
                                        70)
                                        + "...";
                }

                JLabel preview = new JLabel(
                                content);

                preview.setForeground(
                                new Color(
                                                148,
                                                163,
                                                184));

                preview.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                12));

                information.add(sender);

                information.add(
                                Box.createVerticalStrut(3));

                information.add(subject);

                information.add(
                                Box.createVerticalStrut(3));

                information.add(preview);

                item.add(
                                information,
                                BorderLayout.CENTER);

                // =====================================================
                // RIGHT SIDE
                // =====================================================

                JPanel right = new JPanel();

                right.setOpaque(false);

                right.setLayout(
                                new BoxLayout(
                                                right,
                                                BoxLayout.Y_AXIS));

                JLabel id = new JLabel(
                                "#" + email.getId());

                id.setForeground(
                                TEXT_SECONDARY);

                id.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                11));

                right.add(id);

                item.add(
                                right,
                                BorderLayout.EAST);

                // =====================================================
                // CLICK EMAIL
                // =====================================================

                item.setCursor(
                                new Cursor(
                                                Cursor.HAND_CURSOR));

                item.addMouseListener(
                                new MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        MouseEvent e) {

                                                item.setBackground(
                                                                new Color(
                                                                                248,
                                                                                250,
                                                                                252));
                                        }

                                        @Override
                                        public void mouseExited(
                                                        MouseEvent e) {

                                                item.setBackground(
                                                                Color.WHITE);
                                        }

                                        @Override
                                        public void mouseClicked(
                                                        MouseEvent e) {

                                                showEmailDetail(email);
                                        }
                                });

                return item;
        }

        private void showEmailDetail(Email email) {

                contentPanel.removeAll();

                JPanel main = new JPanel(new BorderLayout(0, 20));
                main.setBackground(BACKGROUND);
                main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // =========================
                // HEADER
                // =========================

                JPanel header = new JPanel(new BorderLayout());
                header.setOpaque(false);

                RoundedButton back = new RoundedButton("BACK", new Color(226, 232, 240),
                                new Color(15, 23, 42));

                back.addActionListener(e -> showInbox());

                header.add(back, BorderLayout.WEST);

                JLabel title = new JLabel(
                                email.getSubject() == null || email.getSubject().isEmpty()
                                                ? "(No subject)"
                                                : email.getSubject());

                title.setFont(new Font("Segoe UI", Font.BOLD, 24));
                title.setForeground(TEXT);

                header.add(title, BorderLayout.CENTER);

                // =========================
                // EMAIL CONTENT
                // =========================

                JPanel mailPanel = new JPanel();
                mailPanel.setLayout(new BoxLayout(mailPanel, BoxLayout.Y_AXIS));
                mailPanel.setBackground(Color.WHITE);
                mailPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                                new Color(226, 232, 240)),
                                BorderFactory.createEmptyBorder(
                                                25, 25, 25, 25)));

                JLabel fromLabel = new JLabel(
                                "From: " + email.getSender());

                fromLabel.setFont(
                                new Font("Segoe UI", Font.BOLD, 14));

                fromLabel.setForeground(
                                new Color(71, 85, 105));

                JLabel toLabel = new JLabel(
                                "To: " + email.getReceiver());

                toLabel.setFont(
                                new Font("Segoe UI", Font.PLAIN, 13));

                toLabel.setForeground(
                                new Color(100, 116, 139));

                JTextArea messageArea = new JTextArea(
                                email.getContent() == null
                                                ? ""
                                                : email.getContent());

                messageArea.setFont(
                                new Font("Segoe UI", Font.PLAIN, 14));

                messageArea.setForeground(
                                new Color(15, 23, 42));

                messageArea.setBackground(Color.WHITE);
                messageArea.setLineWrap(true);
                messageArea.setWrapStyleWord(true);
                messageArea.setEditable(false);
                messageArea.setBorder(
                                BorderFactory.createEmptyBorder(
                                                20, 0, 20, 0));

                mailPanel.add(fromLabel);
                mailPanel.add(Box.createVerticalStrut(8));

                mailPanel.add(toLabel);
                mailPanel.add(Box.createVerticalStrut(15));

                mailPanel.add(messageArea);

                // =========================
                // DELETE BUTTON
                // =========================

                JPanel bottom = new JPanel(
                                new FlowLayout(FlowLayout.RIGHT));

                bottom.setOpaque(false);

                RoundedButton delete = new RoundedButton(
                                "DELETE",
                                new Color(220, 38, 38),
                                Color.WHITE);

                delete.addActionListener(e -> {

                        int result = JOptionPane.showConfirmDialog(
                                        this,
                                        "Delete this email?",
                                        "Confirm Delete",
                                        JOptionPane.YES_NO_OPTION);

                        if (result != JOptionPane.YES_OPTION) {
                                return;
                        }

                        try {

                                boolean success = emailDAO.deleteMail(
                                                email.getId(),
                                                getUserEmail());

                                if (success) {

                                        JOptionPane.showMessageDialog(
                                                        this,
                                                        "Email moved to Trash.");

                                        showInbox();

                                } else {

                                        JOptionPane.showMessageDialog(
                                                        this,
                                                        "Cannot delete email.",
                                                        "Error",
                                                        JOptionPane.ERROR_MESSAGE);
                                }

                        } catch (Exception ex) {

                                ex.printStackTrace();

                                JOptionPane.showMessageDialog(
                                                this,
                                                "Error: " + ex.getMessage(),
                                                "Database Error",
                                                JOptionPane.ERROR_MESSAGE);
                        }
                });

                bottom.add(delete);

                main.add(header, BorderLayout.NORTH);
                main.add(mailPanel, BorderLayout.CENTER);
                main.add(bottom, BorderLayout.SOUTH);

                contentPanel.add(main, BorderLayout.CENTER);

                contentPanel.revalidate();
                contentPanel.repaint();
        }

        // =========================================================
        // COMPOSE
        // =========================================================

        private void showCompose() {

                setPageTitle("Compose");

                contentPanel.removeAll();

                JPanel card = createCard();

                card.setLayout(
                                new BorderLayout(
                                                0,
                                                20));

                // Header

                JLabel title = new JLabel("New Message");

                title.setForeground(TEXT);

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                21));

                card.add(
                                title,
                                BorderLayout.NORTH);

                // Form

                JPanel form = new JPanel();

                form.setOpaque(false);

                form.setLayout(
                                new BoxLayout(
                                                form,
                                                BoxLayout.Y_AXIS));

                // Receiver
                JLabel receiverLabel = createLabel("To");
                receiverLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextField receiver = createTextField();
                receiver.setAlignmentX(Component.LEFT_ALIGNMENT);
                receiver.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                form.add(receiverLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(receiver);
                form.add(Box.createVerticalStrut(15));

                // Subject
                JLabel subjectLabel = createLabel("Subject");
                subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextField subject = createTextField();
                subject.setAlignmentX(Component.LEFT_ALIGNMENT);
                subject.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                form.add(subjectLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(subject);
                form.add(Box.createVerticalStrut(15));

                // Message
                JLabel messageLabel = createLabel("Message");
                messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JTextArea message = new JTextArea();

                message.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                message.setForeground(TEXT);
                message.setLineWrap(true);
                message.setWrapStyleWord(true);

                message.setBorder(
                                new EmptyBorder(
                                                12,
                                                12,
                                                12,
                                                12));

                JScrollPane messageScroll = new JScrollPane(message);

                messageScroll.setBorder(
                                BorderFactory.createLineBorder(BORDER));

                messageScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                messageScroll.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                250));

                form.add(messageLabel);
                form.add(Box.createVerticalStrut(7));
                form.add(messageScroll);
                form.add(Box.createVerticalStrut(18));

                // Send button

                RoundedButton send = new RoundedButton(
                                "SEND MAIL",
                                PRIMARY,
                                PRIMARY_HOVER);

                send.setPreferredSize(
                                new Dimension(
                                                150,
                                                44));

                send.setMaximumSize(
                                new Dimension(
                                                150,
                                                44));

                send.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                form.add(send);

                // Temporary action

                send.addActionListener(e -> {

                        String to = receiver.getText().trim();

                        String sub = subject.getText().trim();

                        String msg = message.getText().trim();

                        // =====================================================
                        // VALIDATE
                        // =====================================================

                        if (to.isEmpty()) {

                                JOptionPane.showMessageDialog(
                                                this,
                                                "Please enter receiver email.",
                                                "Missing receiver",
                                                JOptionPane.WARNING_MESSAGE);

                                receiver.requestFocus();

                                return;
                        }

                        if (sub.isEmpty()) {

                                JOptionPane.showMessageDialog(
                                                this,
                                                "Please enter subject.",
                                                "Missing subject",
                                                JOptionPane.WARNING_MESSAGE);

                                subject.requestFocus();

                                return;
                        }

                        if (msg.isEmpty()) {

                                JOptionPane.showMessageDialog(
                                                this,
                                                "Please enter message.",
                                                "Missing message",
                                                JOptionPane.WARNING_MESSAGE);

                                message.requestFocus();

                                return;
                        }

                        // =====================================================
                        // DISABLE BUTTON
                        // =====================================================

                        send.setEnabled(false);

                        send.setText("SENDING...");

                        // =====================================================
                        // SEND SMTP IN BACKGROUND
                        // =====================================================

                        new Thread(() -> {

                                boolean success = sendSMTPMail(
                                                to,
                                                sub,
                                                msg);

                                SwingUtilities.invokeLater(() -> {

                                        send.setEnabled(true);

                                        send.setText("SEND MAIL");

                                        if (success) {

                                                JOptionPane.showMessageDialog(
                                                                this,
                                                                "Email sent successfully!",
                                                                "SMTP",
                                                                JOptionPane.INFORMATION_MESSAGE);

                                                // Clear form

                                                receiver.setText("");

                                                subject.setText("");

                                                message.setText("");

                                        } else {

                                                JOptionPane.showMessageDialog(
                                                                this,
                                                                "Cannot send email.\n"
                                                                                + "Please check whether SMTP Server is running.",
                                                                "SMTP Error",
                                                                JOptionPane.ERROR_MESSAGE);
                                        }
                                });

                        }).start();
                });

                card.add(
                                form,
                                BorderLayout.CENTER);

                contentPanel.add(
                                card,
                                BorderLayout.CENTER);

                refreshUI();
        }

        private boolean sendSMTPMail(
                        String receiver,
                        String subject,
                        String content) {

                final String SMTP_HOST = "localhost";

                final int SMTP_PORT = 2526;

                String sender = getUserEmail();

                try (
                                Socket socket = new Socket(
                                                SMTP_HOST,
                                                SMTP_PORT);

                                BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(
                                                                socket.getInputStream()));

                                PrintWriter writer = new PrintWriter(
                                                socket.getOutputStream(),
                                                true)) {

                        // =================================================
                        // SERVER GREETING
                        // =================================================

                        String response = reader.readLine();

                        System.out.println(
                                        "SMTP << " + response);

                        if (response == null ||
                                        !response.startsWith("220")) {

                                return false;
                        }

                        // =================================================
                        // HELO
                        // =================================================

                        writer.println(
                                        "HELO localhost");

                        System.out.println(
                                        "SMTP >> HELO localhost");

                        response = reader.readLine();

                        System.out.println(
                                        "SMTP << " + response);

                        if (!response.startsWith("250")) {

                                return false;
                        }

                        // =================================================
                        // MAIL FROM
                        // =================================================

                        writer.println(
                                        "MAIL FROM: "
                                                        + sender);

                        System.out.println(
                                        "SMTP >> MAIL FROM: "
                                                        + sender);

                        response = reader.readLine();

                        System.out.println(
                                        "SMTP << " + response);

                        if (!response.startsWith("250")) {

                                return false;
                        }

                        // =================================================
                        // RCPT TO
                        // =================================================

                        writer.println(
                                        "RCPT TO: "
                                                        + receiver);

                        System.out.println(
                                        "SMTP >> RCPT TO: "
                                                        + receiver);

                        response = reader.readLine();

                        System.out.println(
                                        "SMTP << " + response);

                        if (!response.startsWith("250")) {

                                return false;
                        }

                        // =================================================
                        // DATA
                        // =================================================

                        writer.println("DATA");

                        System.out.println(
                                        "SMTP >> DATA");

                        response = reader.readLine();

                        System.out.println(
                                        "SMTP << " + response);

                        if (!response.startsWith("354")) {

                                return false;
                        }

                        // =================================================
                        // SUBJECT
                        // =================================================

                        writer.println(
                                        "Subject: " + subject);

                        // =================================================
                        // CONTENT
                        // =================================================

                        String[] lines = content.split(
                                        "\\R");

                        for (String line : lines) {

                                writer.println(line);
                        }

                        // =================================================
                        // END DATA
                        // =================================================

                        writer.println("<END>");

                        response = reader.readLine();

                        System.out.println(
                                        "SMTP << " + response);

                        if (!response.startsWith("250")) {

                                return false;
                        }

                        // =================================================
                        // QUIT
                        // =================================================

                        writer.println("QUIT");

                        System.out.println(
                                        "SMTP >> QUIT");

                        response = reader.readLine();

                        System.out.println(
                                        "SMTP << " + response);

                        return response != null &&
                                        response.startsWith("221");

                } catch (Exception e) {

                        System.out.println(
                                        "SMTP connection error: "
                                                        + e.getMessage());

                        e.printStackTrace();

                        return false;
                }
        }
        // =========================================================
        // SENT
        // =========================================================

        private void showSent() {

                setPageTitle("Sent");

                contentPanel.removeAll();

                JPanel card = createCard();

                // =====================================================
                // HEADER
                // =====================================================

                JPanel cardHeader = new JPanel(new BorderLayout());

                cardHeader.setOpaque(false);

                JLabel title = new JLabel("Sent Mail");

                title.setForeground(TEXT);

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                21));

                RoundedButton refresh = new RoundedButton(
                                "REFRESH",
                                new Color(239, 246, 255),
                                new Color(219, 234, 254));

                refresh.setForeground(PRIMARY);

                refresh.setPreferredSize(
                                new Dimension(100, 36));

                refresh.setMaximumSize(
                                new Dimension(100, 36));

                cardHeader.add(
                                title,
                                BorderLayout.WEST);

                cardHeader.add(
                                refresh,
                                BorderLayout.EAST);

                card.add(
                                cardHeader,
                                BorderLayout.NORTH);

                // =====================================================
                // EMAIL LIST
                // =====================================================

                JPanel emailList = new JPanel();

                emailList.setOpaque(false);

                emailList.setLayout(
                                new BoxLayout(
                                                emailList,
                                                BoxLayout.Y_AXIS));

                EmailDAO emailDAO = new EmailDAO();

                try {

                        List<Email> emails = emailDAO.getSentMails(
                                        getUserEmail());

                        if (emails == null ||
                                        emails.isEmpty()) {

                                JPanel empty = createEmptyPanel(
                                                "No sent emails",
                                                "Emails you send will appear here.");

                                emailList.add(empty);

                        } else {

                                for (Email email : emails) {

                                        JPanel emailItem = createSentEmailItem(email);

                                        emailList.add(emailItem);

                                        emailList.add(
                                                        Box.createVerticalStrut(8));
                                }
                        }

                } catch (Exception e) {

                        JLabel error = new JLabel(
                                        "Cannot load sent emails: "
                                                        + e.getMessage());

                        error.setForeground(
                                        new Color(
                                                        220,
                                                        38,
                                                        38));

                        error.setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        Font.PLAIN,
                                                        14));

                        emailList.add(error);

                        e.printStackTrace();
                }

                JScrollPane scroll = new JScrollPane(
                                emailList);

                scroll.setBorder(null);

                scroll.setBackground(WHITE);

                scroll.getVerticalScrollBar()
                                .setUnitIncrement(16);

                card.add(
                                scroll,
                                BorderLayout.CENTER);

                // =====================================================
                // REFRESH
                // =====================================================

                refresh.addActionListener(
                                e -> showSent());

                contentPanel.add(
                                card,
                                BorderLayout.CENTER);

                refreshUI();
        }

        private JPanel createSentEmailItem(Email email) {
                JPanel panel = new JPanel(new BorderLayout(20, 0));
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                                BorderFactory.createEmptyBorder(15, 18, 15, 18)));
                panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

                // =========================
                // LEFT - MAIL INFO
                // =========================
                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setOpaque(false);

                JLabel receiverLabel = new JLabel(
                                "To: " + email.getReceiver());
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

                // =========================
                // RIGHT - EMAIL ID
                // =========================
                JLabel idLabel = new JLabel("#" + email.getId());
                idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                idLabel.setForeground(new Color(148, 163, 184));

                panel.add(info, BorderLayout.CENTER);
                panel.add(idLabel, BorderLayout.EAST);

                // =========================
                // CLICK TO VIEW
                // =========================
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

        private void showSentEmailDetail(
                        Email email) {

                setPageTitle("Sent Mail");

                contentPanel.removeAll();

                JPanel card = createCard();

                // =====================================================
                // SUBJECT
                // =====================================================

                String subjectText = email.getSubject();

                if (subjectText == null ||
                                subjectText.trim().isEmpty()) {

                        subjectText = "(No subject)";
                }

                JLabel subject = new JLabel(
                                subjectText);

                subject.setForeground(TEXT);

                subject.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                22));

                card.add(
                                subject,
                                BorderLayout.NORTH);

                // =====================================================
                // BODY
                // =====================================================

                JPanel body = new JPanel();

                body.setOpaque(false);

                body.setLayout(
                                new BoxLayout(
                                                body,
                                                BoxLayout.Y_AXIS));

                JLabel from = new JLabel(
                                "From: "
                                                + email.getSender());

                from.setForeground(
                                TEXT_SECONDARY);

                from.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                JLabel to = new JLabel(
                                "To: "
                                                + email.getReceiver());

                to.setForeground(
                                TEXT_SECONDARY);

                to.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                body.add(from);

                body.add(
                                Box.createVerticalStrut(5));

                body.add(to);

                body.add(
                                Box.createVerticalStrut(20));

                JTextArea content = new JTextArea(
                                email.getContent());

                content.setEditable(false);

                content.setLineWrap(true);

                content.setWrapStyleWord(true);

                content.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                15));

                content.setForeground(TEXT);

                content.setBackground(WHITE);

                content.setBorder(
                                new EmptyBorder(
                                                5,
                                                0,
                                                5,
                                                0));

                body.add(content);

                card.add(
                                body,
                                BorderLayout.CENTER);

                // =====================================================
                // BACK
                // =====================================================

                RoundedButton back = new RoundedButton(
                                "BACK TO SENT",
                                PRIMARY,
                                PRIMARY_HOVER);

                back.setPreferredSize(
                                new Dimension(
                                                150,
                                                40));

                back.addActionListener(
                                e -> showSent());

                card.add(
                                back,
                                BorderLayout.SOUTH);

                contentPanel.add(
                                card,
                                BorderLayout.CENTER);

                refreshUI();
        }

        // =========================================================
        // TRASH
        // =========================================================

        private void showTrash() {

                contentPanel.removeAll();

                // =========================
                // HEADER
                // =========================

                JPanel header = new JPanel(new BorderLayout());
                header.setOpaque(false);

                JLabel title = new JLabel("Trash");
                title.setFont(
                                new Font("Segoe UI", Font.BOLD, 28));
                title.setForeground(TEXT);

                RoundedButton refresh = new RoundedButton(
                                "REFRESH",
                                PRIMARY,
                                Color.WHITE);

                refresh.addActionListener(e -> showTrash());

                header.add(title, BorderLayout.WEST);
                header.add(refresh, BorderLayout.EAST);

                // =========================
                // EMAIL LIST
                // =========================

                JPanel emailList = new JPanel();

                emailList.setLayout(
                                new BoxLayout(
                                                emailList,
                                                BoxLayout.Y_AXIS));

                emailList.setBackground(BACKGROUND);

                try {

                        List<Email> emails = emailDAO.getTrash(getUserEmail());

                        if (emails.isEmpty()) {

                                JPanel emptyPanel = new JPanel();

                                emptyPanel.setLayout(
                                                new BoxLayout(
                                                                emptyPanel,
                                                                BoxLayout.Y_AXIS));

                                emptyPanel.setOpaque(false);

                                emptyPanel.add(
                                                Box.createVerticalStrut(40));

                                JLabel emptyIcon = new JLabel("Trash is empty");

                                emptyIcon.setFont(
                                                new Font(
                                                                "Segoe UI",
                                                                Font.BOLD,
                                                                20));

                                emptyIcon.setForeground(new Color(100, 116, 139));

                                emptyIcon.setAlignmentX(
                                                Component.CENTER_ALIGNMENT);

                                emptyPanel.add(emptyIcon);

                                emailList.add(emptyPanel);

                        } else {

                                for (Email email : emails) {

                                        JPanel item = createTrashEmailItem(email);

                                        emailList.add(item);

                                        emailList.add(
                                                        Box.createVerticalStrut(10));
                                }
                        }

                } catch (Exception e) {

                        e.printStackTrace();

                        JLabel error = new JLabel(
                                        "Cannot load Trash: "
                                                        + e.getMessage());

                        error.setForeground(Color.RED);

                        emailList.add(error);
                }

                // =========================
                // SCROLL
                // =========================

                JScrollPane scrollPane = new JScrollPane(emailList);

                scrollPane.setBorder(null);

                scrollPane
                                .getVerticalScrollBar()
                                .setUnitIncrement(16);

                // =========================
                // ADD TO MAIN CONTENT
                // =========================

                contentPanel.add(
                                header,
                                BorderLayout.NORTH);

                contentPanel.add(
                                scrollPane,
                                BorderLayout.CENTER);

                contentPanel.revalidate();
                contentPanel.repaint();
        }

        private JPanel createTrashEmailItem(Email email) {

                JPanel panel = new JPanel(
                                new BorderLayout(15, 0));

                panel.setBackground(Color.WHITE);

                panel.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(226, 232, 240)),
                                                BorderFactory.createEmptyBorder(
                                                                15, 18, 15, 18)));

                panel.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                95));

                // =========================
                // EMAIL INFO
                // =========================

                JPanel info = new JPanel();

                info.setLayout(
                                new BoxLayout(
                                                info,
                                                BoxLayout.Y_AXIS));

                info.setOpaque(false);

                JLabel sender = new JLabel(
                                "From: " + email.getSender());

                sender.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                sender.setForeground(
                                new Color(15, 23, 42));

                JLabel subject = new JLabel(
                                email.getSubject() == null
                                                || email.getSubject().isEmpty()
                                                                ? "(No subject)"
                                                                : email.getSubject());

                subject.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                subject.setForeground(
                                new Color(100, 116, 139));

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
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                12));

                content.setForeground(
                                new Color(148, 163, 184));

                info.add(sender);

                info.add(
                                Box.createVerticalStrut(4));

                info.add(subject);

                info.add(
                                Box.createVerticalStrut(4));

                info.add(content);

                // =========================
                // RESTORE BUTTON
                // =========================

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

                                        JOptionPane.showMessageDialog(
                                                        this,
                                                        "Email restored.");

                                        showTrash();

                                } else {

                                        JOptionPane.showMessageDialog(
                                                        this,
                                                        "Cannot restore email.",
                                                        "Error",
                                                        JOptionPane.ERROR_MESSAGE);
                                }

                        } catch (Exception ex) {

                                ex.printStackTrace();

                                JOptionPane.showMessageDialog(
                                                this,
                                                "Error: " + ex.getMessage(),
                                                "Database Error",
                                                JOptionPane.ERROR_MESSAGE);
                        }
                });

                // =========================
                // ADD COMPONENTS
                // =========================

                panel.add(
                                info,
                                BorderLayout.CENTER);

                panel.add(
                                restore,
                                BorderLayout.EAST);

                return panel;
        }
        // =========================================================
        // EMPTY PANEL
        // =========================================================

        private JPanel createEmptyPanel(
                        String titleText,
                        String description) {

                JPanel panel = new JPanel();

                panel.setOpaque(false);

                panel.setLayout(
                                new BoxLayout(
                                                panel,
                                                BoxLayout.Y_AXIS));

                MailLargeIcon icon = new MailLargeIcon();

                icon.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                JLabel title = new JLabel(titleText);

                title.setForeground(TEXT);

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                19));

                title.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                JLabel text = new JLabel(description);

                text.setForeground(
                                TEXT_SECONDARY);

                text.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                text.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                panel.add(
                                Box.createVerticalGlue());

                panel.add(icon);

                panel.add(
                                Box.createVerticalStrut(15));

                panel.add(title);

                panel.add(
                                Box.createVerticalStrut(7));

                panel.add(text);

                panel.add(
                                Box.createVerticalGlue());

                return panel;
        }

        // =========================================================
        // CARD
        // =========================================================

        private JPanel createCard() {

                JPanel card = new JPanel(
                                new BorderLayout(
                                                0,
                                                15));

                card.setBackground(
                                WHITE);

                card.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                BORDER),
                                                new EmptyBorder(
                                                                25,
                                                                25,
                                                                25,
                                                                25)));

                return card;
        }

        // =========================================================
        // LABEL
        // =========================================================

        private JLabel createLabel(
                        String text) {

                JLabel label = new JLabel(text);

                label.setForeground(TEXT);

                label.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                return label;
        }

        // =========================================================
        // TEXT FIELD
        // =========================================================

        private JTextField createTextField() {

                JTextField field = new JTextField();

                field.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                field.setForeground(TEXT);

                field.setBackground(
                                new Color(248, 250, 252));

                field.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                BORDER),
                                                new EmptyBorder(
                                                                9,
                                                                12,
                                                                9,
                                                                12)));

                field.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                42));

                return field;
        }

        // =========================================================
        // SMALL BUTTON
        // =========================================================

        private JButton createSmallButton(
                        String text) {

                JButton button = new JButton(text);

                button.setFocusPainted(false);

                button.setBorderPainted(false);

                button.setBackground(
                                new Color(239, 246, 255));

                button.setForeground(
                                PRIMARY);

                button.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                12));

                button.setCursor(
                                new Cursor(
                                                Cursor.HAND_CURSOR));

                return button;
        }

        // =========================================================
        // PAGE TITLE
        // =========================================================

        private void setPageTitle(
                        String title) {

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

                        new MailClientGUI()
                                        .setVisible(true);

                        dispose();
                }
        }

        // =========================================================
        // ROUNDED BUTTON
        // =========================================================

        static class RoundedButton
                        extends JButton {

                private final Color normalColor;

                private final Color hoverColor;

                public RoundedButton(
                                String text,
                                Color normalColor,
                                Color hoverColor) {

                        super(text);

                        this.normalColor = normalColor;

                        this.hoverColor = hoverColor;

                        setForeground(
                                        Color.WHITE);

                        setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        Font.BOLD,
                                                        13));

                        setFocusPainted(false);

                        setBorderPainted(false);

                        setContentAreaFilled(false);

                        setOpaque(false);

                        setBackground(
                                        normalColor);

                        setCursor(
                                        new Cursor(
                                                        Cursor.HAND_CURSOR));

                        addMouseListener(
                                        new MouseAdapter() {

                                                @Override
                                                public void mouseEntered(
                                                                MouseEvent e) {

                                                        setBackground(
                                                                        hoverColor);

                                                        repaint();
                                                }

                                                @Override
                                                public void mouseExited(
                                                                MouseEvent e) {

                                                        setBackground(
                                                                        normalColor);

                                                        repaint();
                                                }
                                        });
                }

                @Override
                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(
                                        getBackground());

                        g2.fillRoundRect(
                                        0,
                                        0,
                                        getWidth(),
                                        getHeight(),
                                        14,
                                        14);

                        g2.setColor(
                                        getForeground());

                        FontMetrics fm = g2.getFontMetrics();

                        int x = (getWidth()
                                        - fm.stringWidth(
                                                        getText()))
                                        / 2;

                        int y = (getHeight()
                                        - fm.getHeight()) / 2
                                        + fm.getAscent();

                        g2.drawString(
                                        getText(),
                                        x,
                                        y);

                        g2.dispose();
                }
        }

        // =========================================================
        // SIDEBAR MENU BUTTON
        // =========================================================

        static class MailMenuButton
                        extends JButton {

                public static final int INBOX = 1;

                public static final int COMPOSE = 2;

                public static final int SENT = 3;

                public static final int TRASH = 4;

                public static final int LOGOUT = 5;

                private final int iconType;

                private boolean hovered = false;

                public MailMenuButton(
                                String text,
                                int iconType) {

                        super(text);

                        this.iconType = iconType;

                        setForeground(
                                        new Color(
                                                        226,
                                                        232,
                                                        240));

                        setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        Font.BOLD,
                                                        14));

                        setHorizontalAlignment(
                                        SwingConstants.LEFT);

                        setBorderPainted(false);

                        setFocusPainted(false);

                        setContentAreaFilled(false);

                        setOpaque(false);

                        setPreferredSize(
                                        new Dimension(
                                                        205,
                                                        44));

                        setMaximumSize(
                                        new Dimension(
                                                        Integer.MAX_VALUE,
                                                        44));

                        setCursor(
                                        new Cursor(
                                                        Cursor.HAND_CURSOR));

                        addMouseListener(
                                        new MouseAdapter() {

                                                @Override
                                                public void mouseEntered(
                                                                MouseEvent e) {

                                                        hovered = true;

                                                        repaint();
                                                }

                                                @Override
                                                public void mouseExited(
                                                                MouseEvent e) {

                                                        hovered = false;

                                                        repaint();
                                                }
                                        });
                }

                @Override
                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        // Background

                        if (hovered) {

                                g2.setColor(
                                                SIDEBAR_HOVER);

                                g2.fillRoundRect(
                                                0,
                                                0,
                                                getWidth(),
                                                getHeight(),
                                                10,
                                                10);
                        }

                        // Icon

                        drawIcon(
                                        g2,
                                        iconType,
                                        17,
                                        getHeight() / 2);

                        // Text

                        g2.setColor(
                                        getForeground());

                        g2.setFont(
                                        getFont());

                        FontMetrics fm = g2.getFontMetrics();

                        int textX = 47;

                        int textY = (getHeight()
                                        - fm.getHeight()) / 2
                                        + fm.getAscent();

                        g2.drawString(
                                        getText(),
                                        textX,
                                        textY);

                        g2.dispose();
                }

                // =====================================================
                // DRAW ICON
                // =====================================================

                private void drawIcon(
                                Graphics2D g2,
                                int type,
                                int x,
                                int y) {

                        Color iconColor = new Color(
                                        203,
                                        213,
                                        225);

                        g2.setColor(
                                        iconColor);

                        /*
                         * Dùng java.awt.BasicStroke đầy đủ
                         * để tránh lỗi import / conflict.
                         */

                        g2.setStroke(
                                        new java.awt.BasicStroke(
                                                        2.0f,
                                                        java.awt.BasicStroke.CAP_ROUND,
                                                        java.awt.BasicStroke.JOIN_ROUND));

                        // =================================================
                        // INBOX ICON
                        // =================================================

                        if (type == INBOX) {

                                g2.drawRoundRect(
                                                x,
                                                y - 7,
                                                18,
                                                14,
                                                3,
                                                3);

                                g2.drawLine(
                                                x,
                                                y - 6,
                                                x + 9,
                                                y + 1);

                                g2.drawLine(
                                                x + 18,
                                                y - 6,
                                                x + 9,
                                                y + 1);
                        }

                        // =================================================
                        // COMPOSE ICON
                        // =================================================

                        else if (type == COMPOSE) {

                                g2.drawLine(
                                                x + 3,
                                                y + 7,
                                                x + 16,
                                                y - 6);

                                g2.drawLine(
                                                x + 2,
                                                y + 9,
                                                x + 7,
                                                y + 8);

                                g2.drawLine(
                                                x + 2,
                                                y + 9,
                                                x + 3,
                                                y + 4);

                                g2.drawLine(
                                                x + 13,
                                                y - 9,
                                                x + 17,
                                                y - 5);
                        }

                        // =================================================
                        // SENT ICON
                        // =================================================

                        else if (type == SENT) {

                                g2.drawLine(
                                                x + 2,
                                                y,
                                                x + 17,
                                                y - 7);

                                g2.drawLine(
                                                x + 17,
                                                y - 7,
                                                x + 13,
                                                y + 8);

                                g2.drawLine(
                                                x + 13,
                                                y + 8,
                                                x + 9,
                                                y + 3);

                                g2.drawLine(
                                                x + 9,
                                                y + 3,
                                                x + 2,
                                                y);
                        }

                        // =================================================
                        // TRASH ICON
                        // =================================================

                        else if (type == TRASH) {

                                g2.drawRoundRect(
                                                x + 3,
                                                y - 5,
                                                13,
                                                15,
                                                2,
                                                2);

                                g2.drawLine(
                                                x + 1,
                                                y - 8,
                                                x + 18,
                                                y - 8);

                                g2.drawLine(
                                                x + 7,
                                                y - 11,
                                                x + 13,
                                                y - 11);

                                g2.drawLine(
                                                x + 7,
                                                y - 2,
                                                x + 7,
                                                y + 6);

                                g2.drawLine(
                                                x + 12,
                                                y - 2,
                                                x + 12,
                                                y + 6);
                        }

                        // =================================================
                        // LOGOUT ICON
                        // =================================================

                        else if (type == LOGOUT) {

                                g2.drawRoundRect(
                                                x + 1,
                                                y - 9,
                                                10,
                                                18,
                                                2,
                                                2);

                                g2.drawLine(
                                                x + 8,
                                                y,
                                                x + 18,
                                                y);

                                g2.drawLine(
                                                x + 14,
                                                y - 4,
                                                x + 18,
                                                y);

                                g2.drawLine(
                                                x + 14,
                                                y + 4,
                                                x + 18,
                                                y);
                        }
                }
        }

        // =========================================================
        // MAIL LOGO
        // =========================================================

        static class MailLogo
                        extends JComponent {

                public MailLogo() {

                        setPreferredSize(
                                        new Dimension(
                                                        35,
                                                        35));
                }

                @Override
                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(
                                        new Color(
                                                        59,
                                                        130,
                                                        246));

                        g2.fillRoundRect(
                                        2,
                                        5,
                                        30,
                                        23,
                                        6,
                                        6);

                        g2.setColor(
                                        Color.WHITE);

                        g2.setStroke(
                                        new java.awt.BasicStroke(
                                                        2.2f,
                                                        java.awt.BasicStroke.CAP_ROUND,
                                                        java.awt.BasicStroke.JOIN_ROUND));

                        g2.drawLine(
                                        5,
                                        8,
                                        17,
                                        18);

                        g2.drawLine(
                                        17,
                                        18,
                                        29,
                                        8);

                        g2.dispose();
                }
        }

        // =========================================================
        // USER AVATAR
        // =========================================================

        static class UserAvatar
                        extends JComponent {

                public UserAvatar() {

                        setPreferredSize(
                                        new Dimension(
                                                        38,
                                                        38));
                }

                @Override
                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(
                                        new Color(
                                                        59,
                                                        130,
                                                        246));

                        g2.fillOval(
                                        1,
                                        1,
                                        36,
                                        36);

                        g2.setColor(
                                        Color.WHITE);

                        // Head

                        g2.fillOval(
                                        13,
                                        7,
                                        12,
                                        12);

                        // Body

                        g2.fillRoundRect(
                                        9,
                                        20,
                                        20,
                                        12,
                                        8,
                                        8);

                        g2.dispose();
                }
        }

        // =========================================================
        // STATUS DOT
        // =========================================================

        static class StatusDot
                        extends JComponent {

                public StatusDot() {

                        setPreferredSize(
                                        new Dimension(
                                                        10,
                                                        10));
                }

                @Override
                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(
                                        new Color(
                                                        34,
                                                        197,
                                                        94));

                        g2.fillOval(
                                        1,
                                        1,
                                        8,
                                        8);

                        g2.dispose();
                }
        }

        // =========================================================
        // LARGE MAIL ICON
        // =========================================================

        static class MailLargeIcon
                        extends JComponent {

                public MailLargeIcon() {

                        setPreferredSize(
                                        new Dimension(
                                                        80,
                                                        70));

                        setMaximumSize(
                                        new Dimension(
                                                        80,
                                                        70));
                }

                @Override
                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        int x = 10;
                        int y = 12;

                        // Outer box

                        g2.setColor(
                                        new Color(
                                                        219,
                                                        234,
                                                        254));

                        g2.fillRoundRect(
                                        x,
                                        y,
                                        60,
                                        45,
                                        10,
                                        10);

                        // Envelope

                        g2.setColor(
                                        PRIMARY);

                        g2.setStroke(
                                        new java.awt.BasicStroke(
                                                        2.5f,
                                                        java.awt.BasicStroke.CAP_ROUND,
                                                        java.awt.BasicStroke.JOIN_ROUND));

                        g2.drawRoundRect(
                                        x + 8,
                                        y + 10,
                                        44,
                                        28,
                                        5,
                                        5);

                        g2.drawLine(
                                        x + 8,
                                        y + 11,
                                        x + 30,
                                        y + 27);

                        g2.drawLine(
                                        x + 52,
                                        y + 11,
                                        x + 30,
                                        y + 27);

                        g2.dispose();
                }
        }

        // =========================================================
        // MAIN
        // =========================================================

        public static void main(String[] args) {

                SwingUtilities.invokeLater(() -> {

                        MainMailGUI gui = new MainMailGUI("thanh");

                        gui.setVisible(true);
                });
        }
}