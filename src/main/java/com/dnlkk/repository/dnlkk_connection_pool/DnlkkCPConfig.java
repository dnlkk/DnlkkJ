package com.dnlkk.repository.dnlkk_connection_pool;

import com.dnlkk.boot.AppConfig;

import lombok.Data;

@Data
public class DnlkkCPConfig {
    private String driverClass;
    private String jdbcUrl;
    private String username;
    private String password;
    private Integer maximumPoolSize;

    public DnlkkCPConfig() {
        driverClass = AppConfig.getProperty("app.datasource.driver");
        jdbcUrl = AppConfig.getProperty("app.datasource.url");
        username = AppConfig.getProperty("app.datasource.username");
        password = AppConfig.getProperty("app.datasource.password");
        setMaximumPoolSize(10);
    }

}