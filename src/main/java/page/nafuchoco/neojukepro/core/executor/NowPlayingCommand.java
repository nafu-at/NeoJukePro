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
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.ExceptionUtil;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeObjectItem;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;
import page.nafuchoco.neojukepro.core.player.GuildTrackContext;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class NowPlayingCommand extends CommandExecutor {
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
                    if (audioTrack instanceof YoutubeAudioTrack) {
                        try {
                            context.getChannel().sendMessage(getYouTubeEmbed(audioPlayer)).queue();
                        } catch (IOException | NullPointerException e) {
                            ExceptionUtil.sendStackTrace(context.getGuild(), e, MessageManager.getMessage("command.nowplay.failed"));
                        }
                    } else if (audioTrack instanceof SoundCloudAudioTrack) {
                        context.getChannel().sendMessage(getDefaultEmbed(audioPlayer, SOUNDCLOUD_COLOR)).queue();
                    } else if (audioTrack instanceof BandcampAudioTrack) {
                        context.getChannel().sendMessage(getDefaultEmbed(audioPlayer, BANDCAMP_COLOR)).queue();
                    } else if (audioTrack instanceof VimeoAudioTrack) {
                        context.getChannel().sendMessage(getDefaultEmbed(audioPlayer, VIMEO_COLOR)).queue();
                    } else if (audioTrack instanceof TwitchStreamAudioTrack) {
                        context.getChannel().sendMessage(getDefaultEmbed(audioPlayer, TWITCH_COLOR)).queue();
                    } else if (audioTrack instanceof HttpAudioTrack) {
                        context.getChannel().sendMessage(getDefaultEmbed(audioPlayer, BLACK)).queue();
                    } else if (audioTrack instanceof LocalAudioTrack) {
                        context.getChannel().sendMessage(getDefaultEmbed(audioPlayer, BLACK)).queue();
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
                        context.getChannel().sendMessage(MessageManager.getMessage("command.list.playing") + audioTrack.getInfo().title + "\n" +
                                MessageUtil.format(MessageManager.getMessage("command.nowplay.currenttime"),
                                        MessageUtil.formatTime(audioPlayer.getTrackPosition()),
                                        MessageUtil.formatTime(audioTrack.getDuration() - audioPlayer.getTrackPosition()))).queue();

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

    private MessageEmbed getYouTubeEmbed(GuildAudioPlayer audioPlayer) throws IOException, NullPointerException {
        if (client == null)
            return getDefaultEmbed(audioPlayer, YOUTUBE);

        YouTubeObjectItem youtubeVideo = client.getYoutubeObjects(YouTubeAPIClient.YOUTUBE_VIDEO,
                audioPlayer.getNowPlaying().getTrack().getIdentifier()).getItems()[0];
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
                "[" + MessageUtil.formatTime(audioPlayer.getTrackPosition()) + "/" + MessageUtil.formatTime(audioPlayer.getNowPlaying().getTrack().getDuration()) + "]", true);
        builder.addField(time);

        String descMessage = youtubeVideo.getSnippet().getLocalized().getDescription();
        if (descMessage.length() > 800)
            descMessage = descMessage.substring(0, 800) + " [...]";
        MessageEmbed.Field description = new MessageEmbed.Field("Description", descMessage, false);
        builder.addField(description);
        builder.setFooter(MessageUtil.format(MessageManager.getMessage("command.nowplay.request"), audioPlayer.getNowPlaying().getInvoker().getEffectiveName()),
                audioPlayer.getNowPlaying().getInvoker().getUser().getAvatarUrl());
        return builder.build();
    }

    private MessageEmbed getDefaultEmbed(GuildAudioPlayer audioPlayer, Color color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(audioPlayer.getNowPlaying().getTrack().getInfo().title);
        builder.setColor(color);
        builder.setAuthor(audioPlayer.getNowPlaying().getTrack().getInfo().author);
        MessageEmbed.Field time = new MessageEmbed.Field("Time",
                "[" + MessageUtil.formatTime(audioPlayer.getTrackPosition()) + "/" + MessageUtil.formatTime(audioPlayer.getNowPlaying().getTrack().getDuration()) + "]",
                true);
        builder.addField(time);
        MessageEmbed.Field source = new MessageEmbed.Field("",
                "Loaded from " + audioPlayer.getNowPlaying().getTrack().getSourceManager().getSourceName() + ".", false);
        builder.addField(source);
        builder.setFooter(MessageUtil.format(MessageManager.getMessage("command.nowplay.request"), audioPlayer.getNowPlaying().getInvoker().getEffectiveName()),
                audioPlayer.getNowPlaying().getInvoker().getUser().getAvatarUrl());
        return builder.build();
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
