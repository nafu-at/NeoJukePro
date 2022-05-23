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
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

public class SkipCommand extends CommandExecutor {

    public SkipCommand(String name) {
        super(name);

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
        NeoGuildPlayer audioPlayer = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild()).getAudioPlayer();
        if (audioPlayer.getPlayingTrack() != null) {
            if (context.getOptions().isEmpty()) {
                context.getHook().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip"),
                        audioPlayer.getPlayingTrack().getTrack().getInfo().title)).queue();
                audioPlayer.skip();
            } else if (context.getOptions().get("invoker") != null) {
                if (context.getOptions().get("invoker").getValue() instanceof Member member) {
                    int skipcount = audioPlayer.skip(member).size();
                    context.getResponseSender().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip.skip.count"), skipcount)).queue();
                }
            } else if (context.getOptions().get("index") != null) {
                val indexS = (String) context.getOptions().get("index").getValue();
                if (indexS.contains("-")) {
                    String[] split = indexS.split("-");
                    if (split.length == 1 && indexS.endsWith("-")) {
                        int below = Integer.parseInt(indexS.replace("-", ""));
                        audioPlayer.skip(below);
                        context.getResponseSender().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip.below"), below)).queue();
                    } else {
                        audioPlayer.skip(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                        context.getResponseSender().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip.between"), split[0], split[1])).queue();
                    }
                } else {
                    try {
                        context.getHook().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip"),
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


}
