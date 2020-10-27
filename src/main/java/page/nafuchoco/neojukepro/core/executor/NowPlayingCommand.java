/*
 * Copyright 2019 くまねこそふと.
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

package page.nafuchoco.neojukepro.core.executor;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.*;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeObjectItem;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;
import page.nafuchoco.neojukepro.core.player.GuildTrackContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class NowPlayingCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private static final YouTubeAPIClient client;

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(launcher.getConfig().getAdvancedConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        client = apiClient;
    }

    public NowPlayingCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        GuildAudioPlayer audioPlayer = launcher.getPlayerRegistry().getGuildAudioPlayer(context.getGuild());
        if (audioPlayer.getNowPlaying() != null) {
            GuildTrackContext trackContext = audioPlayer.getNowPlaying();
            if (trackContext != null) {
                AudioTrack audioTrack = trackContext.getTrack();
                if (context.getArgs().length == 0) {
                    try {
                        context.getChannel().sendMessage(TrackEmbedUtil.getTrackEmbed(audioPlayer)).queue();
                    } catch (IOException e) {
                        ExceptionUtil.sendStackTrace(context.getGuild(), e, MessageManager.getMessage("command.nowplay.failed"));
                    }
                } else switch (context.getArgs()[0]) {
                    case "thumbnail":
                    case "th":
                        if (audioTrack instanceof YoutubeAudioTrack) {
                            try {
                                InputStream thumbnail = getThumbnailStream(audioTrack.getIdentifier());
                                if (thumbnail != null) {
                                    context.getChannel().sendMessage(MessageManager.getMessage("command.nowplay.getthumbnail")).queue();
                                    context.getChannel().sendFile(thumbnail, "thumbnail.jpg").queue();
                                }
                            } catch (IOException e) {
                                ExceptionUtil.sendStackTrace(context.getGuild(), e, MessageManager.getMessage("command.nowplay.failed"));
                            }
                        } else {
                            context.getChannel().sendMessage(MessageManager.getMessage("command.nowplay.nosupport")).queue();
                        }
                        break;

                    case "time":
                    case "t":
                        context.getChannel().sendMessage(
                                MessageUtil.format(MessageManager.getMessage("command.list.playing"),
                                        audioPlayer.getNowPlaying().getTrack().getInfo().title) + "\n" +
                                        MessageUtil.format(MessageManager.getMessage("command.nowplay.currenttime"),
                                                MessageUtil.formatTime(audioPlayer.getTrackPosition()),
                                                MessageUtil.formatTime(audioTrack.getDuration() - audioPlayer.getTrackPosition()))).queue();
                        break;

                    default:
                        break;
                }
            }
        } else {
            context.getChannel().sendMessage(MessageManager.getMessage("command.nowplay.nothing")).queue();
        }
    }

    private InputStream getThumbnailStream(String identifier) throws IOException {
        if (client == null)
            return null;

        YouTubeObjectItem youtubeVideo =
                client.getYoutubeObjects(YouTubeAPIClient.YOUTUBE_VIDEO, identifier).getItems()[0];
        return new URL(youtubeVideo.getSnippet().getThumbnails().getHigh().getURL()).openStream();
    }

    @Override
    public String getDescription() {
        return "Displays detailed information about the currently playing track.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
