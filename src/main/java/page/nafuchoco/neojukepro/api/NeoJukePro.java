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

package page.nafuchoco.neojukepro.api;

import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.sharding.ShardManager;
import page.nafuchoco.neojukepro.core.command.CommandRegistry;
import page.nafuchoco.neojukepro.core.config.NeoJukeConfig;
import page.nafuchoco.neojukepro.core.database.DatabaseConnector;
import page.nafuchoco.neojukepro.core.guild.NeoGuildRegistry;
import page.nafuchoco.neojukepro.core.http.discord.DiscordAppInfo;
import page.nafuchoco.neojukepro.core.module.ModuleManager;
import page.nafuchoco.neojukepro.core.player.CustomSourceRegistry;

public interface NeoJukePro {

    NeoJukeConfig getConfig();

    DatabaseConnector getConnector();

    /**
     * @since v2.0
     */
    DiscordAppInfo getDiscordAppInfo();

    /**
     * @since v2.0
     */
    NeoGuildRegistry getGuildRegistry();

    CustomSourceRegistry getCustomSourceRegistry();

    CommandRegistry getCommandRegistry();

    ModuleManager getModuleManager();

    ShardManager getShardManager();

    JdaLavalink getLavaLink();
}
