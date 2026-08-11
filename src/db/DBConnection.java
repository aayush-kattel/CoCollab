package db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new RuntimeException("config.properties not found on classpath (expected at src/config.properties)");
            }
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("db.url");
            user = p.getProperty("db.user");
            password = p.getProperty("db.password");
        } catch (Exception e) {
            System.out.println("Could not load config.properties");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        }
        return con;
    }
}