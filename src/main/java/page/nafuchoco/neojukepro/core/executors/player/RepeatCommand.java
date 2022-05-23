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
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.guild.NeoGuildPlayerOptions;
import page.nafuchoco.neojukepro.module.NeoJuke;

@Slf4j
public class RepeatCommand extends CommandExecutor {

    public RepeatCommand(String name) {
        super(name);

        getOptions().add(new CommandValueOption(OptionType.STRING,
                "repeat",
                "Select NONE/SINGLE/ALL.",
                true,
                false));
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayerOptions.RepeatMode repeatMode;
        try {
            repeatMode = NeoGuildPlayerOptions.RepeatMode.valueOf(((String) context.getOptions().get("repeat").getValue()).toUpperCase());
        } catch (IllegalArgumentException e) {
            repeatMode = NeoGuildPlayerOptions.RepeatMode.NONE;
        }
        NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild()).getSettings().setRepeatMode(repeatMode);
        context.getResponseSender().sendMessage("Repeat mode has been changed.").queue();
    }

    @Override
    public String getDescription() {
        return "Repeat the track.";
    }


}
