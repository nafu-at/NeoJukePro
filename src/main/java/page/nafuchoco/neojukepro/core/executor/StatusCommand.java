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

package page.nafuchoco.neojukepro.core.executor;

import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;

public class StatusCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();

    public StatusCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        GuildAudioPlayer audioPlayer = launcher.getPlayerRegistry().getGuildAudioPlayer(context.getGuild());
        StringBuilder builder = new StringBuilder("**Now guild player status**\n```");
        builder.append("Playing Track: " + audioPlayer.getNowPlaying() + "\n");
        builder.append("Registered Queues: " + audioPlayer.getTrackProvider().getQueues().size() + "\n");
        builder.append("Pause: " + audioPlayer.isPaused() + "\n");
        builder.append("Volume: " + audioPlayer.getVolume() + "\n");
        builder.append("Shuffle: " + audioPlayer.isShuffle() + "\n");
        builder.append("Repeat Mode: " + audioPlayer.getRepeatType().name() + "\n");
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
