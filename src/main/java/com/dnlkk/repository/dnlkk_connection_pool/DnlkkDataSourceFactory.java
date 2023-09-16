package com.dnlkk.repository.dnlkk_connection_pool;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnlkkDataSourceFactory {
    private static final DnlkkCPConfig config = new DnlkkCPConfig();
    private static boolean isFirstConnect = true;
    private static final Logger logger = LoggerFactory.getLogger(DnlkkDataSourceFactory.class);

    public static DataSource createDataSource() {
        DataSource dataSource = new DnlkkDataSource(config);
        
        try {
            Class.forName(config.getDriverClass());
        } catch (ClassNotFoundException e) {
            logger.error("DnlkkCP failed to load database driver");
            throw new RuntimeException("Failed to load database driver", e);
        }
        
        try (Connection connection = dataSource.getConnection()) {
            if (isFirstConnect) {
                logger.info("DnlkkCP successfully connected with database");
                isFirstConnect = false;
            }
        } catch (SQLException e) {
            logger.info("DnlkkCP failed to connect with database");
            e.printStackTrace();
            System.exit(-1);
        }
        
        return dataSource;
    }
}