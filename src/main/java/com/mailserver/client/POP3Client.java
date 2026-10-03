package com.mailserver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.mailserver.ServerConfig;

/**
 * POP3 Client — kết nối tới POP3Server.
 *
 * Hỗ trợ:
 * - POP3 chuẩn: LIST / RETR / DELE / STAT
 * - SENT: danh sách mail đã gửi
 * - RTRS <n>: lấy mail đã gửi
 * - THREAD <threadId>: lấy toàn bộ conversation
 */
public class POP3Client {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public POP3Client(
            String username,
            String password) {

        this.host = ServerConfig.getServerHost();
        this.port = ServerConfig.POP3_PORT;
        this.username = username;
        this.password = password;
    }

    // ==================================================
    // DATA CLASS
    // ==================================================

    public static class MessageInfo {

        public final int number;
        public final int size;

        public MessageInfo(
                int number,
                int size) {

            this.number = number;
            this.size = size;
        }
    }

    public static class RawMessage {

        public final int number;
        public final int messageId;
        public final String sender;
        public final String receiver;
        public final String subject;
        public final String content;
        public final int threadId;
        public final Integer inReplyTo;

        public RawMessage(
                int number,
                int messageId,
                String sender,
                String receiver,
                String subject,
                String content,
                int threadId,
                Integer inReplyTo) {

            this.number = number;
            this.messageId = messageId;
            this.sender = sender;
            this.receiver = receiver;
            this.subject = subject;
            this.content = content;
            this.threadId = threadId;
            this.inReplyTo = inReplyTo;
        }

        // Constructor cũ để code khác vẫn tương thích.
        public RawMessage(
                int number,
                String sender,
                String receiver,
                String subject,
                String content) {

            this(
                    number,
                    number,
                    sender,
                    receiver,
                    subject,
                    content,
                    number,
                    null);
        }
    }

    // ==================================================
    // INBOX API
    // ==================================================

    public List<RawMessage> fetchInbox()
            throws IOException {

        List<RawMessage> messages =
                new ArrayList<>();

        try (Session session = openSession()) {

            List<MessageInfo> list =
                    session.list();

            list.sort((a, b) ->
                    Integer.compare(
                            b.number,
                            a.number));

            for (MessageInfo info : list) {

                RawMessage msg =
                        session.retr(info.number);

                messages.add(msg);
            }
        }

        return messages;
    }

    public boolean deleteMessage(
            int number)
            throws IOException {

        try (Session session = openSession()) {
            return session.dele(number);
        }
    }

    // ==================================================
    // CONVERSATION API
    // ==================================================

    /**
     * Lấy toàn bộ mail thuộc cùng một thread.
     */
    public List<RawMessage> fetchConversation(
            int threadId)
            throws IOException {

        try (Session session = openSession()) {
            return session.thread(threadId);
        }
    }

    // ==================================================
    // SENT API
    // ==================================================

    /**
     * Lấy toàn bộ mail đã gửi.
     */
    public List<RawMessage> fetchSent()
            throws IOException {

        List<RawMessage> messages =
                new ArrayList<>();

        try (Session session = openSession()) {

            List<MessageInfo> list =
                    session.sent();

            list.sort((a, b) ->
                    Integer.compare(
                            b.number,
                            a.number));

            for (MessageInfo info : list) {

                RawMessage msg =
                        session.rtrs(info.number);

                messages.add(msg);
            }
        }

        return messages;
    }

    /** Chỉ lấy metadata sent. */
    public List<MessageInfo> listSent()
            throws IOException {

        try (Session session = openSession()) {
            return session.sent();
        }
    }

    /** Lấy chi tiết 1 mail đã gửi. */
    public RawMessage retrieveSent(
            int number)
            throws IOException {

        try (Session session = openSession()) {
            return session.rtrs(number);
        }
    }

    // ==================================================
    // SESSION
    // ==================================================

    private Session openSession()
            throws IOException {

        Session s =
                new Session(
                        host,
                        port);

        s.login(
                username,
                password);

        return s;
    }

    // ==================================================
    // INNER SESSION
    // ==================================================

    private static class Session
            implements AutoCloseable {

        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;

        Session(
                String host,
                int port)
                throws IOException {

            this.socket =
                    new Socket(
                            host,
                            port);

            this.reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));

