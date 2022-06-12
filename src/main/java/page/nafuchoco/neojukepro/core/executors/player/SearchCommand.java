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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neobot.api.command.SubCommandOption;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.http.youtube.SearchItem;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeSearchResults;
import page.nafuchoco.neojukepro.core.player.AudioTrackLoader;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.player.TrackContext;
import page.nafuchoco.neojukepro.core.utils.ExceptionUtil;
import page.nafuchoco.neojukepro.module.NeoJuke;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SearchCommand extends CommandExecutor {
    private static final YouTubeAPIClient YOUTUBE_CLIENT;

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(NeoJuke.getInstance().getConfig().getBasicConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        YOUTUBE_CLIENT = apiClient;
    }

    public SearchCommand(String name) {
        super(name);

        getOptions().add(new SearchSubCommand("search"));
        getOptions().add(new PageSubCommand("next"));
        getOptions().add(new PageSubCommand("prev"));
        getOptions().add(new SearchPlaySubCommand("play"));
    }

    @Override
    public void onInvoke(CommandContext context) {
    }

    @Override
    public String getDescription() {
        return "Do a search on YouTube for keywords.";
    }


    public static class SearchSubCommand extends SubCommandOption {

        public SearchSubCommand(String name) {
            super(name);

            getOptions().add(new CommandValueOption(OptionType.STRING,
                    "keyword",
                    "Keywords you want to search.",
                    true,
                    false));
        }

        @Override
        public void onInvoke(CommandContext context) {
            var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
            neoGuild.setLastJoinedChannel(context.getChannel());
            
            if (YOUTUBE_CLIENT == null) {
                context.getResponseSender().sendMessage(MessageManager.getMessage("command.play.search.disabled")).queue();
            } else if (context.getOptions().get("keyword") != null) { // TODO: 2022/03/12 このNullチェックは不要なので消す
                val keyword = (String) context.getOptions().get("keyword").getValue();
                try {
                    YouTubeSearchResults result =
                            new YouTubeAPIClient(NeoJuke.getInstance().getConfig().getBasicConfig().getGoogleAPIToken()).searchVideos(keyword);
                    if (result == null || result.getItems().length == 0) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage("command.play.search.notfound")).queue();
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage("command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : result.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage("command.play.search.select"));

                    context.getResponseSender().sendMessage(message.toString()).queue(send ->
                            neoGuild.getGuildTempRegistry().registerTemp(
                                    "searchResults", Arrays.asList(result, keyword)));
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(
                            neoGuild,
                            e,
                            MessageManager.getMessage("command.play.search.failed"));
                }
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Do a search on YouTube for keywords.";
        }
    }


    public static class PageSubCommand extends SubCommandOption {

        public PageSubCommand(String name) {
            super(name);
        }

        @Override
        public void onInvoke(CommandContext context) {
            var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
            if (getName().equals("next")) {
                List<Object> objects = (List<Object>) neoGuild.getGuildTempRegistry().deleteTemp("searchResults");
                YouTubeSearchResults results = null;
                String keyword = null;

                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof String) {
                    results = (YouTubeSearchResults) objects.get(0);
                    keyword = (String) objects.get(1);
                } else {
                    context.getResponseSender().sendMessage(MessageManager.getMessage("command.play.search.searchfirst")).queue();
                }

                try {
                    if (StringUtils.isEmpty(results.getNextPageToken())) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage("command.play.search.nopage")).queue();
                    }
                    results = YOUTUBE_CLIENT.searchVideos(YouTubeAPIClient.SearchType.SEARCH, keyword, results.getNextPageToken());

                    if (results == null || results.getItems().length == 0) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage("command.play.search.notfound")).queue();
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage("command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : results.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage("command.play.search.select"));

                    String finalKeyword = keyword;
                    YouTubeSearchResults finalResults = results;
                    neoGuild.getGuildTempRegistry().registerTemp(
                            "searchResults", Arrays.asList(finalResults, finalKeyword));
                    context.getResponseSender().sendMessage(message.toString()).queue();
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(
                            neoGuild,
                            e,
                            MessageManager.getMessage("command.play.search.failed"));
                }
            } else if (getName().equals("prev")) {
                List<Object> objects = (List<Object>) neoGuild.getGuildTempRegistry().deleteTemp("searchResults");
                YouTubeSearchResults results = null;
                String keyword = null;

                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof String) {
                    results = (YouTubeSearchResults) objects.get(0);
                    keyword = (String) objects.get(1);
                } else {
                    context.getResponseSender().sendMessage(MessageManager.getMessage("command.play.search.searchfirst")).queue();
                }

                try {
                    if (StringUtils.isEmpty(results.getPrevPageToken())) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage("command.play.search.nopage")).queue();
                    }
                    results = YOUTUBE_CLIENT.searchVideos(YouTubeAPIClient.SearchType.SEARCH, keyword, results.getPrevPageToken());

                    if (results == null || results.getItems().length == 0) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage("command.play.search.notfound")).queue();
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage("command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : results.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage("command.play.search.select"));

                    String finalKeyword = keyword;
                    YouTubeSearchResults finalResults = results;
                    neoGuild.getGuildTempRegistry().registerTemp(
                            "searchResults", Arrays.asList(finalResults, finalKeyword));
                    context.getResponseSender().sendMessage(message.toString()).queue();
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(
                            neoGuild,
                            e,
                            MessageManager.getMessage("command.play.search.failed"));
                }
            }
        }

        @Override
        public String getDescription() {
            return "Flip through the pages of search results.";
        }
    }


    public static class SearchPlaySubCommand extends SubCommandOption {

        public SearchPlaySubCommand(String name) {
            super(name);

            getOptions().add(new CommandValueOption(OptionType.INTEGER,
                    "index",
                    "Specify the index of the search results you wish to play.",
                    true,
                    false));
        }

        @Override
        public void onInvoke(CommandContext context) {
            var neoGuild = NeoJuke.getInstance().getGuildRegistry().getNeoGuild(context.getGuild());
            NeoGuildPlayer audioPlayer = neoGuild.getAudioPlayer();
            List<Object> objects = (List<Object>) neoGuild.getGuildTempRegistry().deleteTemp("searchResults");
            if (objects != null
                    && objects.get(0) instanceof YouTubeSearchResults) {
                YouTubeSearchResults searchResult = (YouTubeSearchResults) objects.get(0);
                val index = (Integer) context.getOptions().get("index").getValue();
                audioPlayer.play(new AudioTrackLoader(new TrackContext(neoGuild, context.getInvoker(), 0,
                        "https://www.youtube.com/watch?v=" + searchResult.getItems()[index - 1].getID().getVideoID())));
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Select and play the search results.";
        }
    }
}
