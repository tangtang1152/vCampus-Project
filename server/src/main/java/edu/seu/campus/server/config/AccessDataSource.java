package edu.seu.campus.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class AccessDataSource {

    @Value("${vcampus.db.path:")
    private String dbPath;

    @Bean
    public DataSource dataSource() throws Exception {
        // Use UCanAccess SimpleDataSource
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        String fallback = System.getProperty("user.dir") + java.io.File.separator +
                "src" + java.io.File.separator + "main" + java.io.File.separator +
                "resources" + java.io.File.separator + "database" + java.io.File.separator + "vCampus.accdb";
        String path = (dbPath != null && !dbPath.isBlank()) ? dbPath : fallback;
        String url = "jdbc:ucanaccess://" + path;

        org.apache.commons.dbcp2.BasicDataSource ds = new org.apache.commons.dbcp2.BasicDataSource();
        ds.setUrl(url);
        ds.setDriverClassName("net.ucanaccess.jdbc.UcanaccessDriver");
        ds.setInitialSize(1);
        ds.setMaxTotal(5);
        return ds;
    }
}


