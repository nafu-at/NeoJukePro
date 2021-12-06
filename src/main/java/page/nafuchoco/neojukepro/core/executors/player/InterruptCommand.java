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
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.apache.commons.lang3.math.NumberUtils;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.player.AudioTrackLoader;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.player.TrackContext;
import page.nafuchoco.neojukepro.core.utils.ChannelPermissionUtil;

public class InterruptCommand extends CommandExecutor {

    public InterruptCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
        if (context.getArgs().length >= 2) {
            if (!context.getNeoGuild().getJDAGuild().getSelfMember().getVoiceState().inAudioChannel()
                    && context.getInvoker().getJDAMember().getVoiceState().getChannel().getType() != ChannelType.VOICE) {
                VoiceChannel targetChannel = (VoiceChannel) context.getInvoker().getJDAMember().getVoiceState().getChannel();
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
                audioPlayer.joinChannel(targetChannel);
            }
            audioPlayer.play(new AudioTrackLoader(
                    new TrackContext(context.getNeoGuild(), context.getInvoker(), NumberUtils.toInt(context.getArgs()[0], 0), context.getArgs()[1])));
            if (context.getNeoGuild().getJDAGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE))
                context.getMessage().delete().submit();
        }
    }

    @Override
    public String getDescription() {
        return "Interrupts the track into the queue.";
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
