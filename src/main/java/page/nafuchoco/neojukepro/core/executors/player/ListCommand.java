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

import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.player.LoadedTrackContext;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;

import java.util.List;

public class ListCommand extends CommandExecutor {

    public ListCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
        List<LoadedTrackContext> tracks = audioPlayer.getTrackProvider().getQueues();
        if (!tracks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int range = 15;
            int page = 1;

            if (context.getArgs().length != 0) {
                try {
                    page = Integer.parseInt(context.getArgs()[0]);
                    if (page < 1) {
                        page = 1;
                    }
                } catch (NumberFormatException e) {
                    context.getChannel().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.page.specify")).queue();
                }
            }

            int listPage = tracks.size() / range;
            if (tracks.size() % range >= 1)
                listPage++;

            if (page > listPage) {
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.page.large")).queue();
                return;
            }

            long totalTime = 0;
            for (LoadedTrackContext track : tracks)
                totalTime += track.getTrack().getDuration() - track.getStartPosition();

            if (audioPlayer.getPlayingTrack() != null)
                sb.append(MessageUtil.format(
                        MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.list.playing"),
                        audioPlayer.getPlayingTrack().getTrack().getInfo().title) + "\n");
            sb.append(MessageUtil.format(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.list.list"),
                    tracks.size(), page, listPage, MessageUtil.formatTime(totalTime)));
            for (int count = range * page - range + 1; count <= range * page; count++) {
                if (tracks.size() >= count && sb.length() < 1800) {
                    LoadedTrackContext track = tracks.get(count - 1);
                    sb.append("\n`[" + count + "]` **" + track.getTrack().getInfo().title
                            + " (" + track.getInvoker().getJDAMember().getEffectiveName() + ")** `[" + MessageUtil.formatTime(track.getTrack().getDuration() - track.getStartPosition()) + "]`");
                }
            }
            context.getChannel().sendMessage(sb.toString()).queue();
        } else if (audioPlayer.getPlayingTrack() != null) {
            context.getChannel().sendMessage(
                    MessageUtil.format(MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.list.playing"),
                            audioPlayer.getPlayingTrack().getTrack().getInfo().title)).queue();
        } else {
            context.getChannel().sendMessage(MessageManager.getMessage(
                    context.getNeoGuild().getSettings().getLang(),
                    "command.list.nothing")).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Displays the list of queues registered in the Bot.";
    }

    @Override
    public String getHelp() {
        return getName() + " <args>\n----\n" +
                "<PageNumber>: Switches the page to be displayed.\n";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
