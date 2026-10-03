package com.mailserver.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.mailserver.ServerConfig;

public class MailClientGUI extends JFrame {

        // ==================================================
        // COLOR PALETTE
        // ==================================================

        private static final Color PRIMARY = new Color(36, 99, 235);
        private static final Color PRIMARY_HOVER = new Color(29, 78, 216);
        private static final Color SUCCESS = new Color(16, 185, 129);
        private static final Color SUCCESS_HOVER = new Color(5, 150, 105);
        private static final Color SECONDARY = new Color(100, 116, 139);
        private static final Color SECONDARY_HOVER = new Color(71, 85, 105);
        private static final Color BACKGROUND = new Color(245, 247, 251);
        private static final Color TEXT = new Color(30, 41, 59);

        // ==================================================
        // FIELDS
        // ==================================================

        private static final int SERVER_PORT = 2525;

        private CardLayout cardLayout;
        private JPanel cardPanel;

        // Login fields
        private JTextField loginServerIpField;
        private JTextField loginUsernameField;
        private JPasswordField loginPasswordField;

        // Register fields
        private JTextField regServerIpField;
        private JTextField regUsernameField;
        private JTextField regEmailField;
        private JPasswordField regPasswordField;
        private JPasswordField regConfirmField;

        // ==================================================
        // CONSTRUCTOR
        // ==================================================

        public MailClientGUI() {

                setTitle("Mail Client");
                setSize(500, 640);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLocationRelativeTo(null);

                createUI();
        }

        // ==================================================
        // CREATE UI
        // ==================================================

        private void createUI() {

                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBackground(BACKGROUND);

                // ==============================
                // TOP HEADER (dùng chung)
                // ==============================

                JPanel topPanel = new JPanel();
                topPanel.setBackground(PRIMARY);
                topPanel.setPreferredSize(new Dimension(500, 120));
                topPanel.setLayout(new GridBagLayout());

                JLabel title = new JLabel("MAIL SERVER");
                title.setForeground(Color.WHITE);
                title.setFont(new Font("Segoe UI", Font.BOLD, 30));

                JLabel subtitle = new JLabel("Secure Mail Client");
                subtitle.setForeground(new Color(220, 230, 255));
                subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                JPanel titlePanel = new JPanel();
                titlePanel.setOpaque(false);
                titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

                title.setAlignmentX(Component.CENTER_ALIGNMENT);
                subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

                titlePanel.add(title);
                titlePanel.add(Box.createVerticalStrut(5));
                titlePanel.add(subtitle);

                topPanel.add(titlePanel);

                mainPanel.add(topPanel, BorderLayout.NORTH);

                // ==============================
                // CARD PANEL (Login / Register)
                // ==============================

                cardLayout = new CardLayout();
                cardPanel = new JPanel(cardLayout);
                cardPanel.setBackground(BACKGROUND);

                cardPanel.add(createLoginPanel(), "LOGIN");
                cardPanel.add(createRegisterPanel(), "REGISTER");

                mainPanel.add(cardPanel, BorderLayout.CENTER);

                add(mainPanel);
        }

        // ==================================================
        // LOGIN PANEL
        // ==================================================

        private JPanel createLoginPanel() {

                JPanel formWrapper = new JPanel(new GridBagLayout());
                formWrapper.setBackground(BACKGROUND);

                JPanel formPanel = createFormCard();

                GridBagConstraints gbc = defaultGbc();

                // ==============================
                // TITLE
                // ==============================

                JLabel formTitle = new JLabel("Sign in");
                formTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
                formTitle.setForeground(TEXT);

                gbc.gridy = 0;
                gbc.insets = new Insets(0, 0, 20, 0);
                formPanel.add(formTitle, gbc);

                // ==============================
                // SERVER IP
                // ==============================

                gbc.gridy = 1;
                gbc.insets = new Insets(0, 0, 6, 0);
                formPanel.add(createFieldLabel("Server IP Address"), gbc);

                loginServerIpField = new JTextField();
                styleTextField(loginServerIpField);
                loginServerIpField.setText(ServerConfig.getServerHost());

                gbc.gridy = 2;
                gbc.insets = new Insets(0, 0, 15, 0);
                formPanel.add(loginServerIpField, gbc);

                // ==============================
                // USERNAME
                // ==============================

                gbc.gridy = 3;
                gbc.insets = new Insets(0, 0, 6, 0);
                formPanel.add(createFieldLabel("Username"), gbc);

                loginUsernameField = new JTextField();
                styleTextField(loginUsernameField);

                gbc.gridy = 4;
                gbc.insets = new Insets(0, 0, 15, 0);
                formPanel.add(loginUsernameField, gbc);

                // ==============================
                // PASSWORD
                // ==============================

                gbc.gridy = 5;
                gbc.insets = new Insets(0, 0, 6, 0);
                formPanel.add(createFieldLabel("Password"), gbc);

                loginPasswordField = new JPasswordField();
                styleTextField(loginPasswordField);

                gbc.gridy = 6;
                gbc.insets = new Insets(0, 0, 22, 0);
                formPanel.add(loginPasswordField, gbc);

                // ==============================
                // LOGIN BUTTON
                // ==============================

                RoundedButton loginButton = new RoundedButton(
                                "LOGIN", PRIMARY, PRIMARY_HOVER);
                loginButton.setPreferredSize(new Dimension(300, 45));
                loginButton.addActionListener(e -> login());

                gbc.gridy = 7;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.insets = new Insets(0, 0, 12, 0);
                formPanel.add(loginButton, gbc);

                // ==============================
                // REGISTER BUTTON
                // ==============================

                RoundedButton registerButton = new RoundedButton(
                                "CREATE ACCOUNT", SUCCESS, SUCCESS_HOVER);
                registerButton.setPreferredSize(new Dimension(300, 45));
                registerButton.addActionListener(e -> showRegisterForm());

                gbc.gridy = 8;
                gbc.insets = new Insets(0, 0, 0, 0);
                formPanel.add(registerButton, gbc);

                formWrapper.add(formPanel);
                return formWrapper;
        }

