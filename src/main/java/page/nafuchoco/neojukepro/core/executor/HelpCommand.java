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

import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

import java.util.List;
import java.util.regex.Pattern;

public class HelpCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private static final Pattern NOMBER_REGEX = Pattern.compile("^\\d+$");


    public HelpCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getArgs().length == 0) {
            context.getChannel().sendMessage(printCommandList(1)).queue();
        } else {
            if (NOMBER_REGEX.matcher(context.getArgs()[0]).find()) {
                try {
                    context.getChannel().sendMessage(printCommandList(Integer.parseInt(context.getArgs()[0]))).queue();
                } catch (NumberFormatException e) {
                    context.getChannel().sendMessage("Please specify the number of pages with a number.").queue();
                }
            } else {
                CommandExecutor executor = launcher.getCommandRegistry().getExecutor(context.getArgs()[0]);
                if (executor != null) {
                    StringBuilder builder = new StringBuilder("```");
                    builder.append(executor.getName() + ": " + executor.getDescription() + "\n");
                    builder.append(executor.getHelp());
                    builder.append("```");
                    context.getChannel().sendMessage(builder.toString()).queue();
                } else {
                    context.getChannel().sendMessage("No such command is registered.\n" +
                            "You can see the information about the command by executing \"help\".").queue();
                }
            }
        }
    }

    private String printCommandList(int page) {
        List<CommandExecutor> commands = launcher.getCommandRegistry().getCommands();
        int range = 10;
        int listPage = commands.size() / range;
        if (commands.size() % range >= 1)
            listPage++;

        if (page > listPage)
            return "The page numbers specified are too large!";

        StringBuilder builder = new StringBuilder(
                "These are common NeoJukePlus commands that are used in a variety of situations. `[" + page + "/" + listPage + "]`:\n\n```");
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
