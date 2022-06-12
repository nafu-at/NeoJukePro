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

import lombok.val;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.player.AudioTrackLoader;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.player.TrackContext;
import page.nafuchoco.neojukepro.core.utils.ChannelPermissionUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

public class InterruptCommand extends CommandExecutor {

    public InterruptCommand(String name) {
        super(name);

        getOptions().add(new CommandValueOption(OptionType.STRING,
                "url",
                "URL of track to play",
                true,
                false));
        getOptions().add(new CommandValueOption(OptionType.INTEGER,
                "index",
                "Index to interrupt",
                true,
                false));
    }

    @Override
    public void onInvoke(CommandContext context) {
        var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
        neoGuild.setLastJoinedChannel(context.getChannel());
        
        NeoGuildPlayer audioPlayer = neoGuild.getAudioPlayer();
        if (!neoGuild.getJDAGuild().getSelfMember().getVoiceState().inAudioChannel()
                && context.getInvoker().getVoiceState().getChannel().getType() != ChannelType.VOICE) {
            VoiceChannel targetChannel = (VoiceChannel) context.getInvoker().getVoiceState().getChannel();
            if (targetChannel == null) {
                context.getResponseSender().sendMessage(MessageManager.getMessage("command.join.before")).queue();
            }
            if (!ChannelPermissionUtil.checkAccessVoiceChannel(targetChannel, context.getGuild().getSelfMember())) {
                context.getResponseSender().sendMessage(MessageManager.getMessage("command.channel.permission")).queue();
            }
            audioPlayer.joinChannel(targetChannel);
        }
        val url = (String) context.getOptions().get("url").getValue();
        val index = (Integer) context.getOptions().get("index").getValue();
        audioPlayer.play(new AudioTrackLoader(
                new TrackContext(neoGuild, context.getInvoker(), index, url)));
    }

    @Override
    public String getDescription() {
        return "Interrupts the track into the queue.";
    }
}
