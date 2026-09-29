package db;

import models.Challenge;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChallengeDAO {

    public int create(Challenge c) {
        String sql = "INSERT INTO challenges (room_id, title, problem, sample_input, sample_output, test_cases, constraints, created_by, sequence_no) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, c.getRoomId());
            stmt.setString(2, c.getTitle());
            stmt.setString(3, c.getProblem());
            stmt.setString(4, c.getSampleInput());
            stmt.setString(5, c.getSampleOutput());
            stmt.setString(6, c.getTestCases());
            stmt.setString(7, c.getConstraints());
            stmt.setInt(8, c.getCreatedBy());
            stmt.setInt(9, c.getSequenceNo() > 0 ? c.getSequenceNo() : 1);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.out.println("challenge create error: " + e.getMessage());
        }
        return -1;
    }

    public Challenge findByRoomIdAndSequence(int roomId, int sequenceNo) {
        String sql = "SELECT * FROM challenges WHERE room_id = ? AND sequence_no = ? LIMIT 1";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, roomId);
            stmt.setInt(2, sequenceNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.out.println("findByRoomIdAndSequence error: " + e.getMessage());
        }
        return null;
    }

    public int countByRoomId(int roomId) {
        String sql = "SELECT COUNT(*) FROM challenges WHERE room_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("countByRoomId error: " + e.getMessage());
        }
        return 0;
    }

    private Challenge map(ResultSet rs) throws SQLException {
        Challenge c = new Challenge();
        c.setId(rs.getInt("id"));
        c.setRoomId(rs.getInt("room_id"));
        c.setTitle(rs.getString("title"));
        c.setProblem(rs.getString("problem"));
        c.setSampleInput(rs.getString("sample_input"));
        c.setSampleOutput(rs.getString("sample_output"));
        c.setTestCases(rs.getString("test_cases"));
        c.setConstraints(rs.getString("constraints"));
        c.setCreatedBy(rs.getInt("created_by"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        try { c.setSequenceNo(rs.getInt("sequence_no")); } catch (Exception ignored) {}
        return c;
    }
}