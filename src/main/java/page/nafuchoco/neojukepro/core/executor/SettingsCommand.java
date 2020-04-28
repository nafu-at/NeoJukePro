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
import net.dv8tion.jda.api.entities.Guild;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandCache;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.database.GuildSettingsTable;

import java.sql.SQLException;

@Slf4j
public class SettingsCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();

    public SettingsCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        GuildSettingsTable settingsTable = (GuildSettingsTable) CommandCache.getCache(null, "settingsTable");
        try {
            if (context.getArgs().length < 2) {
                context.getChannel().sendMessage(getGuildSettings(context.getGuild(), settingsTable)).queue();
            } else switch (context.getArgs()[0]) {
                case "prefix":
                    if (context.getArgs()[1].equals("default"))
                        settingsTable.setGuildSetting(context.getGuild().getIdLong(), "prefix",
                                launcher.getConfig().getBasicConfig().getPrefix());
                    else
                        settingsTable.setGuildSetting(context.getGuild().getIdLong(), "prefix", context.getArgs()[1]);
                    context.getChannel().sendMessage(MessageManager.getMessage("command.set.prefix.set")).queue();
                    break;

                default:
                    context.getChannel().sendMessage(getGuildSettings(context.getGuild(), settingsTable)).queue();
                    break;

            }
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"));
        }
    }

    private String getGuildSettings(Guild guild, GuildSettingsTable settingsTable) throws SQLException {
        StringBuilder builder = new StringBuilder(MessageManager.getMessage("command.set.current") + "\n```\n");
        builder.append("Prefix: " + settingsTable.getGuildSetting(guild.getIdLong(), "prefix") + "\n");
        builder.append("```");
        return builder.toString();
    }

    @Override
    public String getDescription() {
        return "Change the guild-specific settings.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 252;
    }
}
