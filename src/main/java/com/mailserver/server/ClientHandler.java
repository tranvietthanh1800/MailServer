package com.mailserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.mailserver.dao.EmailDAO;
import com.mailserver.dao.UserDAO;
import com.mailserver.model.Email;
import com.mailserver.model.User;

public class ClientHandler implements Runnable {

        private Socket socket;

        public ClientHandler(Socket socket) {
                this.socket = socket;
        }

        @Override
        public void run() {

                String clientAddress = socket.getInetAddress().getHostAddress();
                int clientPort = socket.getPort();

                try (
                                BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(
                                                                socket.getInputStream()));

                                PrintWriter writer = new PrintWriter(
                                                socket.getOutputStream(),
                                                true)) {

                        UserDAO userDAO = new UserDAO();
                        EmailDAO emailDAO = new EmailDAO();

                        // =====================================================
                        // CLIENT CONNECTED
                        // =====================================================

                        ServerLogger.log(
                                        "Client connected: "
                                                        + clientAddress
                                                        + ":"
                                                        + clientPort);

                        // =====================================================
                        // ĐỌC REQUEST ĐẦU TIÊN: LOGIN hay REGISTER?
                        // =====================================================

                        writer.println("WELCOME TO MAIL SERVER");
                        writer.println("REQUEST_TYPE:"); // client gửi "LOGIN" hoặc "REGISTER"

                        String requestType = reader.readLine();

                        // Nếu client cũ không gửi gì (null) → mặc định LOGIN
                        if (requestType == null) {
                                ServerLogger.log(
                                                "Client disconnected before request - "
                                                                + clientAddress);
                                return;
                        }

                        requestType = requestType.trim().toUpperCase();

                        // =====================================================
                        // NHÁNH REGISTER
                        // =====================================================

                        if ("REGISTER".equals(requestType)) {

                                handleRegister(reader, writer, userDAO,
                                                clientAddress, clientPort);
                                return; // đóng kết nối sau khi đăng ký
                        }

                        // =====================================================
                        // NHÁNH LOGIN (mặc định)
                        // =====================================================

                        handleLogin(reader, writer, userDAO, emailDAO,
                                        clientAddress, clientPort, requestType);

                } catch (Exception e) {

                        // ServerLogger.log(
                        // "CLIENT ERROR - "
                        // + clientAddress
                        // + ":"
                        // + clientPort
                        // + " | "
                        // + e.getMessage());

                        e.printStackTrace();

                } finally {

                        try {
                                socket.close();
                                ServerLogger.log(
                                                "Client connection closed - "
                                                                + clientAddress
                                                                + ":"
                                                                + clientPort);

                        } catch (IOException e) {
                                ServerLogger.log(
                                                "ERROR closing client socket - "
                                                                + e.getMessage());
                        }
                }
        }

        // =====================================================
        // REGISTER HANDLER
        // =====================================================

        private void handleRegister(
                        BufferedReader reader,
                        PrintWriter writer,
                        UserDAO userDAO,
                        String clientAddress,
                        int clientPort) throws IOException {

                writer.println("USERNAME:");
                String username = reader.readLine();

                writer.println("PASSWORD:");
                String password = reader.readLine();

                writer.println("EMAIL:");
                String email = reader.readLine();

                ServerLogger.log(
                                "REGISTER attempt - Username: "
                                                + username
                                                + " | Email: "
                                                + email
                                                + " from "
                                                + clientAddress);

                // ---------- Validate cơ bản ----------

                if (username == null || username.isBlank()
                                || password == null || password.isBlank()
                                || email == null || email.isBlank()) {

                        writer.println("INVALID_INPUT");
                        ServerLogger.log(
                                        "REGISTER FAILED (invalid input) - Username: "
                                                        + username);
                        return;
                }

                username = username.trim();
                email = email.trim();

                if (!email.contains("@")) {
                        writer.println("INVALID_EMAIL");
                        ServerLogger.log(
                                        "REGISTER FAILED (invalid email) - Email: "
                                                        + email);
                        return;
                }

                // ---------- Kiểm tra trùng ----------

                if (userDAO.existsByUsername(username)) {
                        writer.println("USER_EXISTS");
                        ServerLogger.log(
                                        "REGISTER FAILED (username exists) - "
                                                        + username);
                        return;
                }

                if (userDAO.existsByEmail(email)) {
                        writer.println("EMAIL_EXISTS");
                        ServerLogger.log(
                                        "REGISTER FAILED (email exists) - "
                                                        + email);
                        return;
                }

                // ---------- Tạo user ----------

                User newUser = new User(username, password, email);
                boolean success = userDAO.register(newUser);

                if (success) {
                        writer.println("REGISTER_SUCCESS");
                        ServerLogger.log(
                                        "REGISTER SUCCESS - Username: "
                                                        + username
                                                        + " | Email: "
                                                        + email);
                } else {
                        writer.println("REGISTER_FAILED");
                        ServerLogger.log(
                                        "REGISTER FAILED (DB error) - Username: "
                                                        + username);
                }
        }

        // =====================================================
        // LOGIN HANDLER (giữ nguyên logic cũ)
        // =====================================================

        private void handleLogin(
                        BufferedReader reader,
                        PrintWriter writer,
                        UserDAO userDAO,
                        EmailDAO emailDAO,
                        String clientAddress,
                        int clientPort,
                        String requestType) throws IOException {

                writer.println("Username:");
                String username = reader.readLine();

                ServerLogger.log(
                                "Login attempt - Username: "
                                                + username
                                                + " from "
                                                + clientAddress);

                writer.println("Password:");
                String password = reader.readLine();

                User user = userDAO.login(username, password);

                // =====================================================
                // LOGIN FAILED
                // =====================================================

                if (user == null) {

                        writer.println("LOGIN_FAILED");

                        ServerLogger.log(
                                        "LOGIN FAILED - Username: "
                                                        + username
                                                        + " - Client: "
                                                        + clientAddress);

                        return;
                }

                // =====================================================
                // LOGIN SUCCESS
                // =====================================================

                writer.println("LOGIN_SUCCESS");

                ServerLogger.log(
                                "LOGIN SUCCESS - User: "
                                                + username
                                                + " - Email: "
                                                + user.getEmail()
                                                + " - Client: "
                                                + clientAddress);

                // =====================================================
                // MENU
                // =====================================================

                while (true) {

                        writer.println("MENU");
                        writer.println("1. SEND MAIL");
                        writer.println("2. INBOX");
                        writer.println("3. READ MAIL");
                        writer.println("4. SENT MAIL");
                        writer.println("5. DELETE MAIL");
                        writer.println("6. TRASH");
                        writer.println("7. RESTORE MAIL");
                        writer.println("8. LOGOUT");
                        writer.println("9. EXIT");
                        writer.println("Choose:");

                        String choice = reader.readLine();

                        if (choice == null) {
                                ServerLogger.log(
                                                "Client disconnected unexpectedly - User: "
                                                                + username);
                                break;
                        }

                        ServerLogger.log(
                                        "User "
                                                        + username
                                                        + " selected menu option: "
                                                        + choice);

                        // =================================================
                        // SEND MAIL
                        // =================================================

                        if ("1".equals(choice)) {

                                writer.println("Receiver:");
                                String receiver = reader.readLine();

                                writer.println("Subject:");
                                String subject = reader.readLine();

                                writer.println("Content:");
                                String content = reader.readLine();

                                String sender = user.getEmail();

                                Email email = new Email(
                                                sender,
                                                receiver,
                                                subject,
                                                content);

                                int newEmailId = emailDAO.sendEmail(email);
                                boolean success = (newEmailId > 0);

                                if (success) {
                                        writer.println("SEND_SUCCESS");
                                        ServerLogger.log(
                                                        "EMAIL SENT - "
                                                                        + sender
                                                                        + " -> "
                                                                        + receiver
                                                                        + " | Subject: "
                                                                        + subject);
                                } else {
                                        writer.println("SEND_FAILED");
                                        ServerLogger.log(
                                                        "EMAIL SEND FAILED - "
                                                                        + sender
                                                                        + " -> "
                                                                        + receiver
                                                                        + " | Subject: "
                                                                        + subject);
                                }
                        }

                        // =================================================
                        // INBOX
                        // =================================================

                        else if ("2".equals(choice)) {

                                List<Email> emails = emailDAO.getInbox(
                                                user.getEmail());

                                writer.println("INBOX_COUNT:" + emails.size());

                                for (Email email : emails) {
                                        writer.println(
                                                        email.getId()
                                                                        + " | From: "
                                                                        + email.getSender()
                                                                        + " | Subject: "
                                                                        + email.getSubject()
                                                                        + " | Read: "
                                                                        + email.isRead());
                                }

                                writer.println("INBOX_END");

                                ServerLogger.log(
                                                "INBOX VIEWED - User: "
                                                                + username
                                                                + " | Emails: "
                                                                + emails.size());
                        }

                        // =================================================
                        // READ MAIL
                        // =================================================

                        else if ("3".equals(choice)) {

                                writer.println("Enter email ID:");
                                String idInput = reader.readLine();

                                try {
                                        int emailId = Integer.parseInt(idInput);

                                        Email email = emailDAO.getEmailById(
                                                        emailId,
                                                        user.getEmail());

                                        if (email == null) {
                                                writer.println("EMAIL_NOT_FOUND");
                                                ServerLogger.log(
                                                                "EMAIL NOT FOUND - ID: "
                                                                                + emailId
                                                                                + " | User: "
                                                                                + username);
                                        } else {
                                                writer.println("EMAIL_FOUND");
                                                writer.println("From: " + email.getSender());
                                                writer.println("To: " + email.getReceiver());
                                                writer.println("Subject: " + email.getSubject());
                                                writer.println("Content: " + email.getContent());

                                                emailDAO.markAsRead(
                                                                emailId,
                                                                user.getEmail());

                                                ServerLogger.log(
                                                                "EMAIL READ - ID: "
                                                                                + emailId
                                                                                + " | User: "
                                                                                + username
                                                                                + " | Subject: "
                                                                                + email.getSubject());
                                        }

                                } catch (NumberFormatException e) {
                                        writer.println("INVALID_EMAIL_ID");
                                        ServerLogger.log(
                                                        "INVALID EMAIL ID - Input: "
                                                                        + idInput
                                                                        + " | User: "
                                                                        + username);
                                }
                        }

                        // =================================================
                        // SENT MAIL
                        // =================================================

                        else if ("4".equals(choice)) {

                                List<Email> emails = emailDAO.getSentMails(
                                                user.getEmail());

                                writer.println("SENT_COUNT:" + emails.size());

                                for (Email email : emails) {
                                        writer.println(
                                                        email.getId()
                                                                        + " | To: "
                                                                        + email.getReceiver()
                                                                        + " | Subject: "
                                                                        + email.getSubject());
                                }

                                writer.println("SENT_END");

                                ServerLogger.log(
                                                "SENT MAIL VIEWED - User: "
                                                                + username
                                                                + " | Emails: "
                                                                + emails.size());
                        }

                        // =================================================
                        // DELETE MAIL
                        // =================================================

                        else if ("5".equals(choice)) {

                                writer.println("Enter email ID to delete:");
                                String idInput = reader.readLine();

                                try {
                                        int emailId = Integer.parseInt(idInput);

                                        boolean success = emailDAO.deleteMail(
                                                        emailId,
                                                        user.getEmail());

                                        if (success) {
                                                writer.println("DELETE_SUCCESS");
                                                ServerLogger.log(
                                                                "EMAIL DELETED - ID: "
                                                                                + emailId
                                                                                + " | User: "
                                                                                + username);
                                        } else {
                                                writer.println("DELETE_FAILED");
                                                ServerLogger.log(
                                                                "EMAIL DELETE FAILED - ID: "
                                                                                + emailId
                                                                                + " | User: "
                                                                                + username);
                                        }

                                } catch (NumberFormatException e) {
                                        writer.println("INVALID_EMAIL_ID");
                                        ServerLogger.log(
                                                        "INVALID DELETE EMAIL ID - Input: "
                                                                        + idInput
                                                                        + " | User: "
                                                                        + username);
                                }
                        }

                        // =================================================
                        // TRASH
                        // =================================================

                        else if ("6".equals(choice)) {

                                List<Email> emails = emailDAO.getTrash(
                                                user.getEmail());

                                writer.println("TRASH_COUNT:" + emails.size());

                                for (Email email : emails) {
                                        writer.println(
                                                        email.getId()
                                                                        + " | From: "
                                                                        + email.getSender()
                                                                        + " | Subject: "
                                                                        + email.getSubject()
                                                                        + " | Read: "
                                                                        + email.isRead());
                                }

                                writer.println("TRASH_END");

                                ServerLogger.log(
                                                "TRASH VIEWED - User: "
                                                                + username
                                                                + " | Emails: "
                                                                + emails.size());
                        }

                        // =================================================
                        // RESTORE MAIL
                        // =================================================

                        else if ("7".equals(choice)) {

                                writer.println("Enter email ID to restore:");
                                String idInput = reader.readLine();

                                try {
                                        int emailId = Integer.parseInt(idInput);

                                        boolean success = emailDAO.restoreMail(
                                                        emailId,
                                                        user.getEmail());

                                        if (success) {
                                                writer.println("RESTORE_SUCCESS");
                                                ServerLogger.log(
                                                                "EMAIL RESTORED - ID: "
                                                                                + emailId
                                                                                + " | User: "
                                                                                + username);
                                        } else {
                                                writer.println("RESTORE_FAILED");
                                                ServerLogger.log(
                                                                "EMAIL RESTORE FAILED - ID: "
                                                                                + emailId
                                                                                + " | User: "
                                                                                + username);
                                        }

                                } catch (NumberFormatException e) {
                                        writer.println("INVALID_EMAIL_ID");
                                        ServerLogger.log(
                                                        "INVALID RESTORE EMAIL ID - Input: "
                                                                        + idInput
                                                                        + " | User: "
                                                                        + username);
                                }
                        }

                        // =================================================
                        // LOGOUT
                        // =================================================

                        else if ("8".equals(choice)) {

                                writer.println("LOGOUT_SUCCESS");
                                ServerLogger.log(
                                                "USER LOGOUT - User: "
                                                                + username
                                                                + " - Client: "
                                                                + clientAddress);
                                break;
                        }

                        // =================================================
                        // EXIT
                        // =================================================

                        else if ("9".equals(choice)) {

                                writer.println("GOODBYE");
                                ServerLogger.log(
                                                "CLIENT EXIT - User: "
                                                                + username
                                                                + " - Client: "
                                                                + clientAddress);
                                break;
                        }

                        // =================================================
                        // INVALID CHOICE
                        // =================================================

                        else {

                                writer.println("INVALID_CHOICE");
                                ServerLogger.log(
                                                "INVALID MENU CHOICE - User: "
                                                                + username
                                                                + " | Choice: "
                                                                + choice);
                        }
                }
        }
}