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

package page.nafuchoco.neojukepro.core;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import io.sentry.Sentry;
import lavalink.client.io.jda.JdaLavalink;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.commons.lang3.StringUtils;
import page.nafuchoco.neojukepro.core.command.CommandCache;
import page.nafuchoco.neojukepro.core.command.CommandExecuteAuth;
import page.nafuchoco.neojukepro.core.command.CommandRegistry;
import page.nafuchoco.neojukepro.core.config.DatabaseSection;
import page.nafuchoco.neojukepro.core.config.LavalinkConfigSection;
import page.nafuchoco.neojukepro.core.config.NeoJukeConfig;
import page.nafuchoco.neojukepro.core.database.CustomPlaylistTable;
import page.nafuchoco.neojukepro.core.database.DatabaseConnector;
import page.nafuchoco.neojukepro.core.database.GuildSettingsTable;
import page.nafuchoco.neojukepro.core.database.GuildUsersPermTable;
import page.nafuchoco.neojukepro.core.discord.handler.GuildVoiceJoinEventHandler;
import page.nafuchoco.neojukepro.core.discord.handler.GuildVoiceLeaveEventHandler;
import page.nafuchoco.neojukepro.core.discord.handler.MessageReceivedEventHandler;
import page.nafuchoco.neojukepro.core.executor.*;
import page.nafuchoco.neojukepro.core.executor.system.ShutdownCommand;
import page.nafuchoco.neojukepro.core.executor.system.SystemCommand;
import page.nafuchoco.neojukepro.core.executor.system.UpdateCommand;
import page.nafuchoco.neojukepro.core.http.discord.DiscordAPIClient;
import page.nafuchoco.neojukepro.core.http.discord.DiscordAppInfo;
import page.nafuchoco.neojukepro.core.module.ModuleManager;
import page.nafuchoco.neojukepro.core.player.GuildPlayerRegistry;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

@Slf4j
public class Launcher implements NeoJukeLauncher {
    private ConfigManager configManager;
    private NeoJukeConfig config;

    private DatabaseConnector connector;
    private GuildSettingsTable settingsTable;
    private GuildUsersPermTable usersPermTable;
    private CustomPlaylistTable playlistTable;

    private ModuleManager moduleManager;
    private CommandRegistry commandRegistry;

    private ShardManager shardManager;
    private JdaLavalink lavalink;
    private GuildPlayerRegistry playerRegistry;

    @Override
    public void launch() {
        log.info(MessageManager.getMessage("system.config.load"));
        configManager = new ConfigManager();
        if (configManager.existsConfig(true)) {
            configManager.reloadConfig();
            config = configManager.getConfig();
            log.info(MessageManager.getMessage("system.config.load.success"));
        } else {
            log.error(MessageManager.getMessage("system.config.load.failed"));
            return;
        }

        if (!StringUtils.isEmpty(config.getAdvancedConfig().getSentryDsn()))
            Sentry.init(config.getAdvancedConfig().getSentryDsn());

        MessageManager.setDefaultLocale(config.getBasicConfig().getLanguage());

        log.info(MessageManager.getMessage("system.db.connection"));
        DatabaseSection database = config.getBasicConfig().getDatabase();
        connector = new DatabaseConnector(
                database.getDatabaseType().getAddressPrefix() + database.getAddress(), database.getDatabase(),
                database.getUsername(), database.getPassword());
        settingsTable = new GuildSettingsTable(connector);
        usersPermTable = new GuildUsersPermTable(connector);

        try {
            settingsTable.createTable();
            usersPermTable.createTable();
            playlistTable.createTable();
            CommandCache.registerCache(null, "settingsTable", settingsTable);
            CommandCache.registerCache(null, "playlistTable", playlistTable);
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.initialize.error"));
            return;
        }

        DiscordAppInfo appInfo;
        try {
            appInfo = new DiscordAPIClient().getBotApplicationInfo(config.getBasicConfig().getDiscordToken());
        } catch (IOException e) {
            log.error(MessageManager.getMessage("system.api.about.error"), e);
            return;
        }

        commandRegistry = new CommandRegistry();
        DefaultShardManagerBuilder shardManagerBuilder =
                DefaultShardManagerBuilder.createDefault(config.getBasicConfig().getDiscordToken());
        shardManagerBuilder.addEventListeners(new MessageReceivedEventHandler(
                new CommandExecuteAuth(config.getBasicConfig().getBotAdmins(), appInfo, usersPermTable), commandRegistry));
        shardManagerBuilder.addEventListeners(new GuildVoiceJoinEventHandler());
        shardManagerBuilder.addEventListeners(new GuildVoiceLeaveEventHandler());
        try {
            if (config.getAdvancedConfig().isUseNodeServer() && !config.getAdvancedConfig().getNodesInfo().isEmpty()) {
                lavalink =
                        new JdaLavalink(new DiscordAPIClient().getBotApplicationInfo(config.getBasicConfig().getDiscordToken()).getID(),
                                getShardsTotal(), this::getJdaFromId);
                for (LavalinkConfigSection node : config.getAdvancedConfig().getNodesInfo())
                    lavalink.addNode(node.getNodeName(), URI.create(node.getAddress()), node.getPassword());
                shardManagerBuilder.addEventListeners(lavalink);
                shardManagerBuilder.setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor());
            }
        } catch (IOException e) {
            log.error(MessageManager.getMessage("system.node.connection.failed"), e);
        }

