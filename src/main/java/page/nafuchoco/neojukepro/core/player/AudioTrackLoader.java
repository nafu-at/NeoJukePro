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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeConfig;
import page.nafuchoco.neojukepro.core.utils.ExceptionUtil;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;
import page.nafuchoco.neojukepro.core.utils.URLUtils;
import page.nafuchoco.neojukepro.module.NeoJuke;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class AudioTrackLoader implements AudioLoadResultHandler {
    private static final NeoJukeConfig.MusicSourceSection musicSource = NeoJuke.getInstance().getConfig().getBasicConfig().getMusicSource();

    private NeoGuildPlayer audioPlayer;

    private final TrackContext trackContext;

    public AudioTrackLoader(TrackContext trackContext) {
        this.trackContext = trackContext;
    }

    protected void setAudioPlayer(NeoGuildPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    protected void loadTrack() {
        if (audioPlayer == null)
            throw new IllegalStateException(MessageManager.getMessage("player.loader.error"));
        audioPlayer.getNeoGuild().getAudioPlayerManager().loadItemOrdered(this, trackContext.getTrackUrl(), this);
    }

    public TrackContext getTrackContext() {
        return trackContext;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if (!checkAudioSource(track))
            return;

        var position = 0;
        try {
            String time = URLUtils.parseUrl(getTrackContext().getTrackUrl()).getQuery().get("t");
            position = time != null ? NumberUtils.toInt(time.replace("s", "")) : 0;
        } catch (MalformedURLException e) {
            // nothing.
        }
        audioPlayer.play(new LoadedTrackContext(getTrackContext(), position * 1000L, track));
        if (audioPlayer.getTrackProvider().getQueues().isEmpty())
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.playing"),
                    track.getInfo().title));
        else
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.addqueue"),
                    track.getInfo().title));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        List<LoadedTrackContext> contextList = new LinkedList<>();
        AudioTrack firstTrack = playlist.getSelectedTrack();
        playlist.getTracks().forEach(track -> {
            if (!track.equals(firstTrack))
                contextList.add(new LoadedTrackContext(getTrackContext(), 0, track));
        });
        if (firstTrack != null) {
            var position = 0;
            try {
                String time = URLUtils.parseUrl(getTrackContext().getTrackUrl()).getQuery().get("t");
                position = time != null ? NumberUtils.toInt(time.replace("s", "")) : 0;
            } catch (MalformedURLException e) {
                // nothing.
            }
            contextList.add(0, new LoadedTrackContext(getTrackContext(), position * 1000L, firstTrack));
        }
        audioPlayer.play(contextList);
        trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                MessageManager.getMessage("player.playlist.add"),
                playlist.getTracks().size(), playlist.getName()));
    }

    @Override
    public void noMatches() {
        trackContext.getNeoGuild().sendMessageToLatest(
                MessageManager.getMessage("player.loader.notfound")
        );
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (exception.severity == FriendlyException.Severity.COMMON) {
            try {
                URLUtils.URLStructure url = URLUtils.parseUrl(trackContext.getTrackUrl());
                if (!url.getPath().equals("playlist") && url.getQuery().get("list") != null) {
                    url.getQuery().remove("list");
                    url.getQuery().remove("index");
                    trackContext.getNeoGuild().getAudioPlayer().play(
                            new AudioTrackLoader(
                                    new TrackContext(
                                            trackContext.getNeoGuild(),
                                            trackContext.getInvoker(),
                                            trackContext.getInterruptNumber(),
                                            URLUtils.buildURL(url).toString())
                            )
                    );
                } else {
                    trackContext.getNeoGuild().sendMessageToLatest(
                            MessageManager.getMessage("player.loader.notfound")
                    );
                }
            } catch (MalformedURLException e) {
                // do nothing.
            }
        } else {
            ExceptionUtil.sendStackTrace(
                    audioPlayer.getNeoGuild(),
                    exception,
                    MessageManager.getMessage("player.loader.failed")
            );
        }
    }

    private boolean checkAudioSource(AudioTrack track) {
        if (track instanceof YoutubeAudioTrack && !musicSource.enableYoutube()) {
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.source.disable"),
                    "YouTube"));
            return false;
        } else if (track instanceof SoundCloudAudioTrack && !musicSource.enableSoundCloud()) {
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.source.disable"),
                    "SoundCloud"));
            return false;
        } else if (track instanceof BandcampAudioTrack && !musicSource.enableBandCamp()) {
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.source.disable"),
                    "BandCamp"));
            return false;
        } else if (track instanceof VimeoAudioTrack && !musicSource.enableVimeo()) {
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.source.disable"),
                    "Vimeo"));
            return false;
        } else if (track instanceof TwitchStreamAudioTrack && !musicSource.enableTwitch()) {
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.source.disable"),
                    "Twitch"));
            return false;
        } else if (track instanceof HttpAudioTrack && !musicSource.enableHttp()) {
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.source.disable"),
                    "HTTP"));
            return false;
        } else if (track instanceof LocalAudioTrack && !musicSource.enableLocal()) {
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.source.disable"),
                    "Local"));
            return false;
        } else if (audioPlayer.getPlayerOptions().getDisabledSources().contains(track.getSourceManager().getSourceName())) {
            trackContext.getNeoGuild().sendMessageToLatest(MessageUtil.format(
                    MessageManager.getMessage("player.source.disable"),
                    track.getSourceManager().getSourceName()));
            return false;
        }
        return true;
    }
}
