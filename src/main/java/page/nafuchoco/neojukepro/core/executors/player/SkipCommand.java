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

package page.nafuchoco.neojukepro.core.executors.player;

import lombok.val;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;

public class SkipCommand extends CommandExecutor {

    public SkipCommand(String name, String... aliases) {
        super(name, aliases);

        getOptions().add(new CommandValueOption(OptionType.STRING,
                "index",
                "Skips tracks between or after the specified numbers.",
                false,
                false));
        getOptions().add(new CommandValueOption(OptionType.MENTIONABLE,
                "invoker",
                "Skips the tracks added by the specified user.",
                false,
                false));
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
        if (audioPlayer.getPlayingTrack() != null) {
            if (context.getOptions().isEmpty()) {
                context.getHook().sendMessage(MessageUtil.format(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.skip"),
                        context.getNeoGuild().getAudioPlayer().getPlayingTrack().getTrack().getInfo().title)).queue();
                audioPlayer.skip();
            } else if (context.getOptions().get("invoker") != null) {
                if (context.getOptions().get("invoker").getValue() instanceof Member member) {
                    int skipcount = audioPlayer.skip(member).size();
                    context.getResponseSender().sendMessage(MessageUtil.format(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.skip.skip.count"), skipcount)).queue();
                }
            } else if (context.getOptions().get("index") != null) {
                val indexS = (String) context.getOptions().get("index").getValue();
                if (indexS.contains("-")) {
                    String[] split = indexS.split("-");
                    if (split.length == 1 && indexS.endsWith("-")) {
                        int below = Integer.parseInt(indexS.replace("-", ""));
                        audioPlayer.skip(below);
                        context.getResponseSender().sendMessage(MessageUtil.format(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.skip.below"), below)).queue();
                    } else {
                        audioPlayer.skip(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                        context.getResponseSender().sendMessage(MessageUtil.format(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.skip.between"), split[0], split[1])).queue();
                    }
                } else {
                    try {
                        context.getHook().sendMessage(MessageUtil.format(MessageManager.getMessage(
                                        context.getNeoGuild().getSettings().getLang(),
                                        "command.skip"),
                                audioPlayer.skip(Integer.parseInt(indexS), Integer.parseInt(indexS)).get(0).getTrack().getInfo().title)).queue();
                    } catch (IllegalArgumentException e) {
                        // nothing
                    }
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Skip the track.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
