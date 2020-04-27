/*
 * Copyright 2019 くまねこそふと.
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
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;

public class SeekCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();

    public SeekCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getArgs().length != 0) {
            GuildAudioPlayer audioPlayer = launcher.getPlayerRegistry().getGuildAudioPlayer(context.getGuild());
            if (audioPlayer.getNowPlaying() != null)
                audioPlayer.seekTo(MessageUtil.parseTimeToMillis(context.getArgs()[0]));
        }
    }

    @Override
    public String getDescription() {
        return "Seek the currently playing track.";
    }

    @Override
    public String getHelp() {
        return getName() + "[<ToTime>]\n----\n" +
                "[<ToTime>]: Seek the currently playing track.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
