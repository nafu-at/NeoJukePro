/*
 * Copyright 2020 NAFU_at.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.neojukepro.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DatabaseTable {
    private final String tablename;
    private final DatabaseConnector connector;

    public DatabaseTable(String prefix, String tablename, DatabaseConnector connector) {
        this.tablename = prefix + tablename;
        this.connector = connector;
    }

    protected String getTablename() {
        return tablename;
    }

    protected DatabaseConnector getConnector() {
        return connector;
    }

    /**
     * Creates a table with the specified structure.
     * If a table with the same name already exists, it exits without executing the process.
     *
     * @param construction Structure of the table to be created
     * @throws SQLException Thrown when creating a table fails.
     */
    public void createTable(String construction) throws SQLException {
        try (Connection connection = connector.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + tablename + " (" + construction + ")")) {
                ps.execute();
            }
        }
    }

    /**
     * Add a column to the table.
     *
     * @param name The name of the column to add
     * @param type Column data type
     * @throws SQLException Thrown when adding a column fails.
     */
    public void createTableColumn(String name, String type) throws SQLException {
        try (Connection connection = connector.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "ALTER TABLE " + tablename + " ADD " + name + " " + type)) {
                ps.execute();
            }
        }
    }

    /**
     * Add a column to the table.
     *
     * @param name         The name of the column to add
     * @param type         Column data type
     * @param defaultValue Column default value
     * @throws SQLException Thrown when adding a column fails.
     */
    public void createTableColumn(String name, String type, String defaultValue) throws SQLException {
        try (Connection connection = connector.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "ALTER TABLE " + tablename + " ADD " + name + " " + type + "NOT NULL DEFAULT" + defaultValue)) {
                ps.execute();
            }
        }
    }

    /**
     * Remove a column from a table.
     *
     * @param name The name of the column to delete
     * @throws SQLException Thrown when deleting a column fails.
     */
    public void dropTableColumn(String name) throws SQLException {
        try (Connection connection = connector.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "ALTER TABLE " + tablename + " DROP " + name)) {
                ps.execute();
            }
        }
    }
}