            this.writer =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true);

            String greeting =
                    reader.readLine();

            if (greeting == null ||
                    !greeting.startsWith("+OK")) {

                throw new IOException(
                        "Server greeting failed: "
                                + greeting);
            }
        }

        // ==================================================
        // LOGIN
        // ==================================================

        void login(
                String username,
                String password)
                throws IOException {

            sendAndExpect(
                    "USER " + username,
                    "+OK");

            sendAndExpect(
                    "PASS " + password,
                    "+OK");
        }

        // ==================================================
        // LIST
        // ==================================================

        List<MessageInfo> list()
                throws IOException {

            writer.println("LIST");

            String first =
                    reader.readLine();

            if (first == null ||
                    !first.startsWith("+OK")) {

                throw new IOException(
                        "LIST failed: " + first);
            }

            List<MessageInfo> result =
                    new ArrayList<>();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.equals("."))
                    break;

                String[] parts =
                        line.split("\\s+");

                if (parts.length >= 2) {

                    int n =
                            Integer.parseInt(
                                    parts[0]);

                    int size =
                            Integer.parseInt(
                                    parts[1]);

                    result.add(
                            new MessageInfo(
                                    n,
                                    size));
                }
            }

            return result;
        }

        // ==================================================
        // RETR
        // ==================================================

        RawMessage retr(
                int number)
                throws IOException {

            writer.println(
                    "RETR " + number);

            String first =
                    reader.readLine();

            if (first == null ||
                    !first.startsWith("+OK")) {

                throw new IOException(
                        "RETR "
                                + number
                                + " failed: "
                                + first);
            }

            return readSingleMessage(
                    number,
                    number);
        }

        // ==================================================
        // DELE
        // ==================================================

        boolean dele(
                int number)
                throws IOException {

            writer.println(
                    "DELE " + number);

            String resp =
                    reader.readLine();

            return resp != null &&
                    resp.startsWith("+OK");
        }

        // ==================================================
        // THREAD
        // ==================================================

        List<RawMessage> thread(
                int threadId)
                throws IOException {

            writer.println(
                    "THREAD " + threadId);

            String first =
                    reader.readLine();

            if (first == null ||
                    !first.startsWith("+OK")) {

                throw new IOException(
                        "THREAD "
                                + threadId
                                + " failed: "
                                + first);
            }

            int count =
                    parseCount(first);

            List<RawMessage> result =
                    new ArrayList<>();

            for (int i = 0;
                    i < count;
                    i++) {

                String marker =
                        reader.readLine();

                if (!"---MESSAGE---".equals(marker)) {

                    throw new IOException(
                            "Invalid conversation marker: "
                                    + marker);
                }

                result.add(
                        readSingleMessage(
                                i + 1,
                                null));
            }

            String finalDot =
                    reader.readLine();

            if (!".".equals(finalDot)) {

                throw new IOException(
                        "THREAD response not terminated");
            }

            return result;
        }

        // ==================================================
        // SENT
        // ==================================================

        List<MessageInfo> sent()
                throws IOException {

            writer.println("SENT");

            String first =
                    reader.readLine();

            if (first == null ||
                    !first.startsWith("+OK")) {

                throw new IOException(
                        "SENT failed: " + first);
            }

            List<MessageInfo> result =
                    new ArrayList<>();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.equals("."))
                    break;

                String[] parts =
                        line.split("\\s+", 4);

                if (parts.length >= 2) {

                    try {

                        int n =
                                Integer.parseInt(
                                        parts[0]);

                        int size =
                                Integer.parseInt(
                                        parts[1]);

                        result.add(
                                new MessageInfo(
                                        n,
                                        size));

                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            return result;
        }

        // ==================================================
        // RTRS
        // ==================================================

        RawMessage rtrs(
                int number)
                throws IOException {

            writer.println(
                    "RTRS " + number);

            String first =
                    reader.readLine();

            if (first == null ||
                    !first.startsWith("+OK")) {

                throw new IOException(
                        "RTRS "
                                + number
                                + " failed: "
                                + first);
            }

            return readSingleMessage(
                    number,
                    number);
        }

        // ==================================================
        // READ SINGLE MESSAGE
        // ==================================================

        private RawMessage readSingleMessage(
                int defaultNumber,
                Integer fallbackMessageId)
                throws IOException {

            int messageId =
                    fallbackMessageId == null
                            ? defaultNumber
                            : fallbackMessageId;

            int threadId = 0;
            Integer inReplyTo = null;

            String sender = "";
            String receiver = "";
            String subject = "";

            StringBuilder body =
                    new StringBuilder();

            boolean inBody = false;

            String line;

            while ((line = reader.readLine()) != null) {

                // End of current message
                if (line.equals("."))
                    break;

                // POP3 dot unstuffing
                if (line.startsWith(".."))
                    line = line.substring(1);

                if (!inBody) {

                    if (line.isEmpty()) {
                        inBody = true;
                        continue;
                    }

                    if (line.startsWith("Message-ID:")) {

                        messageId = parseIntSafe(
                                line.substring(
                                        "Message-ID:".length()));

                    } else if (line.startsWith("From:")) {

                        sender =
                                line.substring(5).trim();

                    } else if (line.startsWith("To:")) {

                        receiver =
                                line.substring(3).trim();

                    } else if (line.startsWith("Subject:")) {

                        subject =
                                line.substring(8).trim();

                    } else if (line.startsWith("Thread-ID:")) {

                        threadId = parseIntSafe(
                                line.substring(
                                        "Thread-ID:".length()));

                    } else if (line.startsWith("In-Reply-To:")) {

                        int replyId = parseIntSafe(
                                line.substring(
                                        "In-Reply-To:".length()));

                        if (replyId > 0) {
                            inReplyTo = replyId;
                        }
                    }

                } else {

                    body.append(line)
                            .append("\n");
                }
            }

            return new RawMessage(
                    defaultNumber,
                    messageId,
                    sender,
                    receiver,
                    subject,
                    body.toString().trim(),
                    threadId,
                    inReplyTo);
        }

        // ==================================================
        // UTIL
        // ==================================================

        private int parseCount(
                String response) {

            String[] parts =
                    response.split("\\s+");

            if (parts.length < 2) {
                return 0;
            }

            try {
                return Integer.parseInt(
                        parts[1]);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }

        private int parseIntSafe(
                String value) {

            try {
                return Integer.parseInt(
                        value.trim());
            } catch (Exception ex) {
                return 0;
            }
        }

        private void sendAndExpect(
                String command,
                String expectedPrefix)
                throws IOException {

            writer.println(command);

            String resp =
                    reader.readLine();

            if (resp == null ||
                    !resp.startsWith(expectedPrefix)) {

                throw new IOException(
                        "Command failed: "
                                + command
                                + " -> "
                                + resp);
            }
        }

        @Override
        public void close() {

            try {
                if (writer != null)
                    writer.println("QUIT");
            } catch (Exception ignored) {
            }

            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }
}
