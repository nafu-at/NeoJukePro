/*
 * Copyright 2021 NAFU_at.
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

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import page.nafuchoco.neojukepro.module.NeoJuke;

@AllArgsConstructor
public class GuildLeaveEventHandler extends ListenerAdapter {

    public void onGuildLeave(GuildLeaveEvent event) {
        deleteGuildData(event.getGuild().getIdLong());
    }

    public void onUnavailableGuildLeave(UnavailableGuildLeaveEvent event) {
        deleteGuildData(event.getGuildIdLong());
    }

    private void deleteGuildData(long guildId) {
        NeoJuke.getInstance().getGuildRegistry().deleteGuildData(guildId);
    }
}
