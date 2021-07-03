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

import lombok.val;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.guild.NeoGuildPlayerOptions;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;

public class StatusCommand extends CommandExecutor {

    public StatusCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        NeoGuildPlayerOptions playerOptions = context.getNeoGuild().getSettings().getPlayerOptions();
        NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
        val builder = new StringBuilder(MessageManager.getMessage(
                context.getNeoGuild().getSettings().getLang(),
                "command.status") + "\n```");
        if (audioPlayer.getPlayingTrack() != null)
            builder.append("Playing Track: ").append(audioPlayer.getPlayingTrack().getTrack().getInfo().title).append("\n");
        builder.append("Registered Queues: ").append(audioPlayer.getTrackProvider().getQueues().size()).append("\n");
        builder.append("Pause: ").append(audioPlayer.isPaused()).append("\n");
        builder.append("Volume: ").append(audioPlayer.getVolume()).append("\n");
        builder.append("Shuffle: ").append(playerOptions.isShuffle()).append("\n");
        builder.append("Repeat Mode: ").append(playerOptions.getRepeatMode()).append("\n");
        builder.append("```");
        context.getChannel().sendMessage(builder.toString()).queue();
    }

    @Override
    public String getDescription() {
        return "Displays the current state of the player.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
