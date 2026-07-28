package auth;

import db.DBConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public boolean register(String name, String email, String password) {
        String hashed = hashPassword(password);
        // role default 'user'; status default 'offline' (matches users.status: online/offline/busy/banned)
        String sql = "INSERT INTO users (name, email, password, role, status) VALUES (?, ?, ?, 'user', 'offline')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, hashed);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Register failed: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String email, String password) {
        String hashed = hashPassword(password);
        String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND status != 'banned'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, hashed);
            ResultSet rs = stmt.executeQuery();

            boolean success = rs.next();
            if (success) {
                markOnline(email);
            }
            return success;

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    private void markOnline(String email) {
        String sql = "UPDATE users SET status = 'online' WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Could not update online status: " + e.getMessage());
        }
    }

    public void markOffline(String email) {
        String sql = "UPDATE users SET status = 'offline' WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Could not update offline status: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }
}