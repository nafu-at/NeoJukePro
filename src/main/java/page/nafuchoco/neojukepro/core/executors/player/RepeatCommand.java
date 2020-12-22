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
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.guild.NeoGuildPlayerOptions;

@Slf4j
public class RepeatCommand extends CommandExecutor {

    public RepeatCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getArgs().length != 0) {
            NeoGuildPlayerOptions.RepeatMode repeatMode;
            try {
                repeatMode = NeoGuildPlayerOptions.RepeatMode.valueOf(context.getArgs()[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                repeatMode = NeoGuildPlayerOptions.RepeatMode.NONE;
            }
            context.getNeoGuild().getSettings().setRepeatMode(repeatMode);
            context.getChannel().sendMessage("Repeat mode has been changed.").queue();
        }
    }

    @Override
    public String getDescription() {
        return "Repeat the track.";
    }

    @Override
    public String getHelp() {
        return getName() + "[option]\n----\n" +
                "[NONE]: Do not repeat.\n" +
                "[SINGLE]: Repeat one song.\n" +
                "[ALL]: Repeat all.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
