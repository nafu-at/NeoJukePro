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

package page.nafuchoco.neojukepro.core.guild;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.database.GuildUsersPermTable;
import page.nafuchoco.neojukepro.core.database.NeoGuildSettingsTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class NeoGuildRegistry {
    private final Map<Long, NeoGuild> guilds = new HashMap<>();

    private final NeoJukePro neoJukePro;
    private final NeoGuildSettingsTable settingsTable;
    private final GuildUsersPermTable permTable;

    public NeoGuildRegistry(NeoJukePro api, NeoGuildSettingsTable settingsTable, GuildUsersPermTable permTable) {
        this.neoJukePro = api;
        this.settingsTable = settingsTable;
        this.permTable = permTable;
    }

    public NeoJukePro getNeoJukePro() {
        return neoJukePro;
    }

    public NeoGuild getNeoGuild(long guildId) {
        return guilds.computeIfAbsent(guildId, key -> {
            NeoGuildSettings guildSettings = null;
            try {
                guildSettings = settingsTable.getGuildSettings(key);
            } catch (SQLException e) {
                log.warn(MessageManager.getMessage("system.db.retrieving.error"), e);
            }
            if (guildSettings == null) {
                guildSettings = new NeoGuildSettings(
                        getNeoJukePro(),
                        settingsTable,
                        key,
                        getNeoJukePro().getConfig().getBasicConfig().getLanguage(),
                        getNeoJukePro().getConfig().getBasicConfig().getPrefix(),
                        false,
                        false,
                        new NeoGuildPlayerOptions(80, NeoGuildPlayerOptions.RepeatMode.NONE, false, new ArrayList<>()));
                try {
                    settingsTable.registerGuildSettings(guildSettings);
                } catch (SQLException e) {
                    log.error(MessageManager.getMessage("system.db.communicate.error"));
                }
            }

            return new NeoGuild(getNeoJukePro(), key, guildSettings, permTable);
        });
    }

    public NeoGuild getNeoGuild(Guild guild) {
        return getNeoGuild(guild.getIdLong());
    }

    public List<NeoGuild> getNeoGuilds() {
        return new ArrayList<>(guilds.values());
    }

    public List<NeoGuild> getPlayerActiveGuilds() {
        return getNeoGuilds().stream().filter(guild -> guild.audioPlayer != null).collect(Collectors.toList());
    }

    /**
     * Delete the saved guild data.
     * This process will delete all the data of the specified guild stored in the database.
     *
     * @param guildId Guild to delete data
     */
    public void deleteGuildData(long guildId) {
        try {
            settingsTable.deleteSettings(guildId);
            permTable.deleteGuildUsers(guildId);
        } catch (SQLException e) {
            log.error("An error occurred while deleting data.", e);
        }
        var neoGuild = guilds.get(guildId);
        if (neoGuild != null)
            neoGuild.destroyAudioPlayer();
        guilds.remove(guildId);
    }
}
