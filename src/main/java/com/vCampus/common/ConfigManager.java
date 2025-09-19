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
            } else {
                // 使用默认配置
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("配置文件加载失败，使用默认配置");
            setDefaultProperties();
        }
    }
    
    private static void setDefaultProperties() {
        properties.setProperty("database.path", "src/main/resources/database/vCampus.accdb");
        properties.setProperty("app.title", "vCampus系统");
        properties.setProperty("app.width", "1000");
        properties.setProperty("app.height", "700");
        properties.setProperty("api.baseUrl", "http://127.0.0.1:8080/api/v1");
    }
    
    public static String getDatabasePath() {
        return properties.getProperty("database.path");
    }
    
    public static String getAppTitle() {
        return properties.getProperty("app.title");
    }
    
    public static int getAppWidth() {
        return Integer.parseInt(properties.getProperty("app.width", "800"));
    }
    
    public static int getAppHeight() {
        return Integer.parseInt(properties.getProperty("app.height", "600"));
    }

    public static String getApiBaseUrl() {
        return properties.getProperty("api.baseUrl", "http://127.0.0.1:8080/api/v1");
    }
}