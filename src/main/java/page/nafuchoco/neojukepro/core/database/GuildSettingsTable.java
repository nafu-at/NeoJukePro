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
 * ギルド設定が保持されるテーブルです。
 */
public class GuildSettingsTable extends DatabaseTable {

    public GuildSettingsTable(String prefix, String tablename, DatabaseConnector connector) {
        super(prefix, tablename, connector);
    }

    public GuildSettingsTable(String prefix, DatabaseConnector connector) {
        this(prefix, "guild_settings", connector);
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
     *     <tr><td>option_name</td><td>VARCHAR(32)</td><td>NOT NULL</td></tr>
     *     <tr><td>option_value</td><td>LONGTEXT</td><td>NOT NULL</td></tr>
     * </tbody></table>
     * <br>
     *
     * @throws SQLException テーブルの作成に失敗した場合にスローされます。
     */
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
     * 保存されているギルドをすべて取得します。
     *
     * @return 保存されているギルド一覧
     * @throws SQLException データの取得に失敗した場合にスローされます。
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
     * 保存されているギルド設定を取得します。
     *
     * @param guildId 取得するギルドのギルドID
     * @return 保存されているギルド設定
     * @throws SQLException ギルド設定の取得に失敗した場合にスローされます。
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
     * 保存されているギルド設定を取得します。
     *
     * @param guildId 取得するギルドのギルドID
     * @param name    取得するギルド設定の項目名
     * @return 保存されているギルド設定
     * @throws SQLException ギルド設定の取得に失敗した場合にスローされます。
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
     * ギルド設定を保存します。
     *
     * @param guildId ギルド設定を保存するギルドのギルドID
     * @param name    ギルド設定の項目名
     * @param value   ギルド設定の設定内容
     * @throws SQLException ギルド設定の保存に失敗したか、ギルドIDと設定項目名が同一のものが既に存在する場合にスローされます。
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
     * ギルドに紐付けられたすべてのギルド設定を削除します。
     *
     * @param guildId 削除するギルド設定のギルドID
     * @throws SQLException ギルド設定の削除に失敗した場合にスローされます。
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
     * ギルドに紐付けられたギルド設定を削除します。
     *
     * @param guildId 削除するギルド設定のギルドID
     * @param name    削除するギルド設定の項目
     * @throws SQLException ギルド設定の削除に失敗した場合にスローされます。
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
