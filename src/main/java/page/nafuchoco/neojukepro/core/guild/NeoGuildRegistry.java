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

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import page.nafuchoco.neojukepro.module.NeoJuke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class NeoGuildRegistry {
    private static final Gson gson = new Gson();
    private final Map<Long, NeoGuild> guilds = new HashMap<>();

    public NeoGuild getNeoGuild(long guildId) {
        return guilds.computeIfAbsent(guildId, key -> {
            NeoGuildSettings guildSettings;
            try {
                String settingsJson = NeoJuke.getInstance().getSettingsStore().getStoreData(guildId, "player_options");

                if (settingsJson == null) {
                    guildSettings = new NeoGuildSettings(guildId, new NeoGuildPlayerOptions(80, NeoGuildPlayerOptions.RepeatMode.NONE, false, new ArrayList<>()));
                    NeoJuke.getInstance().getSettingsStore().registerStoreData(guildId, gson.toJson(guildSettings.getPlayerOptions()));
                } else {
                    guildSettings = new NeoGuildSettings(guildId, gson.fromJson(settingsJson, NeoGuildPlayerOptions.class));
                }

                return new NeoGuild(key, guildSettings);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

    public void deleteGuildData(long guildId) {
        NeoJuke.getInstance().getSettingsStore().deleteStoredData(guildId);

        var neoGuild = guilds.get(guildId);
        if (neoGuild != null)
            neoGuild.destroyAudioPlayer();
        guilds.remove(guildId);
    }
}
