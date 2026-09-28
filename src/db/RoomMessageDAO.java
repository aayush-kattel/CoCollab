package db;

import models.RoomMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomMessageDAO {

    public int create(int roomId, int userId, String message) {
        String sql = "INSERT INTO room_messages (room_id, user_id, message) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return -1;
            PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            stmt.setString(3, message);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.out.println("room message create error: " + e.getMessage());
        }
        return -1;
    }

    public List<RoomMessage> findHistoryByRoomId(int roomId) {
        List<RoomMessage> list = new ArrayList<>();
        String sql = "SELECT rm.id, rm.room_id, rm.user_id, rm.message, rm.sent_at, u.name " +
                "FROM room_messages rm " +
                "JOIN users u ON u.id = rm.user_id " +
                "WHERE rm.room_id = ? ORDER BY rm.sent_at ASC";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return list;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RoomMessage rm = new RoomMessage();
                rm.setId(rs.getInt("id"));
                rm.setRoomId(rs.getInt("room_id"));
                rm.setUserId(rs.getInt("user_id"));
                rm.setMessage(rs.getString("message"));
                rm.setSentAt(rs.getTimestamp("sent_at"));
                rm.setSenderName(rs.getString("name"));
                list.add(rm);
            }
        } catch (SQLException e) {
            System.out.println("room message history error: " + e.getMessage());
        }
        return list;
    }
}