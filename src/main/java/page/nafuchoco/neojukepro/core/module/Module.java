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

import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.player.CustomAudioSourceManager;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface Module {

    /**
     * This is called when the module is loaded.
     * At this point, most of the features of the bot are not available, but you can change the behavior of the bot.
     */
    void onLoad();

    /**
     * Called when this module is enabled.
     */
    void onEnable();

    /**
     * Called when this module is disabled.
     */
    void onDisable();

    /**
     * Returns the enabled status of the module.
     *
     * @return enabled status of the module
     */
    boolean isEnable();

    /**
     * Register a customized AudioSourceManager.
     * This method can only be used within onLoad.
     *
     * @param customAudioSourceManager 登録するAudioSourceManager
     */
    void registerAudioSourceManager(CustomAudioSourceManager customAudioSourceManager);

    /**
     * Register the CommandExecutor.
     *
     * @param executor CommandExecutor class to be registered
     */
    default void registerCommand(CommandExecutor executor) {
        registerCommand(null, executor);
    }

    /**
     * Register the CommandExecutor.
     *
     * @param groupName       Name of the command group to which the command executor belongs.
     * @param commandExecutor CommandExecutor class to be registered
     * @since v2.2
     */
    void registerCommand(String groupName, CommandExecutor commandExecutor);

    /**
     * Register all CommandExecutors.
     *
     * @param executors List containing the CommandExecutor
     */
    default void registerCommands(List<CommandExecutor> executors) {
        registerCommand(null, executors);
    }

    /**
     * Register all CommandExecutors.
     *
     * @param groupName Name of the command group to which the command executor belongs.
     * @param executors List containing the CommandExecutor
     * @since v2.2
     */
    default void registerCommand(String groupName, List<CommandExecutor> executors) {
        executors.forEach(e -> registerCommand(groupName, e));
    }

    /**
     * Unregisters all commands related to the specified CommandExecutor class.
     *
     * @param executor CommandExecutor class that wants to be unregistered
     */
    void removeCommand(CommandExecutor executor);

    /**
     * Unregisters all CommandExecutor classes registered from this module.
     */
    void removeCommands();

    /**
     * Returns a description of this module.
     *
     * @return module description
     */
    ModuleDescription getDescription();

    /**
     * Returns the Bot's controller class.
     *
     * @return Bot's controller class
     */
    NeoJukePro getNeoJukePro();

    /**
     * Get the embedded resources for this module.
     *
     * @param filename Resource file name
     * @return InputStream of the file, or null if the file is not found
     */
    InputStream getResources(String filename);

    /**
     * Returns a folder to store the plugin data files.
     *
     * @return Folder for storing plugin data files.
     */
    File getDataFolder();

    /**
     * Returns the module logger associated with this Bot's logger.
     *
     * @return Module logger associated with this bot's logger
     */
    NeoModuleLogger getModuleLogger();

    /**
     * Returns the class loader that loaded the module.
     *
     * @return The class loader that loaded the module
     */
    ClassLoader getClassLoader();
}
