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

package page.nafuchoco.neojukepro.core.command;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;
import page.nafuchoco.neojukepro.core.guild.user.NeoGuildMember;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CommandContext {
    private final NeoJukePro neoJukePro;
    private final NeoGuild neoGuild;
    private final TextChannel channel;
    private final NeoGuildMember invoker;
    private final Message message;

    private final String trigger;
    private final String[] args;
    private final CommandExecutor command;

    public NeoJukePro getNeoJukePro() {
        return neoJukePro;
    }

    /**
     * @return Command Executed Guild
     * @since v2.0
     */
    public NeoGuild getNeoGuild() {
        return neoGuild;
    }

    /**
     * @return Command executed text channel
     */
    public TextChannel getChannel() {
        return channel;
    }

    /**
     * @return Command executed member
     * @since v2.0
     */
    public NeoGuildMember getInvoker() {
        return invoker;
    }

    /**
     * @return Received message
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @return Executed command name
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * @return Specified options
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * @return The command class corresponding to the command name
     */
    public CommandExecutor getCommand() {
        return command;
    }
}
