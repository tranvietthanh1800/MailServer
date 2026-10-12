package com.mailserver.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.mailserver.dao.AttachmentDAO;
import com.mailserver.dao.EmailDAO;
import com.mailserver.model.Attachment;
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
                        AttachmentDAO attachmentDAO = new AttachmentDAO();

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

                                        writer.println("354 End data with <END>");

                                        String subject = "";
                                        StringBuilder content = new StringBuilder();
                                        int threadId = 0;
                                        Integer inReplyTo = null;
                                        boolean bodyStarted = false;

                                        // ⭐ Chỉ lưu metadata attachment (đã upload qua PUTATT)
                                        List<Attachment> attachments = new ArrayList<>();

                                        while ((line = reader.readLine()) != null) {

                                                if (line.equals("<END>"))
                                                        break;

                                                // ========== Attachment metadata ==========
                                                // Format mới: Attachment: attId|filename|contentType|size
                                                // ========== Attachment metadata ==========
                                                // Parse BẤT KỂ vị trí (header hay body)
                                                if (line.startsWith("Attachment:")) {
                                                        String meta = line.substring("Attachment:".length()).trim();
                                                        String[] parts = meta.split("\\|");
                                                        if (parts.length >= 4) {
                                                                try {
                                                                        int attId = Integer.parseInt(parts[0]);
                                                                        Attachment att = new Attachment();
                                                                        att.setId(attId);
                                                                        att.setFilename(parts[1]);
                                                                        att.setContentType(parts[2]);
                                                                        att.setFileSize(Integer.parseInt(parts[3]));
                                                                        attachments.add(att);
                                                                } catch (NumberFormatException ignored) {
                                                                }
                                                        }
                                                        continue; // ⭐ Bỏ qua, không append vào body
                                                }

                                                // ========== Subject ==========
                                                if (!bodyStarted && line.startsWith("Subject:")) {
                                                        subject = line.substring("Subject:".length()).trim();
                                                        continue;
                                                }

                                                // ========== Thread-ID ==========
                                                if (!bodyStarted && line.startsWith("Thread-ID:")) {
                                                        try {
                                                                threadId = Integer.parseInt(
                                                                                line.substring("Thread-ID:".length())
                                                                                                .trim());
                                                        } catch (NumberFormatException ex) {
                                                                threadId = 0;
                                                        }
                                                        continue;
                                                }

                                                // ========== In-Reply-To ==========
                                                if (!bodyStarted && line.startsWith("In-Reply-To:")) {
                                                        try {
                                                                int replyId = Integer.parseInt(
                                                                                line.substring("In-Reply-To:".length())
                                                                                                .trim());
                                                                if (replyId > 0)
                                                                        inReplyTo = replyId;
                                                        } catch (NumberFormatException ex) {
                                                                inReplyTo = null;
                                                        }
                                                        continue;
                                                }

                                                // ========== Body ==========
                                                bodyStarted = true;
                                                content.append(line).append("\n");
                                        }

                                        if (sender == null || sender.isBlank() || receivers.isEmpty()) {
                                                writer.println("550 Missing sender or receiver");
                                                continue;
                                        }

                                        boolean success = true;
                                        int sharedThreadId = threadId;

                                        for (String recipient : receivers) {

                                                Email email = new Email(sender, recipient, subject,
                                                                content.toString().trim());
                                                email.setThreadId(sharedThreadId);
                                                email.setInReplyTo(inReplyTo);

                                                int newEmailId = emailDAO.sendEmail(email);

                                                if (newEmailId <= 0) {
                                                        success = false;
                                                        break;
                                                }

                                                email.setId(newEmailId);

                                                // ⭐ LINK ATTACHMENT ĐÃ UPLOAD VÀO EMAIL
                                                for (Attachment att : attachments) {
                                                        boolean ok = attachmentDAO.linkToEmail(
                                                                        att.getId(),
                                                                        newEmailId);
                                                        if (!ok) {
                                                                System.out.println("Failed to link attachment: "
                                                                                + att.getFilename()
                                                                                + " (id=" + att.getId() + ")");
                                                        }
                                                }

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
                                        "SMTP client disconnected: " + e.getMessage());

                } finally {

                        try {
                                socket.close();
                        } catch (Exception ignored) {
                        }
                }
        }
}