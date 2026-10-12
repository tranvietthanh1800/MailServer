package com.mailserver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.mailserver.ServerConfig;

public class POP3Client {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public POP3Client(String username, String password) {
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

        public MessageInfo(int number, int size) {
            this.number = number;
            this.size = size;
        }
    }

    public static class AttachmentMeta {
        public final int id;
        public final String filename;
        public final String contentType;
        public final int size;

        public AttachmentMeta(int id, String filename, String contentType, int size) {
            this.id = id;
            this.filename = filename;
            this.contentType = contentType;
            this.size = size;
        }

        public String getReadableSize() {
            if (size < 1024)
                return size + " B";
            if (size < 1024 * 1024)
                return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024));
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
        public final boolean isRead;
        public final List<AttachmentMeta> attachments;

        public RawMessage(int number, int messageId, String sender, String receiver,
                String subject, String content, int threadId, Integer inReplyTo,
                boolean isRead, List<AttachmentMeta> attachments) {
            this.number = number;
            this.messageId = messageId;
            this.sender = sender;
            this.receiver = receiver;
            this.subject = subject;
            this.content = content;
            this.threadId = threadId;
            this.inReplyTo = inReplyTo;
            this.isRead = isRead;
            this.attachments = attachments;
        }

        public RawMessage(int number, int messageId, String sender, String receiver,
                String subject, String content, int threadId, Integer inReplyTo) {
            this(number, messageId, sender, receiver, subject, content, threadId,
                    inReplyTo, false, new ArrayList<>());
        }

