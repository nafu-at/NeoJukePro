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
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.CommandValueOption;
import page.nafuchoco.neojukepro.core.command.SubCommandOption;
import page.nafuchoco.neojukepro.core.http.youtube.SearchItem;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeSearchResults;
import page.nafuchoco.neojukepro.core.player.AudioTrackLoader;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;
import page.nafuchoco.neojukepro.core.player.TrackContext;
import page.nafuchoco.neojukepro.core.utils.ExceptionUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SearchCommand extends CommandExecutor {
    private static final YouTubeAPIClient YOUTUBE_CLIENT;

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(Main.getLauncher().getConfig().getAdvancedConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        YOUTUBE_CLIENT = apiClient;
    }

    public SearchCommand(String name, String... aliases) {
        super(name, aliases);

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

    @Override
    public int getRequiredPerm() {
        return 0;
    }


    public static class SearchSubCommand extends SubCommandOption {

        public SearchSubCommand(String name, String... aliases) {
            super(name, aliases);

            getOptions().add(new CommandValueOption(OptionType.STRING,
                    "keyword",
                    "Keywords you want to search.",
                    true,
                    false));
        }

        @Override
        public void onInvoke(CommandContext context) {
            if (YOUTUBE_CLIENT == null) {
                context.getResponseSender().sendMessage(MessageManager.getMessage(
                        context.getNeoGuild().getSettings().getLang(),
                        "command.play.search.disabled")).queue();
            } else if (context.getOptions().get("keyword") != null) { // TODO: 2022/03/12 このNullチェックは不要なので消す
                val keyword = (String) context.getOptions().get("keyword").getValue();
                try {
                    YouTubeSearchResults result =
                            new YouTubeAPIClient(context.getNeoJukePro().getConfig().getAdvancedConfig().getGoogleAPIToken()).searchVideos(keyword);
                    if (result == null || result.getItems().length == 0) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.notfound")).queue();
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : result.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.select"));

                    context.getResponseSender().sendMessage(message.toString()).queue(send ->
                            context.getNeoGuild().getGuildTempRegistry().registerTemp(
                                    "searchResults", Arrays.asList(result, keyword)));
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(
                            context.getNeoGuild(),
                            e,
                            MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.play.search.failed"));
                }
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Do a search on YouTube for keywords.";
        }

        @Override
        public int getRequiredPerm() {
            return 0;
        }
    }


    public static class PageSubCommand extends SubCommandOption {

        public PageSubCommand(String name, String... aliases) {
            super(name, aliases);
        }

        @Override
        public void onInvoke(CommandContext context) {
            if (getName().equals("next")) {
                List<Object> objects = (List<Object>) context.getNeoGuild().getGuildTempRegistry().deleteTemp("searchResults");
                YouTubeSearchResults results = null;
                String keyword = null;

                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof String) {
                    results = (YouTubeSearchResults) objects.get(0);
                    keyword = (String) objects.get(1);
                } else {
                    context.getResponseSender().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.searchfirst")).queue();
                }

                try {
                    if (StringUtils.isEmpty(results.getNextPageToken())) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.nopage")).queue();
                    }
                    results = YOUTUBE_CLIENT.searchVideos(YouTubeAPIClient.SearchType.SEARCH, keyword, results.getNextPageToken());

                    if (results == null || results.getItems().length == 0) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.notfound")).queue();
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : results.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.select"));

                    String finalKeyword = keyword;
                    YouTubeSearchResults finalResults = results;
                    context.getNeoGuild().getGuildTempRegistry().registerTemp(
                            "searchResults", Arrays.asList(finalResults, finalKeyword));
                    context.getResponseSender().sendMessage(message.toString()).queue();
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(
                            context.getNeoGuild(),
                            e,
                            MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.play.search.failed"));
                }
            } else if (getName().equals("prev")) {
                List<Object> objects = (List<Object>) context.getNeoGuild().getGuildTempRegistry().deleteTemp("searchResults");
                YouTubeSearchResults results = null;
                String keyword = null;

                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof String) {
                    results = (YouTubeSearchResults) objects.get(0);
                    keyword = (String) objects.get(1);
                } else {
                    context.getResponseSender().sendMessage(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.searchfirst")).queue();
                }

                try {
                    if (StringUtils.isEmpty(results.getPrevPageToken())) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.nopage")).queue();
                    }
                    results = YOUTUBE_CLIENT.searchVideos(YouTubeAPIClient.SearchType.SEARCH, keyword, results.getPrevPageToken());

                    if (results == null || results.getItems().length == 0) {
                        context.getResponseSender().sendMessage(MessageManager.getMessage(
                                context.getNeoGuild().getSettings().getLang(),
                                "command.play.search.notfound")).queue();
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : results.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage(
                            context.getNeoGuild().getSettings().getLang(),
                            "command.play.search.select"));

                    String finalKeyword = keyword;
                    YouTubeSearchResults finalResults = results;
                    context.getNeoGuild().getGuildTempRegistry().registerTemp(
                            "searchResults", Arrays.asList(finalResults, finalKeyword));
                    context.getResponseSender().sendMessage(message.toString()).queue();
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(
                            context.getNeoGuild(),
                            e,
                            MessageManager.getMessage(
                                    context.getNeoGuild().getSettings().getLang(),
                                    "command.play.search.failed"));
                }
            }
        }

        @Override
        public String getDescription() {
            return "Flip through the pages of search results.";
        }

        @Override
        public int getRequiredPerm() {
            return 0;
        }
    }


    public static class SearchPlaySubCommand extends SubCommandOption {

        public SearchPlaySubCommand(String name, String... aliases) {
            super(name, aliases);

            getOptions().add(new CommandValueOption(OptionType.INTEGER,
                    "index",
                    "Specify the index of the search results you wish to play.",
                    true,
                    false));
        }

        @Override
        public void onInvoke(CommandContext context) {
            NeoGuildPlayer audioPlayer = context.getNeoGuild().getAudioPlayer();
            List<Object> objects = (List<Object>) context.getNeoGuild().getGuildTempRegistry().deleteTemp("searchResults");
            if (objects != null
                    && objects.get(0) instanceof YouTubeSearchResults) {
                YouTubeSearchResults searchResult = (YouTubeSearchResults) objects.get(0);
                val index = (Integer) context.getOptions().get("index").getValue();
                audioPlayer.play(new AudioTrackLoader(new TrackContext(context.getNeoGuild(), context.getInvoker(), 0,
                        "https://www.youtube.com/watch?v=" + searchResult.getItems()[index - 1].getID().getVideoID())));
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Select and play the search results.";
        }

        @Override
        public int getRequiredPerm() {
            return 0;
        }
    }
}
