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

package page.nafuchoco.neojukepro.core.database.instead;

import page.nafuchoco.neojukepro.core.database.DatabaseConnector;
import page.nafuchoco.neojukepro.core.database.GuildUsersPermTable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GuildUsersPermTableInstead extends GuildUsersPermTable {
    private final Map<Long, Map<Long, Integer>> tableMap;

    public GuildUsersPermTableInstead() {
        super(null);
        tableMap = new HashMap<>();
    }

    @Deprecated
    @Override
    public void createTable() throws SQLException {
        throw new UnsupportedOperationException();
    }

    private Map<Long, Integer> getGuildSettings(long guildId) {
        return tableMap.computeIfAbsent(guildId, key -> new HashMap<>());
    }

    @Override
    public int getUserPermission(long guildId, long userId) throws SQLException {
        return getGuildSettings(guildId).getOrDefault(userId, -1);
    }

    @Override
    public void setUserPermission(long guildId, long userId, int permissionCode) throws SQLException {
        getGuildSettings(guildId).put(userId, permissionCode);
    }

    @Override
    public void deleteGuildUsers(long guildId) throws SQLException {
        tableMap.remove(guildId);
    }

    @Deprecated
    @Override
    public void deleteUser(long userId) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteGuildUser(long guildId, long userId) throws SQLException {
        getGuildSettings(guildId).remove(userId);
    }

    @Deprecated
    @Override
    protected String getTablename() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    protected DatabaseConnector getConnector() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void createTable(String construction) throws SQLException {
        throw new UnsupportedOperationException();
    }
}
