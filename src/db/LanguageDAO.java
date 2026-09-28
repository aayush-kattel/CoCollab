package db;

import models.Language;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LanguageDAO {

    public List<Language> findAll() {
        List<Language> list = new ArrayList<>();
        String sql = "SELECT * FROM languages ORDER BY name";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Language l = new Language();
                l.setId(rs.getInt("id"));
                l.setName(rs.getString("name"));
                l.setExtension(rs.getString("extension"));
                l.setDefaultCode(rs.getString("default_code"));
                list.add(l);
            }
        } catch (SQLException e) {
            System.out.println("languages findAll error: " + e.getMessage());
        }
        return list;
    }

    public Language findByName(String name) {
        String sql = "SELECT * FROM languages WHERE name = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Language l = new Language();
                l.setId(rs.getInt("id"));
                l.setName(rs.getString("name"));
                l.setExtension(rs.getString("extension"));
                l.setDefaultCode(rs.getString("default_code"));
                return l;
            }
        } catch (SQLException e) {
            System.out.println("findByName language error: " + e.getMessage());
        }
        return null;
    }
}