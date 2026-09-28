package db;

import models.Room;
import models.RoomMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public int create(Room room) {
        String sql = "INSERT INTO rooms (owner_id, name, code, topic, language, difficulty, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, room.getOwnerId());
            stmt.setString(2, room.getName());
            stmt.setString(3, room.getCode());
            stmt.setString(4, room.getTopic());
            stmt.setString(5, room.getLanguage());
            stmt.setString(6, room.getDifficulty());
            stmt.setString(7, room.getStatus() == null ? "waiting" : room.getStatus());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                keys.close();
                stmt.close();
                con.close();
                return id;
            }
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("create room failed: " + e.getMessage());
        }
        return -1;
    }

    public Room findByCode(String code) {
        String sql = "SELECT r.*, u.name AS owner_name, " +
                "(SELECT COUNT(*) FROM room_members rm WHERE rm.room_id = r.id) AS member_count " +
                "FROM rooms r JOIN users u ON r.owner_id = u.id WHERE r.code = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Room r = mapRoom(rs);
                rs.close();
                stmt.close();
                con.close();
                return r;
            }
        } catch (SQLException e) {
            System.out.println("findByCode failed: " + e.getMessage());
        }
        return null;
    }

    public Room findById(int id) {
        String sql = "SELECT r.*, u.name AS owner_name, " +
                "(SELECT COUNT(*) FROM room_members rm WHERE rm.room_id = r.id) AS member_count " +
                "FROM rooms r JOIN users u ON r.owner_id = u.id WHERE r.id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRoom(rs);
            }
        } catch (SQLException e) {
            System.out.println("findById failed: " + e.getMessage());
        }
        return null;
    }

    public boolean codeExists(String code) {
        String sql = "SELECT 1 FROM rooms WHERE code = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            stmt.close();
            con.close();
            return exists;
        } catch (SQLException e) {
            System.out.println("codeExists error: " + e.getMessage());
        }
        return false;
    }

    public List<Room> findByUser(int userId) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name AS owner_name, " +
                "(SELECT COUNT(*) FROM room_members rm2 WHERE rm2.room_id = r.id) AS member_count " +
                "FROM rooms r " +
                "JOIN room_members rm ON r.id = rm.room_id " +
                "JOIN users u ON r.owner_id = u.id " +
                "WHERE rm.user_id = ? ORDER BY r.created_at DESC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRoom(rs));
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("findByUser error: " + e.getMessage());
        }
        return list;
    }

    public List<Room> findActiveRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name AS owner_name, " +
                "(SELECT COUNT(*) FROM room_members rm WHERE rm.room_id = r.id) AS member_count " +
                "FROM rooms r JOIN users u ON r.owner_id = u.id " +
                "WHERE r.status IN ('waiting','active') ORDER BY r.created_at DESC LIMIT 50";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            System.out.println("findActiveRooms error: " + e.getMessage());
        }
        return list;
    }

    public boolean addMember(int roomId, int userId, String role) {
        if (isMember(roomId, userId)) {
            return true;
        }
        String sql = "INSERT INTO room_members (room_id, user_id, role) VALUES (?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            stmt.setString(3, role);
            int rows = stmt.executeUpdate();
            stmt.close();
            con.close();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("addMember failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isMember(int roomId, int userId) {
        String sql = "SELECT 1 FROM room_members WHERE room_id = ? AND user_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("isMember error: " + e.getMessage());
        }
        return false;
    }

    public List<RoomMember> getMembers(int roomId) {
        List<RoomMember> list = new ArrayList<>();
        String sql = "SELECT rm.*, u.name AS user_name FROM room_members rm " +
                "JOIN users u ON rm.user_id = u.id WHERE rm.room_id = ? ORDER BY rm.joined_at";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RoomMember m = new RoomMember();
                m.setId(rs.getInt("id"));
                m.setRoomId(rs.getInt("room_id"));
                m.setUserId(rs.getInt("user_id"));
                m.setRole(rs.getString("role"));
                m.setJoinedAt(rs.getTimestamp("joined_at"));
                m.setUserName(rs.getString("user_name"));
                list.add(m);
            }
        } catch (SQLException e) {
            System.out.println("getMembers error: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(int roomId, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateStatus error: " + e.getMessage());
            return false;
        }
    }

    private Room mapRoom(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt("id"));
        r.setOwnerId(rs.getInt("owner_id"));
        r.setName(rs.getString("name"));
        r.setCode(rs.getString("code"));
        r.setTopic(rs.getString("topic"));
        r.setLanguage(rs.getString("language"));
        r.setDifficulty(rs.getString("difficulty"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            r.setOwnerName(rs.getString("owner_name"));
        } catch (Exception e) {
            // column might not be there
        }
        try {
            r.setMemberCount(rs.getInt("member_count"));
        } catch (Exception e) {
        }
        try {
            r.setCurrentQuestion(rs.getInt("current_question"));
        } catch (Exception ignored) {
        }
        return r;
    }
    public boolean updateCurrentQuestion(int roomId, int questionNo) {
        String sql = "UPDATE rooms SET current_question = ? WHERE id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, questionNo);
            stmt.setInt(2, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateCurrentQuestion error: " + e.getMessage());
            return false;
        }
    }
}