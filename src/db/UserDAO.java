package db;

import models.User;

import java.sql.*;

public class UserDAO {

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("findByEmail error: " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("findById error: " + e.getMessage());
        }
        return null;
    }

    public int getTotalScore(int userId) {
        String sql = "SELECT COALESCE(SUM(score), 0) FROM submissions WHERE user_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("getTotalScore error: " + e.getMessage());
        }
        return 0;
    }

    public int getSolvedCount(int userId) {
        String sql = "SELECT COUNT(*) FROM submissions WHERE user_id = ? AND result = 'Pass'";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("getSolvedCount error: " + e.getMessage());
        }
        return 0;
    }

    public int getRoomsJoined(int userId) {
        String sql = "SELECT COUNT(*) FROM room_members WHERE user_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("getRoomsJoined error: " + e.getMessage());
        }
        return 0;
    }

    // rank based on total score
    public int getGlobalRank(int userId) {
        String sql =
                "SELECT ranked.room_rank FROM (" +
                        "  SELECT r.id, RANK() OVER (ORDER BY COALESCE(SUM(s.score),0) DESC) AS room_rank " +
                        "  FROM rooms r " +
                        "  LEFT JOIN challenges c ON c.room_id = r.id " +
                        "  LEFT JOIN submissions s ON s.challenge_id = c.id " +
                        "  GROUP BY r.id" +
                        ") ranked " +
                        "WHERE ranked.id = (" +
                        "  SELECT rm.room_id FROM room_members rm " +
                        "  LEFT JOIN challenges c2 ON c2.room_id = rm.room_id " +
                        "  LEFT JOIN submissions s2 ON s2.challenge_id = c2.id " +
                        "  WHERE rm.user_id = ? " +
                        "  GROUP BY rm.room_id " +
                        "  ORDER BY COALESCE(SUM(s2.score),0) DESC " +
                        "  LIMIT 1" +
                        ")";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return 0;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("room_rank");
        } catch (SQLException e) {
            System.out.println("getGlobalRank error: " + e.getMessage());
        }
        return 0;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}