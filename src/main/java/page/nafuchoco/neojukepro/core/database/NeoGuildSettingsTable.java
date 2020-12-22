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

import com.google.gson.Gson;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.guild.NeoGuildPlayerOptions;
import page.nafuchoco.neojukepro.core.guild.NeoGuildSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NeoGuildSettingsTable extends DatabaseTable {
    private static final Gson gson = new Gson();

    private final NeoJukePro neoJukePro;

    public NeoGuildSettingsTable(NeoJukePro neoJukePro, String prefix, String tablename, DatabaseConnector connector) {
        super(prefix, tablename, connector);
        this.neoJukePro = neoJukePro;
    }

    public NeoGuildSettingsTable(NeoJukePro neoJukePro, String prefix, DatabaseConnector connector) {
        super(prefix, "neo_guild_settings", connector);
        this.neoJukePro = neoJukePro;
    }

    public void createTable() throws SQLException {
        super.createTable("guild_id BIGINT NOT NULL, " +
                "command_prefix VARCHAR(16) NULL, robot_mode BOOL, jukebox_mode BOOL, player_options LONGTEXT NOT NULL, custom_field LONGTEXT NULL");
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "CREATE UNIQUE INDEX settings_index ON " + getTablename() + "(guild_id)")) {
            ps.execute();
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate key"))
                throw e;
        }
    }

    /**
     * Get the saved guild settings.
     *
     * @param guildId Guild ID of the guild to be obtained.
     * @return Saved guild settings
     * @throws SQLException Thrown if the guild setting fails to be obtained.
     */
    public NeoGuildSettings getGuildSettings(long guildId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE guild_id = ?")) {
            ps.setLong(1, guildId);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    String commandPrefix = resultSet.getString("command_prefix");
                    boolean robotMode = resultSet.getBoolean("robot_mode");
                    boolean jukeboxMode = resultSet.getBoolean("jukebox_mode");
                    NeoGuildPlayerOptions playerOptions = gson.fromJson(resultSet.getString("player_options"), NeoGuildPlayerOptions.class);
                    NeoGuildSettings guildSettings = new NeoGuildSettings(neoJukePro, this, guildId, commandPrefix, robotMode, jukeboxMode, playerOptions);
                    guildSettings.deserializeCustomFieldFromJson(resultSet.getString("custom_field"));

                    return guildSettings;
                }
                return null;
            }
        }
    }

    /**
     * Register the guild settings.
     *
     * @param settings Guild configuration classes to save.
     * @throws SQLException Thrown if a guild setting failed to be saved.
     */
    public void registerGuildSettings(NeoGuildSettings settings) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (guild_id, command_prefix, robot_mode, jukebox_mode, player_options, custom_field) " +
                             "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, settings.getGuildId());
            ps.setString(2, settings.getCommandPrefix());
            ps.setBoolean(3, settings.isRobotMode());
            ps.setBoolean(4, settings.isJukeboxMode());
            ps.setString(5, gson.toJson(settings.getPlayerOptions()));
            ps.setString(6, settings.serializeCustomFieldToJson());
            ps.execute();
        }
    }

    public void updateCommandPrefixSetting(long guildId, String commandPrefix) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET command_prefix = ? WHERE guild_id = ?"
             )) {
            ps.setString(1, commandPrefix);
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updateRobotModeSetting(long guildId, boolean robotMode) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET robot_mode = ? WHERE guild_id = ?"
             )) {
            ps.setBoolean(1, robotMode);
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updateJukeboxModeSetting(long guildId, boolean jukeboxMode) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET jukebox_mode = ? WHERE guild_id = ?"
             )) {
            ps.setBoolean(1, jukeboxMode);
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updatePlayerOptions(long guildId, NeoGuildPlayerOptions playerOptions) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET player_options = ? WHERE guild_id = ?"
             )) {
            ps.setString(1, gson.toJson(playerOptions));
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updateCustomField(long guildId, String customField) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET custom_field = ? WHERE guild_id = ?"
             )) {
            ps.setString(1, customField);
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    /**
     * Remove guild settings tied to the guild.
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
}
