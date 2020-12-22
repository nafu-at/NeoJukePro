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

import lavalink.client.io.LavalinkSocket;
import lavalink.client.io.jda.JdaLavalink;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.MessageUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class NodesCommand extends CommandExecutor {

    public NodesCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (context.getNeoJukePro().getConfig().getAdvancedConfig().isUseNodeServer()) {
            JdaLavalink lavalink = context.getNeoJukePro().getLavaLink();
            if (context.getArgs().length == 0) {
                StringBuilder builder = new StringBuilder();
                List<LavalinkSocket> nodes = lavalink.getNodes();
                builder.append(MessageUtil.format(MessageManager.getMessage("command.nodes.list"), nodes.size()) + "\n");
                builder.append("```");
                for (int i = 0; nodes.size() > i; i++) {
                    LavalinkSocket node = nodes.get(i);
                    builder.append("\nNo." + i);
                    builder.append("\nNodeName: " + node.getName() + "\n");
                    builder.append("Address: " + node.getRemoteUri() + "\n");
                    builder.append("Memory: " + fromByteToMB(node.getStats().getMemUsed()) + "MB / " + fromByteToMB(node.getStats().getMemReservable()) + "MB\n");
                    builder.append("Players: " + node.getStats().getPlayingPlayers() + " / " + node.getStats().getPlayers() + "\n");
                }
                builder.append("```");
                context.getChannel().sendMessage(builder.toString()).queue();
            } else switch (context.getArgs()[0]) {
                case "add":
                    try {
                        if (context.getArgs().length == 3) {
                            lavalink.addNode(new URI(context.getArgs()[1]),
                                    context.getArgs()[2]);
                        } else if (context.getArgs().length == 4) {
                            lavalink.addNode(context.getArgs()[1], new URI(context.getArgs()[2]), context.getArgs()[3]);
                        }
                        context.getMessage().delete().submit();
                        context.getChannel().sendMessage(MessageUtil.format(
                                MessageManager.getMessage("command.nodes.add"), lavalink.getNodes().size() - 1)).queue();
                    } catch (URISyntaxException e) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.nodes.notcorrect")).queue();
                    }
                    break;

                case "remove":
                    try {
                        lavalink.removeNode(Integer.parseInt(context.getArgs()[1]));
                        context.getChannel().sendMessage(MessageManager.getMessage("command.nodes.remove")).queue();
                    } catch (NumberFormatException e) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.nodes.number")).queue();
                    }
                    break;

                default:
                    break;
            }
        } else {
            context.getChannel().sendMessage(MessageManager.getMessage("command.nodes.disabled")).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Manipulate the nodes registered in the bot.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 254;
    }

    private float fromByteToMB(long value) {
        BigDecimal bd = BigDecimal.valueOf((float) value / 1024 / 1014);
        return bd.setScale(1, RoundingMode.HALF_UP).floatValue();
    }
}
