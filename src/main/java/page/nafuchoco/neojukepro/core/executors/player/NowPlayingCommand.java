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

package page.nafuchoco.neojukepro.core.executors.player;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.SubCommandOption;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeObjectItem;
import page.nafuchoco.neojukepro.core.player.LoadedTrackContext;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.utils.ExceptionUtil;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;
import page.nafuchoco.neojukepro.core.utils.TrackEmbedUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class NowPlayingCommand extends CommandExecutor {
    private static final YouTubeAPIClient YOUTUBE_CLIENT;

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(NeoJuke.getInstance().getConfig().getBasicConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        YOUTUBE_CLIENT = apiClient;
    }

    public NowPlayingCommand(String name) {
        super(name);

        getOptions().add(new InfoSubCommand("info"));
        getOptions().add(new ThumbnailSubCommand("thumbnail"));
        getOptions().add(new TimeSubCommand("time"));
    }

    @Override
    public void onInvoke(CommandContext context) {
    }

    @Override
    public String getDescription() {
        return "Displays detailed information about the currently playing track.";
    }


    public static class InfoSubCommand extends SubCommandOption {

        public InfoSubCommand(String name) {
            super(name);
        }

        @Override
        public void onInvoke(CommandContext context) {
            var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
            NeoGuildPlayer audioPlayer = neoGuild.getAudioPlayer();
            if (audioPlayer.getPlayingTrack() != null) {
                LoadedTrackContext trackContext = audioPlayer.getPlayingTrack();
                if (trackContext != null) {
                    try {
                        context.getResponseSender().sendMessageEmbeds(TrackEmbedUtil.getTrackEmbed(audioPlayer)).setEphemeral(false).queue();
                    } catch (IOException e) {
                        ExceptionUtil.sendStackTrace(
                                neoGuild,
                                e,
                                MessageManager.getMessage("command.nowplay.failed"));
                    }
                }
            } else {
                context.getResponseSender().sendMessage(MessageManager.getMessage("command.nowplay.nothing")).queue();
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Displays detailed information about the currently playing track.";
        }
    }


    public static class ThumbnailSubCommand extends SubCommandOption {

        public ThumbnailSubCommand(String name) {
            super(name);
        }

        @Override
        public void onInvoke(CommandContext context) {
            var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
            NeoGuildPlayer audioPlayer = neoGuild.getAudioPlayer();
            if (audioPlayer.getPlayingTrack() != null) {
                LoadedTrackContext trackContext = audioPlayer.getPlayingTrack();
                if (trackContext != null) {
                    AudioTrack audioTrack = trackContext.getTrack();
                    if (audioTrack instanceof YoutubeAudioTrack) {
                        try {
                            InputStream thumbnail = getThumbnailStream(audioTrack.getIdentifier());
                            if (thumbnail != null) {
                                context.getHook().sendMessage(MessageManager.getMessage("command.nowplay.getthumbnail"))
                                        .addFile(thumbnail, "thumbnail.jpg")
                                        .queue();
                            }
                        } catch (IOException e) {
                            ExceptionUtil.sendStackTrace(
                                    neoGuild,
                                    e,
                                    MessageManager.getMessage("command.nowplay.failed"));
                        }
                    } else {
                        context.getResponseSender().sendMessage(MessageManager.getMessage("command.nowplay.nosupport")).queue();
                    }
                }
            }
        }

        private InputStream getThumbnailStream(String identifier) throws IOException {
            if (YOUTUBE_CLIENT == null)
                return null;

            YouTubeObjectItem youtubeVideo =
                    YOUTUBE_CLIENT.getYoutubeObjects(YouTubeAPIClient.YOUTUBE_VIDEO, identifier).getItems()[0];
            return new URL(youtubeVideo.getSnippet().getThumbnails().getHigh().getURL()).openStream();
        }

        @Override
        public @NotNull String getDescription() {
            return "Get a thumbnail of the currently playing track.";
        }
    }


    public static class TimeSubCommand extends SubCommandOption {

        public TimeSubCommand(String name) {
            super(name);
        }

        @Override
        public void onInvoke(CommandContext context) {
            NeoGuildPlayer audioPlayer = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild()).getAudioPlayer();
            if (audioPlayer.getPlayingTrack() != null) {
                LoadedTrackContext trackContext = audioPlayer.getPlayingTrack();
                if (trackContext != null) {
                    AudioTrack audioTrack = trackContext.getTrack();
                    context.getHook().sendMessage(
                            MessageUtil.format(MessageManager.getMessage("command.list.playing"),
                                    audioPlayer.getPlayingTrack().getTrack().getInfo().title) + "\n" +
                                    MessageUtil.format(MessageManager.getMessage("command.nowplay.currenttime"),
                                            MessageUtil.formatTime(audioPlayer.getTrackPosition()),
                                            MessageUtil.formatTime(audioTrack.getDuration() - audioPlayer.getTrackPosition()))).queue();
                }
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Displays the playback time of the currently playing track.";
        }
    }
}
