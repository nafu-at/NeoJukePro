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
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

@Slf4j
public abstract class CommandRegistrar {
    private final NeoJukeLauncher launcher;

    private CommandListUpdateAction updateAction;

    protected CommandRegistrar(NeoJukeLauncher launcher) {
        this.launcher = launcher;
    }

    protected void registerCommandToDiscord(CommandExecutor executor) {
        if (updateAction == null)
            updateAction = launcher.getShardManager().getShardById(0).updateCommands();

        val command = Commands.slash(executor.getName(), executor.getDescription());
        addCommandOptions(executor, command);
        updateAction.addCommands(command);
        executor.getAliases().forEach(alias -> {
            val commandAlias = Commands.slash(alias, executor.getDescription());
            addCommandOptions(executor, commandAlias);
            updateAction.addCommands(commandAlias);
        });
    }

    private void addCommandOptions(CommandExecutor executor, SlashCommandData command) {
        executor.getValueOptions().forEach(option -> command.addOption(option.optionType(), option.optionName(), option.optionDescription(), option.required(), option.autoComplete()));
        executor.getSubCommands().forEach(sub -> command.addSubcommands(new SubcommandData(sub.optionName(), sub.optionDescription())));
    }

    protected void queue() {
        updateAction.queue(queue -> log.debug("Queue command registration: {}", queue.toString()));
        updateAction = null;
    }
}
