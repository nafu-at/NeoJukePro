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

package page.nafuchoco.neojukepro.core.guild;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandGroup;
import page.nafuchoco.neojukepro.core.database.NeoGuildSettingsTable;
import page.nafuchoco.neojukepro.core.module.Module;
import page.nafuchoco.neojukepro.core.module.ModuleDescription;
import page.nafuchoco.neojukepro.core.module.NeoModule;
import page.nafuchoco.neojukepro.core.module.NeoModuleLogger;
import page.nafuchoco.neojukepro.core.player.CustomAudioSourceManager;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NeoGuildSettings {
    private final NeoJukePro neoJukePro;
    private final NeoGuildSettingsTable settingsTable;

    private final long guildId;

    private String lang;
    private String commandPrefix;
    private boolean robotMode;
    private boolean jukeboxMode;

    @NonNull
    private final List<String> disableCommandGroup;
    private final NeoGuildPlayerOptions playerOptions;
    private final Map<Module, Map<String, Object>> customField = new HashMap<>();

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
        try {
            settingsTable.updateCommandPrefixSetting(guildId, commandPrefix);
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void setLang(String lang) {
        this.lang = lang;
        try {
            settingsTable.updateLanguageSetting(guildId, lang);
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void setRobotMode(boolean robotMode) {
        this.robotMode = robotMode;
        try {
            settingsTable.updateRobotModeSetting(guildId, robotMode);
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void setJukeboxMode(boolean jukeboxMode) {
        this.jukeboxMode = jukeboxMode;
        try {
            settingsTable.updateJukeboxModeSetting(guildId, jukeboxMode);
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public List<CommandGroup> getDisableCommandGroupList() {
        return disableCommandGroup.stream()
                .map(groupName -> neoJukePro.getCommandRegistry().getCommandGroup(groupName))
                .filter(v -> v != null)
                .collect(Collectors.toList());
    }

    public void disableCommandGroup(CommandGroup commandGroup) {
        if (commandGroup != null && !getDisableCommandGroup().contains(commandGroup.getGroupName()))
            getDisableCommandGroup().add(commandGroup.getGroupName());
        try {
            settingsTable.updateDisableCommandGroup(guildId, getDisableCommandGroup());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void enableCommandGroup(CommandGroup commandGroup) {
        getDisableCommandGroup().remove(commandGroup.getGroupName());
        try {
            settingsTable.updateDisableCommandGroup(guildId, getDisableCommandGroup());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void setVolumeLevel(int volumeLevel) {
        getPlayerOptions().setVolumeLevel(volumeLevel);
        getNeoJukePro().getGuildRegistry().getNeoGuild(guildId).getAudioPlayer().setVolume(volumeLevel);
        try {
            settingsTable.updatePlayerOptions(guildId, getPlayerOptions());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void setRepeatMode(NeoGuildPlayerOptions.RepeatMode repeatMode) {
        getPlayerOptions().setRepeatMode(repeatMode);
        try {
            settingsTable.updatePlayerOptions(guildId, getPlayerOptions());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void setShuffle(boolean shuffle) {
        getPlayerOptions().setShuffle(shuffle);
        if (shuffle)
            getNeoJukePro().getGuildRegistry().getNeoGuild(guildId).getAudioPlayer().getTrackProvider().shuffle();
        try {
            settingsTable.updatePlayerOptions(guildId, getPlayerOptions());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void disableSource(String sourceName) {
        getPlayerOptions().disableSource(sourceName);
        try {
            settingsTable.updatePlayerOptions(guildId, getPlayerOptions());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void enableSource(String sourceName) {
        getPlayerOptions().enableSource(sourceName);
        try {
            settingsTable.updatePlayerOptions(guildId, getPlayerOptions());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void setCustomField(NeoModule module, Map<String, Object> field) {
        customField.put(module, field);
        try {
            settingsTable.updateCustomField(guildId, serializeCustomFieldToJson());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }

    public void setCustomFieldObject(NeoModule module, String key, Object value) {
        customField.computeIfAbsent(module, k -> new HashMap<>()).put(key, value);
        try {
            settingsTable.updateCustomField(guildId, serializeCustomFieldToJson());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }


    public String serializeCustomFieldToJson() {
        Gson gson = new Gson();
        JsonObject customFieldJson = new JsonObject();
        for (Map.Entry<Module, Map<String, Object>> moduleField : customField.entrySet())
            customFieldJson.add(moduleField.getKey().getDescription().getName(), gson.toJsonTree(moduleField.getValue()));
        return customFieldJson.toString();
    }

    public void deserializeCustomFieldFromJson(String jsonString) {
        Gson gson = new Gson();
        JsonObject customFieldJson = gson.fromJson(jsonString, JsonObject.class);
        for (String moduleName : customFieldJson.keySet()) {
            Module module = getNeoJukePro().getModuleManager().getModule(moduleName);
            if (module == null)
                module = new DummyNeoModule(moduleName);
            Map<String, Object> field = gson.fromJson(customFieldJson.get(moduleName).toString(), new TypeToken<Map<String, Object>>() {
            }.getType());
            customField.put(module, field);
        }
    }


    public static class DummyNeoModule implements Module {
        private final ModuleDescription description;

        public DummyNeoModule(String moduleName) {
            this.description = new DummyModuleDescription(moduleName);
        }

        @Override
        public void onLoad() {
        }

        @Override
        public void onEnable() {
        }

        @Override
        public void onDisable() {
        }

        @Override
        public boolean isEnable() {
            return false;
        }

        @Override
        public void registerAudioSourceManager(CustomAudioSourceManager customAudioSourceManager) {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public void registerCommand(String groupName, CommandExecutor executor) {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public void removeCommand(CommandExecutor executor) {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public void removeCommands() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public ModuleDescription getDescription() {
            return description;
        }

        @Override
        public NeoJukePro getNeoJukePro() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public InputStream getResources(String filename) {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public File getDataFolder() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public NeoModuleLogger getModuleLogger() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public ClassLoader getClassLoader() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

    }

    public static class DummyModuleDescription extends ModuleDescription {
        private final String name;

        public DummyModuleDescription(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public List<String> getAuthors() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public String getWebsite() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public String getMain() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public List<String> getDependency() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public List<String> getLoadBefore() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }

        @Override
        public String getRequiredVersion() {
            throw new UnsupportedOperationException("This module class is a dummy for data retention.");
        }
    }
}
