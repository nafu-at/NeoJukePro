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
import com.google.gson.reflect.TypeToken;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.guild.NeoGuildPlayerOptions;
import page.nafuchoco.neojukepro.core.guild.NeoGuildSettings;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        super.createTable("guild_id BIGINT NOT NULL, lang VARCHAR(5) NOT NULL, " +
                "command_prefix VARCHAR(16) NULL, robot_mode BOOL, jukebox_mode BOOL, disable_commandgroup LONGTEXT NULL," +
                "player_options LONGTEXT NOT NULL, custom_field LONGTEXT NULL");
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "CREATE UNIQUE INDEX settings_index ON " + getTablename() + "(guild_id)")) {
            ps.execute();
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate key"))
                throw e;
        }
    }

    /**
     * Get the list of guilds where the settings are saved.
     *
     * @return List of guilds where settings are saved.
     * @throws SQLException Thrown if the guild setting fails to be obtained.
     * @since v2.0
     */
    public List<Long> getGuilds() throws SQLException {
        List<Long> guilds = new ArrayList<>();
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT guild_id FROM " + getTablename())) {
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    guilds.add(resultSet.getLong("guild_id"));
                }
                return guilds;
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
    public NeoGuildSettings getGuildSettings(long guildId) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE guild_id = ?")) {
            ps.setLong(1, guildId);
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    var lang = resultSet.getString("lang");
                    var commandPrefix = resultSet.getString("command_prefix");
                    var robotMode = resultSet.getBoolean("robot_mode");
                    var jukeboxMode = resultSet.getBoolean("jukebox_mode");
                    List<String> disableCommandGroup = gson.fromJson(resultSet.getString("disable_commandgroup"), new TypeToken<List<String>>() {
                    }.getType());
                    if (disableCommandGroup == null)
                        disableCommandGroup = new ArrayList();
                    NeoGuildPlayerOptions playerOptions =
                            gson.fromJson(resultSet.getString("player_options"), NeoGuildPlayerOptions.class);

                    var guildSettings = new NeoGuildSettings(
                            neoJukePro,
                            this,
                            guildId,
                            lang,
                            commandPrefix,
                            robotMode,
                            jukeboxMode,
                            disableCommandGroup,
                            playerOptions);
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
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (guild_id, lang, command_prefix, robot_mode, jukebox_mode, disable_commandgroup, player_options, custom_field) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, settings.getGuildId());
            ps.setString(2, settings.getLang());
            ps.setString(3, settings.getCommandPrefix());
            ps.setBoolean(4, settings.isRobotMode());
            ps.setBoolean(5, settings.isJukeboxMode());
            ps.setString(6, gson.toJson(settings.getDisableCommandGroup()));
            ps.setString(7, gson.toJson(settings.getPlayerOptions()));
            ps.setString(8, settings.serializeCustomFieldToJson());
            ps.execute();
        }
    }

    public void updateCommandPrefixSetting(long guildId, String commandPrefix) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET command_prefix = ? WHERE guild_id = ?"
             )) {
            ps.setString(1, commandPrefix);
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updateLanguageSetting(long guildId, String lang) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET lang = ? WHERE guild_id = ?"
             )) {
            ps.setString(1, lang);
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updateRobotModeSetting(long guildId, boolean robotMode) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET robot_mode = ? WHERE guild_id = ?"
             )) {
            ps.setBoolean(1, robotMode);
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updateJukeboxModeSetting(long guildId, boolean jukeboxMode) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET jukebox_mode = ? WHERE guild_id = ?"
             )) {
            ps.setBoolean(1, jukeboxMode);
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updateDisableCommandGroup(long guildId, List<String> disableCommandGroup) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET disable_commandgroup = ? WHERE guild_id = ?"
             )) {
            ps.setString(1, gson.toJson(disableCommandGroup));
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updatePlayerOptions(long guildId, NeoGuildPlayerOptions playerOptions) throws SQLException {
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET player_options = ? WHERE guild_id = ?"
             )) {
            ps.setString(1, gson.toJson(playerOptions));
            ps.setLong(2, guildId);
            ps.execute();
        }
    }

    public void updateCustomField(long guildId, String customField) throws SQLException {
        try (var connection = getConnector().getConnection();
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
        try (var connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE guild_id = ?")) {
            ps.setLong(1, guildId);
            ps.execute();
        }
    }
}
