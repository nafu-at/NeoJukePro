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

package page.nafuchoco.neojukepro.core.discord.handler;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandCache;
import page.nafuchoco.neojukepro.core.database.GuildSettingsTable;
import page.nafuchoco.neojukepro.core.discord.guild.GuildSettings;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
public final class GuildVoiceJoinEventHandler extends ListenerAdapter {
    private static final NeoJukeLauncher launcher = Main.getLauncher();

    private final GuildSettingsTable settingsTable;

    public GuildVoiceJoinEventHandler() {
        this.settingsTable = (GuildSettingsTable) CommandCache.getCache(null, "settingsTable");
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        GuildAudioPlayer audioPlayer = launcher.getPlayerRegistry().getGuildAudioPlayer(event.getGuild());
        if (event.getMember().equals(event.getEntity().getGuild().getSelfMember())) {
            try {
                Map<String, String> settings = settingsTable.getGuildSettings(event.getGuild().getIdLong());
                audioPlayer.setVolume(NumberUtils.toInt(settings.get("volume"), 80));
                audioPlayer.setRepeatType(GuildSettings.REPEATTYPE.valueOf(StringUtils.defaultString(settings.get("repeat"), "NONE")));
            } catch (SQLException e) {
                log.error(MessageManager.getMessage("system.db.retrieving.error"), e);
            }
        }
    }
}
