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

package page.nafuchoco.neojukepro.core.database.dummy;

import page.nafuchoco.neojukepro.core.database.NeoGuildSettingsTable;
import page.nafuchoco.neojukepro.core.guild.NeoGuildPlayerOptions;
import page.nafuchoco.neojukepro.core.guild.NeoGuildSettings;

import java.sql.SQLException;

public class DummyNeoGuildSettingsTable extends NeoGuildSettingsTable {

    public DummyNeoGuildSettingsTable() {
        super(null, null, null);
    }

    @Override
    public void createTableColumn(String name, String type) throws SQLException {
    }

    @Override
    public void dropTableColumn(String name) throws SQLException {
    }

    @Override
    public void createTable() throws SQLException {
    }

    @Override
    public NeoGuildSettings getGuildSettings(long guildId) throws SQLException {
        return null;
    }

    @Override
    public void registerGuildSettings(NeoGuildSettings settings) throws SQLException {
    }

    @Override
    public void updateCommandPrefixSetting(long guildId, String commandPrefix) throws SQLException {
    }

    @Override
    public void updateRobotModeSetting(long guildId, boolean robotMode) throws SQLException {
    }

    @Override
    public void updateJukeboxModeSetting(long guildId, boolean jukeboxMode) throws SQLException {
    }

    @Override
    public void updatePlayerOptions(long guildId, NeoGuildPlayerOptions playerOptions) throws SQLException {
    }

    @Override
    public void updateCustomField(long guildId, String customField) throws SQLException {
    }

    @Override
    public void deleteSettings(long guildId) throws SQLException {
    }
}
