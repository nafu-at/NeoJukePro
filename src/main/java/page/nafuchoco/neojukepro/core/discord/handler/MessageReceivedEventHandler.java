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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandRegistry;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;
import page.nafuchoco.neojukepro.core.guild.user.NeoGuildMember;
import page.nafuchoco.neojukepro.core.utils.ExceptionUtil;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public final class MessageReceivedEventHandler extends ListenerAdapter {
    private static final Pattern MENTION_REGEX = Pattern.compile("<@(?:!|)[0-9]{18}>");
    private final NeoJukePro neoJukePro;
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

        var neoGuild = neoJukePro.getGuildRegistry().getNeoGuild(event.getGuild());
        String prefix = neoGuild.getSettings().getCommandPrefix();
        boolean robot = neoGuild.getSettings().isRobotMode();

        String raw = event.getMessage().getContentRaw();
        log.debug("Raw input message: {}", raw);
        String input;
        if (raw.startsWith(prefix)) {
            input = raw.substring(prefix.length()).trim();
        } else if (!event.getMessage().getMentions().isEmpty()) { // 自分宛てのメンションの場合はコマンドとして認識
            if (!event.getMessage().isMentioned(event.getJDA().getSelfUser(), Message.MentionType.USER, Message.MentionType.ROLE))
                return;
            input = raw.substring(event.getJDA().getSelfUser().getAsMention().length() + 1).trim();
        } else { // コマンドではないメッセージを無視
            return;
        }
        if (!robot && (event.getAuthor().isBot() || event.isWebhookMessage()))
            return; // Robotモード以外でBotからの投稿に反応しない。

        if (input.isEmpty())
            return;

        var neoGuildMember = neoGuild.getGuildMemberRegistry().getNeoGuildMember(event.getMember().getIdLong());
        neoGuild.setLastJoinedChannel(event.getTextChannel());

        String[] commands = input.split("; ");
        for (String commandString : commands) {
            CommandContext context = parseCommand(neoGuild, neoGuildMember, commandString, event);
            if (context == null) {
                event.getChannel().sendMessage(MessageUtil.format(
                        MessageManager.getMessage(neoGuild.getSettings().getLang(), "command.nocommand"), commandString, prefix)).queue();
            } else {
                log.debug("Command Received: {}", context);

                // コマンドの実行権限の確認
                // 0 = Normal User.
                // 252 = Guild Admin.
                // 253 = Guild Owner.
                // 254 = Bot Admin.
                // 255 = Bot Owner.
                if (neoGuildMember.getUserPermission() < context.getCommand().getRequiredPerm()) {
                    event.getChannel().sendMessage(MessageManager.getMessage(neoGuild.getSettings().getLang(), "command.nopermission")).queue();
                    continue;
                }

                try {
                    context.getCommand().onInvoke(context);
                } catch (Exception e) {
                    ExceptionUtil.sendStackTrace(
                            neoJukePro.getGuildRegistry().getNeoGuild(event.getGuild()),
                            e,
                            MessageManager.getMessage(neoGuild.getSettings().getLang(), "command.execute.failed"));
                }
            }
        }
    }

    private CommandContext parseCommand(NeoGuild neoGuild, NeoGuildMember neoGuildMember, String commandString, MessageReceivedEvent event) {
        // コマンドオプションを分割
        String[] args = commandString.split("\\p{javaSpaceChar}+");
        if (args.length == 0)
            return null;
        String commandTrigger = args[0];

        // メンションされたユーザーの一覧
        List<Member> mentioned = Arrays.stream(args)
                .filter(arg -> MENTION_REGEX.matcher(arg).find())
                .map(member -> {
                    Member memberObj = neoGuild.getJDAGuild().getMemberById(member.substring(3, member.length() - 1));
                    if (memberObj == null) {
                        try {
                            memberObj = neoGuild.getJDAGuild().retrieveMemberById(member.substring(3, member.length() - 1)).submit().get();
                        } catch (InterruptedException | ExecutionException e) {
                            ExceptionUtil.sendStackTrace(
                                    neoJukePro.getGuildRegistry().getNeoGuild(event.getGuild()),
                                    e,
                                    MessageManager.getMessage(neoGuild.getSettings().getLang(), "command.execute.failed"));
                        }
                    }
                    return memberObj;
                })
                .collect(Collectors.toList());

        // メンションを削除
        args = Arrays.stream(args).filter(arg -> !MENTION_REGEX.matcher(arg).find()).toArray(String[]::new);

        // コマンドクラスの取得
        CommandExecutor command = registry.getExecutor(neoGuild, commandTrigger.toLowerCase());
        if (command == null)
            return null;
        else
            return new CommandContext(
                    neoJukePro,
                    neoGuild,
                    event.getTextChannel(),
                    neoGuildMember,
                    event.getMessage(),
                    commandTrigger,
                    Arrays.copyOfRange(args, 1, args.length),
                    mentioned,
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
