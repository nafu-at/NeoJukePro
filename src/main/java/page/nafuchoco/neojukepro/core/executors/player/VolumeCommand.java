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
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.command.SubCommandOption;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;

@Slf4j
public class VolumeCommand extends CommandExecutor {

    public VolumeCommand(String name, String... aliases) {
        super(name, aliases);

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
        if (!context.getOptions().isEmpty()) {
            int volume = -1;
            volume = (int) context.getOptions().get("volume").getValue();
            if (volume > 200) {
                context.getNeoGuild().getGuildTempRegistry().registerTemp("volumevalue", volume);
                context.getResponseSender().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.volume.warn")).queue();
            }

            if (volume >= 0)
                context.getNeoGuild().getSettings().setVolumeLevel(volume);
        }
        context.getHook().sendMessage(MessageUtil.format(
                MessageManager.getMessage(context.getNeoGuild().getSettings().getLang(), "command.volume.corrent"),
                context.getNeoGuild().getSettings().getPlayerOptions().getVolumeLevel())).queue();
    }

    @Override
    public String getDescription() {
        return "Changes the player's volume.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }


    public static class VolumeConfirmSubCommand extends SubCommandOption {

        public VolumeConfirmSubCommand(String name, String... aliases) {
            super(name, aliases);
        }

        @Override
        public void onInvoke(CommandContext context) {
            int volume = -1;
            if (context.getNeoGuild().getGuildTempRegistry().getTemp("volumevalue") != null) {
                volume = (int) context.getNeoGuild().getGuildTempRegistry().getTemp("volumevalue");
                context.getNeoGuild().getGuildTempRegistry().deleteTemp("volumevalue");
            }

            if (volume >= 0)
                context.getNeoGuild().getSettings().setVolumeLevel(volume);

            context.getHook().sendMessage(MessageUtil.format(
                    MessageManager.getMessage(context.getNeoGuild().getSettings().getLang(), "command.volume.corrent"),
                    context.getNeoGuild().getSettings().getPlayerOptions().getVolumeLevel())).queue();
        }

        @Override
        public @NotNull String getDescription() {
            return "Confirmed change to high volume";
        }

        @Override
        public int getRequiredPerm() {
            return 0;
        }
    }
}
