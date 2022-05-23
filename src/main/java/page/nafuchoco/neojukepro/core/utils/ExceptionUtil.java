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

package page.nafuchoco.neojukepro.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
public class ExceptionUtil {

    private ExceptionUtil() {
        throw new IllegalStateException();
    }

    public static void sendStackTrace(NeoGuild guild, Throwable throwable, String... message) {
        sendStackTrace(guild, null, true, throwable, message);
    }

    public static void sendStackTrace(NeoGuild guild, boolean toLog, Throwable throwable, String... message) {
        sendStackTrace(guild, null, toLog, throwable, message);
    }

    public static void sendStackTrace(NeoGuild guild, CommandContext context, boolean toLog, Throwable throwable, String... message) {
        var stringWriter = new StringWriter();
        var printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        var trace = stringWriter.toString();

        var builder = new StringBuilder();
        builder.append(MessageManager.getMessage("command.exception") + "\n");
        for (String msg : message)
            builder.append(msg + "\n");

        if (trace.length() > 1650)
            trace = trace.substring(0, 1650) + " [...]";

        builder.append("`").append(throwable.getClass().getName()).append("`\n");
        builder.append("```");
        builder.append(trace);
        builder.append("```");

        guild.sendMessageToLatest(builder.toString());

        if (toLog) {
            var sb = new StringBuilder();
            for (String msg : message)
                sb.append(msg + "\n");
            MDC.put("GuildId", guild.getJDAGuild().getId());
            if (context != null) {
                MDC.put("CommandExecutor", context.getCommand().getName());
                MDC.put("CommandArgs", context.getOptions().toString());
            }
            log.error(sb.toString(), throwable);
        }
    }
}
