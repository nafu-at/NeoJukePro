/*
 * Copyright 2022 NAFU_at.
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

package page.nafuchoco.neojukepro.core;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class CommandRegistrar {
    private final NeoJukeLauncher launcher;
    private final boolean registerAlias;

    private CommandListUpdateAction updateAction;
    private List<Command> registeredCommands = new ArrayList<>();

    protected CommandRegistrar(NeoJukeLauncher launcher, boolean registerAlias) {
        this.launcher = launcher;
        this.registerAlias = registerAlias;
    }

    protected void registerCommandToDiscord(CommandExecutor executor) {
        if (updateAction == null)
            updateAction = launcher.getShardManager().getShardById(0).updateCommands();

        val command = Commands.slash(executor.getName(), executor.getDescription());
        addCommandOptions(executor, command);
        updateAction.addCommands(command);

        if (registerAlias) {
            executor.getAliases().forEach(alias -> {
                val commandAlias = Commands.slash(alias, executor.getDescription());
                addCommandOptions(executor, commandAlias);
                updateAction.addCommands(commandAlias);
            });
        }
    }

    private void addCommandOptions(CommandExecutor executor, SlashCommandData command) {
        executor.getValueOptions().forEach(option -> command.addOption(option.optionType(), option.optionName(), option.optionDescription(), option.required(), option.autoComplete()));
        executor.getSubCommands().forEach(sub -> {
            val subCommand = new SubcommandData(sub.optionName(), sub.optionDescription());
            sub.getValueOptions().forEach(option -> subCommand.addOption(option.optionType(), option.optionName(), option.optionDescription(), option.required(), option.autoComplete()));
            command.addSubcommands(subCommand);
        });
    }

    protected void queue() {
        updateAction.queue(commands -> registeredCommands = commands);
        updateAction = null;
    }

    protected List<Command> getRegisteredCommands() {
        return registeredCommands;
    }
}
