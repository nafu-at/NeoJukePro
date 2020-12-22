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

import org.apache.commons.lang3.RandomStringUtils;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.MessageUtil;

public class ShutdownCommand extends CommandExecutor {

    public ShutdownCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getArgs().length == 0) {
            String pass = RandomStringUtils.randomAlphanumeric(6);
            context.getNeoGuild().getGuildTempRegistry().registerTemp("shutdownKey", pass);
            context.getChannel().sendMessage(MessageUtil.format(MessageManager.getMessage("command.shutdown.key"), pass)).queue();
        } else {
            if (context.getArgs()[0].equals(context.getNeoGuild().getGuildTempRegistry().deleteTemp("shutdownKey")))
                Runtime.getRuntime().exit(0);
            else
                context.getChannel().sendMessage(MessageManager.getMessage("command.shutdown.key.incorrect")).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Exit the system.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 254;
    }
}
