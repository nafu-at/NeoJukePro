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

package page.nafuchoco.neojukepro.core.executor;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandCache;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.ExceptionUtil;
import page.nafuchoco.neojukepro.core.http.youtube.SearchItem;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeSearchResults;
import page.nafuchoco.neojukepro.core.player.AudioTrackLoader;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PlayCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private static final Pattern URL_REGEX = Pattern.compile("^(http|https)://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$");
    private static final Pattern NUMBER_REGEX = Pattern.compile("^[1-5]$");

    public PlayCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        GuildAudioPlayer audioPlayer = launcher.getPlayerRegistry().getGuildAudioPlayer(context.getGuild());
        if (!context.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            VoiceChannel targetChannel = context.getInvoker().getVoiceState().getChannel();
            if (targetChannel == null) {
                context.getChannel().sendMessage(MessageManager.getMessage("command.join.before")).queue();
                return;
            }
            try {
                audioPlayer.joinChannel(targetChannel);
            } catch (InsufficientPermissionException e) {
                context.getChannel().sendMessage(
                        MessageManager.getMessage("command.channel.permission")).queue();
                return;
            }
        }
        audioPlayer.play();

        if (context.getArgs().length == 0) {
            // 添付ファイルが有る場合は添付ファイルを再生
            if (!context.getMessage().getAttachments().isEmpty())
                context.getMessage().getAttachments().forEach(attachment ->
                        audioPlayer.play(new AudioTrackLoader(attachment.getUrl(), context.getInvoker(), 0)));
        } else {
            if (URL_REGEX.matcher(context.getArgs()[0]).find()) { // 指定された引数がURLの場合はURLのトラックを再生
                audioPlayer.play(new AudioTrackLoader(context.getArgs()[0], context.getInvoker(), 0));
                if (context.getGuild().getSelfMember().hasPermission(context.getChannel(), Permission.MESSAGE_MANAGE))
                    context.getMessage().delete().submit();
            } else if (NUMBER_REGEX.matcher(context.getArgs()[0]).find()) { // 指定された引数が数字の場合は保存された検索結果を取得して指定されたトラックを再生
                List<Object> objects = (List<Object>) CommandCache.deleteCache(context.getGuild(), "searchResults");
                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof Message
                        && objects.get(2) instanceof Message) {
                    YouTubeSearchResults searchResult = (YouTubeSearchResults) objects.get(0);
                    Message message = (Message) objects.get(1);
                    Message sendMessage = (Message) objects.get((2));
                    audioPlayer.play(new AudioTrackLoader("https://www.youtube.com/watch?v="
                            + searchResult.getItems()[Integer.parseInt(context.getArgs()[0]) - 1].getID().getVideoID(),
                            context.getInvoker(), 0));
                    if (context.getGuild().getSelfMember().hasPermission(context.getChannel(), Permission.MESSAGE_MANAGE)) {
                        context.getMessage().delete().submit();
                        sendMessage.delete().submit();
                        message.delete().submit();
                    }
                }
            } else {
                File file = new File(context.getArgs()[0]);
                if (file.exists()) { // 入力された引数がファイルパスかを確認しファイルが存在する場合再生
                    audioPlayer.play(new AudioTrackLoader(file.getPath(), context.getInvoker(), 0));
                    return;
                }

                // それ以外は入力された単語をYouTubeで検索
                StringBuilder builder = new StringBuilder();
                for (String arg : context.getArgs())
                    builder.append(arg + " ");
                try {
                    YouTubeSearchResults result =
                            new YouTubeAPIClient(launcher.getConfig().getAdvancedConfig().getGoogleAPIToken()).searchVideos(builder.toString());
                    if (result == null || result.getItems().length == 0) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.notfound")).queue();
                        return;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage("command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : result.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage("command.play.search.select"));

                    context.getChannel().sendMessage(message.toString()).queue(send ->
                            CommandCache.registerCache(context.getGuild(), "searchResults", Arrays.asList(result, context.getMessage(), send)));
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(context.getGuild(), e, MessageManager.getMessage("command.play.search.failed"));
                } catch (IllegalArgumentException e) {
                    context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.disabled")).queue();
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Play and search for tracks.";
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
