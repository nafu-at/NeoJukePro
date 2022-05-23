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

package page.nafuchoco.neojukepro.module;

import page.nafuchoco.neobot.api.ConfigLoader;
import page.nafuchoco.neobot.api.NeoBot;
import page.nafuchoco.neobot.api.datastore.DataStore;
import page.nafuchoco.neobot.api.datastore.DataStoreGenerateException;
import page.nafuchoco.neobot.api.module.NeoModule;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeConfig;
import page.nafuchoco.neojukepro.core.discord.handler.GuildLeaveEventHandler;
import page.nafuchoco.neojukepro.core.discord.handler.GuildVoiceEventHandler;
import page.nafuchoco.neojukepro.core.executors.guild.SettingsCommand;
import page.nafuchoco.neojukepro.core.executors.player.*;
import page.nafuchoco.neojukepro.core.executors.system.StatusCommand;
import page.nafuchoco.neojukepro.core.guild.NeoGuildRegistry;
import page.nafuchoco.neojukepro.core.player.CustomSourceRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class NeoJuke extends NeoModule {
    private static NeoJuke instance;

    private NeoJukeConfig config;
    private DataStore settingsStore;
    private NeoGuildRegistry guildRegistry;
    private CustomSourceRegistry customSourceRegistry;


    public static NeoJuke getInstance() {
        if (instance == null)
            instance = (NeoJuke) NeoBot.getModuleManager().getModule("NeoJukePro");
        return instance;
    }

    @Override
    public void onLoad() {
        getModuleLogger().info("Starting NeoJuke module...");

        // load configuration
        var configFile = new File(getDataFolder(), "NeoJukeConfig.yaml");
        if (!configFile.exists()) {
            try (InputStream original = getResources("NeoJukeConfig.yaml")) {
                Files.copy(original, configFile.toPath());
                getModuleLogger().info(MessageManager.getMessage("system.config.generate"));
                getModuleLogger().debug(MessageManager.getMessage("system.config.generate.debug"), configFile.getPath());
            } catch (IOException e) {
                getModuleLogger().error(MessageManager.getMessage("system.config.generate.failed"), e);
            }
        }
        config = ConfigLoader.loadConfig(configFile, NeoJukeConfig.class);
        MessageManager.setDefaultLocale(config.getBasicConfig().getLanguage());

        // create datastore
        try {
            var builder = getLauncher().getDataStoreManager().createDataStoreBuilder();
            builder.storeName("NeoJukeSettings");
            builder.addIndex(String.class, "player_options");
            settingsStore = builder.build();
            getLauncher().getDataStoreManager().registerDataStore(settingsStore);
        } catch (DataStoreGenerateException e) {
            getModuleLogger().error("Failed to create datastore.", e);
        }

        guildRegistry = new NeoGuildRegistry();
        customSourceRegistry = new CustomSourceRegistry();
    }

    @Override
    public void onEnable() {
        // register commands
        registerCommand(new SettingsCommand("settings"));
        registerCommand(new StatusCommand("status"));

        registerCommand(new JoinCommand("join"));
        registerCommand(new LeaveCommand("leave"));
        registerCommand(new NowPlayingCommand("nowplaying"));
        registerCommand(new ListCommand("list"));
        registerCommand(new PlayCommand("play"));
        registerCommand(new SearchCommand("search"));
        registerCommand(new RePlayCommand("replay"));
        registerCommand(new InterruptCommand("interrupt"));
        registerCommand(new PauseCommand("pause"));
        registerCommand(new StopCommand("stop"));
        registerCommand(new SkipCommand("skip"));
        registerCommand(new SeekCommand("seek"));
        registerCommand(new VolumeCommand("volume"));
        registerCommand(new RepeatCommand("repeat"));
        registerCommand(new ShuffleCommand("shuffle"));
        registerCommand(new DestroyCommand("destroy"));
        queueCommandRegister();

        // register listeners
        getLauncher().getDiscordApi().addEventListener(new GuildLeaveEventHandler(), new GuildVoiceEventHandler());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }


    public NeoJukeConfig getConfig() {
        return config;
    }

    public DataStore getSettingsStore() {
        return settingsStore;
    }

    public NeoGuildRegistry getGuildRegistry() {
        return guildRegistry;
    }

    public CustomSourceRegistry getCustomSourceRegistry() {
        return customSourceRegistry;
    }
}
