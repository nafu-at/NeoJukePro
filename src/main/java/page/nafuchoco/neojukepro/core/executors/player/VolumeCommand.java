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
import org.apache.commons.lang3.math.NumberUtils;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;

@Slf4j
public class VolumeCommand extends CommandExecutor {

    public VolumeCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getArgs().length != 0) {
            int volume = -1;
            if (context.getArgs()[0].equals("confirm")
                    && context.getNeoGuild().getGuildTempRegistry().getTemp("volumevalue") != null) {
                volume = (int) context.getNeoGuild().getGuildTempRegistry().getTemp("volumevalue");
                context.getNeoGuild().getGuildTempRegistry().deleteTemp("volumevalue");
            } else {
                try {
                    volume = NumberUtils.toInt(context.getArgs()[0], -1);
                    if (volume > 200) {
                        context.getChannel().sendMessage(
                                MessageManager.getMessage(
                                        context.getNeoGuild().getSettings().getLang(),
                                        "command.volume.warn"
                                )
                        ).queue();
                        context.getNeoGuild().getGuildTempRegistry().registerTemp("volumevalue", volume);
                        volume = -1;
                    }
                } catch (NumberFormatException e) {
                    context.getChannel().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.volume.correct")).queue();
                }
            }

            if (volume >= 0)
                context.getNeoGuild().getSettings().setVolumeLevel(volume);
        }
        context.getChannel().sendMessage(MessageUtil.format(
                MessageManager.getMessage(context.getNeoGuild().getSettings().getLang(), "command.volume.corrent"),
                context.getNeoGuild().getSettings().getPlayerOptions().getVolumeLevel())).queue();
    }

    @Override
    public String getDescription() {
        return "Changes the player's volume.";
    }

    @Override
    public String getHelp() {
        return getName() + " <args>\n----\n" +
                "<0-200>: Change to that volume.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
