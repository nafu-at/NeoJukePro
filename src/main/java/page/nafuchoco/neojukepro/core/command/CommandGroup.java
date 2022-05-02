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

package page.nafuchoco.neojukepro.core.command;

import page.nafuchoco.neojukepro.core.module.NeoModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandGroup {
    private final String groupName;
    private final Map<NeoModule, Map<String, CommandExecutor>> executors;
    private boolean enabled;

    protected CommandGroup(String groupName) {
        this.groupName = groupName;
        executors = new LinkedHashMap<>();
        enabled = true;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void registerCommand(CommandExecutor executor, NeoModule module) {
        Map<String, CommandExecutor> reg = executors.computeIfAbsent(module, key -> new LinkedHashMap<>());
        String name = executor.getName();
        reg.put(name, executor);
        for (String alias : executor.getAliases())
            reg.put(alias, executor);
    }

    public void removeCommand(String name, NeoModule module) {
        if (executors.containsKey(module)) {
            CommandExecutor executor = executors.get(module).get(name);
            executors.get(module).remove(executor.getName());
            executor.getAliases().forEach(executors.get(module)::remove);
        }
    }

    public void removeCommand(CommandExecutor executor, NeoModule module) {
        if (executors.containsKey(module)) {
            executors.get(module).remove(executor.getName());
            executor.getAliases().forEach(executors.get(module)::remove);
        }
    }

    public void removeCommands(NeoModule module) {
        executors.remove(module);
    }

    public List<CommandExecutor> getCommands() {
        return executors.values().stream().flatMap(v -> v.values().stream()).distinct().collect(Collectors.toList());
    }

    public CommandExecutor getExecutor(String name) {
        CommandExecutor executor = null;
        List<NeoModule> modules = new ArrayList<>(executors.keySet());
        for (int i = modules.size() - 1; i >= 0; i--) {
            if (executor != null)
                break;
            NeoModule module = modules.get(i);
            if (module != null && !module.isEnable())
                continue;
            Map<String, CommandExecutor> reg = executors.get(module);
            if (reg != null)
                executor = reg.get(name);
        }
        return executor;
    }
}
