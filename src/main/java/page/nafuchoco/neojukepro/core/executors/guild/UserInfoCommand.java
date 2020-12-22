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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.guild.user.NeoGuildMember;

import java.time.format.DateTimeFormatter;

public class UserInfoCommand extends CommandExecutor {

    public UserInfoCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (!context.getMessage().getMentionedMembers().isEmpty()) {
            context.getMentioned().forEach(member -> context.getChannel().sendMessage(
                    buildMemberEmbed(context.getNeoGuild().getGuildMemberRegistry().getNeoGuildMember(member))).queue());
        }
    }

    private MessageEmbed buildMemberEmbed(NeoGuildMember member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(member.getJDAMember().getEffectiveName() + "'s Information");
        builder.setThumbnail(member.getJDAMember().getUser().getAvatarUrl());
        builder.addField(new MessageEmbed.Field("Username", member.getJDAMember().getUser().getAsTag(), true));
        builder.addField(new MessageEmbed.Field("ID", String.valueOf(member.getDiscordUserId()), true));
        builder.addField(new MessageEmbed.Field("Nickname", member.getJDAMember().getEffectiveName(), true));
        builder.addField(new MessageEmbed.Field("Join Date",
                member.getJDAMember().getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), true));
        builder.addField(new MessageEmbed.Field("Account Create",
                member.getJDAMember().getUser().getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), true));
        builder.addField(new MessageEmbed.Field("Permission", String.valueOf(member.getUserPermission()), true));
        return builder.build();
    }

    @Override
    public String getDescription() {
        return "Displays user information.";
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
