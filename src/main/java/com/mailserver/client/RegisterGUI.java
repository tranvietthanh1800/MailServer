package com.mailserver.client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

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

import com.mailserver.dao.UserDAO;
import com.mailserver.model.User;

public class RegisterGUI extends JFrame {

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterGUI() {

        setTitle("Create Account");
        setSize(450, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        30, 40, 30, 40));

        JLabel title = new JLabel("CREATE ACCOUNT");
        title.setFont(
                new Font("Arial", Font.BOLD, 26));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(30));

        // Username
        mainPanel.add(new JLabel("Username"));

        usernameField = new JTextField();
        usernameField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40));

        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Email
        mainPanel.add(new JLabel("Email"));

        emailField = new JTextField();
        emailField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40));

        mainPanel.add(emailField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Password
        mainPanel.add(new JLabel("Password"));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40));

        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Confirm password
        mainPanel.add(new JLabel("Confirm Password"));

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40));

        mainPanel.add(confirmPasswordField);
        mainPanel.add(Box.createVerticalStrut(25));

        // Register button
        JButton registerButton =
                new JButton("CREATE ACCOUNT");

        registerButton.setAlignmentX(
                Component.CENTER_ALIGNMENT);

        registerButton.setPreferredSize(
                new Dimension(200, 45));

        mainPanel.add(registerButton);

        mainPanel.add(Box.createVerticalStrut(15));

        // Back
        JButton backButton =
                new JButton("BACK TO LOGIN");

        backButton.setAlignmentX(
                Component.CENTER_ALIGNMENT);

        mainPanel.add(backButton);

        registerButton.addActionListener(
                e -> register());

        backButton.addActionListener(e -> {

            dispose();

            new MailClientGUI()
                    .setVisible(true);
        });

        add(mainPanel);
    }

    private void register() {

        String username =
                usernameField.getText().trim();

        String email =
                emailField.getText().trim();

        String password =
                new String(
                        passwordField.getPassword());

        String confirmPassword =
                new String(
                        confirmPasswordField
                                .getPassword());

        // ==============================
        // VALIDATE
        // ==============================

        if (username.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ thông tin.");

            return;
        }

        if (!email.contains("@")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Email không hợp lệ.");

            return;
        }

        if (!password.equals(confirmPassword)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Mật khẩu xác nhận không khớp.");

            return;
        }

        if (password.length() < 6) {

            JOptionPane.showMessageDialog(
                    this,
                    "Mật khẩu phải có ít nhất 6 ký tự.");

            return;
        }

        // ==============================
        // CREATE USER
        // ==============================

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        UserDAO userDAO = new UserDAO();

        boolean success =
                userDAO.register(user);

        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Tạo tài khoản thành công!");

            dispose();

            new MailClientGUI()
                    .setVisible(true);

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tạo tài khoản.\n"
                    + "Username hoặc email có thể đã tồn tại.",
                    "Đăng ký thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new RegisterGUI()
                    .setVisible(true);
        });
    }
}