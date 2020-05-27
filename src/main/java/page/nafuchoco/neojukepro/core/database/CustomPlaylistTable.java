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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import page.nafuchoco.neojukepro.core.playlist.CustomPlaylist;
import page.nafuchoco.neojukepro.core.playlist.PlaylistItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomPlaylistTable extends DatabaseTable {
    private static final ObjectMapper mapper = new ObjectMapper();

    public CustomPlaylistTable(String prefix, DatabaseConnector connector) {
        super(prefix, "playlist", connector);
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
        super.createTable("uuid CHAR(36) NOT NULL PRIMARY KEY, guild_id BIGINT NOT NULL, list_name TINYTEXT NOT NULL, " +
                "trucks LONGTEXT NOT NULL");
    }

    public List<CustomPlaylist> getGuildPlaylists(long guildId) throws SQLException, JsonProcessingException {
        List<CustomPlaylist> results = new ArrayList();
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE guild_id = ?")) {
            ps.setLong(1, guildId);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    String uuid = resultSet.getString("uuid");
                    String listName = resultSet.getString("list_name");
                    List<PlaylistItem> trucks = mapper.readValue(resultSet.getString("trucks"), new TypeReference<>() {
                    });
                    CustomPlaylist playlist = new CustomPlaylist(uuid, guildId, listName, trucks);
                    results.add(playlist);
                }
            }
        }
        return results;
    }

    public CustomPlaylist getPlaylist(String uuid) throws SQLException, JsonProcessingException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE uuid = ?")) {
            ps.setString(1, uuid);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    long guildId = resultSet.getLong("guild_id");
                    String listName = resultSet.getString("list_name");
                    List<PlaylistItem> trucks = mapper.readValue(resultSet.getString("trucks"), new TypeReference<>() {
                    });
                    CustomPlaylist playlist = new CustomPlaylist(uuid, guildId, listName, trucks);
                    return playlist;
                }
                return null;
            }
        }
    }

    public List<CustomPlaylist> searchPlaylist(long guildId, String name) throws SQLException, JsonProcessingException {
        List<CustomPlaylist> results = new ArrayList();
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE guild_id = ? AND list_name LIKE ?")) {
            ps.setLong(1, guildId);
            ps.setString(2, name);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    String uuid = resultSet.getString("uuid");
                    String listName = resultSet.getString("list_name");
                    List<PlaylistItem> trucks = mapper.readValue(resultSet.getString("trucks"), new TypeReference<>() {
                    });
                    CustomPlaylist playlist = new CustomPlaylist(uuid, guildId, listName, trucks);
                    results.add(playlist);
                }
            }
        }
        return results;
    }

    public void registerPlaylist(CustomPlaylist playlist) throws SQLException, JsonProcessingException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " VALUES (?, ?, ?, ?)")) {
            ps.setString(1, playlist.getUuid());
            ps.setLong(2, playlist.getGuildId());
            ps.setString(3, playlist.getListname());
            ps.setString(4, mapper.writeValueAsString(playlist.getItems()));
            ps.execute();
        }
    }

    public void deletePlaylist(String uuid) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ps.execute();
        }
    }
}
