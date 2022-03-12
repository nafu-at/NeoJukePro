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

package page.nafuchoco.neojukepro.core.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CommandExecutor implements ICommandExecutor {
    private final List<CommandOption> options = new ArrayList<>();

    private final String name;
    private final List<String> aliases;

    protected CommandExecutor(String name, String... aliases) {
        this.name = name;
        this.aliases = Arrays.asList(aliases);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    public List<CommandOption> getOptions() {
        return options;
    }

    public List<CommandValueOption> getValueOptions() {
        return options.stream()
                .filter(CommandValueOption.class::isInstance)
                .map(CommandValueOption.class::cast)
                .collect(Collectors.toList());
    }

    public List<SubCommandOption> getSubCommands() {
        return options.stream()
                .filter(SubCommandOption.class::isInstance)
                .map(SubCommandOption.class::cast)
                .collect(Collectors.toList());
    }

    public enum CommandExecutorPermission {
        NORMAL(0), GUILD_ADMIN(252), GUILD_OWNER(253), BOT_ADMIN(254), BOT_OWNER(255);

        private final int permission;

        CommandExecutorPermission(int permission) {
            this.permission = permission;
        }

        public int getPermission() {
            return permission;
        }
    }
}
