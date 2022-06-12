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

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.utils.ChannelPermissionUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

public class JoinCommand extends CommandExecutor {

    public JoinCommand(String name) {
        super(name);
    }

    @Override
    public void onInvoke(CommandContext context) {
        var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
        neoGuild.setLastJoinedChannel(context.getChannel());

        NeoGuildPlayer audioPlayer = neoGuild.getAudioPlayer();
        VoiceChannel targetChannel = null;
        if (context.getInvoker().getVoiceState().getChannel().getType() == ChannelType.VOICE)
            targetChannel = (VoiceChannel) context.getInvoker().getVoiceState().getChannel();

        try {
            if (targetChannel == null)
                context.getResponseSender().sendMessage(MessageManager.getMessage("command.join.before")).queue();
            else if (!ChannelPermissionUtil.checkAccessVoiceChannel(targetChannel, neoGuild.getJDAGuild().getSelfMember()))
                context.getResponseSender().sendMessage(MessageManager.getMessage("command.channel.permission")).queue();
            else
                audioPlayer.joinChannel(targetChannel);
        } catch (InsufficientPermissionException e) {
            context.getResponseSender().sendMessage(MessageManager.getMessage("command.channel.permission")).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Connect the bot to the voice channel.";
    }


}