        try {
            log.info(MessageManager.getMessage("system.api.login"));
            shardManager = shardManagerBuilder.build();
            while (!shardManager.getStatus(0).equals(JDA.Status.CONNECTED))
                Thread.sleep(100);
        } catch (LoginException e) {
            log.error(MessageManager.getMessage("system.api.login.failed"), e);
            Runtime.getRuntime().exit(1);
        } catch (InterruptedException e) {
            log.error(MessageManager.getMessage("system.api.login.error"), e);
            Runtime.getRuntime().exit(1);
        }
        log.info(MessageManager.getMessage("system.api.login.success"));
        log.debug("Ping! {}ms", shardManager.getAverageGatewayPing());

        playerRegistry = new GuildPlayerRegistry(new DefaultAudioPlayerManager(), lavalink);

        moduleManager = new ModuleManager("modules");
        moduleManager.loadAllModules();

        initCommand();

        moduleManager.enableAllModules();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down the system...");
            moduleManager.disableAllModules();
            if (lavalink != null) {
                playerRegistry.getPlayers().forEach(player -> playerRegistry.destroyPlayer(player.getGuild()));
                lavalink.shutdown();
            }
            shardManager.shutdown();
            connector.close();
            log.info("See you again!");
        }));
    }

    private void initCommand() {
        commandRegistry.registerCommand(new HelpCommand("help", "h"), null);

        commandRegistry.registerCommand(new SettingsCommand("settings", "set"), null);
        commandRegistry.registerCommand(new StatusCommand("status", "stats"), null);
        commandRegistry.registerCommand(new JoinCommand("join", "j"), null);
        commandRegistry.registerCommand(new LeaveCommand("leave", "lv"), null);
        commandRegistry.registerCommand(new NowPlayingCommand("nowplaying", "np"), null);
        commandRegistry.registerCommand(new ListCommand("list", "l"), null);

        commandRegistry.registerCommand(new PlayCommand("play", "p"), null);
        commandRegistry.registerCommand(new SearchCommand("search", "se"), null);
        commandRegistry.registerCommand(new RePlayCommand("replay", "re"), null);
        commandRegistry.registerCommand(new InterruptCommand("interrupt", "in"), null);
        commandRegistry.registerCommand(new PlaylistCommand("playlist", "pl"), null);
        commandRegistry.registerCommand(new PauseCommand("pause"), null);
        commandRegistry.registerCommand(new StopCommand("stop", "st", "s"), null);
        commandRegistry.registerCommand(new SkipCommand("skip", "sk"), null);
        commandRegistry.registerCommand(new SeekCommand("seek"), null);
        commandRegistry.registerCommand(new VolumeCommand("volume", "vol"), null);
        commandRegistry.registerCommand(new RepeatCommand("repeat", "rep"), null);
        commandRegistry.registerCommand(new ShuffleCommand("shuffle", "sh"), null);

        commandRegistry.registerCommand(new DeleteCommand("delete", "clean"), null);

        commandRegistry.registerCommand(new SystemCommand("system", "sinfo"), null);
        commandRegistry.registerCommand(new NodesCommand("nodes", "node"), null);
        commandRegistry.registerCommand(new ModuleCommand("module", "mod"), null);
        if (Main.isDebugMode()) commandRegistry.registerCommand(new UpdateCommand("update"), null);
        commandRegistry.registerCommand(new ShutdownCommand("shutdown", "exit"), null);
    }

    private JDA getJdaFromId(int shardId) {
        if (shardManager != null)
            return shardManager.getShardById(shardId);
        return null;
    }

    private int getShardsTotal() {
        if (shardManager != null)
            return shardManager.getShardsTotal();
        return 1;
    }

    @Override
    public NeoJukeConfig getConfig() {
        return config;
    }

    @Override
    public DatabaseConnector getConnector() {
        return connector;
    }

    @Override
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public ShardManager getShardManager() {
        return shardManager;
    }

    @Override
    public JdaLavalink getLavaLink() {
        return lavalink;
    }

    @Override
    public GuildPlayerRegistry getPlayerRegistry() {
        return playerRegistry;
    }
}
