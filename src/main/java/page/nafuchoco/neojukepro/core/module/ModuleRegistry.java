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

package page.nafuchoco.neojukepro.core.module;

import page.nafuchoco.neojukepro.core.module.exception.ModuleDuplicateException;

import java.util.*;
import java.util.Map.Entry;

public class ModuleRegistry {
    private final Map<String, NeoModule> modules = new LinkedHashMap<>();

    /**
     * Register a module.
     *
     * @param module Modules to register.
     * @throws ModuleDuplicateException Thrown if the module you are trying to register already exists.
     */
    public synchronized void registerModule(NeoModule module) throws ModuleDuplicateException {
        if (modules.containsKey(module.getDescription().getName()))
            throw new ModuleDuplicateException(module.getDescription().getName());
        else
            modules.put(module.getDescription().getName(), module);
    }

    /**
     * Deletes a registered module.
     *
     * @param module Module to be removed.
     */
    public synchronized void deleteModule(NeoModule module) {
        for (Iterator<Map.Entry<String, NeoModule>> i = modules.entrySet().iterator(); i.hasNext(); ) {
            Entry<String, NeoModule> entry = i.next();
            if (entry.getValue().equals(module))
                i.remove();
        }
    }

    /**
     * Returns a list of all registered modules.
     *
     * @return List of all registered modules
     */
    public synchronized List<NeoModule> getModules() {
        return new ArrayList<>(modules.values());
    }

    /**
     * Get the matching module from the name.
     *
     * @param name Name of the module to get.
     * @return The appropriate module
     */
    public synchronized NeoModule getModule(String name) {
        return modules.get(name);
    }
}
