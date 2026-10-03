package com.mailserver.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.mailserver.dao.EmailDAO;
import com.mailserver.model.Email;

public class SMTPClientHandler implements Runnable {

        private Socket socket;

        public SMTPClientHandler(Socket socket) {
                this.socket = socket;
        }

        @Override
        public void run() {

                try (
                                BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(
                                                                socket.getInputStream()));

                                PrintWriter writer = new PrintWriter(
                                                socket.getOutputStream(),
                                                true)) {

                        EmailDAO emailDAO = new EmailDAO();

                        // SMTP greeting
                        writer.println("220 MAIL SERVER SMTP READY");

                        String sender = null;
                        List<String> receivers = new ArrayList<>();
                        String line;

                        while (true) {

                                String command = reader.readLine();

                                if (command == null) {
                                        break;
                                }

                                System.out.println(
                                                "SMTP << " + command);

                                // =========================
                                // HELO
                                // =========================
                                if (command.toUpperCase()
                                                .startsWith("HELO")) {

                                        writer.println(
                                                        "250 Hello");
                                }

                                // =========================
                                // MAIL FROM
                                // =========================
                                else if (command.toUpperCase()
                                                .startsWith("MAIL FROM:")) {

                                        sender = command.substring(10)
                                                        .trim();

                                        receivers.clear();

                                        writer.println(
                                                        "250 Sender OK");
                                }

                                // =========================
                                // RCPT TO
                                // =========================
                                else if (command.toUpperCase()
                                                .startsWith("RCPT TO:")) {

                                        String receiver = command.substring(8)
                                                        .trim();

                                        if (!receiver.isEmpty()) {
                                                receivers.add(receiver);
                                        }

                                        writer.println(
                                                        "250 Receiver OK");
                                }

                                // =========================================================
                                // DATA
                                // =========================================================

                                else if (command.equalsIgnoreCase("DATA")) {

                                        writer.println(
                                                        "354 End data with <END>");

                                        String subject = "";
                                        StringBuilder content = new StringBuilder();

                                        int threadId = 0;
                                        Integer inReplyTo = null;

                                        boolean bodyStarted = false;

                                        while ((line = reader.readLine()) != null) {

                                                if (line.equals("<END>")) {
                                                        break;
                                                }

                                                // ==============================================
                                                // SUBJECT
                                                // ==============================================

                                                if (!bodyStarted &&
                                                                line.startsWith("Subject:")) {

                                                        subject = line.substring(
                                                                        "Subject:".length()).trim();

                                                        continue;
                                                }

                                                // ==============================================
                                                // THREAD-ID
                                                // ==============================================

                                                if (!bodyStarted &&
                                                                line.startsWith("Thread-ID:")) {

                                                        try {

                                                                threadId = Integer.parseInt(
                                                                                line.substring(
                                                                                                "Thread-ID:".length()).trim());

                                                        } catch (NumberFormatException ex) {

                                                                threadId = 0;
                                                        }

                                                        continue;
                                                }

                                                // ==============================================
                                                // IN-REPLY-TO
                                                // ==============================================

                                                if (!bodyStarted &&
                                                                line.startsWith("In-Reply-To:")) {

                                                        try {

                                                                int replyId = Integer.parseInt(
                                                                                line.substring(
                                                                                                "In-Reply-To:".length()).trim());

                                                                if (replyId > 0) {
                                                                        inReplyTo = replyId;
                                                                }

                                                        } catch (NumberFormatException ex) {

                                                                inReplyTo = null;
                                                        }

                                                        continue;
                                                }

                                                // ==============================================
                                                // BODY
                                                // ==============================================

                                                bodyStarted = true;

                                                content
                                                                .append(line)
                                                                .append("\n");
                                        }

                                        if (sender == null || sender.isBlank() || receivers.isEmpty()) {
                                                writer.println("550 Missing sender or receiver");
                                                continue;
                                        }

                                        boolean success = true;
                                        int sharedThreadId = threadId;

                                        for (String recipient : receivers) {

                                                Email email = new Email(
                                                                sender,
                                                                recipient,
                                                                subject,
                                                                content.toString().trim());

                                                email.setThreadId(sharedThreadId);
                                                email.setInReplyTo(inReplyTo);

                                                boolean saved = emailDAO.sendEmail(email);

                                                if (!saved) {
                                                        success = false;
                                                        break;
                                                }

                                                // Mail mới gửi cho nhiều người phải dùng chung 1 thread.
                                                if (sharedThreadId <= 0) {
                                                        sharedThreadId = email.getThreadId();
                                                }
                                        }

                                        if (success) {
                                                writer.println("250 Message accepted");
                                        } else {
                                                writer.println("550 Failed to save message");
                                        }

                                        continue;
                                }

                                // =========================
                                // QUIT
                                // =========================
                                else if (command.equalsIgnoreCase(
                                                "QUIT")) {

                                        writer.println(
                                                        "221 Bye");

                                        break;
                                }

                                // =========================
                                // UNKNOWN COMMAND
                                // =========================
                                else {

                                        writer.println(
                                                        "502 Command not implemented");
                                }
                        }

                } catch (Exception e) {

                        System.out.println(
                                        "SMTP client disconnected.");

                } finally {

                        try {
                                socket.close();
                        } catch (Exception ignored) {
                        }
                }
        }
}