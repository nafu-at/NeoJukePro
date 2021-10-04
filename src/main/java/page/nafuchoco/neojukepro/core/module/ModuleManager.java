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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neojukepro.api.event.Event;
import page.nafuchoco.neojukepro.api.event.EventHandler;
import page.nafuchoco.neojukepro.api.event.EventListener;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.module.exception.EventException;
import page.nafuchoco.neojukepro.core.module.exception.InvalidModuleException;
import page.nafuchoco.neojukepro.core.module.exception.ModuleDuplicateException;
import page.nafuchoco.neojukepro.core.module.exception.UnknownDependencyException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class ModuleManager {
    private final ModuleRegistry moduleRegistry;
    private final ModuleLoader moduleLoader;
    private final List<File> files;

    public ModuleManager(NeoJukeLauncher launcher, String moduleDir) {
        this.moduleRegistry = new ModuleRegistry();
        moduleLoader = new ModuleLoader(launcher, moduleRegistry, moduleDir);
        files = moduleLoader.searchModules();
    }

    /**
     * @return Returns all registered modules.
     */
    public List<NeoModule> getModules() {
        return moduleRegistry.getModules();
    }

    /**
     * Get a module.
     *
     * @param name Name of the module to get.
     * @return module
     */
    public NeoModule getModule(String name) {
        return moduleRegistry.getModule(name);
    }

    /**
     * Load all the modules found.
     */
    public void loadAllModules() {
        List<File> waitingModule = new ArrayList<>();
        while (!files.isEmpty()) {
            Iterator<File> iterator = files.iterator();
            iterator:
            while (iterator.hasNext()) {
                var file = iterator.next();
                ModuleDescription description;
                try {
                    description = moduleLoader.loadModuleDescription(file);
                } catch (InvalidModuleException e) {
                    log.warn(MessageManager.getMessage("system.module.load.info.failed"), file.getName(), e);
                    iterator.remove();
                    continue iterator;
                }

                if (!CollectionUtils.isEmpty(description.getLoadBefore())
                        && waitingModule.size() != IterableUtils.size(Collections.singleton(iterator))) {
                    for (String module : description.getLoadBefore()) {
                        if (moduleRegistry.getModule(module) == null) {
                            waitingModule.add(file);
                            continue iterator;
                        }
                        waitingModule.remove(file);
                    }
                }

                loadModule(file);
                iterator.remove();
            }
        }
    }

    /**
     * Load the module.
     *
     * @param file Jar file of the module.
     * @return Returns true if the module is successfully loaded.
     */
    public boolean loadModule(File file) {
        NeoModule module = null;
        try {
            module = moduleLoader.loadModule(file);
        } catch (UnknownDependencyException e) {
            log.error(MessageManager.getMessage("system.module.load.info.depend.unknown"), e);
        } catch (InvalidModuleException e) {
            log.error(MessageManager.getMessage("system.module.load.invalid"), e);
        }

        if (module == null)
            return false;

        try {
            moduleRegistry.registerModule(module);
        } catch (ModuleDuplicateException e) {
            log.warn(MessageManager.getMessage("system.module.load.already"), module.getDescription().getName());
            return false;
        }

        try {
            module.onLoad();
            log.info(MessageManager.getMessage("system.module.load.success"), module.getDescription().getName());
        } catch (Throwable e) {
            log.warn(MessageManager.getMessage("system.module.load.error"), e);
            unloadModule(module);
            return false;
        }
        return true;
    }

    /**
     * Enable all loaded modules.
     */
    public void enableAllModules() {
        for (NeoModule module : moduleRegistry.getModules()) {
            enableModule(module);
        }
    }

    /**
     * Enables the module.
     *
     * @param name Module name to enable.
     * @return Returns true if the module was successfully enabled.
     * @throws IllegalArgumentException It will be thrown if the specified module does not exist.
     */
    public boolean enableModule(String name) {
        NeoModule module = moduleRegistry.getModule(name);
        if (module == null)
            throw new IllegalArgumentException("The specified module is not registered.");
        return enableModule(module);
    }

    /**
     * Enables the module.
     *
     * @param module The module to enable.
     * @return Returns true if the module was successfully enabled.
     */
    public boolean enableModule(NeoModule module) {
        return module.setEnable(true);
    }

    /**
     * Disable all loaded modules.
     */
    public void disableAllModules() {
        for (NeoModule module : moduleRegistry.getModules()) {
            disableModule(module);
        }
    }

    /**
     * Disables the module.
     *
     * @param name Module name to disable.
     * @return Returns true if the module has been successfully disabled.
     * @throws IllegalArgumentException It will be thrown if the specified module does not exist.
     */
    public boolean disableModule(String name) {
        NeoModule module = moduleRegistry.getModule(name);
        if (module == null)
            throw new IllegalArgumentException("The specified module is not registered.");
        return disableModule(module);
    }

    /**
     * Disables the module.
     *
     * @param module Modules to disable.
     * @return Returns true if the module has been successfully disabled.
     */
    public boolean disableModule(NeoModule module) {
        return module.setEnable(false);

    }

    /**
     * Unload all loaded modules.
     *
     * @deprecated(since=v2.2.0, forRemoval=true)
     */
    @Deprecated
    public void unloadAllModules() {
        for (NeoModule module : moduleRegistry.getModules()) {
            unloadModule(module);
        }
    }

    /**
     * Unloads the module.
     *
     * @param name Module name to unload.
     * @throws IllegalArgumentException It will be thrown if the specified module does not exist.
     * @deprecated(since=v2.2.0, forRemoval=true)
     */
    @Deprecated
    public void unloadModule(String name) {
        NeoModule module = moduleRegistry.getModule(name);
        if (module == null)
            throw new IllegalArgumentException("The specified module is not registered.");
        unloadModule(module);
    }

    /**
     * Unloads the module.
     *
     * @param module Modules to unload
     * @deprecated(since=v2.2.0, forRemoval=true)
     */
    @Deprecated
    public void unloadModule(NeoModule module) {
        log.warn("Module unloading has been disabled.");

        /* Disabled as it may cause problems.
        if (module.isEnable()) {
            log.warn(MessageManager.getMessage("system.module.unload.failed"), module.getDescription().getName());
        } else {
            try {
                ModuleClassLoader classLoader = module.getClassLoader();

                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    if (thread.getClass().getClassLoader() == classLoader) {
                        log.info("Stopping module thread: {}", thread.getName());
                        thread.interrupt();
                        thread.join(3000);
                        if (thread.isAlive())
                            thread.stop();
                    }
                }

                moduleRegistry.deleteModule(module);
                classLoader.close();
                log.info(MessageManager.getMessage("system.module.unload.success"), module.getDescription().getName());
            } catch (IOException | InterruptedException e) {
                log.error(MessageManager.getMessage("system.module.unload.error"), e);
            }
        }
        */
    }


    public void fireEvent(@NotNull Event event) {
        for (RegisteredListener listener : event.getHandlerList()) {
            if (!listener.getModule().isEnable())
                continue;

            try {
                listener.callEvent(event);
            } catch (EventException e) {
                log.warn("An error occurred during the execution of event {} caused by {}.",
                        event.getClass().getSimpleName(),
                        listener.getModule().getDescription().getName());
            }
        }
    }

    public void registerListener(@NotNull EventListener listener, @NotNull NeoModule module) {
        if (!module.isEnable()) {
            log.warn("The module {} to which you are trying to add an event listener is not enabled.", module.getDescription().getName());
            return;
        }

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : createRegisteredListener(listener, module).entrySet()) {
            var eventClass = entry.getKey();
            getRegisteredListenersList(eventClass).addAll(entry.getValue());
        }
    }

    private Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListener(EventListener listener, NeoModule module) {
        Map<Class<? extends Event>, Set<RegisteredListener>> regMap = new HashMap<>();

        List<Method> methods = Arrays.asList(listener.getClass().getMethods());
        for (Method method : methods) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null)
                continue;

            if (method.isBridge() || method.isSynthetic())
                continue;

            Class<?> parameterClass = null;
            if (method.getParameterTypes().length != 1 && !Event.class.isAssignableFrom(parameterClass = method.getParameterTypes()[0]))
                continue;

            Class<? extends Event> eventClass = parameterClass.asSubclass(Event.class);
            var listenerSet = regMap.computeIfAbsent(eventClass, k -> new HashSet<>());
            var executor = new EventExecutor() {
                @Override
                public void execute(@NotNull EventListener listener, @NotNull Event event) throws EventException {
                    try {
                        method.invoke(listener, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new EventException(e);
                    } catch (Throwable e) {
                        throw new EventException(e);
                    }
                }
            };

            var registeredListener = new RegisteredListener(listener, module, executor);
            listenerSet.add(registeredListener);
        }

        return regMap;
    }

    private List<RegisteredListener> getRegisteredListenersList(Class<? extends Event> event) {
        try {
            Method method = event.getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (List<RegisteredListener>) method.invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
