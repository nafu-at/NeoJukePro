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
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

import java.lang.management.ManagementFactory;

public class SystemCommand extends CommandExecutor {

    public SystemCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        long max = Runtime.getRuntime().maxMemory() / 1048576L;
        long total = Runtime.getRuntime().totalMemory() / 1048576L;
        long free = Runtime.getRuntime().freeMemory() / 1048576L;
        long used = total - free;
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        val shardManager = context.getNeoJukePro().getShardManager();

        val builder = new StringBuilder();
        builder.append("This Bot has been running for " + formatTime(uptime) + " since it was started.\n");
        builder.append("```\n");
        builder.append("====== System Info ======\n");
        builder.append("Operating System:      ").append(System.getProperty("os.name")).append("\n");
        builder.append("JVM Version:           ").append(System.getProperty("java.version")).append("\n");
        builder.append("NeoJukePro Version:    ").append(Main.class.getPackage().getImplementationVersion()).append("\n\n");
        builder.append("====== Memory Info ======\n");
        builder.append("Reserved memory:       ").append(total).append("MB\n");
        builder.append("  -> Used:             ").append(used).append("MB\n");
        builder.append("  -> Free:             ").append(free).append("MB\n");
        builder.append("Max. reserved memory:  ").append(max).append("MB\n\n");
        builder.append("====== Statistic Info ======\n");
        builder.append("Guild Count:           ").append(shardManager.getGuildCache().size()).append("\n");
        builder.append("Active Guilds:         ").append(context.getNeoJukePro().getGuildRegistry().getNeoGuilds().size()).append("\n");
        builder.append("Active Player:         ").append(context.getNeoJukePro().getGuildRegistry().getPlayerActiveGuilds().size()).append("\n");
        builder.append("User Count:            ").append(shardManager.getUserCache().size()).append("\n");
        builder.append("Text Channels:         ").append(shardManager.getTextChannelCache().size()).append("\n");
        builder.append("Voice Channels:        ").append(shardManager.getVoiceChannelCache().size()).append("\n");
        builder.append("```");

        context.getResponseSender().sendMessage(builder.toString()).queue();
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
    public int getRequiredPerm() {
        return 254;
    }
}
