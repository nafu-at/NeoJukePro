/*
 * Copyright 2021 NAFU_at.
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

package page.nafuchoco.neojukepro.api.event.command;

import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;
import page.nafuchoco.neojukepro.core.guild.user.NeoGuildMember;
import page.nafuchoco.neojukepro.core.module.RegisteredListener;

import java.util.ArrayList;
import java.util.List;

public class GuildCommandEvent extends GuildEvent {
    private static final List<RegisteredListener> handlerList = new ArrayList<>();

    private final NeoGuildMember invoker;
    private final String trigger;
    private final String[] args;
    private final CommandExecutor command;
    private final CommandContext context;

    public GuildCommandEvent(@NotNull NeoGuild guild, NeoGuildMember invoker, String trigger, String[] args, CommandExecutor command, CommandContext context) {
        super(guild);
        this.invoker = invoker;
        this.trigger = trigger;
        this.args = args;
        this.command = command;
        this.context = context;
    }

    public NeoGuildMember getInvoker() {
        return invoker;
    }

    public String getTrigger() {
        return trigger;
    }

    public List<String> getArgs() {
        return List.of(args);
    }

    public CommandExecutor getCommand() {
        return command;
    }

    public CommandContext getContext() {
        return context;
    }

    @Override
    public List<RegisteredListener> getHandlerList() {
        return null;
    }
}
