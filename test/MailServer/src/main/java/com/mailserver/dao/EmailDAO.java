package com.mailserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.mailserver.database.DatabaseConnection;
import com.mailserver.model.Email;

public class EmailDAO {

    // =========================
    // SEND EMAIL
    // =========================
    public boolean sendEmail(Email email) {

        String sql = """
                INSERT INTO emails
                (sender, receiver, subject, content, is_read, is_deleted)
                VALUES (?, ?, ?, ?, FALSE, FALSE)
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.getSender());
            stmt.setString(2, email.getReceiver());
            stmt.setString(3, email.getSubject());
            stmt.setString(4, email.getContent());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // GET INBOX
    // =========================
    public List<Email> getInbox(String receiver) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT id, sender, receiver, subject,
                       content, is_read, is_deleted
                FROM emails
                WHERE receiver = ?
                AND is_deleted = FALSE
                ORDER BY id DESC
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, receiver);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Email email = new Email();

                email.setId(rs.getInt("id"));
                email.setSender(rs.getString("sender"));
                email.setReceiver(rs.getString("receiver"));
                email.setSubject(rs.getString("subject"));
                email.setContent(rs.getString("content"));
                email.setRead(rs.getBoolean("is_read"));
                email.setDeleted(rs.getBoolean("is_deleted"));

                emails.add(email);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return emails;
    }

    // =========================
    // GET EMAIL BY ID
    // =========================
    public Email getEmailById(int id, String receiver) {

        String sql = """
                SELECT id, sender, receiver, subject,
                       content, is_read, is_deleted
                FROM emails
                WHERE id = ?
                AND receiver = ?
                AND is_deleted = FALSE
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setString(2, receiver);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                Email email = new Email();

                email.setId(rs.getInt("id"));
                email.setSender(rs.getString("sender"));
                email.setReceiver(rs.getString("receiver"));
                email.setSubject(rs.getString("subject"));
                email.setContent(rs.getString("content"));
                email.setRead(rs.getBoolean("is_read"));
                email.setDeleted(rs.getBoolean("is_deleted"));

                return email;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================
    // MARK EMAIL AS READ
    // =========================
    public boolean markAsRead(int id, String receiver) {

        String sql = """
                UPDATE emails
                SET is_read = TRUE
                WHERE id = ?
                AND receiver = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setString(2, receiver);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // GET SENT MAIL
    // =========================
    public List<Email> getSentMails(String sender) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT id, sender, receiver, subject,
                       content, is_read, is_deleted
                FROM emails
                WHERE sender = ?
                AND is_deleted = FALSE
                ORDER BY id DESC
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sender);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Email email = new Email();

                email.setId(rs.getInt("id"));
                email.setSender(rs.getString("sender"));
                email.setReceiver(rs.getString("receiver"));
                email.setSubject(rs.getString("subject"));
                email.setContent(rs.getString("content"));
                email.setRead(rs.getBoolean("is_read"));
                email.setDeleted(rs.getBoolean("is_deleted"));

                emails.add(email);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return emails;
    }

    // =========================
    // DELETE MAIL - SOFT DELETE
    // =========================
    public boolean deleteMail(int id, String receiver) {

        String sql = """
                UPDATE emails
                SET is_deleted = TRUE
                WHERE id = ?
                AND receiver = ?
                AND is_deleted = FALSE
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setString(2, receiver);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // GET TRASH
    // =========================
    public List<Email> getTrash(String receiver) {

        List<Email> emails = new ArrayList<>();

        String sql = """
                SELECT id, sender, receiver, subject,
                       content, is_read, is_deleted
                FROM emails
                WHERE receiver = ?
                AND is_deleted = TRUE
                ORDER BY id DESC
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, receiver);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Email email = new Email();

                email.setId(rs.getInt("id"));
                email.setSender(rs.getString("sender"));
                email.setReceiver(rs.getString("receiver"));
                email.setSubject(rs.getString("subject"));
                email.setContent(rs.getString("content"));
                email.setRead(rs.getBoolean("is_read"));
                email.setDeleted(rs.getBoolean("is_deleted"));

                emails.add(email);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return emails;
    }

    // =========================
    // RESTORE MAIL
    // =========================
    public boolean restoreMail(int id, String receiver) {

        String sql = """
                UPDATE emails
                SET is_deleted = FALSE
                WHERE id = ?
                AND receiver = ?
                AND is_deleted = TRUE
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setString(2, receiver);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
