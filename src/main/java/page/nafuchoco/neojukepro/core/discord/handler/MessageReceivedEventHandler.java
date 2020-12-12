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

package page.nafuchoco.neojukepro.core.discord.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.*;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;

import java.util.Arrays;

@Slf4j
@AllArgsConstructor
public final class MessageReceivedEventHandler extends ListenerAdapter {
    private final NeoJukePro neoJukePro;
    private final CommandExecuteAuth authManager;
    private final CommandRegistry registry;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // レジストリが登録されて居ない場合は無視
        if (registry == null)
            return;

        // 自分自身の投稿, Botによる投稿, WebHookによる投稿, Botが投稿できないチャンネルへの投稿を無視
        if (event.getAuthor() == event.getJDA().getSelfUser() ||
                !event.getTextChannel().canTalk())
            return;

        NeoGuild neoGuild = neoJukePro.getGuildRegistry().getNeoGuild(event.getGuild());
        String prefix = neoGuild.getSettings().getCommandPrefix();
        boolean robot = neoGuild.getSettings().isRobotMode();

        String raw = event.getMessage().getContentRaw();
        String input;
        if (raw.startsWith(prefix)) {
            input = raw.substring(prefix.length()).trim();
        } else if (!event.getMessage().getMentions().isEmpty()) { // 自分宛てのメンションの場合はコマンドとして認識
            if (!event.getMessage().isMentioned(event.getJDA().getSelfUser()))
                return;
            input = raw.substring(event.getJDA().getSelfUser().getAsMention().length() + 1).trim();
        } else { // コマンドではないメッセージを無視
            return;
        }
        if (!robot && (event.getAuthor().isBot() || event.isWebhookMessage()))
            return; // Robotモード以外でBotからの投稿に反応しない。

        if (input.isEmpty())
            return;

        String[] commands = input.split("; ");
        for (String commandString : commands) {
            CommandContext context = parseCommand(neoGuild, commandString, event);
            if (context == null) {
                event.getChannel().sendMessage(MessageUtil.format(
                        MessageManager.getMessage("command.nocommand"), commandString, prefix)).queue();
            } else {
                log.debug("Command Received: {}", context.toString());

                // コマンドの実行権限の確認
                if (authManager.getUserPerm(event.getMember()) <
                        context.getCommand().getRequiredPerm()) {
                    event.getChannel().sendMessage(MessageManager.getMessage("command.nopermission")).queue();
                    continue;
                }

                try {
                    context.getCommand().onInvoke(context);
                } catch (Exception e) {
                    ExceptionUtil.sendStackTrace(event.getGuild(), e, MessageManager.getMessage("command.execute.failed"));
                }
            }
        }
        neoGuild.setLastJoinedChannel(event.getTextChannel());
    }

    private CommandContext parseCommand(NeoGuild neoGuild, String commandString, MessageReceivedEvent event) {
        // コマンドオプションを分割
        String[] args = commandString.split("\\p{javaSpaceChar}+");
        if (args.length == 0)
            return null;
        String commandTrigger = args[0];

        // コマンドクラスの取得
        CommandExecutor command = registry.getExecutor(commandTrigger.toLowerCase());
        if (command == null)
            return null;
        else
            return new CommandContext(
                    neoJukePro,
                    neoGuild,
                    event.getTextChannel(),
                    event.getMember(),
                    event.getMessage(),
                    commandTrigger,
                    Arrays.copyOfRange(args, 1, args.length),
                    command);
    }

    /**
     * Returns the command registry associated with the handler.
     *
     * @return The command registry associated with the handler
     */
    public CommandRegistry getRegistry() {
        return registry;
    }
}
