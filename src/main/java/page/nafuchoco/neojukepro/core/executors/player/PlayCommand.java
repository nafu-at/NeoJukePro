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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.apache.commons.lang3.StringUtils;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.http.youtube.SearchItem;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeSearchResults;
import page.nafuchoco.neojukepro.core.player.AudioTrackLoader;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.player.TrackContext;
import page.nafuchoco.neojukepro.core.utils.ChannelPermissionUtil;
import page.nafuchoco.neojukepro.core.utils.ExceptionUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PlayCommand extends CommandExecutor {
    private static final Pattern URL_REGEX = Pattern.compile("^(http|https)://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$");
    private static final Pattern NUMBER_REGEX = Pattern.compile("^[1-5]$");

    private static final YouTubeAPIClient client;

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(Main.getLauncher().getConfig().getAdvancedConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        client = apiClient;
    }

    public PlayCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
        if (!context.getNeoGuild().getJDAGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            VoiceChannel targetChannel = null;
            if (context.getInvoker().getJDAMember().getVoiceState().getChannel().getType() == ChannelType.VOICE)
                targetChannel = (VoiceChannel) context.getInvoker().getJDAMember().getVoiceState().getChannel();
            
            if (targetChannel == null) {
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.join.before")).queue();
                return;
            }
            if (!ChannelPermissionUtil.checkAccessVoiceChannel(targetChannel, context.getNeoGuild().getJDAGuild().getSelfMember())) {
                context.getChannel().sendMessage(
                        MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.channel.permission")).queue();
                return;
            }
            try {
                audioPlayer.joinChannel(targetChannel);
            } catch (InsufficientPermissionException e) {
                context.getChannel().sendMessage(
                        MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.channel.permission")).queue();
                return;
            }
        }
        audioPlayer.play();

        if (context.getArgs().length == 0) {
            // 添付ファイルが有る場合は添付ファイルを再生
            if (!context.getMessage().getAttachments().isEmpty())
                context.getMessage().getAttachments().forEach(attachment ->
                        audioPlayer.play(new AudioTrackLoader(new TrackContext(context.getNeoGuild(), context.getInvoker(), 0, attachment.getUrl()))));
        } else {
            if (context.getArgs()[0].equalsIgnoreCase("next")) {
                List<Object> objects = (List<Object>) context.getNeoGuild().getGuildTempRegistry().deleteTemp("searchResults");
                YouTubeSearchResults results;
                String keyword;

                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof String) {
                    results = (YouTubeSearchResults) objects.get(0);
                    keyword = (String) objects.get(1);
                } else {
                    context.getChannel().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.searchfirst")).queue();
                    return;
                }

                try {
                    if (StringUtils.isEmpty(results.getNextPageToken())) {
                        context.getChannel().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.nopage")).queue();
                        return;
                    }
                    results = client.searchVideos(YouTubeAPIClient.SearchType.SEARCH, keyword, results.getNextPageToken());

                    if (results == null || results.getItems().length == 0) {
                        context.getChannel().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.notfound")).queue();
                        return;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : results.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.select"));

                    String finalKeyword = keyword;
                    YouTubeSearchResults finalResults = results;
                    context.getChannel().sendMessage(message.toString()).queue(send ->
                            context.getNeoGuild().getGuildTempRegistry().registerTemp(
                                    "searchResults", Arrays.asList(finalResults, finalKeyword, context.getMessage(), send)));
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(
                            context.getNeoGuild(),
                            e,
                            MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.play.search.failed"));
                }

            } else if (context.getArgs()[0].equalsIgnoreCase("prev")) {
                List<Object> objects = (List<Object>) context.getNeoGuild().getGuildTempRegistry().deleteTemp("searchResults");
                YouTubeSearchResults results;
                String keyword;

                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof String) {
                    results = (YouTubeSearchResults) objects.get(0);
                    keyword = (String) objects.get(1);
                } else {
                    context.getChannel().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.searchfirst")).queue();
                    return;
                }

                try {
                    if (StringUtils.isEmpty(results.getPrevPageToken())) {
                        context.getChannel().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.nopage")).queue();
                        return;
                    }
                    results = client.searchVideos(YouTubeAPIClient.SearchType.SEARCH, keyword, results.getPrevPageToken());

                    if (results == null || results.getItems().length == 0) {
                        context.getChannel().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.notfound")).queue();
                        return;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : results.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.select"));

                    String finalKeyword = keyword;
                    YouTubeSearchResults finalResults = results;
                    context.getChannel().sendMessage(message.toString()).queue(send ->
                            context.getNeoGuild().getGuildTempRegistry().registerTemp(
                                    "searchResults", Arrays.asList(finalResults, finalKeyword, context.getMessage(), send)));
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(
                            context.getNeoGuild(),
                            e,
                            MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.play.search.failed"));
                }
            } else if (URL_REGEX.matcher(context.getArgs()[0]).find()) { // 指定された引数がURLの場合はURLのトラックを再生
                audioPlayer.play(new AudioTrackLoader(new TrackContext(context.getNeoGuild(), context.getInvoker(), 0, context.getArgs()[0])));
                if (context.getNeoGuild().getJDAGuild().getSelfMember().hasPermission(context.getChannel(), Permission.MESSAGE_MANAGE))
                    context.getMessage().delete().submit();
            } else if (NUMBER_REGEX.matcher(context.getArgs()[0]).find()) { // 指定された引数が数字の場合は保存された検索結果を取得して指定されたトラックを再生
                List<Object> objects = (List<Object>) context.getNeoGuild().getGuildTempRegistry().deleteTemp("searchResults");
                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(2) instanceof Message
                        && objects.get(3) instanceof Message) {
                    YouTubeSearchResults searchResult = (YouTubeSearchResults) objects.get(0);
                    Message message = (Message) objects.get(2);
                    Message sendMessage = (Message) objects.get((3));
                    audioPlayer.play(new AudioTrackLoader(new TrackContext(context.getNeoGuild(), context.getInvoker(), 0,
                            "https://www.youtube.com/watch?v=" + searchResult.getItems()[Integer.parseInt(context.getArgs()[0]) - 1].getID().getVideoID())));
                    if (context.getNeoGuild().getJDAGuild().getSelfMember().hasPermission(context.getChannel(), Permission.MESSAGE_MANAGE)) {
                        context.getMessage().delete().submit();
                        sendMessage.delete().submit();
                        message.delete().submit();
                    }
                }
            } else {
                File file = new File(context.getArgs()[0]);
                if (file.exists()) { // 入力された引数がファイルパスかを確認しファイルが存在する場合再生
                    audioPlayer.play(new AudioTrackLoader(new TrackContext(context.getNeoGuild(), context.getInvoker(), 0, file.getPath())));
                } else if (client == null) {
                    context.getChannel().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.disabled")).queue();
                } else {
                    StringBuilder builder = new StringBuilder();
                    for (String arg : context.getArgs())
                        builder.append(arg + " ");
                    try {
                        YouTubeSearchResults result =
                                new YouTubeAPIClient(context.getNeoJukePro().getConfig().getAdvancedConfig().getGoogleAPIToken()).searchVideos(builder.toString());
                        if (result == null || result.getItems().length == 0) {
                            context.getChannel().sendMessage(MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.play.search.notfound")).queue();
                            return;
                        }

                        StringBuilder message = new StringBuilder();
                        message.append(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.found"));
                        int count = 1;
                        for (SearchItem item : result.getItems()) {
                            message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                            count++;
                        }
                        message.append("\n\n" + MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.select"));

                        context.getChannel().sendMessage(message.toString()).queue(send ->
                                context.getNeoGuild().getGuildTempRegistry().registerTemp(
                                        "searchResults", Arrays.asList(result, builder.toString(), context.getMessage(), send)));
                    } catch (IOException e) {
                        ExceptionUtil.sendStackTrace(
                                context.getNeoGuild(),
                                e,
                                MessageManager.getMessage(
                                        context.getNeoGuild().getSettings().getLang(),
                                        "command.play.search.failed"));
                    }
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Play for tracks.";
    }

    @Override
    public String getHelp() {
        return getName() + " [options]\n----\n" +
                "[<VideoUrl>]: Play the specified URL.\n" +
                "[<SearchKeyword>]: Search by the specified keyword.\n" +
                "[next]: Displays the next page of search results.\n" +
                "[prev]: Displays the previous page of search results.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
