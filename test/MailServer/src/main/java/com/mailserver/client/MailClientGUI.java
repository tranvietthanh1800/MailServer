package com.mailserver.client;

import java.awt.BorderLayout;
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

public class MailClientGUI extends JFrame {

        private JTextField serverIpField;

        private JTextField usernameField;

        private JPasswordField passwordField;

        private static final int SERVER_PORT = 2525;

        public MailClientGUI() {

                setTitle("Mail Client - Login");

                setSize(500, 570);

                setDefaultCloseOperation(
                                JFrame.EXIT_ON_CLOSE);

                setLocationRelativeTo(null);

                createLoginUI();
        }

        private void createLoginUI() {

                // ==============================
                // MAIN PANEL
                // ==============================

                JPanel mainPanel = new JPanel(
                                new BorderLayout());

                mainPanel.setBackground(
                                new Color(
                                                245,
                                                247,
                                                251));

                // ==============================
                // TOP
                // ==============================

                JPanel topPanel = new JPanel();

                topPanel.setBackground(
                                new Color(
                                                36,
                                                99,
                                                235));

                topPanel.setPreferredSize(
                                new Dimension(
                                                500,
                                                120));

                topPanel.setLayout(
                                new GridBagLayout());

                JLabel title = new JLabel(
                                "MAIL SERVER");

                title.setForeground(
                                Color.WHITE);

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                30));

                JLabel subtitle = new JLabel(
                                "Secure Mail Client");

                subtitle.setForeground(
                                new Color(
                                                220,
                                                230,
                                                255));

                subtitle.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                JPanel titlePanel = new JPanel();

                titlePanel.setOpaque(false);

                titlePanel.setLayout(
                                new BoxLayout(
                                                titlePanel,
                                                BoxLayout.Y_AXIS));

                title.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                subtitle.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                titlePanel.add(title);

                titlePanel.add(
                                Box.createVerticalStrut(5));

                titlePanel.add(subtitle);

                topPanel.add(titlePanel);

                mainPanel.add(
                                topPanel,
                                BorderLayout.NORTH);

                // ==============================
                // FORM WRAPPER
                // ==============================

                JPanel formWrapper = new JPanel(
                                new GridBagLayout());

                formWrapper.setBackground(
                                new Color(
                                                245,
                                                247,
                                                251));

                // ==============================
                // FORM PANEL
                // ==============================

                JPanel formPanel = new JPanel(
                                new GridBagLayout());

                formPanel.setBackground(
                                Color.WHITE);

                formPanel.setBorder(
                                new EmptyBorder(
                                                25,
                                                30,
                                                25,
                                                30));

                GridBagConstraints gbc = new GridBagConstraints();

                gbc.gridx = 0;

                gbc.weightx = 1.0;

                gbc.fill = GridBagConstraints.HORIZONTAL;

                gbc.anchor = GridBagConstraints.WEST;

                // ==============================
                // SERVER IP
                // ==============================

                JLabel serverIpLabel = new JLabel(
                                "Server IP Address");

                serverIpLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                serverIpField = new JTextField();

                styleTextField(
                                serverIpField);

                // IP mặc định để test trên chính máy
                serverIpField.setText(
                                "localhost");

                gbc.gridy = 0;

                gbc.insets = new Insets(
                                0,
                                0,
                                6,
                                0);

                formPanel.add(
                                serverIpLabel,
                                gbc);

                gbc.gridy = 1;

                gbc.insets = new Insets(
                                0,
                                0,
                                15,
                                0);

                formPanel.add(
                                serverIpField,
                                gbc);

                // ==============================
                // USERNAME
                // ==============================

                JLabel usernameLabel = new JLabel(
                                "Username");

                usernameLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                usernameField = new JTextField();

                styleTextField(
                                usernameField);

                gbc.gridy = 2;

                gbc.insets = new Insets(
                                0,
                                0,
                                6,
                                0);

                formPanel.add(
                                usernameLabel,
                                gbc);

                gbc.gridy = 3;

                gbc.insets = new Insets(
                                0,
                                0,
                                15,
                                0);

                formPanel.add(
                                usernameField,
                                gbc);

                // ==============================
                // PASSWORD
                // ==============================

                JLabel passwordLabel = new JLabel(
                                "Password");

                passwordLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                passwordField = new JPasswordField();

                styleTextField(
                                passwordField);

                gbc.gridy = 4;

                gbc.insets = new Insets(
                                0,
                                0,
                                6,
                                0);

                formPanel.add(
                                passwordLabel,
                                gbc);

                gbc.gridy = 5;

                gbc.insets = new Insets(
                                0,
                                0,
                                22,
                                0);

                formPanel.add(
                                passwordField,
                                gbc);

                // ==============================
                // LOGIN BUTTON
                // ==============================

                RoundedButton loginButton = new RoundedButton(
                                "LOGIN",
                                new Color(
                                                36,
                                                99,
                                                235),
                                new Color(
                                                29,
                                                78,
                                                216));

                loginButton.setPreferredSize(
                                new Dimension(
                                                300,
                                                45));

                loginButton.addActionListener(
                                e -> login());

                gbc.gridy = 6;

                gbc.fill = GridBagConstraints.NONE;

                gbc.anchor = GridBagConstraints.CENTER;

                gbc.insets = new Insets(
                                0,
                                0,
                                12,
                                0);

                formPanel.add(
                                loginButton,
                                gbc);

                // ==============================
                // REGISTER BUTTON
                // ==============================

                RoundedButton registerButton = new RoundedButton(
                                "CREATE ACCOUNT",
                                new Color(
                                                16,
                                                185,
                                                129),
                                new Color(
                                                5,
                                                150,
                                                105));

                registerButton.setPreferredSize(
                                new Dimension(
                                                300,
                                                45));

                registerButton.addActionListener(
                                e -> openRegister());

                gbc.gridy = 7;

                gbc.insets = new Insets(
                                0,
                                0,
                                0,
                                0);

                formPanel.add(
                                registerButton,
                                gbc);

                // ==============================
                // FORM
                // ==============================

                formWrapper.add(
                                formPanel);

                mainPanel.add(
                                formWrapper,
                                BorderLayout.CENTER);

                add(mainPanel);
        }

        // ==================================================
        // OPEN REGISTER GUI
        // ==================================================

        private void openRegister() {

                RegisterGUI registerGUI = new RegisterGUI();

                registerGUI.setVisible(true);

                dispose();
        }

        // ==================================================
        // TEXT FIELD STYLE
        // ==================================================

        private void styleTextField(
                        JTextField field) {

                field.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                field.setPreferredSize(
                                new Dimension(
                                                300,
                                                40));

                field.setMaximumSize(
                                new Dimension(
                                                300,
                                                40));

                field.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(
                                                                                203,
                                                                                213,
                                                                                225)),
                                                new EmptyBorder(
                                                                8,
                                                                12,
                                                                8,
                                                                12)));

                field.setBackground(
                                new Color(
                                                248,
                                                250,
                                                252));
        }

        // ==================================================
        // LOGIN
        // ==================================================

        private void login() {

                String serverIp = serverIpField.getText().trim();

                String username = usernameField.getText().trim();

                String password = new String(
                                passwordField.getPassword());

                // ==============================
                // VALIDATE
                // ==============================

                if (serverIp.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng nhập địa chỉ IP của Server.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        serverIpField.requestFocus();

                        return;
                }

                if (username.isEmpty()
                                || password.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng nhập đầy đủ username và password.",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                try {

                        // ==============================
                        // CONNECT SERVER
                        // ==============================

                        System.out.println(
                                        "Connecting to Server: "
                                                        + serverIp
                                                        + ":"
                                                        + SERVER_PORT);

                        Socket socket = new Socket(
                                        serverIp,
                                        SERVER_PORT);

                        BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(
                                                        socket.getInputStream()));

                        PrintWriter writer = new PrintWriter(
                                        socket.getOutputStream(),
                                        true);

                        // ==============================
                        // WELCOME
                        // ==============================

                        String welcome = reader.readLine();

                        System.out.println(
                                        "SERVER: "
                                                        + welcome);

                        // ==============================
                        // USERNAME
                        // ==============================

                        String usernameRequest = reader.readLine();

                        System.out.println(
                                        "SERVER: "
                                                        + usernameRequest);

                        writer.println(
                                        username);

                        // ==============================
                        // PASSWORD
                        // ==============================

                        String passwordRequest = reader.readLine();

                        System.out.println(
                                        "SERVER: "
                                                        + passwordRequest);

                        writer.println(
                                        password);

                        // ==============================
                        // LOGIN RESULT
                        // ==============================

                        String result = reader.readLine();

                        System.out.println(
                                        "SERVER: "
                                                        + result);

                        if ("LOGIN_SUCCESS".equals(result)) {

                                socket.close();

                                new MainMailGUI(
                                                username)
                                                .setVisible(true);

                                dispose();

                        } else {

                                socket.close();

                                JOptionPane.showMessageDialog(
                                                this,
                                                "Username hoặc password không đúng.",
                                                "Đăng nhập thất bại",
                                                JOptionPane.ERROR_MESSAGE);
                        }

                } catch (Exception e) {

                        e.printStackTrace();

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Không thể kết nối Mail Server.\n\n"
                                                        + "Server: "
                                                        + serverIp
                                                        + ":"
                                                        + SERVER_PORT
                                                        + "\n\n"
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

        static class RoundedButton
                        extends JButton {

                private Color normalColor;

                private Color hoverColor;

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
                                                        14));

                        setFocusPainted(
                                        false);

                        setBorderPainted(
                                        false);

                        setContentAreaFilled(
                                        false);

                        setOpaque(false);

                        setCursor(
                                        new Cursor(
                                                        Cursor.HAND_CURSOR));

                        addMouseListener(
                                        new java.awt.event.MouseAdapter() {

                                                @Override
                                                public void mouseEntered(
                                                                java.awt.event.MouseEvent e) {

                                                        setBackground(
                                                                        hoverColor);

                                                        repaint();
                                                }

                                                @Override
                                                public void mouseExited(
                                                                java.awt.event.MouseEvent e) {

                                                        setBackground(
                                                                        normalColor);

                                                        repaint();
                                                }
                                        });

                        setBackground(
                                        normalColor);
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
                                        18,
                                        18);

                        super.paintComponent(g);

                        g2.dispose();
                }
        }

        // ==================================================
        // MAIN
        // ==================================================

        public static void main(
                        String[] args) {

                SwingUtilities.invokeLater(() -> {

                        MailClientGUI gui = new MailClientGUI();

                        gui.setVisible(true);
                });
        }
}