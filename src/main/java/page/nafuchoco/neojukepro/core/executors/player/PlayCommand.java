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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeSearchResults;
import page.nafuchoco.neojukepro.core.player.AudioTrackLoader;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.player.TrackContext;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class PlayCommand extends CommandExecutor {
    private static final Pattern URL_REGEX = Pattern.compile("^(http|https)://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$");
    private static final Pattern NUMBER_REGEX = Pattern.compile("^[1-5]$");

    public PlayCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
        if (!context.getNeoGuild().getJDAGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            VoiceChannel targetChannel = context.getInvoker().getJDAMember().getVoiceState().getChannel();
            if (targetChannel == null) {
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.join.before")).queue();
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
            if (URL_REGEX.matcher(context.getArgs()[0]).find()) { // 指定された引数がURLの場合はURLのトラックを再生
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
                } else {
                    context.getChannel().sendMessage("This feature has been integrated into the Search command.").queue();
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
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
