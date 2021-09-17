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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class NeoJukeTable extends DatabaseTable {

    public NeoJukeTable(String prefix, DatabaseConnector connector) {
        super(prefix, "neojuke", connector);
    }

    public void createTable() throws SQLException {
        super.createTable("options_key VARCHAR(128) NOT NULL, option_value VARCHAR(1024) NOT NULL");
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "CREATE UNIQUE INDEX options_index ON " + getTablename() + "(options_key)")) {
            ps.execute();
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate key"))
                throw e;
        }
    }

    /**
     * Get all saved options.
     *
     * @return Map of saved Options.
     * @throws SQLException Thrown if the data fails to be retrieved.
     */
    public Map<String, String> getOptions() throws SQLException {
        Map<String, String> options = new HashMap<>();
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename())) {
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next())
                    options.put(resultSet.getString("options_key"), resultSet.getString("option_value"));
                return options;
            }
        }
    }

    /**
     * Get the saved option.
     *
     * @return Saved option value
     * @throws SQLException Thrown if the data fails to be retrieved.
     */
    public String getOption(String key) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT option_value FROM " + getTablename() + " WHERE options_key = ?")) {
            ps.setString(1, key);
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next())
                    return resultSet.getString("option_value");
                return null;
            }
        }
    }

    /**
     * Save the guild settings.
     *
     * @param key   Item name of option.
     * @param value The option value.
     * @throws SQLException Thrown if a option failed to be saved.
     */
    public void setOption(String key, String value) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (options_key, option_value) VALUES (?, ?) " +
                             "ON DUPLICATE KEY UPDATE option_value = VALUES (option_value)")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.execute();
        }
    }

    /**
     * Removes the option.
     *
     * @param key The option to delete.
     * @throws SQLException Thrown if the option fails to be deleted.
     */
    public void deleteSetting(String key) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE options_key = ?")) {
            ps.setString(1, key);
            ps.execute();
        }
    }
}
