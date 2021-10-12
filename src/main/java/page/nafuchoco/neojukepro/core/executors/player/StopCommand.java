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

import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;

public class StopCommand extends CommandExecutor {

    public StopCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
        audioPlayer.stop();
        context.getChannel().sendMessage(MessageManager.getMessage(
                context.getNeoGuild().getSettings().getLang(),
                "command.stop")).queue();
        context.getNeoGuild().destroyAudioPlayer();
    }

    @Override
    public String getDescription() {
        return "Stops the player.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
