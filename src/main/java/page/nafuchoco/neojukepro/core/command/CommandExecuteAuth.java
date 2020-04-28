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

package page.nafuchoco.neojukepro.core.command;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.database.GuildUsersPermTable;
import page.nafuchoco.neojukepro.core.http.discord.DiscordAppInfo;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class CommandExecuteAuth {
    private final List<Long> admins;
    private final DiscordAppInfo appInfo;
    private final GuildUsersPermTable permTable;

    public CommandExecuteAuth(List<Long> admins, DiscordAppInfo appInfo, GuildUsersPermTable permTable) {
        this.admins = admins;
        this.appInfo = appInfo;
        this.permTable = permTable;
    }

    public int getUserPerm(Member member) {
        if (appInfo.getOwner().getID().equals(member.getId())) {
            return 255;
        } else {
            // 0 = Normal User.
            // 252 = Guild Admin.
            // 253 = Guild Owner.
            // 254 = Bot Admin.
            // 255 = Bot Owner.
            try {
                int perm = permTable.getUserPermission(member.getGuild().getIdLong(), member.getUser().getIdLong());
                if (perm == -1) {
                    return getUserPermLevel(member);
                } else {
                    if (perm > getUserPermLevel(member))
                        return perm;
                    else
                        return getUserPermLevel(member);
                }
            } catch (SQLException e) {
                log.error(MessageManager.getMessage("system.db.retrieving.error"), e);
                return 0;
            }
        }
    }

    private int getUserPermLevel(Member member) {
        if (appInfo.getOwner().getID().equals(member.getId()))
            return 255;
        else if (admins.contains(member.getUser().getIdLong()))
            return 254;
        else if (member.isOwner())
            return 253;
        else
            return 0;
    }
}
