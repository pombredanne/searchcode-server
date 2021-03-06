/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabaseConfig implements IDatabaseConfig {

    private Connection connection = null;

    public synchronized Connection getConnection() throws SQLException {
        try {
            if(connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:./searchcode.sqlite");

                // WAL write ahead logging supposedly helps with performance but did not notice a difference
                // PreparedStatement stmt = connection.prepareStatement("PRAGMA journal_mode=WAL;");
                // stmt.execute();
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return connection;
    }
}
