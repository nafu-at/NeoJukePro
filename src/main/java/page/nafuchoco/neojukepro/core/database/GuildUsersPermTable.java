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

public class GuildUsersPermTable extends DatabaseTable {

    public GuildUsersPermTable(String tablename, DatabaseConnector connector) {
        super(tablename, connector);
    }

    public GuildUsersPermTable(DatabaseConnector connector) {
        this("guild_userperm", connector);
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
        super.createTable("guild_id BIGINT NOT NULL, user_id BIGINT NOT NULL, " +
                "permission_code TINYINT UNSIGNED NOT NULL");
        // ユニークインデックスを作成します。
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "CREATE UNIQUE INDEX userperm_index ON " + getTablename() + "(guild_id, user_id)")) {
            ps.execute();
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate key"))
                throw e;
        }
    }

    /**
     * Retrieves the user's permissions in the stored guild.
     *
     * @param guildId Guild ID of the guild to be obtained.
     * @param userId  User ID of the user to be obtained.
     * @return User permissions for a stored guild
     * @throws SQLException Thrown if the user privileges are not obtained.
     */
    public int getUserPermission(long guildId, long userId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT permission_code FROM " + getTablename() + " WHERE guild_id = ? AND user_id = ?")) {
            ps.setLong(1, guildId);
            ps.setLong(2, userId);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next())
                    return resultSet.getInt("permission_code");
                return -1;
            }
        }
    }

    /**
     * Save the user's permissions in the guild.
     *
     * @param guildId        Guild ID of the guild to store user privileges.
     * @param userId         User ID of the user to store user privileges.
     * @param permissionCode User permissions of the user to store
     * @throws SQLException Thrown if saving user privileges fails.
     */
    public void setUserPermission(long guildId, long userId, int permissionCode) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (guild_id, user_id, permission_code) VALUES (?, ?, ?)" +
                             "ON DUPLICATE KEY UPDATE permission_code = VALUES (permission_code)")) {
            ps.setLong(1, guildId);
            ps.setLong(2, userId);
            ps.setInt(3, permissionCode);
            ps.execute();
        }
    }

    /**
     * Removes all user permissions associated with the guild.
     *
     * @param guildId Guild ID of the guild to be removed.
     * @throws SQLException Thrown if the deletion of user privileges fails.
     */
    public void deleteGuildUsers(long guildId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE guild_id = ?")) {
            ps.setLong(1, guildId);
            ps.execute();
        }
    }

    /**
     * Removes user permissions for all guilds of the user.
     *
     * @param userId User ID of the user to be deleted.
     * @throws SQLException Thrown if the deletion of user privileges fails.
     */
    public void deleteUser(long userId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.execute();
        }
    }

    /**
     * Removes user permissions for users in the guild.
     *
     * @param guildId Guild ID of the guild to be removed.
     * @param userId  User ID of the user to be deleted.
     * @throws SQLException Thrown if the deletion of user privileges fails.
     */
    public void deleteGuildUser(long guildId, long userId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE guild_id = ? AND user_id = ?")) {
            ps.setLong(1, guildId);
            ps.setLong(2, userId);
            ps.execute();
        }
    }
}
