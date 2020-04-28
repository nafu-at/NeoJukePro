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

package page.nafuchoco.neojukepro.core.executor;

import lombok.extern.slf4j.Slf4j;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandCache;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.database.GuildSettingsTable;
import page.nafuchoco.neojukepro.core.discord.guild.GuildSettings;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;

import java.sql.SQLException;

@Slf4j
public class RepeatCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();

    public RepeatCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        GuildAudioPlayer audioPlayer = launcher.getPlayerRegistry().getGuildAudioPlayer(context.getGuild());
        if (audioPlayer == null)
            return;
        if (context.getArgs().length != 0) {
            GuildSettings.REPEATTYPE repeattype;
            try {
                repeattype = GuildSettings.REPEATTYPE.valueOf(context.getArgs()[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                repeattype = GuildSettings.REPEATTYPE.NONE;
            }
            audioPlayer.setRepeatType(repeattype);
            context.getChannel().sendMessage("Repeat mode has been changed.").queue();
            GuildSettingsTable settingsTable = (GuildSettingsTable) CommandCache.getCache(null, "settingsTable");
            try {
                settingsTable.setGuildSetting(context.getGuild().getIdLong(), "repeat", repeattype.name());
            } catch (SQLException e) {
                log.error("An error occurred while saving data to SQL.");
            }
        }
    }

    @Override
    public String getDescription() {
        return "Repeat the track.";
    }

    @Override
    public String getHelp() {
        return getName() + "[option]\n----\n" +
                "[NONE]: Do not repeat.\n" +
                "[SINGLE]: Repeat one song.\n" +
                "[ALL]: Repeat all.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
