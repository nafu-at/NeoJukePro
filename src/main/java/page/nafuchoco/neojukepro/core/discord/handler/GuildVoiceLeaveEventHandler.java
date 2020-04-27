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

package page.nafuchoco.neojukepro.core.discord.handler;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;

import javax.annotation.Nonnull;

public final class GuildVoiceLeaveEventHandler extends ListenerAdapter {
    private static final NeoJukeLauncher launcher = Main.getLauncher();

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        for (Member member : event.getChannelLeft().getMembers())
            if (!member.getUser().isBot())
                return;

        MessageUtil.sendMessage(event.getEntity().getGuild(), "No one seems to have left.\n" +
                "It was automatically exited to relieve the load.");
        GuildAudioPlayer player =
                launcher.getPlayerRegistry().getGuildAudioPlayer(event.getEntity().getGuild());
        player.setPaused(true);
        player.leaveChannel();
    }
}