        // ==================================================
        // REGISTER PANEL
        // ==================================================

        private JPanel createRegisterPanel() {

                JPanel formWrapper = new JPanel(new GridBagLayout());
                formWrapper.setBackground(BACKGROUND);

                JPanel formPanel = createFormCard();

                GridBagConstraints gbc = defaultGbc();

                // ==============================
                // TITLE
                // ==============================

                JLabel formTitle = new JLabel("Create account");
                formTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
                formTitle.setForeground(TEXT);

                gbc.gridy = 0;
                gbc.insets = new Insets(0, 0, 18, 0);
                formPanel.add(formTitle, gbc);

                // ==============================
                // SERVER IP
                // ==============================

                gbc.gridy = 1;
                gbc.insets = new Insets(0, 0, 5, 0);
                formPanel.add(createFieldLabel("Server IP Address"), gbc);

                regServerIpField = new JTextField();
                styleTextField(regServerIpField);
                regServerIpField.setText(ServerConfig.getServerHost());

                gbc.gridy = 2;
                gbc.insets = new Insets(0, 0, 12, 0);
                formPanel.add(regServerIpField, gbc);

                // ==============================
                // USERNAME
                // ==============================

                gbc.gridy = 3;
                gbc.insets = new Insets(0, 0, 5, 0);
                formPanel.add(createFieldLabel("Username"), gbc);

                regUsernameField = new JTextField();
                styleTextField(regUsernameField);

                gbc.gridy = 4;
                gbc.insets = new Insets(0, 0, 12, 0);
                formPanel.add(regUsernameField, gbc);

                // ==============================
                // EMAIL
                // ==============================

                gbc.gridy = 5;
                gbc.insets = new Insets(0, 0, 5, 0);
                formPanel.add(createFieldLabel("Email"), gbc);

                regEmailField = new JTextField();
                styleTextField(regEmailField);

                gbc.gridy = 6;
                gbc.insets = new Insets(0, 0, 12, 0);
                formPanel.add(regEmailField, gbc);

                // ==============================
                // PASSWORD
                // ==============================

                gbc.gridy = 7;
                gbc.insets = new Insets(0, 0, 5, 0);
                formPanel.add(createFieldLabel("Password"), gbc);

                regPasswordField = new JPasswordField();
                styleTextField(regPasswordField);

                gbc.gridy = 8;
                gbc.insets = new Insets(0, 0, 12, 0);
                formPanel.add(regPasswordField, gbc);

                // ==============================
                // CONFIRM PASSWORD
                // ==============================

                gbc.gridy = 9;
                gbc.insets = new Insets(0, 0, 5, 0);
                formPanel.add(createFieldLabel("Confirm Password"), gbc);

                regConfirmField = new JPasswordField();
                styleTextField(regConfirmField);

                gbc.gridy = 10;
                gbc.insets = new Insets(0, 0, 18, 0);
                formPanel.add(regConfirmField, gbc);

                // ==============================
                // REGISTER BUTTON
                // ==============================

                RoundedButton registerButton = new RoundedButton(
                                "REGISTER", SUCCESS, SUCCESS_HOVER);
                registerButton.setPreferredSize(new Dimension(300, 45));
                registerButton.addActionListener(e -> register());

                gbc.gridy = 11;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.insets = new Insets(0, 0, 10, 0);
                formPanel.add(registerButton, gbc);

                // ==============================
                // BACK TO LOGIN BUTTON
                // ==============================

                RoundedButton backButton = new RoundedButton(
                                "BACK TO LOGIN", SECONDARY, SECONDARY_HOVER);
                backButton.setPreferredSize(new Dimension(300, 45));
                backButton.addActionListener(e -> showLoginForm());

                gbc.gridy = 12;
                gbc.insets = new Insets(0, 0, 0, 0);
                formPanel.add(backButton, gbc);

                formWrapper.add(formPanel);
                return formWrapper;
        }

