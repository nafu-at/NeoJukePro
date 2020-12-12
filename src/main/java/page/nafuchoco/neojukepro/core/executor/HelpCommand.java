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

package page.nafuchoco.neojukepro.core.executor;

import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.MessageUtil;

import java.util.List;
import java.util.regex.Pattern;

public class HelpCommand extends CommandExecutor {
    private static final Pattern NOMBER_REGEX = Pattern.compile("^\\d+$");


    public HelpCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoJukePro neoJukePro = context.getNeoJukePro();
        if (context.getArgs().length == 0) {
            context.getChannel().sendMessage(printCommandList(neoJukePro.getCommandRegistry().getCommands(), 1)).queue();
        } else {
            if (NOMBER_REGEX.matcher(context.getArgs()[0]).find()) {
                try {
                    context.getChannel().sendMessage(
                            printCommandList(neoJukePro.getCommandRegistry().getCommands(), Integer.parseInt(context.getArgs()[0]))).queue();
                } catch (NumberFormatException e) {
                    context.getChannel().sendMessage(MessageManager.getMessage("command.page.specify")).queue();
                }
            } else {
                CommandExecutor executor = neoJukePro.getCommandRegistry().getExecutor(context.getArgs()[0]);
                if (executor != null) {
                    StringBuilder builder = new StringBuilder("```");
                    builder.append(executor.getName() + ": " + executor.getDescription() + "\n");
                    builder.append(executor.getHelp());
                    builder.append("```");
                    context.getChannel().sendMessage(builder.toString()).queue();
                } else {
                    context.getChannel().sendMessage(MessageManager.getMessage("command.help.nocommand")).queue();
                }
            }
        }
    }

    private String printCommandList(List<CommandExecutor> commands, int page) {
        int range = 10;
        int listPage = commands.size() / range;
        if (commands.size() % range >= 1)
            listPage++;

        if (page > listPage)
            return MessageManager.getMessage("command.page.large");

        StringBuilder builder = new StringBuilder(
                MessageUtil.format(MessageManager.getMessage("command.help.list"), page, listPage) + "\n\n```");
        for (int count = range * page - range; count < range * page; count++) {
            if (commands.size() > count) {
                CommandExecutor executor = commands.get(count);
                builder.append(executor.getName() + ": " + executor.getDescription() + "\n");
            }
        }
        builder.append("```");
        return builder.toString();
    }

    @Override
    public String getDescription() {
        return "Displays help for commands.";
    }

    @Override
    public String getHelp() {
        return getName() + " <args>\n----\n" +
                "<PageNumber>: Switches the page to be displayed.\n" +
                "<CommandName>: Displays detailed usage of the command.\n";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
