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

import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.RandomStringUtils;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;

public class ShutdownCommand extends CommandExecutor {

    public ShutdownCommand(String name, String... aliases) {
        super(name, aliases);

        getOptions().add(new CommandValueOption(OptionType.STRING, "shutdown-key", "shutdownKey", false, false));
    }

    @Override
    public String onInvoke(CommandContext context) {
        if (context.getOptions().isEmpty()) {
            String pass = RandomStringUtils.randomAlphanumeric(6);
            context.getNeoGuild().getGuildTempRegistry().registerTemp("shutdownKey", pass);
            return MessageUtil.format(
                    MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.shutdown.key"), pass);
        } else {
            if (context.getOptions().get("shutdownKey").getValue().equals(context.getNeoGuild().getGuildTempRegistry().deleteTemp("shutdownKey")))
                Runtime.getRuntime().exit(0);
            else
                return MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.shutdown.key.incorrect");
        }

        return null;
    }

    @Override
    public String getDescription() {
        return "Exit the system.";
    }

    @Override
    public int getRequiredPerm() {
        return 254;
    }
}
