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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CustomPlaylistBuilder implements AudioLoadResultHandler {
    private final AudioPlayerManager playerManager;
    private final String name;
    private final List<PlaylistItem> playlist = new LinkedList<>();


    private PlaylistItem loadedItem;

    public CustomPlaylistBuilder(String name, AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
        this.name = name;
    }

    @Deprecated
    public PlaylistItem loadAndAddTrack(String url) {
        throw new UnsupportedOperationException("");
    }

    public void addTrack(AudioTrack track) {
        PlaylistItem item = new PlaylistItem(track.getInfo().title, track.getInfo().uri);
        playlist.add(item);
    }

    private CustomPlaylist build() {
        return new CustomPlaylist(name, Collections.unmodifiableList(playlist));
    }

    @Override
    public void trackLoaded(AudioTrack track) {

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

    }

    @Override
    public void noMatches() {

    }

    @Override
    public void loadFailed(FriendlyException exception) {

    }
}
