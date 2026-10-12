package com.mailserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.mailserver.database.DatabaseConnection;
import com.mailserver.model.Email;

public class EmailDAO {

    // =========================================================
    // SEND EMAIL
    // =========================================================

    /**
     * Trả về ID email vừa insert, hoặc -1 nếu lỗi.
     */
    public int sendEmail(Email email) {

        String sql = """
                INSERT INTO emails
                (
                    sender,
                    receiver,
                    subject,
                    content,
                    is_read,
                    is_deleted,
                    thread_id,
                    in_reply_to
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, email.getSender());
            ps.setString(2, email.getReceiver());
            ps.setString(3, email.getSubject());
            ps.setString(4, email.getContent());

            ps.setBoolean(5, false);
            ps.setBoolean(6, false);

            if (email.getThreadId() > 0) {
                ps.setInt(7, email.getThreadId());
            } else {
                ps.setInt(7, 0);
            }

            if (email.getInReplyTo() != null) {
                ps.setInt(8, email.getInReplyTo());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            int affected = ps.executeUpdate();

            if (affected == 0) {
                return -1;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {

                    int newId = rs.getInt(1);
                    email.setId(newId);

                    // Mail mới → set thread_id = newId
                    if (email.getThreadId() <= 0) {

                        String updateSql = "UPDATE emails "
                                + "SET thread_id = ? "
                                + "WHERE id = ?";

                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {

                            updatePs.setInt(1, newId);
                            updatePs.setInt(2, newId);
                            updatePs.executeUpdate();
                        }

                        email.setThreadId(newId);
                    }

                    return newId; // ⭐ TRẢ VỀ ID
                }
            }

            return -1;

        } catch (SQLException e) {

            System.out.println("===== SEND EMAIL ERROR =====");
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();

            return -1;
        }
    }
    // =========================================================
    // GET INBOX - MAIL RIÊNG
    // =========================================================

    public List<Email> getInbox(
            String receiver) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    sender,
                    receiver,
                    subject,
                    content,
                    is_read,
                    is_deleted,
                    sent_at,
                    thread_id,
                    in_reply_to
                FROM emails
                WHERE receiver = ?
                  AND is_deleted = FALSE
                ORDER BY sent_at DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, receiver);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    emails.add(
                            mapEmail(rs));
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return emails;
    }

    // =========================================================
    // GET INBOX THREADS
    // =========================================================

    public List<Email> getInboxThreads(
            String userEmail) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT
                    e.id,
                    e.sender,
                    e.receiver,
                    e.subject,
                    e.content,
                    e.is_read,
                    e.is_deleted,
                    e.sent_at,
                    e.thread_id,
                    e.in_reply_to
                FROM emails e
                WHERE e.id = (
                    SELECT MAX(e2.id)
                    FROM emails e2
                    WHERE e2.thread_id = e.thread_id
                      AND e2.is_deleted = FALSE
                      AND (
                          e2.sender = ?
                          OR e2.receiver = ?
                      )
                )
                AND e.is_deleted = FALSE
                AND EXISTS (
                    SELECT 1
                    FROM emails incoming
                    WHERE incoming.thread_id = e.thread_id
                      AND incoming.receiver = ?
                      AND incoming.is_deleted = FALSE
                )
                ORDER BY e.id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userEmail);
            ps.setString(2, userEmail);
            ps.setString(3, userEmail);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    emails.add(
                            mapEmail(rs));
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return emails;
    }

    // =========================================================
    // GET SENT THREADS
    // =========================================================

    public List<Email> getSentThreads(
            String userEmail) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT
                    e.id,
                    e.sender,
                    e.receiver,
                    e.subject,
                    e.content,
                    e.is_read,
                    e.is_deleted,
                    e.sent_at,
                    e.thread_id,
                    e.in_reply_to
                FROM emails e
                WHERE e.id = (
                    SELECT MAX(e2.id)
                    FROM emails e2
                    WHERE e2.thread_id = e.thread_id
                      AND e2.is_deleted = FALSE
                      AND (
                          e2.sender = ?
                          OR e2.receiver = ?
                      )
                )
                AND e.is_deleted = FALSE
                AND EXISTS (
                    SELECT 1
                    FROM emails sent
                    WHERE sent.thread_id = e.thread_id
                      AND sent.sender = ?
                      AND sent.is_deleted = FALSE
                )
                ORDER BY e.id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userEmail);
            ps.setString(2, userEmail);
            ps.setString(3, userEmail);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    emails.add(
                            mapEmail(rs));
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return emails;
    }

    // =========================================================
    // GET CONVERSATION
    // =========================================================

    public List<Email> getConversation(
            int threadId,
            String userEmail) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    sender,
                    receiver,
                    subject,
                    content,
                    is_read,
                    is_deleted,
                    sent_at,
                    thread_id,
                    in_reply_to
                FROM emails
                WHERE thread_id = ?
                  AND is_deleted = FALSE
                  AND (
                      sender = ?
                      OR receiver = ?
                  )
                ORDER BY id ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, threadId);
            ps.setString(2, userEmail);
            ps.setString(3, userEmail);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    emails.add(
                            mapEmail(rs));
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return emails;
    }

    // =========================================================
    // MARK CONVERSATION AS READ
    // =========================================================

    public boolean markConversationAsRead(
            int threadId,
            String receiver) {

        String sql = """
                UPDATE emails
                SET is_read = TRUE
                WHERE thread_id = ?
                  AND receiver = ?
                  AND is_deleted = FALSE
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, threadId);
            ps.setString(2, receiver);

