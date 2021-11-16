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

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.utils.ChannelPermissionUtil;

public class JoinCommand extends CommandExecutor {

    public JoinCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
        VoiceChannel targetChannel = context.getInvoker().getJDAMember().getVoiceState().getChannel();
        try {
            if (targetChannel == null)
                context.getChannel().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.join.before")).queue();
            else if (!ChannelPermissionUtil.checkAccessVoiceChannel(targetChannel, context.getNeoGuild().getJDAGuild().getSelfMember()))
                context.getChannel().sendMessage(
                        MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.channel.permission")).queue();
            else
                audioPlayer.joinChannel(targetChannel);
        } catch (InsufficientPermissionException e) {
            context.getChannel().sendMessage(
                    MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.channel.permission")).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Connect the bot to the voice channel.";
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
