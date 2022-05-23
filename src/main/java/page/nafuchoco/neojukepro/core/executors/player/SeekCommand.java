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

import net.dv8tion.jda.api.interactions.commands.OptionType;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.utils.MessageUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

public class SeekCommand extends CommandExecutor {

    public SeekCommand(String name) {
        super(name);

        getOptions().add(new CommandValueOption(OptionType.STRING, "to-time", "Seek the currently playing track.", true, false));
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild()).getAudioPlayer().getPlayingTrack() != null)
            NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild()).getAudioPlayer().seekTo(
                    MessageUtil.parseTimeToMillis((String) context.getOptions().get("ToTime").getValue())
            );
    }

    @Override
    public String getDescription() {
        return "Seek the currently playing track.";
    }


}