        // ==================================================
        // FORM CARD (khung trắng chung)
        // ==================================================

        private JPanel createFormCard() {

                JPanel formPanel = new JPanel(new GridBagLayout());
                formPanel.setBackground(Color.WHITE);
                formPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
                return formPanel;
        }

        // ==================================================
        // DEFAULT GRIDBAG CONSTRAINTS
        // ==================================================

        private GridBagConstraints defaultGbc() {

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.WEST;
                return gbc;
        }

        // ==================================================
        // FIELD LABEL
        // ==================================================

        private JLabel createFieldLabel(String text) {

                JLabel label = new JLabel(text);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setForeground(TEXT);
                return label;
        }

        // ==================================================
        // TEXT FIELD STYLE
        // ==================================================

        private void styleTextField(JTextField field) {

                field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                field.setPreferredSize(new Dimension(300, 40));
                field.setMaximumSize(new Dimension(300, 40));
                field.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                                new Color(203, 213, 225)),
                                new EmptyBorder(8, 12, 8, 12)));
                field.setBackground(new Color(248, 250, 252));
        }

        // ==================================================
        // SHOW FORMS
        // ==================================================

        private void showLoginForm() {
                cardLayout.show(cardPanel, "LOGIN");
                setTitle("Mail Client - Login");
                setSize(500, 640);
        }

        private void showRegisterForm() {
                cardLayout.show(cardPanel, "REGISTER");
                setTitle("Mail Client - Register");
                setSize(500, 800);
        }

        // ==================================================
        // LOGIN
        // ==================================================

        private void login() {

                String serverIp = loginServerIpField.getText().trim();
                String username = loginUsernameField.getText().trim();
                String password = new String(loginPasswordField.getPassword());

                if (serverIp.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                        "Vui lòng nhập địa chỉ IP của Server.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        loginServerIpField.requestFocus();
                        return;
                }

                if (username.isEmpty() || password.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                        "Vui lòng nhập đầy đủ username và password.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        return;
                }

                try {
                        Socket socket = new Socket(loginServerIpField.getText().trim(), ServerConfig.MAIL_PORT);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                        // Đọc greeting
                        reader.readLine(); // "WELCOME TO MAIL SERVER"
                        reader.readLine(); // "REQUEST_TYPE:"

                        // Gửi loại request
                        writer.println("LOGIN");

                        // Phần còn lại giữ nguyên logic cũ
                        String usernameRequest = reader.readLine(); // "Username:"
                        writer.println(username);

                        String passwordRequest = reader.readLine(); // "Password:"
                        writer.println(password);

                        String result = reader.readLine();
                        // LOGIN_SUCCESS / LOGIN_FAILED
                        System.out.println("SERVER: " + result);

                        if ("LOGIN_SUCCESS".equals(result)) {
                                ServerConfig.setServerHost(serverIp);
                                socket.close();
                                new MainMailGUI(username, password).setVisible(true);
                                dispose();
                        } else {
                                socket.close();
                                JOptionPane.showMessageDialog(this,
                                                "Username hoặc password không đúng.",
                                                "Đăng nhập thất bại",
                                                JOptionPane.ERROR_MESSAGE);
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                        "Không thể kết nối Mail Server.\n\n"
                                                        + "Server: " + serverIp + ":" + SERVER_PORT + "\n\n"
                                                        + "Kiểm tra:\n"
                                                        + "• Server có đang chạy không?\n"
                                                        + "• IP Server có đúng không?\n"
                                                        + "• Port 2525 có được mở Firewall không?\n"
                                                        + "• Hai máy có cùng mạng không?",
                                        "Lỗi kết nối",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        // ==================================================
        // REGISTER
        // ==================================================

        private void register() {

                String serverIp = regServerIpField.getText().trim();
                String username = regUsernameField.getText().trim();
                String email = regEmailField.getText().trim();
                String password = new String(regPasswordField.getPassword());
                String confirm = new String(regConfirmField.getPassword());

                // ==============================
                // VALIDATE
                // ==============================

                if (serverIp.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                        "Vui lòng nhập địa chỉ IP của Server.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        regServerIpField.requestFocus();
                        return;
                }

                if (username.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                        "Vui lòng nhập username.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        regUsernameField.requestFocus();
                        return;
                }

                if (email.isEmpty() || !email.contains("@")) {
                        JOptionPane.showMessageDialog(this,
                                        "Vui lòng nhập email hợp lệ.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        regEmailField.requestFocus();
                        return;
                }

                if (password.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                        "Vui lòng nhập password.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        regPasswordField.requestFocus();
                        return;
                }

                if (!password.equals(confirm)) {
                        JOptionPane.showMessageDialog(this,
                                        "Password và Confirm Password không khớp.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        regConfirmField.requestFocus();
                        return;
                }

                // ==============================
                // SEND REQUEST
                // ==============================

                try {
                        // Trong MailClientGUI.register():

                        Socket socket = new Socket(regServerIpField.getText().trim(), ServerConfig.MAIL_PORT);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                        // Đọc greeting
                        reader.readLine(); // "WELCOME TO MAIL SERVER"
                        reader.readLine(); // "REQUEST_TYPE:"

                        // Gửi loại request
                        writer.println("REGISTER");

                        // Server hỏi từng field
                        reader.readLine(); // "USERNAME:"
                        writer.println(username);

                        reader.readLine(); // "PASSWORD:"
                        writer.println(password);

                        reader.readLine(); // "EMAIL:"
                        writer.println(email);

                        // Đọc kết quả
                        String result = reader.readLine();
                        // REGISTER_SUCCESS / USER_EXISTS / EMAIL_EXISTS / INVALID_INPUT / INVALID_EMAIL
                        // / REGISTER_FAILED
                        System.out.println("SERVER: " + result);

                        socket.close();

                        if ("REGISTER_SUCCESS".equals(result)) {
                                ServerConfig.setServerHost(serverIp);
                                JOptionPane.showMessageDialog(this,
                                                "Đăng ký thành công!\nBạn có thể đăng nhập ngay.",
                                                "Thành công",
                                                JOptionPane.INFORMATION_MESSAGE);

                                // Prefill login form
                                loginUsernameField.setText(username);
                                loginPasswordField.setText("");
                                loginServerIpField.setText(serverIp);

                                showLoginForm();

                        } else if ("USER_EXISTS".equals(result)) {
                                JOptionPane.showMessageDialog(this,
                                                "Username đã tồn tại. Vui lòng chọn username khác.",
                                                "Đăng ký thất bại",
                                                JOptionPane.ERROR_MESSAGE);
                                regUsernameField.requestFocus();

                        } else if ("EMAIL_EXISTS".equals(result)) {
                                JOptionPane.showMessageDialog(this,
                                                "Email đã được sử dụng. Vui lòng chọn email khác.",
                                                "Đăng ký thất bại",
                                                JOptionPane.ERROR_MESSAGE);
                                regEmailField.requestFocus();

                        } else {
                                JOptionPane.showMessageDialog(this,
                                                "Đăng ký thất bại: " + result,
                                                "Lỗi",
                                                JOptionPane.ERROR_MESSAGE);
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                        "Không thể kết nối Mail Server.\n\n"
                                                        + "Server: " + serverIp + ":" + SERVER_PORT + "\n\n"
                                                        + "Kiểm tra:\n"
                                                        + "• Server có đang chạy không?\n"
                                                        + "• IP Server có đúng không?\n"
                                                        + "• Port 2525 có được mở Firewall không?\n"
                                                        + "• Hai máy có cùng mạng không?",
                                        "Lỗi kết nối",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        // ==================================================
        // ROUNDED BUTTON
        // ==================================================

        static class RoundedButton extends JButton {

                private final Color normalColor;
                private final Color hoverColor;

                public RoundedButton(String text, Color normalColor, Color hoverColor) {

                        super(text);
                        this.normalColor = normalColor;
                        this.hoverColor = hoverColor;

                        setForeground(Color.WHITE);
                        setFont(new Font("Segoe UI", Font.BOLD, 14));
                        setFocusPainted(false);
                        setBorderPainted(false);
                        setContentAreaFilled(false);
                        setOpaque(false);
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        setBackground(normalColor);

                        addMouseListener(new java.awt.event.MouseAdapter() {

                                @Override
                                public void mouseEntered(java.awt.event.MouseEvent e) {
                                        setBackground(hoverColor);
                                        repaint();
                                }

                                @Override
                                public void mouseExited(java.awt.event.MouseEvent e) {
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
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                        super.paintComponent(g);
                        g2.dispose();
                }
        }

        // ==================================================
        // MAIN
        // ==================================================

        public static void main(String[] args) {

                SwingUtilities.invokeLater(() -> {
                        MailClientGUI gui = new MailClientGUI();
                        gui.setVisible(true);
                });
        }
}