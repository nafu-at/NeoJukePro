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

package page.nafuchoco.neojukepro.core.executors.guild;

import lombok.val;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

public class ThreadViewCommand extends CommandExecutor {

    public ThreadViewCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        var indentLevel = 0;
        if (!context.getNeoGuild().getJDAGuild().getCategories().isEmpty())
            indentLevel = 1;

        val channelList = new StringBuilder("```");
        for (GuildChannel channel : context.getNeoGuild().getJDAGuild().getChannels()) {
            switch (channel.getType()) {
                case TEXT:
                    TextChannel textChannel = (TextChannel) channel;
                    if (!textChannel.getThreadChannels().isEmpty()) {
                        channelList.append(getIndentSpace(indentLevel)).append(channel.getName()).append("\n");
                        for (ThreadChannel threadChannel : textChannel.getThreadChannels()) {
                            //if (threadChannel.getThreadMember(context.getInvoker().getJDAMember()) != null) {
                            String visibilityPrefix = threadChannel.isPublic() ? "Pub" : "Pri";
                            String archiveSuffix = threadChannel.isArchived() ? "*" : "";
                            channelList.append(getIndentSpace(indentLevel + 1))
                                    .append("(")
                                    .append(visibilityPrefix).append(") ")
                                    .append(threadChannel.getName())
                                    .append(archiveSuffix)
                                    .append("\n");
                            //}
                        }
                    }
                    break;

                case CATEGORY:
                    Category channelCategory = (Category) channel;
                    val name = channelCategory.getName();
                    channelList.append("- ").append(name).append("\n");
                    break;

                case VOICE:
                default:
                    break;
            }
        }
        channelList.append("```");

        context.getChannel().sendMessage(channelList.toString()).queue();
    }

    private String getIndentSpace(int indentLevel) {
        val indentSpace = "    ";
        String indentResult = "";
        for (int i = 0; i < indentLevel; i++) {
            indentResult += indentSpace;
        }
        return indentResult;
    }

    @Override
    public String getDescription() {
        return "Lists the threads in the guild.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return CommandExecutorPermission.NORMAL.getPermission();
    }
}
