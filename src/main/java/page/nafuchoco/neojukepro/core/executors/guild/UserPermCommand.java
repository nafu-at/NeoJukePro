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

import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.guild.user.NeoGuildMember;

public class UserPermCommand extends CommandExecutor {

    public UserPermCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getArgs().length != 0 && !context.getMessage().getMentionedMembers().isEmpty()) {
            try {
                int permissions = Integer.parseInt(context.getArgs()[0]);
                if (permissions >= 0 && permissions <= 255) {
                    context.getMessage().getMentionedMembers().forEach(member -> {
                        NeoGuildMember guildMember = context.getNeoGuild().getGuildMemberRegistry().getNeoGuildMember(member);
                        guildMember.setUserPermission(permissions);
                        context.getChannel().sendMessage(
                                MessageUtil.format(
                                        MessageManager.getMessage("command.userperm.set"),
                                        member.getEffectiveName(),
                                        permissions)).queue();
                    });
                } else {
                    context.getChannel().sendMessage(MessageManager.getMessage("command.userperm.invalid")).queue();
                }
            } catch (NumberFormatException e) {
                context.getChannel().sendMessage(MessageManager.getMessage("command.userperm.invalid")).queue();
            }
        }
    }

    @Override
    public String getDescription() {
        return "Sets the user's command execution permissions.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 252;
    }
}
