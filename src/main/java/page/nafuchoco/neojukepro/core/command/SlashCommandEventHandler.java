/*
 * Copyright 2022 NAFU_at.
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

package page.nafuchoco.neojukepro.core.command;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.utils.ExceptionUtil;

import java.util.HashMap;

@Slf4j
@AllArgsConstructor
public class SlashCommandEventHandler extends ListenerAdapter {
    private final NeoJukePro neoJukePro;
    private final CommandRegistry registry;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // とりあえずDiscordにコマンドを受け付けた事を返す
        event.deferReply(true).queue();
        val hook = event.getHook();

        // レジストリが登録されて居ない場合は無視
        if (registry == null)
            return;

        val neoGuild = neoJukePro.getGuildRegistry().getNeoGuild(event.getGuild());
        // TODO: 2022/03/11 ポリシー的にメンバーを保存するのやめたい 
        val neoGuildMember = neoGuild.getGuildMemberRegistry().getNeoGuildMember(event.getMember().getIdLong());
        neoGuild.setLastJoinedChannel(event.getTextChannel());

        // コマンドクラスの取得
        CommandExecutor command = registry.getExecutor(neoGuild, event.getName());
        CommandExecutor subCommand = command.getSubCommands().stream().filter(option -> option.optionName().equals(event.getSubcommandName())).findAny().orElse(null);

        // オプションの処理
        val optionsMap = new HashMap<String, AssignedCommandValueOption>();
        for (CommandOption option : command.getOptions()) {
            val optionMapping = event.getOption(option.optionName());
            if (optionMapping == null)
                continue;

            val assignedCommandOption = parseCommandOptions(option, optionMapping);
            optionsMap.put(option.optionName(), assignedCommandOption);
        }

        // コマンドコンテキストの生成
        CommandContext context = new CommandContext(
                neoJukePro,
                neoGuild,
                event.getTextChannel(),
                neoGuildMember,
                hook,
                event.getName(),
                optionsMap,
                command,
                subCommand);


        log.debug("Command Received: {}", context);

        // コマンドの実行権限の確認
        // 0 = Normal User.
        // 252 = Guild Admin.
        // 253 = Guild Owner.
        // 254 = Bot Admin.
        // 255 = Bot Owner.
        if (neoGuildMember.getUserPermission() < context.getCommand().getRequiredPerm()) {
            hook.sendMessage(MessageManager.getMessage(neoGuild.getSettings().getLang(), "command.nopermission")).queue();
        }

        try {
            String result;
            if (context.getSubCommand() == null)
                result = context.getCommand().onInvoke(context);
            else
                result = context.getSubCommand().onInvoke(context);
            
            if (result != null)
                hook.sendMessage(result).setEphemeral(true).queue();
        } catch (Exception e) {
            // TODO: 2022/03/11 後々Hookに対してエラーを送信するように
            ExceptionUtil.sendStackTrace(
                    neoJukePro.getGuildRegistry().getNeoGuild(event.getGuild()),
                    e,
                    MessageManager.getMessage(neoGuild.getSettings().getLang(), "command.execute.failed"));
        }
    }

    private AssignedCommandValueOption parseCommandOptions(CommandOption option, OptionMapping mapping) {
        return switch (option.optionType()) {
            case STRING -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsString());

            case INTEGER -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsInt());

            case BOOLEAN -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsBoolean());

            case USER -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsUser());

            case CHANNEL -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsGuildChannel());

            case ROLE -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsRole());

            case MENTIONABLE -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsMentionable());

            case NUMBER -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsDouble());

            case ATTACHMENT -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsAttachment());

            default -> new AssignedCommandValueOption((CommandValueOption) option, mapping);
        };
    }
}
