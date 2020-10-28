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
import page.nafuchoco.neojukepro.core.database.GuildSettingsTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildSettingsTableInstead extends GuildSettingsTable {
    private final Map<Long, Map<String, String>> tableMap;

    public GuildSettingsTableInstead() {
        super(null);
        tableMap = new HashMap<>();
    }

    @Deprecated
    @Override
    public void createTable() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getGuilds() throws SQLException {
        return new ArrayList<>(tableMap.keySet());
    }

    @Override
    public Map<String, String> getGuildSettings(long guildId) throws SQLException {
        return tableMap.computeIfAbsent(guildId, key -> new HashMap<>());
    }

    @Override
    public String getGuildSetting(long guildId, String name) throws SQLException {
        return getGuildSettings(guildId).get(name);
    }

    @Override
    public void setGuildSetting(long guildId, String name, String value) throws SQLException {
        getGuildSettings(guildId).put(name, value);
    }

    @Override
    public void deleteSettings(long guildId) throws SQLException {
        tableMap.remove(guildId);
    }

    @Override
    public void deleteSetting(long guildId, String name) throws SQLException {
        getGuildSettings(guildId).remove(name);
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
