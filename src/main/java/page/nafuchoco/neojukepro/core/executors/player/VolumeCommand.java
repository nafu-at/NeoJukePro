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

package page.nafuchoco.neojukepro.core.executors.player;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neobot.api.command.SubCommandOption;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

@Slf4j
public class VolumeCommand extends CommandExecutor {

    public VolumeCommand(String name) {
        super(name);

        getOptions().add(new CommandValueOption(OptionType.INTEGER,
                "volume",
                "Change to that volume.",
                false,
                false));
        //getOptions().add(new VolumeConfirmSubCommand("confirm"));
        // TODO: 2022/03/12 Confirmをなにか考える
    }

    @Override
    public void onInvoke(CommandContext context) {
        var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
        if (!context.getOptions().isEmpty()) {
            int volume = -1;
            volume = (int) context.getOptions().get("volume").getValue();
            if (volume > 200) {
                neoGuild.getGuildTempRegistry().registerTemp("volumevalue", volume);
                context.getResponseSender().sendMessage(MessageManager.getMessage("command.volume.warn")).queue();
            }

            if (volume >= 0)
                neoGuild.getSettings().setVolumeLevel(volume);
        }
        context.getHook().sendMessage(MessageUtil.format(
                MessageManager.getMessage("command.volume.corrent"),
                neoGuild.getSettings().getPlayerOptions().getVolumeLevel())).queue();
    }

    @Override
    public String getDescription() {
        return "Changes the player's volume.";
    }


    public static class VolumeConfirmSubCommand extends SubCommandOption {

        public VolumeConfirmSubCommand(String name) {
            super(name);
        }

        @Override
        public void onInvoke(CommandContext context) {
            var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
            int volume = -1;
            if (neoGuild.getGuildTempRegistry().getTemp("volumevalue") != null) {
                volume = (int) neoGuild.getGuildTempRegistry().getTemp("volumevalue");
                neoGuild.getGuildTempRegistry().deleteTemp("volumevalue");
            }

            if (volume >= 0)
                neoGuild.getSettings().setVolumeLevel(volume);

            context.getResponseSender().sendMessage(MessageUtil.format(
                    MessageManager.getMessage("command.volume.corrent"),
                    neoGuild.getSettings().getPlayerOptions().getVolumeLevel())).queue();
        }

        @Override
        public @NotNull String getDescription() {
            return "Confirmed change to high volume";
        }
    }
}
