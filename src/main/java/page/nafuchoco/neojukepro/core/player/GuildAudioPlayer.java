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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.BooleanUtils;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandCache;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.database.GuildSettingsTable;
import page.nafuchoco.neojukepro.core.database.RepeatType;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeSearchResults;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class GuildAudioPlayer extends PlayerEventListenerAdapter {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private static final YouTubeAPIClient client;

    private Guild guild;
    private AudioPlayerManager audioPlayerManager;
    private JdaLink link;
    private IPlayer player;
    private GuildTrackProvider trackProvider;

    private boolean isShuffle = false;
    private RepeatType repeatType;

    // TODO: 2020/07/21 Synchronizedへの置き換え作業
    private volatile GuildTrackContext nowPlaying;

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(launcher.getConfig().getAdvancedConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        client = apiClient;
    }

    public GuildAudioPlayer(Guild guild, AudioPlayerManager audioPlayerManager, JdaLink link) {
        this.guild = guild;
        this.audioPlayerManager = audioPlayerManager;
        this.link = link;
        player = this.link != null ? this.link.getPlayer() :
                new LavaplayerPlayerWrapper(audioPlayerManager.createPlayer());
        player.addListener(this);
        trackProvider = new GuildTrackProvider(this);
    }

    public void joinChannel(VoiceChannel targetChannel) throws InsufficientPermissionException {
        if (link != null) {
            link.connect(targetChannel);
        } else {
            AudioManager audioManager = guild.getAudioManager();
            audioManager.openAudioConnection(targetChannel);
        }
    }

    public void leaveChannel() {
        if (link != null) {
            link.disconnect();
        } else {
            AudioManager audioManager = guild.getAudioManager();
            audioManager.closeAudioConnection();
        }
    }


    public void play() {
        if (nowPlaying == null) {
            nowPlaying = trackProvider.provideTrack();
            if (nowPlaying != null)
                player.playTrack(nowPlaying.getTrack());
        } else if (player.isPaused() && nowPlaying != null && player.getPlayingTrack() == null) {
            nowPlaying = nowPlaying.makeClone();
            player.playTrack(nowPlaying.getTrack());
        }
        if (player.isPaused())
            player.setPaused(false);
    }

    public void play(GuildTrackContext context, int desiredNumber) {
        if (nowPlaying == null) {
            player.playTrack(context.getTrack());
            nowPlaying = context;
        } else {
            trackProvider.queue(context, desiredNumber);
        }
        if (player.isPaused())
            player.setPaused(false);
    }

    public void play(List<GuildTrackContext> contextList, int desiredNumber) {
        trackProvider.queue(contextList, desiredNumber);
        play();
    }

    public void play(AudioTrackLoader loader) {
        loader.setAudioPlayer(this);
        loader.loadTrack();
    }

    public void stop() {
        trackProvider.clearTracks();
        nowPlaying = null;
        player.stopTrack();
    }

    public void skip() {
        player.stopTrack();
    }

    public List<GuildTrackContext> skip(int below) {
        List<GuildTrackContext> deleted;
        if (below == 0) {
            deleted = trackProvider.skip(below + 1);
            skip();
        } else {
            deleted = trackProvider.skip(below);
        }
        return deleted;
    }

    public List<GuildTrackContext> skip(int from, int to) {
        List<GuildTrackContext> deleted;
        if (from == 0) {
            deleted = trackProvider.skip(from + 1, to);
            skip();
        } else {
            deleted = trackProvider.skip(from, to);
        }
        return deleted;
    }

    public List<GuildTrackContext> skip(Member invoker) {
        return trackProvider.skip(invoker);
    }

    public void seekTo(long position) {
        player.seekTo(position);
    }


    public GuildTrackContext getNowPlaying() {
        return nowPlaying;
    }

    public int getVolume() {
        return player.getVolume();
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
        if (isShuffle)
            trackProvider.shuffle();
    }

    public RepeatType getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(RepeatType repeatType) {
        this.repeatType = repeatType;
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

    public Guild getGuild() {
        return guild;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public JdaLink getLink() {
        return link;
    }

    public GuildTrackProvider getTrackProvider() {
        return trackProvider;
    }

    protected AudioPlayerSendHandler getSendHandler() {
        return link == null ? new AudioPlayerSendHandler(player) : null;
    }

    protected void destroy() {
        if (link != null) {
            stop();
            player.removeListener(this);
            link.destroy();
        }
    }


    @Override
    public void onPlayerPause(IPlayer player) {
        MessageUtil.sendMessage(guild, MessageManager.getMessage("player.pause"));
    }

    @Override
    public void onPlayerResume(IPlayer player) {
        MessageUtil.sendMessage(guild, MessageManager.getMessage("player.resume"));
    }

    @Override
    public void onTrackStart(IPlayer player, AudioTrack track) {
        super.onTrackStart(player, track);
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason == AudioTrackEndReason.FINISHED || endReason == AudioTrackEndReason.STOPPED) {
            if (endReason == AudioTrackEndReason.FINISHED) {
                if (nowPlaying != null &&
                        repeatType == RepeatType.SINGLE) {
                    GuildTrackContext newTrack = nowPlaying.makeClone();
                    nowPlaying = null;
                    play(newTrack, 0);
                    return;
                }
            }

            if (nowPlaying != null && repeatType == RepeatType.ALL) {
                play(nowPlaying.makeClone(), 0);
            }

            if (nowPlaying.getTrack() instanceof YoutubeAudioTrack
                    && trackProvider.getQueues().isEmpty()
                    && client != null
                    && launcher.getConfig().getAdvancedConfig().isEnableRelatedVideoSearch()) {
                GuildSettingsTable settingsTable = (GuildSettingsTable) CommandCache.getCache(null, "settingsTable");
                boolean autoplay = false;
                try {
                    autoplay = BooleanUtils.toBoolean(settingsTable.getGuildSetting(getGuild().getIdLong(), "autoplay"));
                } catch (SQLException e) {
                    log.error(MessageManager.getMessage("system.db.retrieving.error"), e);
                }
                if (autoplay) {
                    try {
                        YouTubeSearchResults results =
                                client.searchVideos(YouTubeAPIClient.SearchType.RELATED, nowPlaying.getTrack().getIdentifier(), null);
                        play(new AudioTrackLoader("https://www.youtube.com/watch?v=" + results.getItems()[0].getID().getVideoID(),
                                nowPlaying.getInvoker(), 0));
                    } catch (IOException e) {
                        log.warn(MessageManager.getMessage("command.play.search.failed"));
                    }
                }
            }

            nowPlaying = null;
            play();
        } else {
            nowPlaying = null;
        }
    }

    @Override
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
        super.onTrackException(player, track, exception);
    }

    @Override
    public void onTrackStuck(IPlayer player, AudioTrack track, long thresholdMs) {
        super.onTrackStuck(player, track, thresholdMs);
    }
}
