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

package page.nafuchoco.neojukepro.core.executors.system;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

import java.util.regex.Pattern;

@Slf4j
public class ModuleCommand extends CommandExecutor {
    private static final Pattern NOMBER_REGEX = Pattern.compile("^\\d+$");

    // TODO: 2022/03/12 後々

    public ModuleCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
    }

    @Override
    public @NotNull String getDescription() {
        return "null";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
/*
    @Override
    public void onInvoke(CommandContext context) {
        NeoJukePro neoJukePro = context.getNeoJukePro();
        if (context.getOptions().length == 0) {
            context.getChannel().sendMessage(renderModuleList(neoJukePro.getModuleManager().getModules(), 1)).queue();
        } else switch (context.getOptions()[0].toLowerCase()) {
            case "load":
                if (neoJukePro.getModuleManager().loadModule(new File("modules", context.getOptions()[1])))
                    context.getChannel().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.module.load.success")).queue();
                else
                    context.getChannel().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.module.load.failed")).queue();
                break;

            case "unload":
                switch (context.getOptions()[1].toLowerCase()) {
                    case "all":
                        neoJukePro.getModuleManager().unloadAllModules();
                        break;

                    default:
                        try {
                            neoJukePro.getModuleManager().disableModule(context.getOptions()[1]);
                            neoJukePro.getModuleManager().unloadModule(context.getOptions()[1]);
                        } catch (IllegalArgumentException e) {
                            context.getChannel().sendMessage(MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.module.notregist")).queue();
                        }
                        break;
                }
                break;

            case "enable":
                switch (context.getOptions()[1].toLowerCase()) {
                    case "all":
                        neoJukePro.getModuleManager().enableAllModules();
                        break;

                    default:
                        try {
                            neoJukePro.getModuleManager().enableModule(context.getOptions()[1]);
                        } catch (IllegalArgumentException e) {
                            context.getChannel().sendMessage(MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.module.notregist")).queue();
                        }
                        break;
                }
                break;

            case "disable":
                switch (context.getOptions()[1].toLowerCase()) {
                    case "all":
                        neoJukePro.getModuleManager().disableAllModules();
                        break;

                    default:
                        try {
                            neoJukePro.getModuleManager().disableModule(context.getOptions()[1]);
                        } catch (IllegalArgumentException e) {
                            context.getChannel().sendMessage(MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.module.notregist")).queue();
                        }
                        break;
                }
                break;

            default:
                if (NOMBER_REGEX.matcher(context.getOptions()[0]).find()) {
                    context.getChannel().sendMessage(
                            renderModuleList(neoJukePro.getModuleManager().getModules(), Integer.parseInt(context.getOptions()[0]))).queue();
                } else {
                    NeoModule module = neoJukePro.getModuleManager().getModule(context.getOptions()[0]);
                    if (module == null) {
                        context.getChannel().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.module.notregist")).queue();
                    } else {
                        ModuleDescription description = module.getDescription();
                        AsciiTable table = new AsciiTable();
                        table.addRule();
                        table.addRow("Name", description.getName());
                        table.addRule();
                        table.addRow("Description", StringUtils.defaultString(description.getDescription(), "null"));
                        table.addRule();
                        table.addRow("Version", description.getVersion());
                        table.addRule();
                        table.addRow("Authors", StringUtils.defaultString(toStringList(description.getAuthors()), "null"));
                        table.addRule();
                        table.addRow("WebSite", StringUtils.defaultString(description.getWebsite(), "null"));
                        table.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);
                        context.getChannel().sendMessage("```\n" + table.render() + "\n```").queue();
                    }
                }
                break;
        }
    }

    public String renderModuleList(List<NeoModule> modules, int page) {
        int range = 10;
        try {
            if (page < 1)
                page = 1;
        } catch (NumberFormatException e) {
            return MessageManager.getMessage("command.page.specify");
        }

        if (modules.isEmpty())
            return MessageManager.getMessage("command.module.empty");

        int listPage = modules.size() / range;
        if (modules.size() % range >= 1)
            listPage++;

        if (page > listPage)
            return MessageManager.getMessage("command.page.large");

        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("ModuleName", "Description", "Version").setTextAlignment(TextAlignment.CENTER);
        for (int count = range * page - range; count < range * page; count++) {
            if (modules.size() > count) {
                NeoModule module = modules.get(count);
                table.addRule();
                table.addRow(
                                module.getDescription().getName(),
                                StringUtils.defaultString(module.getDescription().getDescription(), "null"),
                                module.getDescription().getVersion())
                        .setTextAlignment(TextAlignment.CENTER);
            }
        }
        table.addRule();
        table.getContext().setGridTheme(TA_GridThemes.CC);
        return "```\n" + table.render() + "\n```";
    }

    private String toStringList(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String val : list) {
            stringBuilder.append(val + ", ");
        }
        return stringBuilder.toString().stripTrailing().replaceFirst(",$", "");
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

 */
}
