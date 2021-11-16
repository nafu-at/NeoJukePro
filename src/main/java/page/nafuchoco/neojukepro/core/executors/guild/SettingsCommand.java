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
import org.apache.commons.lang3.BooleanUtils;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;

@Slf4j
public class SettingsCommand extends CommandExecutor {

    public SettingsCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getArgs().length < 2) {
            context.getChannel().sendMessage(getGuildSettings(context.getNeoGuild())).queue();
        } else switch (context.getArgs()[0]) {
            case "prefix":
                if (context.getArgs()[1].equals("default"))
                    context.getNeoGuild().getSettings().setCommandPrefix(context.getNeoJukePro().getConfig().getBasicConfig().getPrefix());
                else
                    context.getNeoGuild().getSettings().setCommandPrefix(context.getArgs()[1]);
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.set.prefix.set")).queue();
                break;

            case "robot":
                context.getNeoGuild().getSettings().setRobotMode(BooleanUtils.toBoolean(context.getArgs()[1]));
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.set.robot.set")).queue();
                break;

            case "jukebox":
                context.getNeoGuild().getSettings().setJukeboxMode(BooleanUtils.toBoolean(context.getArgs()[1]));
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.set.autoplay.set")).queue();
                break;

            case "lang":
                context.getNeoGuild().getSettings().setLang(context.getArgs()[1]);
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.set.lang.set")).queue();
                break;

            case "enableSource":
                context.getNeoGuild().getSettings().enableSource(context.getArgs()[1]);
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.set.source.enable")).queue();
                break;

            case "disableSource":
                context.getNeoGuild().getSettings().disableSource(context.getArgs()[1]);
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.set.source.disable")).queue();
                break;

            case "enableCommandGroup":
                context.getNeoGuild().getSettings().enableCommandGroup(
                        context.getNeoJukePro().getCommandRegistry().getCommandGroup(context.getArgs()[1])
                );
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.set.commandgroup.enable")).queue();
                break;

            case "disableCommandGroup":
                context.getNeoGuild().getSettings().disableCommandGroup(
                        context.getNeoJukePro().getCommandRegistry().getCommandGroup(context.getArgs()[1])
                );
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.set.commandgroup.disable")).queue();
                break;

            default:
                context.getChannel().sendMessage(getGuildSettings(context.getNeoGuild())).queue();
                break;

        }
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
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 252;
    }
}