            ps.executeUpdate();

            return true;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // GET EMAIL BY ID
    // =========================================================

    public Email getEmailById(
            int id,
            String receiver) {

        String sql = """
                SELECT
                    id,
                    sender,
                    receiver,
                    subject,
                    content,
                    is_read,
                    is_deleted,
                    sent_at,
                    thread_id,
                    in_reply_to
                FROM emails
                WHERE id = ?
                  AND receiver = ?
                  AND is_deleted = FALSE
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, receiver);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapEmail(rs);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }

    // =========================================================
    // MARK AS READ
    // =========================================================

    public boolean markAsRead(
            int id,
            String receiver) {

        String sql = """
                UPDATE emails
                SET is_read = TRUE
                WHERE id = ?
                  AND receiver = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, receiver);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    public boolean markAsUnread(int id, String receiver) {

        String sql = """
                    UPDATE emails
                    SET is_read = 0
                    WHERE id = ? AND receiver = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, receiver);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // SENT MAIL
    // =========================================================

    public List<Email> getSentMails(
            String sender) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    sender,
                    receiver,
                    subject,
                    content,
                    is_read,
                    is_deleted,
                    sent_at,
                    thread_id,
                    in_reply_to
                FROM emails
                WHERE sender = ?
                  AND is_deleted = FALSE
                ORDER BY sent_at DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sender);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    emails.add(
                            mapEmail(rs));
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return emails;
    }

    // =========================================================
    // DELETE
    // =========================================================

    public boolean deleteMail(
            int id,
            String receiver) {

        String sql = """
                UPDATE emails
                SET is_deleted = TRUE
                WHERE id = ?
                  AND receiver = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, receiver);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // TRASH
    // =========================================================

    public List<Email> getTrash(
            String receiver) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    sender,
                    receiver,
                    subject,
                    content,
                    is_read,
                    is_deleted,
                    sent_at,
                    thread_id,
                    in_reply_to
                FROM emails
                WHERE receiver = ?
                  AND is_deleted = TRUE
                ORDER BY sent_at DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, receiver);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    emails.add(
                            mapEmail(rs));
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return emails;
    }

    // =========================================================
    // RESTORE
    // =========================================================

    public boolean restoreMail(
            int id,
            String receiver) {

        String sql = """
                UPDATE emails
                SET is_deleted = FALSE
                WHERE id = ?
                  AND receiver = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, receiver);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // GET ALL EMAILS
    // =========================================================

    public List<Email> getAllEmails() {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    sender,
                    receiver,
                    subject,
                    content,
                    is_read,
                    is_deleted,
                    sent_at,
                    thread_id,
                    in_reply_to
                FROM emails
                ORDER BY sent_at DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                emails.add(
                        mapEmail(rs));
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return emails;
    }

    // =========================================================
    // MAP RESULTSET -> EMAIL
    // =========================================================

    private Email mapEmail(
            ResultSet rs)
            throws SQLException {

        Email email = new Email();

        email.setId(
                rs.getInt("id"));

        email.setSender(
                rs.getString("sender"));

        email.setReceiver(
                rs.getString("receiver"));

        email.setSubject(
                rs.getString("subject"));

        email.setContent(
                rs.getString("content"));

        email.setRead(
                rs.getBoolean("is_read"));

        email.setDeleted(
                rs.getBoolean("is_deleted"));

        Timestamp timestamp = rs.getTimestamp("sent_at");

        if (timestamp != null) {

            email.setSentAt(
                    timestamp
                            .toLocalDateTime());
        }

        email.setThreadId(
                rs.getInt("thread_id"));

        int replyId = rs.getInt("in_reply_to");

        if (!rs.wasNull()) {

            email.setInReplyTo(
                    replyId);
        }

        return email;
    }

    // ============================================================
    // STARRED EMAIL
    // ============================================================

    /**
     * Đánh dấu email là Star.
     */
    public boolean starEmail(int emailId, String username) {

        String sql = """
                INSERT INTO starred_emails (email_id, username)
                VALUES (?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, emailId);
            ps.setString(2, username);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Bỏ đánh dấu Star.
     */
    public boolean unstarEmail(int emailId, String username) {

        String sql = """
                DELETE FROM starred_emails
                WHERE email_id = ?
                  AND username = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, emailId);
            ps.setString(2, username);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra email đã được Star hay chưa.
     */
    public boolean isStarred(int emailId, String username) {

        String sql = """
                SELECT 1
                FROM starred_emails
                WHERE email_id = ?
                  AND username = ?
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, emailId);
            ps.setString(2, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách email đã Star của user.
     */
    public List<Email> getStarredEmails(String username) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT e.*
                FROM emails e
                INNER JOIN starred_emails s
                    ON e.id = s.email_id
                WHERE s.username = ?
                  AND e.is_deleted = FALSE
                ORDER BY s.created_at DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Email email = new Email();

                    email.setId(rs.getInt("id"));
                    email.setSender(rs.getString("sender"));
                    email.setReceiver(rs.getString("receiver"));
                    email.setSubject(rs.getString("subject"));
                    email.setContent(rs.getString("content"));

                    email.setThreadId(rs.getInt("thread_id"));
                    email.setInReplyTo(rs.getInt("in_reply_to"));

                    emails.add(email);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return emails;
    }

    public boolean isRead(int emailId, String userEmail) {

        String sql = """
                    SELECT is_read
                    FROM emails
                    WHERE id = ?
                    AND receiver = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, emailId);
            ps.setString(2, userEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_read");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}