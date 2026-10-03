package com.mailserver.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mailserver.dao.EmailDAO;
import com.mailserver.dao.UserDAO;
import com.mailserver.model.Email;
import com.mailserver.model.User;

public class POP3ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<Integer> markedForDeletion = new HashSet<>();

    public POP3ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                Socket s = socket;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(s.getInputStream()));
                PrintWriter writer = new PrintWriter(
                        s.getOutputStream(), true)) {

            UserDAO userDAO = new UserDAO();
            EmailDAO emailDAO = new EmailDAO();

            String username = null;
            String emailAddress = null;
            boolean authenticated = false;

            List<Email> inbox = null;
            List<Email> sentBox = null;

            send(writer, "+OK POP3 server ready");

            String command;

            while ((command = reader.readLine()) != null) {

                System.out.println("POP3 << " + command);

                String[] parts = command.split("\\s+", 2);
                String cmd = parts[0].toUpperCase();
                String arg = parts.length > 1
                        ? parts[1].trim()
                        : null;

                // ==================================================
                // AUTHORIZATION
                // ==================================================

                if (cmd.equals("USER")) {

                    if (arg == null) {
                        send(writer, "-ERR USER needs argument");
                        continue;
                    }

                    username = arg;
                    authenticated = false;

                    send(writer, "+OK User accepted");
                }

                else if (cmd.equals("PASS")) {

                    if (username == null || arg == null) {
                        send(writer, "-ERR Send USER first");
                        continue;
                    }

                    User user = userDAO.login(username, arg);

                    if (user != null) {

                        authenticated = true;
                        emailAddress =
                                userDAO.getEmailByUsername(username);

                        inbox = emailDAO.getInbox(emailAddress);
                        Collections.reverse(inbox);

                        sentBox = emailDAO.getSentMails(emailAddress);
                        Collections.reverse(sentBox);

                        markedForDeletion.clear();

                        send(writer,
                                "+OK Login successful");

                    } else {

                        authenticated = false;
                        username = null;
                        emailAddress = null;
                        inbox = null;
                        sentBox = null;

                        send(writer,
                                "-ERR Login failed");
                    }
                }

                // ==================================================
                // REQUIRE LOGIN
                // ==================================================

                else if (!authenticated) {

                    send(writer,
                            "-ERR Not authenticated");
                }

                // ==================================================
                // STAT
                // ==================================================

                else if (cmd.equals("STAT")) {

                    int totalSize = inbox.stream()
                            .mapToInt(this::calculateSize)
                            .sum();

                    send(writer,
                            "+OK "
                                    + inbox.size()
                                    + " "
                                    + totalSize);
                }

                // ==================================================
                // LIST
                // ==================================================

                else if (cmd.equals("LIST")) {

                    if (arg != null) {

                        Integer n =
                                parseMessageNumber(
                                        arg,
                                        inbox,
                                        writer);

                        if (n == null)
                            continue;

                        send(writer,
                                "+OK "
                                        + n
                                        + " "
                                        + calculateSize(
                                                inbox.get(n - 1)));

                    } else {

                        send(writer,
                                "+OK "
                                        + inbox.size()
                                        + " messages");

                        for (int i = 0;
                                i < inbox.size();
                                i++) {

                            send(writer,
                                    (i + 1)
                                            + " "
                                            + calculateSize(
                                                    inbox.get(i)));
                        }

                        send(writer, ".");
                    }
                }

                // ==================================================
                // RETR
                // ==================================================

                else if (cmd.equals("RETR")) {

                    Integer n =
                            parseMessageNumber(
                                    arg,
                                    inbox,
                                    writer);

                    if (n == null)
                        continue;

                    Email e = inbox.get(n - 1);

                    sendEmailData(writer, e);

                    emailDAO.markAsRead(
                            e.getId(),
                            emailAddress);
                }

                // ==================================================
                // THREAD <threadId>
                // Custom command: lấy toàn bộ conversation
                // ==================================================

                else if (cmd.equals("THREAD")) {

                    if (arg == null || arg.isBlank()) {
                        send(writer,
                                "-ERR THREAD needs thread id");
                        continue;
                    }

                    int threadId;

                    try {
                        threadId = Integer.parseInt(arg.trim());
                    } catch (NumberFormatException ex) {
                        send(writer,
                                "-ERR Invalid thread id");
                        continue;
                    }

                    if (threadId <= 0) {
                        send(writer,
                                "-ERR Invalid thread id");
                        continue;
                    }

                    List<Email> conversation =
                            emailDAO.getConversation(
                                    threadId,
                                    emailAddress);

                    if (conversation == null
                            || conversation.isEmpty()) {

                        send(writer,
                                "-ERR Conversation not found");
                        continue;
                    }

                    send(writer,
                            "+OK "
                                    + conversation.size()
                                    + " messages");

                    for (Email e : conversation) {

                        send(writer, "---MESSAGE---");
                        send(writer,
                                "Message-ID: "
                                        + e.getId());
                        writeLine(writer,
                                "From: "
                                        + safe(e.getSender()));
                        writeLine(writer,
                                "To: "
                                        + safe(e.getReceiver()));
                        writeLine(writer,
                                "Subject: "
                                        + safe(e.getSubject()));
                        writeLine(writer,
                                "Thread-ID: "
                                        + e.getThreadId());

                        if (e.getInReplyTo() != null) {
                            writeLine(writer,
                                    "In-Reply-To: "
                                            + e.getInReplyTo());
                        } else {
                            writeLine(writer,
                                    "In-Reply-To: 0");
                        }

                        send(writer, "");

                        String content =
                                e.getContent() == null
                                        ? ""
                                        : e.getContent();

                        for (String line :
                                content.split("\\R", -1)) {
                            writeLine(writer, line);
                        }

                        send(writer, ".");
                    }

                    send(writer, ".");

                    emailDAO.markConversationAsRead(
                            threadId,
                            emailAddress);
                }

                // ==================================================
                // DELE
                // ==================================================

                else if (cmd.equals("DELE")) {

                    Integer n =
                            parseMessageNumber(
                                    arg,
                                    inbox,
                                    writer);

                    if (n == null)
                        continue;

                    markedForDeletion.add(n);

                    send(writer,
                            "+OK Message "
                                    + n
                                    + " marked");
                }

                // ==================================================
                // RSET
                // ==================================================

                else if (cmd.equals("RSET")) {

                    markedForDeletion.clear();
                    send(writer, "+OK Reset");
                }

                // ==================================================
                // NOOP
                // ==================================================

                else if (cmd.equals("NOOP")) {

                    send(writer, "+OK");
                }

                // ==================================================
                // UIDL
                // ==================================================

                else if (cmd.equals("UIDL")) {

                    if (arg != null) {

                        Integer n =
                                parseMessageNumber(
                                        arg,
                                        inbox,
                                        writer);

                        if (n == null)
                            continue;

                        send(writer,
                                "+OK "
                                        + n
                                        + " msg-"
                                        + inbox.get(n - 1)
                                                .getId());

                    } else {

                        send(writer, "+OK");

                        for (int i = 0;
                                i < inbox.size();
                                i++) {

                            send(writer,
                                    (i + 1)
                                            + " msg-"
                                            + inbox.get(i)
                                                    .getId());
                        }

                        send(writer, ".");
                    }
                }

                // ==================================================
                // SENT
                // ==================================================

                else if (cmd.equals("SENT")) {

                    if (sentBox == null) {
                        send(writer,
                                "-ERR Sent box unavailable");
                        continue;
                    }

                    send(writer,
                            "+OK "
                                    + sentBox.size()
                                    + " sent messages");

                    for (int i = 0;
                            i < sentBox.size();
                            i++) {

                        Email e = sentBox.get(i);

                        int size =
                                calculateSize(e);

                        // number size receiver subject
                        send(writer,
                                (i + 1)
                                        + " "
                                        + size
                                        + " "
                                        + safe(e.getReceiver())
                                        + " "
                                        + safe(e.getSubject()));
                    }

                    send(writer, ".");
                }

                // ==================================================
                // RTRS
                // ==================================================

                else if (cmd.equals("RTRS")) {

                    if (sentBox == null) {
                        send(writer,
                                "-ERR Sent box unavailable");
                        continue;
                    }

                    Integer n =
                            parseMessageNumber(
                                    arg,
                                    sentBox,
                                    writer);

                    if (n == null)
                        continue;

                    Email e = sentBox.get(n - 1);

                    sendEmailData(writer, e);
                }

                // ==================================================
                // QUIT
                // ==================================================

                else if (cmd.equals("QUIT")) {

                    if (inbox != null) {
                        for (int n : markedForDeletion) {
                            if (n >= 1 && n <= inbox.size()) {
                                emailDAO.deleteMail(
                                        inbox.get(n - 1).getId(),
                                        emailAddress);
                            }
                        }
                    }

                    send(writer, "+OK Bye");
                    break;
                }

                // ==================================================
                // UNKNOWN
                // ==================================================

                else {
                    send(writer,
                            "-ERR Command not implemented");
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "POP3 client error: "
                            + e.getMessage());

        } finally {

            try {
                socket.close();
            } catch (Exception ignored) {
            }

            System.out.println(
                    "POP3 client disconnected");
        }
    }

    // =========================================================
    // SEND EMAIL DATA
    // =========================================================

    private void sendEmailData(
            PrintWriter writer,
            Email e) {

        send(writer,
                "+OK Message follows");

        writeLine(writer,
                "Message-ID: "
                        + e.getId());

        writeLine(writer,
                "From: "
                        + safe(e.getSender()));

        writeLine(writer,
                "To: "
                        + safe(e.getReceiver()));

        writeLine(writer,
                "Subject: "
                        + safe(e.getSubject()));

        writeLine(writer,
                "Thread-ID: "
                        + e.getThreadId());

        if (e.getInReplyTo() != null) {
            writeLine(writer,
                    "In-Reply-To: "
                            + e.getInReplyTo());
        } else {
            writeLine(writer,
                    "In-Reply-To: 0");
        }

        send(writer, "");

        String content =
                e.getContent() == null
                        ? ""
                        : e.getContent();

        for (String line :
                content.split("\\R", -1)) {
            writeLine(writer, line);
        }

        send(writer, ".");
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void send(
            PrintWriter writer,
            String response) {

        writer.println(response);

        System.out.println(
                "POP3 >> "
                        + response);
    }

    private void writeLine(
            PrintWriter writer,
            String line) {

        if (line == null)
            line = "";

        // POP3 dot-stuffing
        if (line.startsWith("."))
            line = "." + line;

        send(writer, line);
    }

    private Integer parseMessageNumber(
            String arg,
            List<Email> list,
            PrintWriter writer) {

        if (arg == null || arg.isBlank()) {

            send(writer,
                    "-ERR Missing argument");

            return null;
        }

        try {

            int n = Integer.parseInt(arg.trim());

            if (n < 1 || n > list.size()) {

                send(writer,
                        "-ERR No such message");

                return null;
            }

            return n;

        } catch (NumberFormatException e) {

            send(writer,
                    "-ERR Invalid message number");

            return null;
        }
    }

    private int calculateSize(Email e) {

        int size = 0;

        if (e.getSender() != null)
            size += e.getSender().length();

        if (e.getReceiver() != null)
            size += e.getReceiver().length();

        if (e.getSubject() != null)
            size += e.getSubject().length();

        if (e.getContent() != null)
            size += e.getContent().length();

        return size;
    }

    /** Loại bỏ ký tự xuống dòng để đảm bảo 1 dòng response. */
    private String safe(String s) {

        if (s == null)
            return "";

        return s
                .replace("\r", " ")
                .replace("\n", " ");
    }
}
