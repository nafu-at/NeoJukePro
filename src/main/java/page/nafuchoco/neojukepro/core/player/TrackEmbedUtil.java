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

import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeObjectItem;

import java.awt.*;
import java.io.IOException;

public class TrackEmbedUtil {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private static final YouTubeAPIClient client;
    private static final Color YOUTUBE = new Color(255, 0, 0);
    private static final Color SOUNDCLOUD_COLOR = new Color(255, 85, 0);
    private static final Color BANDCAMP_COLOR = new Color(0, 161, 198);
    private static final Color TWITCH_COLOR = new Color(75, 54, 124);
    private static final Color VIMEO_COLOR = new Color(15, 174, 241);
    private static final Color BLACK = new Color(0, 0, 0);

    private TrackEmbedUtil() {
        throw new UnsupportedOperationException();
    }

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(launcher.getConfig().getAdvancedConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        client = apiClient;
    }

    public static MessageEmbed getTrackEmbed(NeoGuildPlayer audioPlayer) throws IOException {
        LoadedTrackContext trackContext = audioPlayer.getPlayingTrack();
        if (trackContext != null) {
            AudioTrack audioTrack = trackContext.getTrack();
            if (audioTrack instanceof YoutubeAudioTrack) {
                return getYouTubeEmbed(audioPlayer);
            } else if (audioTrack instanceof SoundCloudAudioTrack) {
                return getDefaultEmbed(audioPlayer, SOUNDCLOUD_COLOR);
            } else if (audioTrack instanceof BandcampAudioTrack) {
                return getDefaultEmbed(audioPlayer, BANDCAMP_COLOR);
            } else if (audioTrack instanceof VimeoAudioTrack) {
                return getDefaultEmbed(audioPlayer, VIMEO_COLOR);
            } else if (audioTrack instanceof TwitchStreamAudioTrack) {
                return getDefaultEmbed(audioPlayer, TWITCH_COLOR);
            } else if (audioTrack instanceof HttpAudioTrack) {
                return getDefaultEmbed(audioPlayer, BLACK);
            } else if (audioTrack instanceof LocalAudioTrack) {
                return getDefaultEmbed(audioPlayer, BLACK);
            } else if (audioTrack.getSourceManager() instanceof CustomAudioSourceManager) {
                return ((CustomAudioSourceManager) audioTrack.getSourceManager()).getNowPlayingEmbed(audioPlayer);
            } else {
                return getDefaultEmbed(audioPlayer, BLACK);
            }
        }
        return null;
    }

    public static MessageEmbed getYouTubeEmbed(NeoGuildPlayer audioPlayer) throws IOException {
        if (client == null)
            return getDefaultEmbed(audioPlayer, YOUTUBE);

        YouTubeObjectItem youtubeVideo = client.getYoutubeObjects(YouTubeAPIClient.YOUTUBE_VIDEO,
                audioPlayer.getPlayingTrack().getTrack().getIdentifier()).getItems()[0];
        YouTubeObjectItem youtubeChannel = client.getYoutubeObjects(YouTubeAPIClient.YOUTUBE_CHANNEL,
                youtubeVideo.getSnippet().getChannelID()).getItems()[0];

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(youtubeVideo.getSnippet().getLocalized().getTitle(),
                "https://www.youtube.com/watch?v=" + youtubeVideo.getID());
        builder.setColor(YOUTUBE);
        builder.setAuthor(youtubeChannel.getSnippet().getLocalized().getTitle(),
                "https://www.youtube.com/channel/" + youtubeVideo.getSnippet().getChannelID(),
                youtubeChannel.getSnippet().getThumbnails().getHigh().getURL());
        builder.setThumbnail(youtubeVideo.getSnippet().getThumbnails().getHigh().getURL());
        MessageEmbed.Field time = new MessageEmbed.Field("Time",
                "[" + MessageUtil.formatTime(audioPlayer.getTrackPosition()) + "/" + MessageUtil.formatTime(audioPlayer.getPlayingTrack().getTrack().getDuration()) + "]", true);
        builder.addField(time);

        String descMessage = youtubeVideo.getSnippet().getLocalized().getDescription();
        if (descMessage.length() > 800)
            descMessage = descMessage.substring(0, 800) + " [...]";
        MessageEmbed.Field description = new MessageEmbed.Field("Description", descMessage, false);
        builder.addField(description);
        builder.setFooter(MessageUtil.format(
                MessageManager.getMessage(
                        audioPlayer.getNeoGuild().getSettings().getLang(),
                        "command.nowplay.request"),
                audioPlayer.getPlayingTrack().getInvoker().getJDAMember().getEffectiveName()),
                audioPlayer.getPlayingTrack().getInvoker().getJDAMember().getUser().getAvatarUrl());
        return builder.build();
    }

    public static MessageEmbed getDefaultEmbed(NeoGuildPlayer audioPlayer, Color color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(audioPlayer.getPlayingTrack().getTrack().getInfo().title);
        builder.setColor(color);
        builder.setAuthor(audioPlayer.getPlayingTrack().getTrack().getInfo().author);
        MessageEmbed.Field time = new MessageEmbed.Field("Time",
                "[" + MessageUtil.formatTime(audioPlayer.getTrackPosition()) + "/" + MessageUtil.formatTime(audioPlayer.getPlayingTrack().getTrack().getDuration()) + "]",
                true);
        builder.addField(time);
        MessageEmbed.Field source = new MessageEmbed.Field("",
                "Loaded from " + audioPlayer.getPlayingTrack().getTrack().getSourceManager().getSourceName() + ".", false);
        builder.addField(source);
        builder.setFooter(MessageUtil.format(
                MessageManager.getMessage(
                        audioPlayer.getNeoGuild().getSettings().getLang(),
                        "command.nowplay.request"),
                audioPlayer.getPlayingTrack().getInvoker().getJDAMember().getEffectiveName()),
                audioPlayer.getPlayingTrack().getInvoker().getJDAMember().getUser().getAvatarUrl());
        return builder.build();
    }
}
