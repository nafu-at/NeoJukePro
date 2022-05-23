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

import net.dv8tion.jda.api.interactions.commands.OptionType;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.player.LoadedTrackContext;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

import java.util.List;

public class ListCommand extends CommandExecutor {

    public ListCommand(String name) {
        super(name);

        getOptions().add(new CommandValueOption(OptionType.INTEGER,
                "page",
                "Switches the page to be displayed.",
                false,
                false));
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayer audioPlayer = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild()).getAudioPlayer();
        List<LoadedTrackContext> tracks = audioPlayer.getTrackProvider().getQueues();
        if (!tracks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int range = 15;
            int page = 1;

            if (context.getOptions().size() != 0) {
                try {
                    page = (Integer) context.getOptions().get("page").getValue();
                    if (page < 1) {
                        page = 1;
                    }
                } catch (NumberFormatException e) {
                    context.getChannel().sendMessage(MessageManager.getMessage("command.page.specify")).queue();
                }
            }

            int listPage = tracks.size() / range;
            if (tracks.size() % range >= 1)
                listPage++;

            if (page > listPage)
                context.getResponseSender().sendMessage(MessageManager.getMessage("command.page.large")).queue();

            long totalTime = 0;
            for (LoadedTrackContext track : tracks)
                totalTime += track.getTrack().getDuration() - track.getStartPosition();

            if (audioPlayer.getPlayingTrack() != null)
                sb.append(MessageUtil.format(
                        MessageManager.getMessage("command.list.playing"),
                        audioPlayer.getPlayingTrack().getTrack().getInfo().title) + "\n");
            sb.append(MessageUtil.format(MessageManager.getMessage("command.list.list"),
                    tracks.size(), page, listPage, MessageUtil.formatTime(totalTime)));
            for (int count = range * page - range + 1; count <= range * page; count++) {
                if (tracks.size() >= count && sb.length() < 1800) {
                    LoadedTrackContext track = tracks.get(count - 1);
                    sb.append("\n`[" + count + "]` **" + track.getTrack().getInfo().title
                            + " (" + track.getInvoker().getEffectiveName() + ")** `[" + MessageUtil.formatTime(track.getTrack().getDuration() - track.getStartPosition()) + "]`");
                }
            }
            context.getResponseSender().sendMessage(sb.toString()).setEphemeral(false).queue();
        } else if (audioPlayer.getPlayingTrack() != null) {
            context.getResponseSender().sendMessage(MessageUtil.format(MessageManager.getMessage("command.list.playing"),
                    audioPlayer.getPlayingTrack().getTrack().getInfo().title)).queue();
        } else {
            context.getResponseSender().sendMessage(MessageManager.getMessage("command.list.nothing")).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Displays the list of queues registered in the Bot.";
    }


}
