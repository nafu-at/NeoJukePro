/*
 * Copyright 2020 なふちょこっと。
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

package page.nafuchoco.neojukepro.core.player;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.entities.Guild;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildPlayerRegistry {
    private final Map<Guild, GuildAudioPlayer> players = new HashMap<>();
    private final JdaLavalink lavalink;
    private final AudioPlayerManager playerManager;

    public GuildPlayerRegistry(AudioPlayerManager playerManager, JdaLavalink lavalink, CustomSourceRegistry sourceRegistry) {
        this.playerManager = playerManager;
        this.lavalink = lavalink;

        playerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new GetyarnAudioSourceManager());

        sourceRegistry.getSources().forEach(playerManager::registerSourceManager);

        playerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public GuildAudioPlayer getGuildAudioPlayer(Guild guild) {
        GuildAudioPlayer player = players.computeIfAbsent(guild,
                key -> new GuildAudioPlayer(guild, playerManager, lavalink != null ? lavalink.getLink(guild) : null));
        if (player.getSendHandler() != null)
            guild.getAudioManager().setSendingHandler(player.getSendHandler());
        return player;
    }

    public void destroyPlayer(Guild guild) {
        GuildAudioPlayer player = players.get(guild);
        if (player != null) {
            if (player.getLink() == null) {
                player.stop();
                player.leaveChannel();
            } else if (player.getLink().getState() == Link.State.DESTROYED) {
                MessageUtil.sendMessage(guild, MessageManager.getMessage("player.destroyed"));
            } else {
                player.destroy();
            }
            players.remove(guild);
        }
    }

    public List<GuildAudioPlayer> getPlayers() {
        return new ArrayList<>(players.values());
    }
}
