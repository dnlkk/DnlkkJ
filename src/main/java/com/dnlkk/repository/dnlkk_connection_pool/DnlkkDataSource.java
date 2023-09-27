package com.dnlkk.repository.dnlkk_connection_pool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class DnlkkDataSource implements DataSource {    
    private final DnlkkCPConfig config;
    private final BlockingQueue<Connection> connectionPool;

    public DnlkkDataSource(DnlkkCPConfig config) {
        this.config = config;
        this.connectionPool = new ArrayBlockingQueue<>(config.getMaximumPoolSize());
        initializeConnectionPool();
    }

    private void initializeConnectionPool() {
        for (int i = 0; i < config.getMaximumPoolSize(); i++) {
            Connection connection = createConnection();
            if (connection != null) {
                connectionPool.add(connection);
            }
        }
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = connectionPool.poll();
        if (connection == null) {
            if (connectionPool.size() < config.getMaximumPoolSize()) {
                connection = createConnection();
            } else {
                throw new RuntimeException("Connection pool is full.");
            }
        }
        return connection;
    }

    public boolean releaseConnection(Connection connection) {
        if (connection != null)
           return connectionPool.offer(connection);
        return false;
    }

    public void closeAllConnections() throws SQLException {
        for (Connection connection : connectionPool) {
            connection.close();
        }
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParentLogger'");
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isWrapperFor'");
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unwrap'");
    }

    @Override
    public Connection getConnection(String arg0, String arg1) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConnection'");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }
    
    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }
    
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }
    
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }
    
}