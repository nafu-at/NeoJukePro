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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandCache;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.database.CustomPlaylistTable;
import page.nafuchoco.neojukepro.core.player.AudioTrackLoader;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;
import page.nafuchoco.neojukepro.core.playlist.CustomPlaylist;
import page.nafuchoco.neojukepro.core.playlist.CustomPlaylistBuilder;
import page.nafuchoco.neojukepro.core.playlist.PlaylistItem;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class PlaylistCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private static final Pattern UUID_REGEX = Pattern.compile("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}");
    private static final Pattern NUMBER_REGEX = Pattern.compile("^[1-5]$");

    public PlaylistCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        GuildAudioPlayer audioPlayer = launcher.getPlayerRegistry().getGuildAudioPlayer(context.getGuild());
        if (context.getArgs().length == 0) {
        } else if (context.getArgs().length != 0 && context.getArgs().length > 2) {
            switch (context.getArgs()[0]) {
                case "flush":
                    CustomPlaylistBuilder builder = (CustomPlaylistBuilder) CommandCache.getCache(context.getGuild(), "playlist");
                    CustomPlaylist playlist = builder.build();
                    CustomPlaylistTable playlistTable = (CustomPlaylistTable) CommandCache.getCache(null, "playlistTable");
                    try {
                        playlistTable.registerPlaylist(playlist);
                    } catch (SQLException e) {
                        log.error(MessageManager.getMessage("system.db.save.error"), e);
                        context.getChannel().sendMessage(MessageManager.getMessage("command.playlist.save.failed")).queue();
                    } catch (JsonProcessingException e) {
                        log.error(MessageManager.getMessage("system.general.error"), e);
                        context.getChannel().sendMessage(MessageManager.getMessage("command.playlist.save.failed")).queue();
                    }
                    break;
            }
        } else switch (context.getArgs()[0]) {
            case "create":
                AudioPlayerManager playerManager =
                        launcher.getPlayerRegistry().getGuildAudioPlayer(context.getGuild()).getAudioPlayerManager();
                CommandCache.registerCache(context.getGuild(), "playlist",
                        new CustomPlaylistBuilder(playerManager, context.getArgs()[1], context.getGuild()));
                context.getChannel().sendMessage(MessageManager.getMessage("command.playlist.start"));
                break;

            case "add":
                CustomPlaylistBuilder builder = (CustomPlaylistBuilder) CommandCache.getCache(context.getGuild(), "playlist");
                if (context.getArgs()[1].equalsIgnoreCase("current") && audioPlayer.getNowPlaying() != null)
                    builder.addTrack(audioPlayer.getNowPlaying().getTrack());
                else
                    builder.loadAndAddTrack(context.getArgs()[1]);
                context.getChannel().sendMessage(MessageManager.getMessage("command.playlist.added")).queue();
                break;

            case "load":
                CustomPlaylist playlist = null;
                CustomPlaylistTable playlistTable = (CustomPlaylistTable) CommandCache.getCache(null, "playlistTable");
                if (NUMBER_REGEX.matcher(context.getArgs()[1]).find()) {
                    List<CustomPlaylist> playlists = (List<CustomPlaylist>) CommandCache.deleteCache(context.getGuild(), "findPlaylists");
                    if (!playlists.isEmpty()) {
                        playlist = playlists.get(NumberUtils.toInt(context.getArgs()[1], 0));
                        for (PlaylistItem item : playlist.getItems()) {
                            audioPlayer.play(new AudioTrackLoader(item.getUrl(), context.getInvoker(), 0));
                        }
                        context.getChannel().sendMessage(MessageUtil.format(
                                MessageManager.getMessage("command.playlist.loaded"), playlist.getListname())).queue();
                    }
                } else if (UUID_REGEX.matcher(context.getArgs()[1]).find()) {
                    try {
                        playlist = playlistTable.getPlaylist(context.getArgs()[1]);
                        if (playlist != null) {
                            for (PlaylistItem item : playlist.getItems()) {
                                audioPlayer.play(new AudioTrackLoader(item.getUrl(), context.getInvoker(), 0));
                            }
                            context.getChannel().sendMessage(MessageUtil.format(
                                    MessageManager.getMessage("command.playlist.loaded"), playlist.getListname())).queue();
                        }
                    } catch (SQLException e) {
                        log.error(MessageManager.getMessage("system.db.retrieving.error"), e);
                        context.getChannel().sendMessage("system.db.retrieving.error").queue();
                    } catch (JsonProcessingException e) {
                        log.error(MessageManager.getMessage("system.general.error"), e);
                        context.getChannel().sendMessage("system.db.retrieving.error").queue();
                    }
                } else {
                    try {
                        List<CustomPlaylist> playlists = playlistTable.searchPlaylist(context.getGuild().getIdLong(), context.getArgs()[1]);
                        if (playlists.isEmpty()) {
                            // TODO: 2020/05/28 Message. 
                        } else { // TODO: 2020/05/28 Replace to page selectable list
                            CommandCache.registerCache(context.getGuild(), "findPlaylists", playlists);
                            StringBuilder message = new StringBuilder();
                            message.append(MessageManager.getMessage("command.playlist.find"));
                            int count = 1;
                            for (CustomPlaylist item : playlists) {
                                if (count > 5)
                                    break;
                                message.append("\n`[" + count + "]` " + item.getListname() + "");
                                count++;
                            }
                            message.append("\n\n" + MessageManager.getMessage("command.playlist.select"));
                            context.getChannel().sendMessage(message.toString()).queue();
                        }
                    } catch (SQLException e) {
                        log.error(MessageManager.getMessage("system.db.retrieving.error"), e);
                        context.getChannel().sendMessage("system.db.retrieving.error").queue();
                    } catch (JsonProcessingException e) {
                        log.error(MessageManager.getMessage("system.general.error"), e);
                        context.getChannel().sendMessage("system.db.retrieving.error").queue();
                    }
                }

            default:
                break;
        }
    }

    @Override
    public String getDescription() {
        return null;
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
