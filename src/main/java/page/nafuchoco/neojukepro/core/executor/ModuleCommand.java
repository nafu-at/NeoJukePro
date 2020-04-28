/*
 * Copyright 2020 PandaSoft.Dev Social Networks.
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

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.module.ModuleDescription;
import page.nafuchoco.neojukepro.core.module.NeoModule;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class ModuleCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private static final Pattern NOMBER_REGEX = Pattern.compile("^\\d+$");

    public ModuleCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {

        int page = 1;
        if (context.getArgs().length == 0) {
            context.getChannel().sendMessage(renderModuleList(1)).queue();
        } else switch (context.getArgs()[0].toLowerCase()) {
            case "load":
                if (launcher.getModuleManager().loadModule(new File(context.getArgs()[1])))
                    context.getChannel().sendMessage(MessageManager.getMessage("command.module.load.success")).queue();
                else
                    context.getChannel().sendMessage(MessageManager.getMessage("command.module.load.failed")).queue();
                break;

            case "unload":
                switch (context.getArgs()[1].toLowerCase()) {
                    case "all":
                        launcher.getModuleManager().unloadAllModules();
                        break;

                    default:
                        try {
                            launcher.getModuleManager().unloadModule(context.getArgs()[1]);
                        } catch (IllegalArgumentException e) {
                            context.getChannel().sendMessage(MessageManager.getMessage("command.module.notregist")).queue();
                        }
                        break;
                }
                break;

            case "enable":
                switch (context.getArgs()[1].toLowerCase()) {
                    case "all":
                        launcher.getModuleManager().enableAllModules();
                        break;

                    default:
                        try {
                            launcher.getModuleManager().enableModule(context.getArgs()[1]);
                        } catch (IllegalArgumentException e) {
                            context.getChannel().sendMessage(MessageManager.getMessage("command.module.notregist")).queue();
                        }
                        break;
                }
                break;

            case "disable":
                switch (context.getArgs()[1].toLowerCase()) {
                    case "all":
                        launcher.getModuleManager().disableAllModules();
                        break;

                    default:
                        try {
                            launcher.getModuleManager().disableModule(context.getArgs()[1]);
                        } catch (IllegalArgumentException e) {
                            context.getChannel().sendMessage(MessageManager.getMessage("command.module.notregist")).queue();
                        }
                        break;
                }
                break;

            default:
                if (NOMBER_REGEX.matcher(context.getArgs()[0]).find()) {
                    context.getChannel().sendMessage(renderModuleList(Integer.parseInt(context.getArgs()[0]))).queue();
                } else {
                    NeoModule module = launcher.getModuleManager().getModule(context.getArgs()[0]);
                    if (module == null) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.module.notregist")).queue();
                    } else {
                        ModuleDescription description = module.getDescription();
                        AsciiTable table = new AsciiTable();
                        table.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);
                        table.addRule();
                        table.addRow("Name", null, null, description.getName());
                        table.addRule();
                        table.addRow("Description", null, null, description.getDescription());
                        table.addRule();
                        table.addRow("Version", null, null, description.getVersion());
                        table.addRule();
                        table.addRow("Authors", null, null, description.getAuthors());
                        table.addRule();
                        table.addRow("WebSite", null, null, description.getWebsite());
                        context.getChannel().sendMessage("```\n" + table.render() + "\n```").queue();
                    }
                }
                break;
        }
    }

    public String renderModuleList(int page) {
        int range = 10;
        try {
            if (page < 1)
                page = 1;
        } catch (NumberFormatException e) {
            return MessageManager.getMessage("command.page.specify");
        }

        List<NeoModule> modules = launcher.getModuleManager().getModules();

        int listPage = modules.size() / range;
        if (modules.size() % range >= 1)
            listPage++;

        if (page > listPage)
            return MessageManager.getMessage("command.page.large");

        StringBuilder builder = new StringBuilder("```");
        AsciiTable table = new AsciiTable();
        table.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);
        table.addRule();
        table.addRow("ModuleName", null, "Description", "Version");
        for (int count = range * page - range; count < range * page; count++) {
            if (modules.size() > count) {
                NeoModule module = modules.get(count);
                table.addLightRule();
                table.addRow(module.getDescription().getName(), null, module.getDescription().getDescription(), module.getDescription().getVersion());
            }
        }
        return "```\n" + table.render() + "\n```";
    }

    @Override
    public String getDescription() {
        return "Module management.";
    }

    @Override
    public String getHelp() {
        return "If no argument is specified, a list of all loaded modules is displayed.\n\n" +
                getName() + " [options] <args>\n----\n" +
                "[<ModuleName>]: Displays detailed information about the specified module.\n" +
                "[load]: Load the specified file as a module.\n" +
                "[unload]: Unloads the specified module.\n" +
                "[enable]: Enables the specified module.\n" +
                "[enable] <all>: Enables all modules.\n" +
                "[disable]: Disables the specified module.\n" +
                "[disable] <all>: Disables all modules.\n";
    }

    @Override
    public int getRequiredPerm() {
        return 254;
    }
}
