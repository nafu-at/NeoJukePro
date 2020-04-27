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
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.discord.guild.GuildSettings;

import java.util.List;

public class GuildAudioPlayer extends PlayerEventListenerAdapter {
    private Guild guild;
    private AudioPlayerManager audioPlayerManager;
    private JdaLink link;
    private IPlayer player;
    private GuildTrackProvider trackProvider;

    private boolean isShuffle = false;
    private GuildSettings.REPEATTYPE repeatType;

    private volatile GuildTrackContext nowPlaying;

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
    }

    public void play(GuildTrackContext context, int desiredNumber) {
        if (nowPlaying == null) {
            player.playTrack(context.getTrack());
            nowPlaying = context;
        } else {
            trackProvider.queue(context, desiredNumber);
        }
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

    public GuildSettings.REPEATTYPE getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(GuildSettings.REPEATTYPE repeatType) {
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
        MessageUtil.sendMessage(guild, "Playback was paused. Please enter it again to resume.");
    }

    @Override
    public void onPlayerResume(IPlayer player) {
        MessageUtil.sendMessage(guild, "Resume playback.");
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
                        repeatType == GuildSettings.REPEATTYPE.SINGLE) {
                    GuildTrackContext newTrack = nowPlaying.makeClone();
                    nowPlaying = null;
                    play(newTrack, 0);
                    return;
                }
            }

            if (nowPlaying != null && repeatType == GuildSettings.REPEATTYPE.ALL) {
                play(nowPlaying.makeClone(), 0);
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
