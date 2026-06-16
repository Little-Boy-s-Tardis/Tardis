package com.antigravity.chatprocessor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Profile("!dev")
@Configuration
public class DatabaseConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
            log.info("Detected DATABASE_URL environment variable. Configuring datasource dynamically.");
            try {
                // Remove jdbc: if it was prepended by mistake, to parse it via URI
                String rawUrl = databaseUrl;
                if (rawUrl.startsWith("jdbc:")) {
                    rawUrl = rawUrl.substring(5);
                }
                
                // If it starts with postgresql:// or postgres://, parse it as a standard URI
                if (rawUrl.startsWith("postgresql://") || rawUrl.startsWith("postgres://")) {
                    URI dbUri = new URI(rawUrl);
                    String userInfo = dbUri.getUserInfo();
                    String username = "";
                    String password = "";
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        username = parts[0];
                        password = parts[1];
                    }
                    
                    // Host and Port
                    String host = dbUri.getHost();
                    int port = dbUri.getPort();
                    if (port == -1) {
                        port = 5432;
                    }
                    
                    // Database Name (path starts with /)
                    String path = dbUri.getPath();
                    
                    String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                    log.info("Configured JDBC URL: {}", jdbcUrl);
                    
                    return DataSourceBuilder.create()
                            .url(jdbcUrl)
                            .username(username)
                            .password(password)
                            .driverClassName("org.postgresql.Driver")
                            .build();
                } else if (databaseUrl.startsWith("jdbc:")) {
                    // Already in JDBC format
                    return DataSourceBuilder.create()
                            .url(databaseUrl)
                            .driverClassName("org.postgresql.Driver")
                            .build();
                }
            } catch (URISyntaxException | ArrayIndexOutOfBoundsException e) {
                log.error("Failed to parse DATABASE_URL: {}. Falling back to standard variables.", databaseUrl, e);
            }
        }
        
        log.info("DATABASE_URL not found or invalid. Using PG* environment variables fallback.");
        String host = System.getenv("PGHOST");
        if (host == null || host.trim().isEmpty()) {
            host = "localhost";
        }
        String port = System.getenv("PGPORT");
        if (port == null || port.trim().isEmpty()) {
            port = "5432";
        }
        String database = System.getenv("PGDATABASE");
        if (database == null || database.trim().isEmpty()) {
            database = "tardis";
        }
        String user = System.getenv("PGUSER");
        if (user == null || user.trim().isEmpty()) {
            user = "postgres";
        }
        String password = System.getenv("PGPASSWORD");
        if (password == null || password.trim().isEmpty()) {
            password = "password";
        }

        String dbUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        log.info("Configured fallback JDBC URL: {}", dbUrl);
        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(user)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
