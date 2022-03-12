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

package page.nafuchoco.neojukepro.core.executors.system;

import lombok.val;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.utils.ChannelPermissionUtil;

public class ChannelCheckCommand extends CommandExecutor {

    public ChannelCheckCommand(String name, String... aliases) {
        super(name, aliases);

        getOptions().add(new CommandValueOption(OptionType.MENTIONABLE, "member", "Select the user for viewing the channel list.", false, false));
    }

    @Override
    public String onInvoke(CommandContext context) {
        if (context.getOptions().get("member") == null) {
            return buildChannelList(context.getNeoGuild().getJDAGuild().getSelfMember());
        } else {
            if (context.getOptions().get("member").getValue() instanceof Member member)
                return buildChannelList(member);
        }

        return null;
    }

    private String getIndentSpace(int indentLevel) {
        val indentSpace = "    ";
        String indentResult = "";
        for (int i = 0; i < indentLevel; i++) {
            indentResult += indentSpace;
        }
        return indentResult;
    }

    private String buildChannelList(Member member) {
        val channelList = new StringBuilder("```diff\n");

        var indentLevel = 0;
        if (!member.getGuild().getCategories().isEmpty())
            indentLevel = 1;

        String channelPrefix;
        String canAccess;

        for (GuildChannel channel : member.getGuild().getChannels()) {
            switch (channel.getType()) {
                case TEXT:
                    channelPrefix = "T";
                    canAccess = ChannelPermissionUtil.checkAccessTextChannel((TextChannel) channel, member) ? "+" : "-";

                    channelList.append(canAccess + " " + getIndentSpace(indentLevel) + channelPrefix + " " + channel.getName() + "\n");
                    break;

                case VOICE:
                    channelPrefix = "V";

                    canAccess = ChannelPermissionUtil.checkAccessVoiceChannel((VoiceChannel) channel, member) ? "+" : "-";

                    channelList.append(canAccess + " " + getIndentSpace(indentLevel) + channelPrefix + " " + channel.getName() + "\n");
                    break;

                case CATEGORY:
                    Category channelCategory = (Category) channel;
                    val name = channelCategory.getName();
                    val childrenSize = channelCategory.getChannels().size();

                    channelList.append(name + " (" + childrenSize + ")\n");
                    break;
            }
        }
        channelList.append("```");

        return channelList.toString();
    }

    @Override
    public String getDescription() {
        return "Displays a list of channels that members can access.";
    }

    @Override
    public int getRequiredPerm() {
        return 252;
    }
}
