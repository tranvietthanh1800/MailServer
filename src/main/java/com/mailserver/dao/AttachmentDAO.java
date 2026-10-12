package com.mailserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mailserver.database.DatabaseConnection;
import com.mailserver.model.Attachment;

public class AttachmentDAO {

    /**
     * Lưu file đính kèm vào DB (đã gắn email).
     */
    public boolean saveAttachment(int emailId, Attachment attachment) {

        String sql = """
                INSERT INTO attachments
                (email_id, filename, file_size, content_type, file_data)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, emailId);
            ps.setString(2, attachment.getFilename());
            ps.setInt(3, attachment.getFileSize());
            ps.setString(4, attachment.getContentType());
            ps.setBytes(5, attachment.getFileData());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("===== SAVE ATTACHMENT ERROR =====");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ⭐ Lưu file "mồ côi" (chưa gắn email).
     * Dùng khi client upload file qua PUTATT trước khi gửi SMTP.
     * Trả về attachment id vừa tạo, hoặc -1 nếu lỗi.
     */
    public int insertOrphan(Attachment attachment) {

        String sql = """
                INSERT INTO attachments
                (email_id, filename, file_size, content_type, file_data)
                VALUES (NULL, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, attachment.getFilename());
            ps.setInt(2, attachment.getFileSize());
            ps.setString(3, attachment.getContentType());
            ps.setBytes(4, attachment.getFileData());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                return -1;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("===== INSERT ORPHAN ATTACHMENT ERROR =====");
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * ⭐ Gắn attachment đã upload (orphan) vào email.
     * Dùng bởi SMTPClientHandler sau khi email được lưu.
     * Trả về true nếu update thành công.
     */
    public boolean linkToEmail(int attachmentId, int emailId) {

        String sql = "UPDATE attachments SET email_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, emailId);
            ps.setInt(2, attachmentId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("===== LINK ATTACHMENT ERROR =====");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy metadata (KHÔNG có file_data) của các file trong 1 email.
     */
    public List<Attachment> getAttachmentsMeta(int emailId) {

        List<Attachment> list = new ArrayList<>();

        String sql = """
                SELECT id, email_id, filename, file_size, content_type
                FROM attachments
                WHERE email_id = ?
                ORDER BY id ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, emailId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Attachment a = new Attachment();
                    a.setId(rs.getInt("id"));
                    a.setEmailId(rs.getInt("email_id"));
                    a.setFilename(rs.getString("filename"));
                    a.setFileSize(rs.getInt("file_size"));
                    a.setContentType(rs.getString("content_type"));
                    list.add(a);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy đầy đủ data file (dùng khi download).
     */
    public Attachment getAttachmentData(int attachmentId) {

        String sql = """
                SELECT id, email_id, filename, file_size, content_type, file_data
                FROM attachments
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, attachmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Attachment a = new Attachment();
                    a.setId(rs.getInt("id"));
                    a.setEmailId(rs.getInt("email_id"));
                    a.setFilename(rs.getString("filename"));
                    a.setFileSize(rs.getInt("file_size"));
                    a.setContentType(rs.getString("content_type"));
                    a.setFileData(rs.getBytes("file_data"));
                    return a;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * ⭐ (Tuỳ chọn) Xóa các attachment "mồ côi" cũ.
     * Gọi định kỳ để dọn file upload nhưng không bao giờ được link vào email.
     * Ví dụ: xóa attachment có email_id IS NULL và created_at < NOW() - INTERVAL '1
     * day'
     */
    public int deleteOrphanAttachments() {

        String sql = """
                DELETE FROM attachments
                WHERE email_id IS NULL
                  AND created_at < NOW() - INTERVAL '1 day'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();

        } catch (SQLException e) {
            // Nếu bảng không có cột created_at, bỏ qua
            System.out.println("Cleanup orphan attachments skipped: " + e.getMessage());
            return 0;
        }
    }
}   