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

package page.nafuchoco.neojukepro.core.executor.system;

import net.dv8tion.jda.api.entities.TextChannel;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

public class DeleteCommand extends CommandExecutor {

    public DeleteCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        TextChannel channel = context.getChannel();
        channel.getHistory().retrievePast(50).queue(messages -> messages.forEach(message -> {
            if (message.getAuthor().equals(context.getMessage().getJDA().getSelfUser())
                    || message.getContentRaw().startsWith(context.getNeoGuild().getSettings().getCommandPrefix()))
                message.delete().submit();
        }));
    }

    @Override
    public String getDescription() {
        return "Delete the last 50 messages posted by a bot command or bot.";
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
