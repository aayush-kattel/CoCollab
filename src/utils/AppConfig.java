package utils;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static Properties props = new Properties();

    static {
        try {
            InputStream in = AppConfig.class.getResourceAsStream("/config.properties");
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.out.println("Could not load config.properties");
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key, "");
    }
}