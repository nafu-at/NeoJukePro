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
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;

import javax.annotation.Nonnull;

public final class GuildVoiceEventHandler extends ListenerAdapter {
    private final NeoJukePro neoJukePro;

    public GuildVoiceEventHandler(NeoJukePro neoJukePro) {
        this.neoJukePro = neoJukePro;
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        checkVoiceChannelMember(event);
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        checkVoiceChannelMember(event);
    }

    private void checkVoiceChannelMember(GenericGuildVoiceUpdateEvent event) {
        if (event.getEntity().getGuild().getSelfMember().getVoiceState().getChannel() == null
                || event.getEntity().getGuild().getSelfMember().getVoiceState().getChannel() != event.getChannelLeft()
                || event.getEntity().getUser() == event.getJDA().getSelfUser())
            return;

        for (Member member : event.getChannelLeft().getMembers())
            if (!member.getUser().isBot())
                return;

        NeoGuild neoGuild = neoJukePro.getGuildRegistry().getNeoGuild(event.getGuild());
        neoGuild.sendMessageToLatest(MessageManager.getMessage(neoGuild.getSettings().getLang(), "player.autoleave"));
        NeoGuildPlayer audioPlayer = neoGuild.getAudioPlayer();
        audioPlayer.setPaused(true);
        audioPlayer.leaveChannel();
    }
}
