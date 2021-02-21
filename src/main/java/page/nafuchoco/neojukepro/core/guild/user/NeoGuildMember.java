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

package page.nafuchoco.neojukepro.core.guild.user;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.database.GuildUsersPermTable;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;

import java.sql.SQLException;

@Slf4j
@EqualsAndHashCode(exclude = {"usersPermTable"})
@ToString
public class NeoGuildMember {
    private final NeoJukePro neoJukePro;
    private final GuildUsersPermTable usersPermTable;

    private final NeoGuild neoGuild;
    private final long discordUserId;
    private int userPermission;

    public NeoGuildMember(NeoJukePro neoJukePro, GuildUsersPermTable usersPermTable, long discordUserId, NeoGuild neoGuild) {
        this.neoJukePro = neoJukePro;
        this.usersPermTable = usersPermTable;
        this.discordUserId = discordUserId;
        this.neoGuild = neoGuild;

        int permission = -1;
        try {
            if (neoJukePro.getDiscordAppInfo().getOwner().getID().equals(String.valueOf(discordUserId)))
                permission = 255;
            else
                permission = usersPermTable.getUserPermission(getNeoGuild().getDiscordGuildId(), getDiscordUserId());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.retrieving.error"), e);
        }
        if (permission == -1) {
            if (neoJukePro.getDiscordAppInfo().getOwner().getID().equals(String.valueOf(discordUserId)))
                permission = 255;
            else if (neoJukePro.getConfig().getBasicConfig().getBotAdmins().contains(discordUserId))
                permission = 254;
            else if (neoGuild.getJDAGuild().getMember(neoGuild.getJDAGuild().getJDA().getUserById(discordUserId)).isOwner())
                permission = 253;
            else
                permission = 0;
        }
        userPermission = permission;
    }

    public NeoJukePro getNeoJukePro() {
        return neoJukePro;
    }

    public NeoGuild getNeoGuild() {
        return neoGuild;
    }

    public long getDiscordUserId() {
        return discordUserId;
    }

    public Member getJDAMember() {
        return getNeoGuild().getJDAGuild().getMember(getNeoJukePro().getShardManager().getUserById(discordUserId));
    }

    public int getUserPermission() {
        return userPermission;
    }

    public void setUserPermission(int userPermission) {
        try {
            usersPermTable.setUserPermission(getNeoGuild().getDiscordGuildId(), getDiscordUserId(), userPermission);
            this.userPermission = userPermission;
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.communicate.error"), e);
        }
    }
}
