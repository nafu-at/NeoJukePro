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

package page.nafuchoco.neojukepro.core.executors.guild;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.guild.user.NeoGuildMember;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;

public class UserPermCommand extends CommandExecutor {

    public UserPermCommand(String name, String... aliases) {
        super(name, aliases);

        getOptions().add(new CommandValueOption(OptionType.MENTIONABLE, "member", "Members to change permissions", true, false));
        getOptions().add(new CommandValueOption(OptionType.INTEGER, "permission", "To Change permissions", true, false));
    }

    @Override
    public void onInvoke(CommandContext context) {
        int permissions = (int) context.getOptions().get("permission").getValue();
        if (permissions >= 0 && permissions <= 255) {
            if (context.getOptions().get("member").getValue() instanceof Member member) {
                NeoGuildMember guildMember = context.getNeoGuild().getGuildMemberRegistry().getNeoGuildMember(member);
                guildMember.setUserPermission(permissions);
                context.getResponseSender().sendMessage(MessageUtil.format(
                        MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.userperm.set"),
                        member.getEffectiveName(),
                        permissions)).queue();
            }
        } else {
            context.getResponseSender().sendMessage(MessageManager.getMessage(
                    context.getNeoGuild().getSettings().getLang(),
                    "command.userperm.invalid")).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Sets the user's command execution permissions.";
    }

    @Override
    public int getRequiredPerm() {
        return 252;
    }
}
