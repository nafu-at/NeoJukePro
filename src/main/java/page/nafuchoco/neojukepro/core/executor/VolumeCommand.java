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
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.MessageUtil;

@Slf4j
public class VolumeCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();

    public VolumeCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getArgs().length != 0) {
            try {
                int volume = Integer.parseInt(context.getArgs()[0]);
                context.getNeoGuild().getSettings().setVolumeLevel(volume);
            } catch (NumberFormatException e) {
                context.getChannel().sendMessage(MessageManager.getMessage("command.volume.correct")).queue();
            }
        }
        context.getChannel().sendMessage(MessageUtil.format(
                MessageManager.getMessage("command.volume.corrent"),
                context.getNeoGuild().getSettings().getPlayerOptions().getVolumeLevel())).queue();
    }

    @Override
    public String getDescription() {
        return "Changes the player's volume.";
    }

    @Override
    public String getHelp() {
        return getName() + "<args>\n----\n" +
                "<0-100>: Change to that volume.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
