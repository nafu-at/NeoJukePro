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

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.command.SubCommandOption;
import page.nafuchoco.neojukepro.core.module.ModuleDescription;
import page.nafuchoco.neojukepro.core.module.NeoModule;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class ModuleCommand extends CommandExecutor {
    private static final Pattern NOMBER_REGEX = Pattern.compile("^\\d+$");

    public ModuleCommand(String name, String... aliases) {
        super(name, aliases);

        getOptions().add(new ModuleLoadSubCommand("load"));
        getOptions().add(new ModuleUnloadSubCommand("unload"));
        getOptions().add(new ModuleEnableSubCommand("enable"));
        getOptions().add(new ModuleDisableSubCommand("disable"));
        getOptions().add(new ModuleInfoSubCommand("info"));
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
        return 254;
    }


    public static class ModuleLoadSubCommand extends SubCommandOption {

        public ModuleLoadSubCommand(String name, String... aliases) {
            super(name, aliases);

            getOptions().add(new CommandValueOption(OptionType.STRING,
                    "module",
                    "Name of module to load",
                    true,
                    false));
        }

        @Override
        public @Nullable void onInvoke(CommandContext context) {
            if (context.getNeoJukePro().getModuleManager().loadModule(new File("modules", (String) context.getOptions().get("module").getValue())))
                context.getResponseSender().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.module.load.success")).queue();
            else
                context.getResponseSender().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.module.load.failed")).queue();
        }

        @Override
        public @NotNull String getDescription() {
            return "Load the specified file as a module.";
        }

        @Override
        public int getRequiredPerm() {
            return 254;
        }
    }

    public static class ModuleUnloadSubCommand extends SubCommandOption {

        public ModuleUnloadSubCommand(String name, String... aliases) {
            super(name, aliases);

            getOptions().add(new CommandValueOption(OptionType.STRING,
                    "module",
                    "Name of module to unload",
                    true,
                    false));
        }

        @Override
        public @Nullable void onInvoke(CommandContext context) {
            switch (((String) context.getOptions().get("module").getValue()).toLowerCase()) {
                case "all":
                    context.getNeoJukePro().getModuleManager().unloadAllModules();
                    break;

                default:
                    try {
                        context.getNeoJukePro().getModuleManager().disableModule((String) context.getOptions().get("module").getValue());
                        context.getNeoJukePro().getModuleManager().unloadModule((String) context.getOptions().get("module").getValue());
                    } catch (IllegalArgumentException e) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.module.notregist")).queue();
                    }
                    break;
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Unloads the specified module.";
        }

        @Override
        public int getRequiredPerm() {
            return 254;
        }
    }

    public static class ModuleEnableSubCommand extends SubCommandOption {

        public ModuleEnableSubCommand(String name, String... aliases) {
            super(name, aliases);

            getOptions().add(new CommandValueOption(OptionType.STRING,
                    "module",
                    "Name of module to enable",
                    true,
                    false));
        }

        @Override
        public @Nullable void onInvoke(CommandContext context) {
            switch (((String) context.getOptions().get("module").getValue()).toLowerCase()) {
                case "all":
                    context.getNeoJukePro().getModuleManager().enableAllModules();
                    break;

                default:
                    try {
                        context.getNeoJukePro().getModuleManager().enableModule((String) context.getOptions().get("module").getValue());
                    } catch (IllegalArgumentException e) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.module.notregist")).queue();
                    }
                    break;
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Enables the specified module.";
        }

        @Override
        public int getRequiredPerm() {
            return 254;
        }
    }

    public static class ModuleDisableSubCommand extends SubCommandOption {

        public ModuleDisableSubCommand(String name, String... aliases) {
            super(name, aliases);

            getOptions().add(new CommandValueOption(OptionType.STRING,
                    "module",
                    "Name of module to disable",
                    true,
                    false));
        }

        @Override
        public @Nullable void onInvoke(CommandContext context) {
            switch (((String) context.getOptions().get("module").getValue()).toLowerCase()) {
                case "all":
                    context.getNeoJukePro().getModuleManager().disableAllModules();
                    break;

                default:
                    try {
                        context.getNeoJukePro().getModuleManager().disableModule((String) context.getOptions().get("module").getValue());
                    } catch (IllegalArgumentException e) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.module.notregist")).queue();
                    }
                    break;
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Disables the specified module.";
        }

        @Override
        public int getRequiredPerm() {
            return 254;
        }
    }

    public static class ModuleInfoSubCommand extends SubCommandOption {

        public ModuleInfoSubCommand(String name, String... aliases) {
            super(name, aliases);

            getOptions().add(new CommandValueOption(OptionType.STRING,
                    "module",
                    "Name of the module for which information is to be displayed",
                    false,
                    false));
        }

        @Override
        public @Nullable void onInvoke(CommandContext context) {
            if (context.getOptions().isEmpty()) {
                context.getResponseSender().sendMessage(renderModuleList(context.getNeoJukePro().getModuleManager().getModules(), 1)).queue();
            } else {
                if (NOMBER_REGEX.matcher((String) context.getOptions().get("module").getValue()).find()) {
                    context.getResponseSender().sendMessage(
                            renderModuleList(context.getNeoJukePro().getModuleManager().getModules(), Integer.parseInt((String) context.getOptions().get("module").getValue()))).queue();
                } else {
                    NeoModule module = context.getNeoJukePro().getModuleManager().getModule((String) context.getOptions().get("module").getValue());
                    if (module == null) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
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
                        context.getResponseSender().sendMessage("```\n" + table.render() + "\n```").queue();
                    }
                }
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Displays detailed information about the specified module.";
        }

        @Override
        public int getRequiredPerm() {
            return 0;
        }
    }


    public static String renderModuleList(List<NeoModule> modules, int page) {
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

    private static String toStringList(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String val : list) {
            stringBuilder.append(val + ", ");
        }
        return stringBuilder.toString().stripTrailing().replaceFirst(",$", "");
    }
}
