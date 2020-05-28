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

package page.nafuchoco.neojukepro.core.playlist;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import page.nafuchoco.neojukepro.core.MessageManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CustomPlaylistBuilder implements AudioLoadResultHandler {
    private final AudioPlayerManager playerManager;
    private final String name;
    private final Guild guild;
    private final List<PlaylistItem> playlist = new LinkedList<>();

    private volatile PlaylistItem loadedItem;

    public CustomPlaylistBuilder(AudioPlayerManager playerManager, String name, Guild guild) {
        this.playerManager = playerManager;
        this.name = name;
        this.guild = guild;
    }

    public CustomPlaylistBuilder loadAndAddTrack(String url) {
        loadedItem = null;
        playerManager.loadItemOrdered(this, url, this);
        while (loadedItem == null) {
            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                log.warn("Oops...!", e); // TODO: 2020/05/20
            }
        }
        if (loadedItem.getName() != null)
            playlist.add(loadedItem);
        return this;
    }

    public CustomPlaylistBuilder addTrack(AudioTrack track) {
        PlaylistItem item = new PlaylistItem(track.getInfo().title, track.getSourceManager().getSourceName(), track.getInfo().uri);
        playlist.add(item);
        return this;
    }

    public CustomPlaylist build() {
        return new CustomPlaylist(UUID.randomUUID().toString(), guild.getIdLong(), name, Collections.unmodifiableList(playlist));
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        loadedItem = new PlaylistItem(track.getInfo().title, track.getSourceManager().getSourceName(), track.getInfo().uri);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void noMatches() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        loadedItem = new PlaylistItem(null, null, null);
        log.warn(MessageManager.getMessage("player.loader.failed"));
    }
}
