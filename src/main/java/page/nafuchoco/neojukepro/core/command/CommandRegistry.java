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

import page.nafuchoco.neojukepro.core.module.NeoModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandRegistry {
    private final Map<NeoModule, Map<String, CommandExecutor>> commands = new LinkedHashMap<>();

    public void registerCommand(CommandExecutor executor, NeoModule module) {
        Map<String, CommandExecutor> reg = commands.computeIfAbsent(module, key -> new LinkedHashMap<>());
        String name = executor.getName();
        reg.put(name, executor);
        for (String alias : executor.getAliases())
            reg.put(alias, executor);
    }

    public void removeCommand(String name, NeoModule module) {
        if (commands.containsKey(module)) {
            CommandExecutor executor = commands.get(module).get(name);
            commands.get(module).remove(executor.getName());
            executor.getAliases().forEach(commands.get(module)::remove);
        }
    }

    public void removeCommand(CommandExecutor executor, NeoModule module) {
        if (commands.containsKey(module)) {
            commands.get(module).remove(executor.getName());
            executor.getAliases().forEach(commands.get(module)::remove);
        }
    }

    public void removeCommands(NeoModule module) {
        commands.remove(module);
    }

    public List<CommandExecutor> getCommands() {
        return commands.values().stream().flatMap(v -> v.values().stream()).distinct().collect(Collectors.toList());
    }

    public CommandExecutor getExecutor(String name) {
        CommandExecutor executor = null;
        List<NeoModule> modules = new ArrayList<>(commands.keySet());
        for (int i = modules.size() - 1; i >= 0; i--) {
            if (executor != null)
                break;
            NeoModule module = modules.get(i);
            if (module != null && !module.isEnable())
                continue;
            Map<String, CommandExecutor> reg = commands.get(module);
            if (reg != null)
                executor = reg.get(name);
        }
        return executor;
    }
}
