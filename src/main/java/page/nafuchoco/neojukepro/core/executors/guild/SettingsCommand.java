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

package page.nafuchoco.neojukepro.core.executors.guild;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.BooleanUtils;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;

@Slf4j
public class SettingsCommand extends CommandExecutor {

    public SettingsCommand(String name, String... aliases) {
        super(name, aliases);

        getOptions().add(new CommandValueOption(OptionType.BOOLEAN,
                "jukebox",
                "Enables or disables the ability to automatically play related videos in succession.",
                false,
                false));
        getOptions().add(new CommandValueOption(OptionType.STRING,
                "lang",
                "Sets the language of messages displayed by the bot.",
                false,
                false));
        getOptions().add(new CommandValueOption(OptionType.STRING,
                "enable-source",
                "Enables disabled playback sources.",
                false,
                false));
        getOptions().add(new CommandValueOption(OptionType.STRING,
                "disable-source",
                "Disables an enabled playback source.",
                false,
                false));
    }

    @Override
    public String onInvoke(CommandContext context) {
        if (context.getOptions().isEmpty()) {
            return getGuildSettings(context.getNeoGuild());
        } else {
            context.getOptions().values().forEach(option -> {
                switch (option.optionName()) {
                    case "jukebox":
                        context.getNeoGuild().getSettings().setJukeboxMode(BooleanUtils.toBoolean((Boolean) option.getValue()));
                        break;

                    case "lang":
                        context.getNeoGuild().getSettings().setLang((String) option.getValue());
                        break;

                    case "enable-source":
                        context.getNeoGuild().getSettings().enableSource((String) option.getValue());
                        break;

                    case "disable-source":
                        context.getNeoGuild().getSettings().disableSource((String) option.getValue());
                        break;

                    default:
                        context.getHook().sendMessage(getGuildSettings(context.getNeoGuild())).setEphemeral(true).queue();
                        break;

                }
            });
        }
        return null;
    }

    private String getGuildSettings(NeoGuild neoGuild) {
        StringBuilder builder =
                new StringBuilder(MessageManager.getMessage(neoGuild.getSettings().getLang(), "command.set.current") + "\n```\n");
        builder.append("Prefix:              ").append(neoGuild.getSettings().getCommandPrefix()).append("\n");
        builder.append("Lang:                ").append(neoGuild.getSettings().getLang()).append("\n");
        builder.append("Volume:              ").append(neoGuild.getSettings().getPlayerOptions().getVolumeLevel()).append("\n");
        builder.append("Repeat:              ").append(neoGuild.getSettings().getPlayerOptions().getRepeatMode()).append("\n");
        builder.append("Shuffle:             ").append(neoGuild.getSettings().getPlayerOptions().isShuffle()).append("\n");
        builder.append("RobotMode:           ").append(neoGuild.getSettings().isRobotMode()).append("\n");
        builder.append("JukeboxMode:         ").append(neoGuild.getSettings().isJukeboxMode()).append("\n");
        builder.append("DisabledSource:      ").append(neoGuild.getSettings().getPlayerOptions().getDisabledSources()).append("\n");
        builder.append("DisableCommandGroup: ").append(neoGuild.getSettings().getDisableCommandGroup());
        builder.append("```");
        return builder.toString();
    }

    @Override
    public String getDescription() {
        return "Change the guild-specific settings.";
    }

    @Override
    public int getRequiredPerm() {
        return 252;
    }
}
