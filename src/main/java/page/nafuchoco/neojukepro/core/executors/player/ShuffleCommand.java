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

import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

public class ShuffleCommand extends CommandExecutor {

    public ShuffleCommand(String name) {
        super(name);
    }

    @Override
    public void onInvoke(CommandContext context) {
        var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
        neoGuild.getSettings().setShuffle(!neoGuild.getSettings().getPlayerOptions().isShuffle());
        context.getHook().sendMessage(
                MessageUtil.format(
                        MessageManager.getMessage("command.shuffle"),
                        neoGuild.getSettings().getPlayerOptions().isShuffle())).queue();
    }

    @Override
    public String getDescription() {
        return "Shuffle the registered queues.";
    }


}
