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
import net.dv8tion.jda.api.entities.Member;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.ExceptionUtil;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.config.MusicSourceSection;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class AudioTrackLoader implements AudioLoadResultHandler {
    private static MusicSourceSection musicSource = Main.getLauncher().getConfig().getBasicConfig().getMusicSource();

    private GuildAudioPlayer audioPlayer;

    private final String loadTrackUrl;
    private final Member invoker;
    private final int desiredNumber;

    public AudioTrackLoader(String loadTrackUrl, Member invoker, int desiredNumber) {
        this.loadTrackUrl = loadTrackUrl;
        this.invoker = invoker;
        this.desiredNumber = desiredNumber;
    }

    protected void setAudioPlayer(GuildAudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    protected void loadTrack() {
        if (audioPlayer == null)
            throw new IllegalStateException(MessageManager.getMessage("player.loader.error"));
        audioPlayer.getAudioPlayerManager().loadItemOrdered(this, loadTrackUrl, this);
    }

    public int getDesiredNumber() {
        return desiredNumber;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if (!checkAudioSorce(track))
            return;

        audioPlayer.play(new GuildTrackContext(invoker.getGuild(), invoker, track), desiredNumber);
        if (audioPlayer.getTrackProvider().getQueues().size() == 0)
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.playing"), track.getInfo().title));
        else
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.addqueue"), track.getInfo().title));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        List<GuildTrackContext> contextList = new LinkedList<>();
        AudioTrack firstTrack = playlist.getSelectedTrack();
        playlist.getTracks().forEach(track -> {
            if (!track.equals(firstTrack))
                contextList.add(new GuildTrackContext(invoker.getGuild(), invoker, track));
        });
        if (firstTrack != null)
            contextList.add(0, new GuildTrackContext(invoker.getGuild(), invoker, firstTrack));
        audioPlayer.play(contextList, desiredNumber);
        MessageUtil.sendMessage(audioPlayer.getGuild(),
                MessageUtil.format(MessageManager.getMessage("player.playlist.add"), playlist.getTracks().size(), playlist.getName()));
    }

    @Override
    public void noMatches() {

    }

    @Override
    public void loadFailed(FriendlyException exception) {
        ExceptionUtil.sendStackTrace(audioPlayer.getGuild(), exception, MessageManager.getMessage("player.loader.failed"));
    }

    private boolean checkAudioSorce(AudioTrack track) {
        if (track instanceof YoutubeAudioTrack && !musicSource.enableYoutube()) {
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.source.disable"), "YouTube"));
            return false;
        } else if (track instanceof SoundCloudAudioTrack && !musicSource.enableSoundCloud()) {
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.source.disable"), "SoundCloud"));
            return false;
        } else if (track instanceof BandcampAudioTrack && !musicSource.enableBandCamp()) {
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.source.disable"), "BandCamp"));
            return false;
        } else if (track instanceof VimeoAudioTrack && !musicSource.enableVimeo()) {
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.source.disable"), "Vimeo"));
            return false;
        } else if (track instanceof TwitchStreamAudioTrack && !musicSource.enableTwitch()) {
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.source.disable"), "Twitch"));
            return false;
        } else if (track instanceof HttpAudioTrack && !musicSource.enableHttp()) {
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.source.disable"), "HTTP"));
            return false;
        } else if (track instanceof LocalAudioTrack && !musicSource.enableLocal()) {
            MessageUtil.sendMessage(audioPlayer.getGuild(),
                    MessageUtil.format(MessageManager.getMessage("player.source.disable"), "Local"));
            return false;
        }
        return true;
    }
}
