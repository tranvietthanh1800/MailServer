package com.mailserver.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ServerGUI extends JFrame {

        // =========================================================
        // DATABASE
        // =========================================================

        private static final String DB_URL = "jdbc:mysql://localhost:3306/mail_server";

        private static final String DB_USER = "root";

        /*
         * ĐỔI PASSWORD NÀY thành password MySQL của bạn.
         *
         * Nếu MySQL của bạn không có password thì để:
         * ""
         */
        private static final String DB_PASSWORD = "";

        // =========================================================
        // COLORS
        // =========================================================

        private final Color DARK = new Color(15, 23, 42);

        private final Color BLUE = new Color(37, 99, 235);

        private final Color GREEN = new Color(22, 163, 74);

        private final Color RED = new Color(220, 38, 38);

        private final Color BG = new Color(241, 245, 249);

        private final Color WHITE = Color.WHITE;

        // =========================================================
        // COMPONENTS
        // =========================================================

        private JTextArea logArea;

        private JCheckBox autoRefreshCheckBox;

        private Timer logTimer;

        private JLabel accountCountLabel;
        private JLabel emailCountLabel;
        private JLabel serverStatusLabel;

        private JTable accountTable;
        private JTable emailTable;

        private DefaultTableModel accountModel;
        private DefaultTableModel emailModel;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public ServerGUI() {

                setTitle("Mail Server - Administration");

                setSize(1200, 750);

                setMinimumSize(
                                new Dimension(1000, 650));

                setDefaultCloseOperation(
                                JFrame.EXIT_ON_CLOSE);

                setLocationRelativeTo(null);

                createUI();

                loadAccounts();

                loadEmails();

                updateStatistics();
        }

        // =========================================================
        // MAIN UI
        // =========================================================

        private void createUI() {

                JPanel root = new JPanel(new BorderLayout());

                root.setBackground(BG);

                // =====================================================
                // HEADER
                // =====================================================

                JPanel header = new JPanel(new BorderLayout());

                header.setBackground(DARK);

                header.setBorder(
                                BorderFactory.createEmptyBorder(
                                                18, 25, 18, 25));

                JLabel title = new JLabel("MAIL SERVER");

                title.setForeground(Color.WHITE);

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                25));

                JLabel subtitle = new JLabel(
                                "Administration Dashboard");

                subtitle.setForeground(
                                new Color(148, 163, 184));

                subtitle.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));

                JPanel titlePanel = new JPanel();

                titlePanel.setOpaque(false);

                titlePanel.setLayout(
                                new javax.swing.BoxLayout(
                                                titlePanel,
                                                javax.swing.BoxLayout.Y_AXIS));

                titlePanel.add(title);

                titlePanel.add(subtitle);

                header.add(
                                titlePanel,
                                BorderLayout.WEST);

                JLabel status = new JLabel("● SERVER ADMIN");

                status.setForeground(
                                new Color(74, 222, 128));

                status.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                header.add(
                                status,
                                BorderLayout.EAST);

                root.add(
                                header,
                                BorderLayout.NORTH);

                // =====================================================
                // TABS
                // =====================================================

                JTabbedPane tabs = new JTabbedPane();

                tabs.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                tabs.addTab(
                                "Dashboard",
                                createDashboard());

                tabs.addTab(
                                "Accounts",
                                createAccountsPanel());

                tabs.addTab(
                                "Emails",
                                createEmailsPanel());

                tabs.addTab(
                                "Server Status",
                                createServerPanel());

                tabs.addTab(
                                "Server Logs",
                                createLogsPanel());
                root.add(
                                tabs,
                                BorderLayout.CENTER);

                setContentPane(root);
        }

        // =========================================================
        // DASHBOARD
        // =========================================================

        private JPanel createDashboard() {

                JPanel panel = new JPanel(new BorderLayout());

                panel.setBackground(BG);

                panel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                25, 25, 25, 25));

                JLabel title = new JLabel("Dashboard");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                28));

                title.setForeground(
                                new Color(15, 23, 42));

                panel.add(
                                title,
                                BorderLayout.NORTH);

                JPanel cards = new JPanel(
                                new GridLayout(
                                                1,
                                                3,
                                                20,
                                                20));

                cards.setOpaque(false);

                cards.setBorder(
                                BorderFactory.createEmptyBorder(
                                                25, 0, 25, 0));

                // ACCOUNT CARD

                JPanel accountCard = createCard(
                                "USERS",
                                "0",
                                BLUE);

                accountCountLabel = getCardNumber(accountCard);

                cards.add(accountCard);

                // EMAIL CARD

                JPanel emailCard = createCard(
                                "EMAILS",
                                "0",
                                GREEN);

                emailCountLabel = getCardNumber(emailCard);

                cards.add(emailCard);

                // SERVER CARD

                JPanel serverCard = createCard(
                                "SERVER",
                                "RUNNING",
                                RED);

                serverStatusLabel = getCardNumber(serverCard);

                cards.add(serverCard);

                panel.add(
                                cards,
                                BorderLayout.NORTH);

                JPanel info = new JPanel(
                                new BorderLayout());

                info.setBackground(Color.WHITE);

                info.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(226, 232, 240)),
                                                BorderFactory.createEmptyBorder(
                                                                25, 25, 25, 25)));

                JLabel infoTitle = new JLabel(
                                "Mail Server Information");

                infoTitle.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                20));

                info.add(
                                infoTitle,
                                BorderLayout.NORTH);

                JLabel infoText = new JLabel(
                                "<html>"
                                                + "This dashboard allows the administrator "
                                                + "to monitor the mail server.<br><br>"
                                                + "• View user accounts<br>"
                                                + "• View stored emails<br>"
                                                + "• Monitor server status<br>"
                                                + "• Refresh database information"
                                                + "</html>");

                infoText.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                15));

                infoText.setForeground(
                                new Color(71, 85, 105));

                info.add(
                                infoText,
                                BorderLayout.CENTER);

                panel.add(
                                info,
                                BorderLayout.CENTER);

                return panel;
        }

        // =========================================================
        // CARD
        // =========================================================

        private JPanel createCard(
                        String title,
                        String number,
                        Color color) {

                JPanel card = new JPanel(
                                new BorderLayout());

                card.setBackground(Color.WHITE);

                card.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(226, 232, 240)),
                                                BorderFactory.createEmptyBorder(
                                                                20, 20, 20, 20)));

                JLabel titleLabel = new JLabel(title);

                titleLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                titleLabel.setForeground(
                                new Color(100, 116, 139));

                JLabel numberLabel = new JLabel(number);

                numberLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                30));

                numberLabel.setForeground(color);

                card.add(
                                titleLabel,
                                BorderLayout.NORTH);

                card.add(
                                numberLabel,
                                BorderLayout.CENTER);

                return card;
        }

        private JLabel getCardNumber(
                        JPanel card) {

                for (Component component : card.getComponents()) {

                        if (component instanceof JLabel) {

                                JLabel label = (JLabel) component;

                                if (label.getFont().getSize() >= 25) {
                                        return label;
                                }
                        }
                }

                return null;
        }

        // =========================================================
        // ACCOUNTS
        // =========================================================

        private JPanel createAccountsPanel() {

                JPanel panel = new JPanel(new BorderLayout());

                panel.setBackground(BG);

                panel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                20, 20, 20, 20));

                JLabel title = new JLabel("User Accounts");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                25));

                title.setBorder(
                                BorderFactory.createEmptyBorder(
                                                0, 0, 15, 0));

                panel.add(
                                title,
                                BorderLayout.NORTH);

                accountModel = new DefaultTableModel(
                                new String[] {
                                                "ID",
                                                "Username",
                                                "Email"
                                }, 0) {

                        @Override
                        public boolean isCellEditable(
                                        int row,
                                        int column) {

                                return false;
                        }
                };

                accountTable = new JTable(accountModel);

                styleTable(accountTable);

                JScrollPane scrollPane = new JScrollPane(accountTable);

                panel.add(
                                scrollPane,
                                BorderLayout.CENTER);

                JButton refresh = createButton(
                                "REFRESH ACCOUNTS",
                                BLUE);

                refresh.addActionListener(
                                e -> {

                                        loadAccounts();

                                        updateStatistics();
                                });

                JPanel bottom = new JPanel(
                                new BorderLayout());

                bottom.setOpaque(false);

                bottom.setBorder(
                                BorderFactory.createEmptyBorder(
                                                15, 0, 0, 0));

                bottom.add(
                                refresh,
                                BorderLayout.EAST);

                panel.add(
                                bottom,
                                BorderLayout.SOUTH);

                return panel;
        }

        // =========================================================
        // EMAILS
        // =========================================================

        private JPanel createEmailsPanel() {

                JPanel panel = new JPanel(new BorderLayout());

                panel.setBackground(BG);

                panel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                20, 20, 20, 20));

                JLabel title = new JLabel("Stored Emails");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                25));

                title.setBorder(
                                BorderFactory.createEmptyBorder(
                                                0, 0, 15, 0));

                panel.add(
                                title,
                                BorderLayout.NORTH);

                emailModel = new DefaultTableModel(
                                new String[] {
                                                "ID",
                                                "Sender",
                                                "Receiver",
                                                "Subject",
                                                "Read",
                                                "Deleted",
                                                "Sent At"
                                }, 0) {

                        @Override
                        public boolean isCellEditable(
                                        int row,
                                        int column) {

                                return false;
                        }
                };

                emailTable = new JTable(emailModel);

                styleTable(emailTable);

                emailTable
                                .getColumnModel()
                                .getColumn(3)
                                .setPreferredWidth(250);

                JScrollPane scrollPane = new JScrollPane(emailTable);

                panel.add(
                                scrollPane,
                                BorderLayout.CENTER);

                JButton refresh = createButton(
                                "REFRESH EMAILS",
                                GREEN);

                refresh.addActionListener(
                                e -> {

                                        loadEmails();

                                        updateStatistics();
                                });

                JPanel bottom = new JPanel(
                                new BorderLayout());

                bottom.setOpaque(false);

                bottom.setBorder(
                                BorderFactory.createEmptyBorder(
                                                15, 0, 0, 0));

                bottom.add(
                                refresh,
                                BorderLayout.EAST);

                panel.add(
                                bottom,
                                BorderLayout.SOUTH);

                return panel;
        }

        // =========================================================
        // SERVER STATUS
        // =========================================================

        private JPanel createServerPanel() {

                JPanel panel = new JPanel();

                panel.setBackground(BG);

                panel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                30, 30, 30, 30));

                panel.setLayout(
                                new GridLayout(
                                                5,
                                                1,
                                                0,
                                                15));

                JLabel title = new JLabel("Server Services");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                25));

                panel.add(title);

                panel.add(
                                createStatusRow(
                                                "Main Mail Server",
                                                "TCP",
                                                "2525"));

                panel.add(
                                createStatusRow(
                                                "SMTP Server",
                                                "SMTP",
                                                "2526"));

                panel.add(
                                createStatusRow(
                                                "POP3 Server",
                                                "POP3",
                                                "1110"));

                panel.add(
                                createStatusRow(
                                                "IMAP Server",
                                                "IMAP",
                                                "1430"));

                return panel;
        }

        // =========================================================
        // SERVER LOGS
        // =========================================================

        private JPanel createLogsPanel() {

                JPanel panel = new JPanel(
                                new BorderLayout());

                panel.setBackground(BG);

                panel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                20, 20, 20, 20));

                // =====================================================
                // TITLE
                // =====================================================

                JLabel title = new JLabel(
                                "Server Logs");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                25));

                title.setBorder(
                                BorderFactory.createEmptyBorder(
                                                0, 0, 15, 0));

                panel.add(
                                title,
                                BorderLayout.NORTH);

                // =====================================================
                // LOG AREA
                // =====================================================

                logArea = new JTextArea();

                logArea.setEditable(false);

                logArea.setFont(
                                new Font(
                                                "Consolas",
                                                Font.PLAIN,
                                                13));

                logArea.setBackground(
                                new Color(15, 23, 42));

                logArea.setForeground(
                                new Color(226, 232, 240));

                logArea.setLineWrap(false);

                logArea.setBorder(
                                BorderFactory.createEmptyBorder(
                                                10, 10, 10, 10));

                JScrollPane scrollPane = new JScrollPane(logArea);

                panel.add(
                                scrollPane,
                                BorderLayout.CENTER);

                // =====================================================
                // BUTTONS
                // =====================================================

                JButton refreshButton = createButton(
                                "REFRESH",
                                BLUE);

                refreshButton.addActionListener(
                                e -> loadServerLogs());

                JButton clearButton = createButton(
                                "CLEAR LOG",
                                RED);

                clearButton.addActionListener(
                                e -> {

                                        int result = JOptionPane.showConfirmDialog(
                                                        this,
                                                        "Bạn có chắc muốn xóa toàn bộ log?",
                                                        "Clear Server Log",
                                                        JOptionPane.YES_NO_OPTION);

                                        if (result == JOptionPane.YES_OPTION) {

                                                ServerLogger.clearLog();

                                                loadServerLogs();
                                        }
                                });

                autoRefreshCheckBox = new JCheckBox(
                                "Auto Refresh");

                autoRefreshCheckBox.setOpaque(false);

                autoRefreshCheckBox.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));

                autoRefreshCheckBox.setSelected(true);

                JPanel buttonPanel = new JPanel();

                buttonPanel.setOpaque(false);

                buttonPanel.setLayout(
                                new javax.swing.BoxLayout(
                                                buttonPanel,
                                                javax.swing.BoxLayout.X_AXIS));

                buttonPanel.add(refreshButton);

                buttonPanel.add(
                                javax.swing.Box.createHorizontalStrut(
                                                10));

                buttonPanel.add(clearButton);

                buttonPanel.add(
                                javax.swing.Box.createHorizontalStrut(
                                                15));

                buttonPanel.add(autoRefreshCheckBox);

                JPanel bottom = new JPanel(
                                new BorderLayout());

                bottom.setOpaque(false);

                bottom.setBorder(
                                BorderFactory.createEmptyBorder(
                                                15, 0, 0, 0));

                bottom.add(
                                buttonPanel,
                                BorderLayout.WEST);

                panel.add(
                                bottom,
                                BorderLayout.SOUTH);

                // =====================================================
                // INITIAL LOAD
                // =====================================================

                loadServerLogs();

                // =====================================================
                // AUTO REFRESH
                // =====================================================

                logTimer = new Timer(
                                1000,
                                e -> {

                                        if (autoRefreshCheckBox != null
                                                        && autoRefreshCheckBox.isSelected()) {

                                                loadServerLogs();
                                        }
                                });

                logTimer.start();

                return panel;
        }

        // =========================================================
        // LOAD SERVER LOGS
        // =========================================================

        private void loadServerLogs() {

                if (logArea == null) {
                        return;
                }

                Path logPath = Paths.get(
                                ServerLogger.getLogFilePath());

                if (!Files.exists(logPath)) {

                        logArea.setText(
                                        "Chưa có server log.\n\n"
                                                        + "Hãy khởi động MailServer.");

                        return;
                }

                try {

                        List<String> lines = Files.readAllLines(logPath);

                        StringBuilder builder = new StringBuilder();

                        for (String line : lines) {

                                builder
                                                .append(line)
                                                .append("\n");
                        }

                        logArea.setText(
                                        builder.toString());

                        // Scroll xuống cuối
                        logArea.setCaretPosition(
                                        logArea.getDocument().getLength());

                } catch (IOException e) {

                        logArea.setText(
                                        "Không thể đọc server log:\n"
                                                        + e.getMessage());
                }
        }

        // =========================================================
        // STATUS ROW
        // =========================================================

        private JPanel createStatusRow(
                        String name,
                        String protocol,
                        String port) {

                JPanel row = new JPanel(
                                new BorderLayout());

                row.setBackground(Color.WHITE);

                row.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(226, 232, 240)),
                                                BorderFactory.createEmptyBorder(
                                                                12, 15, 12, 15)));

                JLabel service = new JLabel(
                                "●  " + name);

                service.setForeground(GREEN);

                service.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                15));

                JLabel detail = new JLabel(
                                protocol
                                                + "   |   Port "
                                                + port);

                detail.setForeground(
                                new Color(71, 85, 105));

                detail.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                row.add(
                                service,
                                BorderLayout.WEST);

                row.add(
                                detail,
                                BorderLayout.EAST);

                return row;
        }

        // =========================================================
        // LOAD USERS
        // =========================================================

        private void loadAccounts() {

                if (accountModel == null) {
                        return;
                }

                accountModel.setRowCount(0);

                String sql = "SELECT id, username, email "
                                + "FROM users ORDER BY id";

                try (
                                Connection connection = getConnection();

                                Statement statement = connection.createStatement();

                                ResultSet result = statement.executeQuery(sql)) {

                        while (result.next()) {

                                accountModel.addRow(
                                                new Object[] {
                                                                result.getInt("id"),
                                                                result.getString("username"),
                                                                result.getString("email")
                                                });
                        }

                } catch (Exception e) {

                        showDatabaseError(e);
                }
        }

        // =========================================================
        // LOAD EMAILS
        // =========================================================

        private void loadEmails() {

                if (emailModel == null) {
                        return;
                }

                emailModel.setRowCount(0);

                String sql = "SELECT id, sender, receiver, subject, "
                                + "is_read, is_deleted, sent_at "
                                + "FROM emails ORDER BY id DESC";

                try (
                                Connection connection = getConnection();

                                Statement statement = connection.createStatement();

                                ResultSet result = statement.executeQuery(sql)) {

                        while (result.next()) {

                                emailModel.addRow(
                                                new Object[] {
                                                                result.getInt("id"),
                                                                result.getString("sender"),
                                                                result.getString("receiver"),
                                                                result.getString("subject"),
                                                                result.getBoolean("is_read"),
                                                                result.getBoolean("is_deleted"),
                                                                result.getTimestamp("sent_at")
                                                });
                        }

                } catch (Exception e) {

                        showDatabaseError(e);
                }
        }

        // =========================================================
        // STATISTICS
        // =========================================================

        private void updateStatistics() {

                try (
                                Connection connection = getConnection();

                                Statement statement = connection.createStatement()) {

                        ResultSet users = statement.executeQuery(
                                        "SELECT COUNT(*) FROM users");

                        if (users.next()
                                        && accountCountLabel != null) {

                                accountCountLabel.setText(
                                                String.valueOf(
                                                                users.getInt(1)));
                        }

                        ResultSet emails = statement.executeQuery(
                                        "SELECT COUNT(*) FROM emails");

                        if (emails.next()
                                        && emailCountLabel != null) {

                                emailCountLabel.setText(
                                                String.valueOf(
                                                                emails.getInt(1)));
                        }

                } catch (Exception e) {

                        System.out.println(
                                        "Cannot update statistics: "
                                                        + e.getMessage());
                }
        }

        // =========================================================
        // DATABASE CONNECTION
        // =========================================================

        private Connection getConnection()
                        throws Exception {

                return DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD);
        }

        // =========================================================
        // TABLE STYLE
        // =========================================================

        private void styleTable(
                        JTable table) {

                table.setRowHeight(35);

                table.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));

                table.setSelectionBackground(
                                new Color(219, 234, 254));

                table.setSelectionForeground(
                                new Color(15, 23, 42));

                table.setGridColor(
                                new Color(226, 232, 240));

                table.setShowVerticalLines(false);

                table.setShowHorizontalLines(true);

                table.getTableHeader()
                                .setFont(
                                                new Font(
                                                                "Segoe UI",
                                                                Font.BOLD,
                                                                13));

                table.getTableHeader()
                                .setBackground(
                                                new Color(241, 245, 249));

                table.getTableHeader()
                                .setForeground(
                                                new Color(30, 41, 59));

                table.getTableHeader()
                                .setPreferredSize(
                                                new Dimension(
                                                                0,
                                                                40));

                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

                renderer.setBorder(
                                BorderFactory.createEmptyBorder(
                                                0, 8, 0, 8));

                table.setDefaultRenderer(
                                Object.class,
                                renderer);
        }

        // =========================================================
        // BUTTON
        // =========================================================

        private JButton createButton(
                        String text,
                        Color color) {

                JButton button = new JButton(text);

                button.setForeground(Color.WHITE);

                button.setBackground(color);

                button.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                button.setFocusPainted(false);

                button.setBorderPainted(false);

                button.setCursor(
                                new Cursor(
                                                Cursor.HAND_CURSOR));

                button.setMargin(
                                new Insets(
                                                10,
                                                18,
                                                10,
                                                18));

                return button;
        }

        // =========================================================
        // DATABASE ERROR
        // =========================================================

        private void showDatabaseError(
                        Exception e) {

                e.printStackTrace();

                JOptionPane.showMessageDialog(
                                this,
                                "Không thể đọc dữ liệu MySQL.\n\n"
                                                + e.getMessage()
                                                + "\n\n"
                                                + "Kiểm tra DB_URL, username "
                                                + "và password MySQL.",
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE);
        }

        // =========================================================
        // MAIN
        // =========================================================

        public static void main(
                        String[] args) {

                SwingUtilities.invokeLater(() -> {

                        ServerGUI gui = new ServerGUI();

                        gui.setVisible(true);
                });
        }
}