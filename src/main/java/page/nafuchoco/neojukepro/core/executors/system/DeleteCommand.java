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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

import java.util.List;

public class DeleteCommand extends CommandExecutor {

    public DeleteCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        boolean checkPrefix = false;
        int maxDeleteMessage = 50;

        if (context.getArgs().length != 0) {
            checkPrefix = BooleanUtils.toBoolean(context.getArgs()[0]);
            if (context.getArgs().length > 1) {
                maxDeleteMessage = NumberUtils.toInt(context.getArgs()[1], 50);
            }
        }

        if (context.getMentioned().isEmpty()) {
            context.getNeoGuild().deleteMessage(context.getChannel(),
                    List.of(context.getNeoGuild().getJDAGuild().getSelfMember()),
                    maxDeleteMessage,
                    !checkPrefix);
        } else {
            context.getNeoGuild().deleteMessage(context.getChannel(),
                    context.getMentioned(),
                    maxDeleteMessage,
                    !checkPrefix);
        }
    }

    @Override
    public String getDescription() {
        return "Deletes the last 50 messages posted by the specified member.";
    }

    @Override
    public String getHelp() {
        return getName() + "[forceDelete] [amount] [member]\n----\n" +
                "[true] [<1-50>] [<selectMember>]: Deletes all messages, including those not related to the command.";
    }

    @Override
    public int getRequiredPerm() {
        return 252;
    }
}
