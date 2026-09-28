package db;

import models.Submission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDAO {

    public int create(Submission s) {
        String sql = "INSERT INTO submissions (challenge_id, user_id, code, language_id, result, score) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, s.getChallengeId());
            stmt.setInt(2, s.getUserId());
            stmt.setString(3, s.getCode());
            stmt.setInt(4, s.getLanguageId());
            stmt.setString(5, s.getResult());
            stmt.setInt(6, s.getScore());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("submission create error: " + e.getMessage());
        }
        return -1;
    }

    public List<Submission> findByUser(int userId) {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, u.name AS user_name, l.name AS language_name, r.code AS room_code, r.topic AS topic " +
                "FROM submissions s " +
                "JOIN users u ON s.user_id = u.id " +
                "JOIN languages l ON s.language_id = l.id " +
                "JOIN challenges c ON s.challenge_id = c.id " +
                "JOIN rooms r ON c.room_id = r.id " +
                "WHERE s.user_id = ? ORDER BY s.submitted_at DESC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Submission s = new Submission();
                s.setId(rs.getInt("id"));
                s.setChallengeId(rs.getInt("challenge_id"));
                s.setUserId(rs.getInt("user_id"));
                s.setCode(rs.getString("code"));
                s.setLanguageId(rs.getInt("language_id"));
                s.setResult(rs.getString("result"));
                s.setScore(rs.getInt("score"));
                s.setSubmittedAt(rs.getTimestamp("submitted_at"));
                s.setUserName(rs.getString("user_name"));
                s.setLanguageName(rs.getString("language_name"));
                s.setRoomCode(rs.getString("room_code"));
                s.setTopic(rs.getString("topic"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.out.println("findByUser submissions error: " + e.getMessage());
        }
        return list;
    }

    public List<Object[]> getLeaderboard(int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT r.name, r.code, COALESCE(SUM(s.score),0) AS total_score " +
                "FROM rooms r " +
                "LEFT JOIN challenges c ON c.room_id = r.id " +
                "LEFT JOIN submissions s ON s.challenge_id = c.id " +
                "GROUP BY r.id, r.name, r.code " +
                "ORDER BY total_score DESC LIMIT ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{ rs.getString(1), rs.getString(2), rs.getInt(3) });
            }
        } catch (SQLException e) {
            System.out.println("getLeaderboard error: " + e.getMessage());
        }
        return list;
    }
    public boolean hasUserPassed(int userId, int challengeId) {
        String sql = "SELECT 1 FROM submissions WHERE user_id = ? AND challenge_id = ? AND result = 'Pass' LIMIT 1";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("hasUserPassed error: " + e.getMessage());
            return false;
        }
    }
}