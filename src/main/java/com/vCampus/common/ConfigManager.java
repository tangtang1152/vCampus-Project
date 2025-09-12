package com.vCampus.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置管理类
 * 统一管理应用程序配置
 */
public class ConfigManager {
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config/application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getDatabasePath() {
        return properties.getProperty("database.path", "database/vCampus.accdb");
    }
    
    public static String getAppTitle() {
        return properties.getProperty("app.title", "vCampus系统");
    }
    
    public static int getAppWidth() {
        return Integer.parseInt(properties.getProperty("app.width", "800"));
    }
    
    public static int getAppHeight() {
        return Integer.parseInt(properties.getProperty("app.height", "600"));
    }
}