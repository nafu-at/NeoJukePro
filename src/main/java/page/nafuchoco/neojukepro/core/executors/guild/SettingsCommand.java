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
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;
import page.nafuchoco.neojukepro.module.NeoJuke;

@Slf4j
public class SettingsCommand extends CommandExecutor {

    public SettingsCommand(String name) {
        super(name);

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
    public void onInvoke(CommandContext context) {
        var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
        if (!context.getOptions().isEmpty()) {

            context.getOptions().values().forEach(option -> {
                switch (option.optionName()) {
                    case "enable-source":
                        neoGuild.getSettings().enableSource((String) option.getValue());
                        break;

                    case "disable-source":
                        neoGuild.getSettings().disableSource((String) option.getValue());
                        break;

                    default:
                        context.getHook().sendMessage(getGuildSettings(neoGuild)).setEphemeral(true).queue();
                        break;

                }
            });
        }

        context.getResponseSender().sendMessage(getGuildSettings(neoGuild)).queue();
    }

    private String getGuildSettings(NeoGuild neoGuild) {
        StringBuilder builder =
                new StringBuilder(MessageManager.getMessage("command.set.current") + "\n```\n");
        builder.append("Volume:              ").append(neoGuild.getSettings().getPlayerOptions().getVolumeLevel()).append("\n");
        builder.append("Repeat:              ").append(neoGuild.getSettings().getPlayerOptions().getRepeatMode()).append("\n");
        builder.append("Shuffle:             ").append(neoGuild.getSettings().getPlayerOptions().isShuffle()).append("\n");
        builder.append("DisabledSource:      ").append(neoGuild.getSettings().getPlayerOptions().getDisabledSources()).append("\n");
        builder.append("```");
        return builder.toString();
    }

    @Override
    public String getDescription() {
        return "Change the guild-specific settings.";
    }
}
