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

package page.nafuchoco.neojukepro.core.command;

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
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
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

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(launcher.getConfig().getAdvancedConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        client = apiClient;
    }

    public static MessageEmbed getTrackEmbed(AudioTrack audioTrack) throws IOException {
        if (audioTrack instanceof YoutubeAudioTrack) {
            return getYouTubeEmbed(audioTrack);
        } else if (audioTrack instanceof SoundCloudAudioTrack) {
            return getDefaultEmbed(audioTrack, SOUNDCLOUD_COLOR);
        } else if (audioTrack instanceof BandcampAudioTrack) {
            return getDefaultEmbed(audioTrack, BANDCAMP_COLOR);
        } else if (audioTrack instanceof VimeoAudioTrack) {
            return getDefaultEmbed(audioTrack, VIMEO_COLOR);
        } else if (audioTrack instanceof TwitchStreamAudioTrack) {
            return getDefaultEmbed(audioTrack, TWITCH_COLOR);
        } else if (audioTrack instanceof HttpAudioTrack) {
            return getDefaultEmbed(audioTrack, BLACK);
        } else if (audioTrack instanceof LocalAudioTrack) {
            return getDefaultEmbed(audioTrack, BLACK);
        }
        return null;
    }

    private static MessageEmbed getYouTubeEmbed(AudioTrack audioTrack) throws IOException, NullPointerException {
        if (client == null)
            return getDefaultEmbed(audioTrack, YOUTUBE);

        YouTubeObjectItem youtubeVideo = client.getYoutubeObjects(YouTubeAPIClient.YOUTUBE_VIDEO,
                audioTrack.getIdentifier()).getItems()[0];
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

        String descMessage = youtubeVideo.getSnippet().getLocalized().getDescription();
        if (descMessage.length() > 800)
            descMessage = descMessage.substring(0, 800) + " [...]";
        MessageEmbed.Field description = new MessageEmbed.Field("Description", descMessage, false);
        builder.addField(description);
        return builder.build();
    }

    private static MessageEmbed getDefaultEmbed(AudioTrack audioTrack, Color color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(audioTrack.getInfo().title);
        builder.setColor(color);
        builder.setAuthor(audioTrack.getInfo().author);
        MessageEmbed.Field source = new MessageEmbed.Field("",
                "Loaded from " + audioTrack.getSourceManager().getSourceName() + ".", false);
        builder.addField(source);
        return builder.build();
    }
}
