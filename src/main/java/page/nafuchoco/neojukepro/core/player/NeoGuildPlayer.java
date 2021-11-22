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

package page.nafuchoco.neojukepro.core.player;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.RandomUtils;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;
import page.nafuchoco.neojukepro.core.guild.NeoGuildPlayerOptions;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeSearchResults;

import java.io.IOException;
import java.util.List;

@Slf4j
@Getter
public class NeoGuildPlayer extends PlayerEventListenerAdapter {
    private static final YouTubeAPIClient client;

    private final NeoJukePro neoJukePro;
    private final NeoGuild neoGuild;
    private final JdaLink link;
    private final IPlayer player;
    private final GuildTrackProvider trackProvider;

    private LoadedTrackContext playingTrack;

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(Main.getLauncher().getConfig().getAdvancedConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        client = apiClient;
    }


    public NeoGuildPlayer(NeoJukePro neoJukePro, NeoGuild neoGuild, JdaLink link) {
        this.neoJukePro = neoJukePro;
        this.neoGuild = neoGuild;
        this.link = link;
        player = this.link != null ? this.link.getPlayer() :
                new LavaplayerPlayerWrapper(getNeoGuild().getAudioPlayerManager().createPlayer());
        player.addListener(this);
        trackProvider = new GuildTrackProvider(this);
    }

    public NeoGuildPlayerOptions getPlayerOptions() {
        return neoGuild.getSettings().getPlayerOptions();
    }

    public void joinChannel(VoiceChannel targetChannel) throws InsufficientPermissionException {
        if (link != null) {
            link.connect(targetChannel);
        } else {
            AudioManager audioManager = getNeoGuild().getJDAGuild().getAudioManager();
            audioManager.openAudioConnection(targetChannel);
        }
    }

    public void leaveChannel() {
        if (link != null) {
            link.disconnect();
        } else {
            AudioManager audioManager = getNeoGuild().getJDAGuild().getAudioManager();
            audioManager.closeAudioConnection();
        }
    }


    public synchronized void play() {
        if (playingTrack == null) {
            playingTrack = trackProvider.provideTrack();
            if (playingTrack != null) {
                player.playTrack(playingTrack.getTrack());
                player.seekTo(playingTrack.getStartPosition());
            }
        } else if (player.isPaused() && player.getPlayingTrack() == null) {
            playingTrack = playingTrack.makeClone(0);
            player.playTrack(playingTrack.getTrack());
            if (playingTrack.getStartPosition() != 0)
                player.seekTo(playingTrack.getStartPosition());
        }
        if (player.isPaused())
            player.setPaused(false);

        // 音量が100%になる問題への一時的な対処
        player.setVolume(getNeoGuild().getSettings().getPlayerOptions().getVolumeLevel());
    }

    public synchronized void play(LoadedTrackContext context) {
        if (playingTrack == null) {
            player.playTrack(context.getTrack());
            if (context.getStartPosition() != 0)
                player.seekTo(context.getStartPosition());
            playingTrack = context;
        } else {
            trackProvider.queue(context);
        }
        if (player.isPaused())
            player.setPaused(false);

        // 音量が100%になる問題への一時的な対処
        player.setVolume(getNeoGuild().getSettings().getPlayerOptions().getVolumeLevel());
    }

    public synchronized void replay() {
        if (playingTrack != null) {
            trackProvider.queue(playingTrack.makeClone(1));
            playingTrack = null;
            skip();
        }
    }

    public void play(List<LoadedTrackContext> contextList) {
        trackProvider.queue(contextList);
        play();
    }

    public void play(AudioTrackLoader loader) {
        loader.setAudioPlayer(this);
        loader.loadTrack();
    }

    public synchronized void stop() {
        trackProvider.clearTracks();
        playingTrack = null;
        player.stopTrack();
    }

    public void skip() {
        player.stopTrack();
    }

    public List<LoadedTrackContext> skip(int below) {
        List<LoadedTrackContext> deleted;
        if (below == 0) {
            deleted = trackProvider.skip(below + 1);
            skip();
        } else {
            deleted = trackProvider.skip(below);
        }
        return deleted;
    }

    public List<LoadedTrackContext> skip(int from, int to) {
        List<LoadedTrackContext> deleted;
        if (from <= 0) {
            deleted = trackProvider.skip(from + 1, to);
            skip();
        } else {
            deleted = trackProvider.skip(from, to);
        }
        return deleted;
    }

    public List<LoadedTrackContext> skip(Member invoker) {
        return trackProvider.skip(invoker);
    }

    public void seekTo(long position) {
        player.seekTo(position);
    }


    public synchronized LoadedTrackContext getPlayingTrack() {
        return playingTrack;
    }

    public int getVolume() {
        return player.getVolume();
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    public void setPaused(boolean b) {
        player.setPaused(b);
    }

    public long getTrackPosition() {
        return player.getTrackPosition();
    }

    public AudioPlayerSendHandler getSendHandler() {
        return link == null ? new AudioPlayerSendHandler(player) : null;
    }

    public void destroy() {
        if (link != null) {
            stop();
            player.removeListener(this);
            link.destroy();
        }
    }

    @Override
    public void onPlayerPause(IPlayer player) {
        getNeoGuild().sendMessageToLatest(MessageManager.getMessage(neoGuild.getSettings().getLang(), "player.pause"));
    }

    @Override
    public void onPlayerResume(IPlayer player) {
        getNeoGuild().sendMessageToLatest(MessageManager.getMessage(neoGuild.getSettings().getLang(), "player.resume"));
    }

    @Override
    public synchronized void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason == AudioTrackEndReason.FINISHED || endReason == AudioTrackEndReason.STOPPED) {
            if (endReason == AudioTrackEndReason.FINISHED
                    && playingTrack != null
                    && getNeoGuild().getSettings().getPlayerOptions().getRepeatMode() == NeoGuildPlayerOptions.RepeatMode.SINGLE) {
                LoadedTrackContext newTrack = playingTrack.makeClone(0);
                playingTrack = null;
                play(newTrack);
                return;
            }

            if (playingTrack != null && getNeoGuild().getSettings().getPlayerOptions().getRepeatMode() == NeoGuildPlayerOptions.RepeatMode.ALL) {
                play(playingTrack.makeClone(0));
            }

            if (playingTrack != null
                    && playingTrack.getTrack() instanceof YoutubeAudioTrack
                    && trackProvider.getQueues().isEmpty()
                    && client != null
                    && getNeoJukePro().getConfig().getAdvancedConfig().isEnableRelatedVideoSearch()
                    && getNeoGuild().getSettings().isJukeboxMode()) {
                try {
                    YouTubeSearchResults results =
                            client.searchVideos(YouTubeAPIClient.SearchType.RELATED, playingTrack.getTrack().getIdentifier(), null);
                    play(new AudioTrackLoader(new TrackContext(getNeoGuild(), getPlayingTrack().getInvoker(), 0, "https://www.youtube.com/watch?v=" + results.getItems()[RandomUtils.nextInt(0, 4)].getID().getVideoID())));
                } catch (IOException e) {
                    log.warn(MessageManager.getMessage("command.play.search.failed"));
                }
            }

            playingTrack = null;
            play();
        } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {
            playingTrack = null;
            play();
        } else {
            playingTrack = null;
        }
    }
}