        public RawMessage(int number, String sender, String receiver,
                String subject, String content) {
            this(number, number, sender, receiver, subject, content, number, null,
                    false, new ArrayList<>());
        }
    }

    // ==================================================
    // INBOX API
    // ==================================================

    public List<RawMessage> fetchInbox() throws IOException {
        List<RawMessage> messages = new ArrayList<>();
        try (Session session = openSession()) {
            List<MessageInfo> list = session.list();
            list.sort((a, b) -> Integer.compare(b.number, a.number));
            for (MessageInfo info : list) {
                messages.add(session.retr(info.number));
            }
        }
        return messages;
    }

    public boolean deleteMessage(int number) throws IOException {
        try (Session session = openSession()) {
            return session.dele(number);
        }
    }

    // ==================================================
    // CONVERSATION
    // ==================================================

    public List<RawMessage> fetchConversation(int threadId) throws IOException {
        try (Session session = openSession()) {
            return session.thread(threadId);
        }
    }

    // ==================================================
    // SENT
    // ==================================================

    public List<RawMessage> fetchSent() throws IOException {
        List<RawMessage> messages = new ArrayList<>();
        try (Session session = openSession()) {
            List<MessageInfo> list = session.sent();
            list.sort((a, b) -> Integer.compare(b.number, a.number));
            for (MessageInfo info : list) {
                messages.add(session.rtrs(info.number));
            }
        }
        return messages;
    }

    public List<MessageInfo> listSent() throws IOException {
        try (Session session = openSession()) {
            return session.sent();
        }
    }

    public RawMessage retrieveSent(int number) throws IOException {
        try (Session session = openSession()) {
            return session.rtrs(number);
        }
    }

    // ==================================================
    // ⭐ ATTACHMENT: DOWNLOAD
    // ==================================================

    public byte[] downloadAttachment(int attachmentId) throws IOException {
        try (Session session = openSession()) {
            return session.getAtt(attachmentId);
        }
    }

    // ==================================================
    // ⭐ ATTACHMENT: UPLOAD (MỚI THÊM)
    // ==================================================

    /**
     * Upload file lên server, trả về attachment ID.
     */
    public int uploadAttachment(String filename, String contentType, byte[] data)
            throws IOException {
        try (Session session = openSession()) {
            return session.putAtt(filename, contentType, data);
        }
    }

    // ==================================================
    // SESSION
    // ==================================================

    private Session openSession() throws IOException {
        Session s = new Session(host, port);
        s.login(username, password);
        return s;
    }

    // ==================================================
    // INNER SESSION
    // ==================================================

    private static class Session implements AutoCloseable {

        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;

        Session(String host, int port) throws IOException {
            this.socket = new Socket(host, port);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);

            String greeting = reader.readLine();
            if (greeting == null || !greeting.startsWith("+OK")) {
                throw new IOException("Server greeting failed: " + greeting);
            }
        }

        void login(String username, String password) throws IOException {
            sendAndExpect("USER " + username, "+OK");
            sendAndExpect("PASS " + password, "+OK");
        }

        // ==================================================
        // LIST
        // ==================================================

        List<MessageInfo> list() throws IOException {
            writer.println("LIST");
            String first = reader.readLine();
            if (first == null || !first.startsWith("+OK")) {
                throw new IOException("LIST failed: " + first);
            }

            List<MessageInfo> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("."))
                    break;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    result.add(new MessageInfo(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1])));
                }
            }
            return result;
        }

        // ==================================================
        // RETR
        // ==================================================

        RawMessage retr(int number) throws IOException {
            writer.println("RETR " + number);
            String first = reader.readLine();
            if (first == null || !first.startsWith("+OK")) {
                throw new IOException("RETR " + number + " failed: " + first);
            }
            return readSingleMessage(number, number);
        }

        // ==================================================
        // DELE
        // ==================================================

        boolean dele(int number) throws IOException {
            writer.println("DELE " + number);
            String resp = reader.readLine();
            return resp != null && resp.startsWith("+OK");
        }

        // ==================================================
        // THREAD
        // ==================================================

        List<RawMessage> thread(int threadId) throws IOException {
            writer.println("THREAD " + threadId);
            String first = reader.readLine();
            if (first == null || !first.startsWith("+OK")) {
                throw new IOException("THREAD " + threadId + " failed: " + first);
            }

            int count = parseCount(first);
            List<RawMessage> result = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                String marker = reader.readLine();
                if (!"---MESSAGE---".equals(marker)) {
                    throw new IOException("Invalid conversation marker: " + marker);
                }
                result.add(readSingleMessage(i + 1, null));
            }

            String finalDot = reader.readLine();
            if (!".".equals(finalDot)) {
                throw new IOException("THREAD response not terminated");
            }
            return result;
        }

        // ==================================================
        // SENT
        // ==================================================

        List<MessageInfo> sent() throws IOException {
            writer.println("SENT");
            String first = reader.readLine();
            if (first == null || !first.startsWith("+OK")) {
                throw new IOException("SENT failed: " + first);
            }

            List<MessageInfo> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("."))
                    break;
                String[] parts = line.split("\\s+", 4);
                if (parts.length >= 2) {
                    try {
                        result.add(new MessageInfo(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1])));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return result;
        }

        // ==================================================
        // RTRS
        // ==================================================

        RawMessage rtrs(int number) throws IOException {
            writer.println("RTRS " + number);
            String first = reader.readLine();
            if (first == null || !first.startsWith("+OK")) {
                throw new IOException("RTRS " + number + " failed: " + first);
            }
            return readSingleMessage(number, number);
        }

        // ==================================================
        // GETATT — download
        // ==================================================

        byte[] getAtt(int attachmentId) throws IOException {
            writer.println("GETATT " + attachmentId);
            String first = reader.readLine();
            if (first == null || !first.startsWith("+OK")) {
                throw new IOException("GETATT " + attachmentId + " failed: " + first);
            }

            StringBuilder base64 = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("."))
                    break;
                base64.append(line);
            }

            try {
                return java.util.Base64.getDecoder().decode(base64.toString());
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid base64 data: " + e.getMessage());
            }
        }

        // ==================================================
        // ⭐ PUTATT — upload (MỚI THÊM)
        // ==================================================

        int putAtt(String filename, String contentType, byte[] data) throws IOException {

            String base64 = java.util.Base64.getEncoder().encodeToString(data);

            // Bước 1: gửi header
            writer.println("PUTATT " + filename + "|" + contentType + "|" + data.length);

            String first = reader.readLine();
            if (first == null || !first.startsWith("+OK")) {
                throw new IOException("PUTATT failed: " + first);
            }

            // Bước 2: gửi base64 nhiều dòng, kết thúc bằng "."
            for (int i = 0; i < base64.length(); i += 76) {
                int end = Math.min(i + 76, base64.length());
                writer.println(base64.substring(i, end));
            }
            writer.println(".");

            // Bước 3: đọc "+OK <id>"
            String ok = reader.readLine();
            if (ok == null || !ok.startsWith("+OK")) {
                throw new IOException("PUTATT failed: " + ok);
            }

            String[] parts = ok.split("\\s+");
            if (parts.length < 2) {
                throw new IOException("PUTATT: missing id: " + ok);
            }

            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                throw new IOException("PUTATT: invalid id: " + ok);
            }
        }

        // ==================================================
        // READ SINGLE MESSAGE
        // ==================================================

        private RawMessage readSingleMessage(int defaultNumber, Integer fallbackMessageId)
                throws IOException {

            int messageId = fallbackMessageId == null ? defaultNumber : fallbackMessageId;
            int threadId = 0;
            Integer inReplyTo = null;
            String sender = "";
            String receiver = "";
            String subject = "";
            boolean isRead = false;
            List<AttachmentMeta> attachments = new ArrayList<>();
            StringBuilder body = new StringBuilder();
            boolean inBody = false;
            String line;

            while ((line = reader.readLine()) != null) {

                if (line.equals("."))
                    break;
                if (line.startsWith(".."))
                    line = line.substring(1);

                // ⭐ Parse Attachment BẤT KỂ vị trí (header hay body)
                if (line.startsWith("Attachment:")) {
                    String meta = line.substring("Attachment:".length()).trim();
                    String[] parts = meta.split("\\|");
                    if (parts.length >= 4) {
                        try {
                            int attId = Integer.parseInt(parts[0]);
                            String attName = parts[1];
                            String attType = parts[2];
                            int attSize = Integer.parseInt(parts[3]);
                            attachments.add(new AttachmentMeta(attId, attName, attType, attSize));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    continue; // ⭐ Bỏ qua, không append vào body
                }

                if (!inBody) {
                    if (line.isEmpty()) {
                        inBody = true;
                        continue;
                    }

                    if (line.startsWith("Message-ID:")) {
                        messageId = parseIntSafe(line.substring("Message-ID:".length()));
                    } else if (line.startsWith("From:")) {
                        sender = line.substring(5).trim();
                    } else if (line.startsWith("To:")) {
                        receiver = line.substring(3).trim();
                    } else if (line.startsWith("Subject:")) {
                        subject = line.substring(8).trim();
                    } else if (line.startsWith("Thread-ID:")) {
                        threadId = parseIntSafe(line.substring("Thread-ID:".length()));
                    } else if (line.startsWith("In-Reply-To:")) {
                        int replyId = parseIntSafe(line.substring("In-Reply-To:".length()));
                        if (replyId > 0)
                            inReplyTo = replyId;
                    } else if (line.startsWith("Is-Read:")) {
                        isRead = Boolean.parseBoolean(
                                line.substring("Is-Read:".length()).trim());
                    }
                } else {
                    body.append(line).append("\n");
                }
            }

            return new RawMessage(defaultNumber, messageId, sender, receiver, subject,
                    body.toString().trim(), threadId, inReplyTo, isRead, attachments);
        }

        // ==================================================
        // UTIL
        // ==================================================

        private int parseCount(String response) {
            String[] parts = response.split("\\s+");
            if (parts.length < 2)
                return 0;
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }

        private int parseIntSafe(String value) {
            try {
                return Integer.parseInt(value.trim());
            } catch (Exception ex) {
                return 0;
            }
        }

        private void sendAndExpect(String command, String expectedPrefix) throws IOException {
            writer.println(command);
            String resp = reader.readLine();
            if (resp == null || !resp.startsWith(expectedPrefix)) {
                throw new IOException("Command failed: " + command + " -> " + resp);
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