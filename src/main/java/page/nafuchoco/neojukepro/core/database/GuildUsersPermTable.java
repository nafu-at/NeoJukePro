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

    public GuildUsersPermTable(String prefix, String tablename, DatabaseConnector connector) {
        super(prefix, tablename, connector);
    }

    public GuildUsersPermTable(String prefix, DatabaseConnector connector) {
        super(prefix, "guild_userperm", connector);
    }

    /**
     * @deprecated このクラスではこのメソッドは動作しません。実行された場合はUnsupportedOperationExceptionを返します。
     */
    @Override
    @Deprecated
    public void createTableColumn(String name, String type) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated このクラスではこのメソッドは動作しません。実行された場合はUnsupportedOperationExceptionを返します。
     */
    @Override
    @Deprecated
    public void dropTableColumn(String name) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * 以下の形式に基づいたテーブルが作成されます。<br>
     * <table><tbody>
     *     <tr><td>Name</td><td>Type</td><td>Null</td></tr>
     *     <tr><td>guild_id</td><td>BIGINT</td><td>NOT NULL</td></tr>
     *     <tr><td>user_id</td><td>BIGINT</td><td>NOT NULL</td></tr>
     *     <tr><td>permission_code</td><td>TINYINT UNSIGNED</td><td>NOT NULL</td></tr>
     * </tbody></table>
     * <br>
     *
     * @throws SQLException テーブルの作成に失敗した場合にスローされます。
     */
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
     * 保存されているギルドでのユーザーの権限を取得します。
     *
     * @param guildId 取得するギルドのギルドID
     * @param userId  取得するユーザーのユーザーID
     * @return 保存されているギルドでのユーザーの権限
     * @throws SQLException ユーザー権限の取得に失敗した場合にスローされます。
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
     * ギルドでのユーザーの権限を保存します。
     *
     * @param guildId        ユーザー権限を保存するギルドのギルドID
     * @param userId         ユーザー権限を保存するユーザーのユーザーID
     * @param permissionCode 保存するユーザーのユーザー権限
     * @throws SQLException ユーザー権限の保存に失敗した場合にスローされます。
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
     * ギルドに紐付けられたすべてのユーザー権限を削除します。
     *
     * @param guildId 削除するギルドのギルドID
     * @throws SQLException ユーザー権限の削除に失敗した場合にスローされます。
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
     * ユーザーの全てのギルドのユーザー権限を削除します。
     *
     * @param userId 削除するユーザーのユーザーID
     * @throws SQLException ユーザー権限の削除に失敗した場合にスローされます。
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
     * ギルドのユーザーのユーザー権限を削除します。
     *
     * @param guildId 削除するギルドのギルドID
     * @param userId  削除するユーザーのユーザーID
     * @throws SQLException ユーザー権限の削除に失敗した場合にスローされます。
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
