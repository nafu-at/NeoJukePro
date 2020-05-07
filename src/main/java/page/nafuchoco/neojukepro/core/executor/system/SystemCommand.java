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

package page.nafuchoco.neojukepro.core.executor.system;

import net.dv8tion.jda.api.sharding.ShardManager;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class SystemCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    public SystemCommand(String name, String... aliases) {
        super(name, aliases);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    }

    @Override
    public void onInvoke(CommandContext context) {
        long max = Runtime.getRuntime().maxMemory() / 1048576L;
        long total = Runtime.getRuntime().totalMemory() / 1048576L;
        long free = Runtime.getRuntime().freeMemory() / 1048576L;
        long useing = total - free;
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        ShardManager shardManager = launcher.getShardManager();

        StringBuilder builder = new StringBuilder();
        builder.append("This Bot has been running for " + formatTime(uptime) + " minutes since it was started.\n");
        builder.append("```\n");
        builder.append("====== Memory Info ======\n");
        builder.append("Reserved memory:       " + total + "MB\n");
        builder.append("  -> Used:             " + useing + "MB\n");
        builder.append("  -> Free:             " + free + "MB\n");
        builder.append("Max. reserved memory:  " + max + "MB\n\n");
        builder.append("====== Statistic Info ======\n");
        builder.append("Server Count:         " + shardManager.getGuilds().size() + "\n");
        builder.append("User Count:           " + shardManager.getUsers().size() + "\n");
        builder.append("Text Channels:        " + shardManager.getTextChannels().size() + "\n");
        builder.append("Voice Channels:       " + shardManager.getVoiceChannels().size() + "\n");
        builder.append("Active Players:       " + launcher.getPlayerRegistry().getPlayers().size() + "\n\n");
        builder.append("```");
        context.getChannel().sendMessage(builder.toString()).queue();
    }

    private String formatTime(long millis) {
        long t = millis / 1000L;
        int sec = (int) (t % 60L);
        int min = (int) ((t % 3600L) / 60L);
        int hrs = (int) (t / 3600L);

        String timestamp;

        if (hrs != 0)
            timestamp = hrs + "hr. " + min + "min. " + sec + "sec.";
        else
            timestamp = min + "min. " + sec + "sec.";
        return timestamp;
    }

    @Override
    public String getDescription() {
        return "Displays system information.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 254;
    }
}
