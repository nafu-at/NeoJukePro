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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The table where the guild settings are held.
 */
public class GuildSettingsTable extends DatabaseTable {

    public GuildSettingsTable(String prefix, String tablename, DatabaseConnector connector) {
        super(prefix, tablename, connector);
    }

    public GuildSettingsTable(String prefix, DatabaseConnector connector) {
        this(prefix, "guild_settings", connector);
    }

    /**
     * @deprecated This method does not work in this class. Returns UnsupportedOperationException if it is executed.
     */
    @Override
    @Deprecated
    public void createTableColumn(String name, String type) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This method does not work in this class. Returns UnsupportedOperationException if it is executed.
     */
    @Override
    @Deprecated
    public void dropTableColumn(String name) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void createTable() throws SQLException {
        super.createTable("guild_id BIGINT NOT NULL, " +
                "option_name VARCHAR(32) NOT NULL, option_value LONGTEXT NOT NULL");
        // ユニークインデックスを作成します。
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "CREATE UNIQUE INDEX settings_index ON " + getTablename() + "(guild_id, option_name)")) {
            ps.execute();
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate key"))
                throw e;
        }
    }

    /**
     * Get all saved guilds.
     *
     * @return List of saved guilds
     * @throws SQLException Thrown if the data fails to be retrieved.
     */
    public List<Long> getGuilds() throws SQLException {
        List<Long> users = new ArrayList<>();
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT guild_id FROM " + getTablename())) {
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next())
                    users.add(resultSet.getLong("guild_id"));
                return users;
            }
        }
    }

    /**
     * Get the saved guild settings.
     *
     * @param guildId Guild ID of the guild to be obtained.
     * @return Saved guild settings
     * @throws SQLException Thrown if the guild setting fails to be obtained.
     */
    public Map<String, String> getGuildSettings(long guildId) throws SQLException {
        Map<String, String> map = new HashMap<>();
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE guild_id = ?")) {
            ps.setLong(1, guildId);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next())
                    map.put(resultSet.getString("option_name"), resultSet.getString("option_value"));
                return map;
            }
        }
    }

    /**
     * Retrieve the saved guild settings.
     *
     * @param guildId Guild ID of the guild to be obtained.
     * @param name    The name of the guild setting to get.
     * @return Saved guild settings
     * @throws SQLException Thrown if the guild setting fails to be obtained.
     */
    public String getGuildSetting(long guildId, String name) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT option_value FROM " + getTablename() + " WHERE guild_id = ? AND option_name = ?")) {
            ps.setLong(1, guildId);
            ps.setString(2, name);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next())
                    return resultSet.getString("option_value");
                return null;
            }
        }
    }

    /**
     * Save the guild settings.
     *
     * @param guildId Guild ID of the guild to store the guild settings.
     * @param name    Item name of guild setting.
     * @param value   The guild setting.
     * @throws SQLException Thrown if a guild setting failed to be saved, or if a guild ID and setting item name already exists that is identical.
     */
    public void setGuildSetting(long guildId, String name, String value) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (guild_id, option_name, option_value) VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE option_value = VALUES (option_value)")) {
            ps.setLong(1, guildId);
            ps.setString(2, name);
            ps.setString(3, value);
            ps.execute();
        }
    }

    /**
     * Removes all guild settings tied to the guild.
     *
     * @param guildId Guild ID of the guild setting to be removed.
     * @throws SQLException Thrown if the guild setting fails to be deleted.
     */
    public void deleteSettings(long guildId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE guild_id = ?")) {
            ps.setLong(1, guildId);
            ps.execute();
        }
    }

    /**
     * Removes the guild settings tied to the guild.
     *
     * @param guildId Guild ID of the guild setting to be removed.
     * @param name    The guild setting to delete.
     * @throws SQLException Thrown if the guild setting fails to be deleted.
     */
    public void deleteSetting(long guildId, String name) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE guild_id = ? AND option_name = ?")) {
            ps.setLong(1, guildId);
            ps.setString(2, name);
            ps.execute();
        }
    }
}
