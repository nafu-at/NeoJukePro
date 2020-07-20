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

import org.apache.commons.lang3.StringUtils;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandCache;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.ExceptionUtil;
import page.nafuchoco.neojukepro.core.http.youtube.SearchItem;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeAPIClient;
import page.nafuchoco.neojukepro.core.http.youtube.YouTubeSearchResults;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SearchCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();
    private static final YouTubeAPIClient client;

    static {
        YouTubeAPIClient apiClient;
        try {
            apiClient = new YouTubeAPIClient(launcher.getConfig().getAdvancedConfig().getGoogleAPIToken());
        } catch (IllegalArgumentException e) {
            apiClient = null;
        }
        client = apiClient;
    }

    public SearchCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        if (client == null) {
            context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.disabled")).queue();
        } else if (context.getArgs().length != 0) {
            if (context.getArgs()[0].equalsIgnoreCase("next")) {
                List<Object> objects = (List<Object>) CommandCache.deleteCache(context.getGuild(), "searchResults");
                YouTubeSearchResults results;
                String keyword;

                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof String) {
                    results = (YouTubeSearchResults) objects.get(0);
                    keyword = (String) objects.get(1);
                } else {
                    context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.searchfirst")).queue();
                    return;
                }

                try {
                    if (StringUtils.isEmpty(results.getNextPageToken())) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.nopage")).queue();
                        return;
                    }
                    results = client.searchVideos(keyword, results.getNextPageToken());

                    if (results == null || results.getItems().length == 0) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.notfound")).queue();
                        return;
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
                    context.getChannel().sendMessage(message.toString()).queue(send ->
                            CommandCache.registerCache(context.getGuild(), "searchResults", Arrays.asList(finalResults,
                                    finalKeyword, context.getMessage(), send)));
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(context.getGuild(), e, MessageManager.getMessage("command.play.search.failed"));
                }

            } else if (context.getArgs()[0].equalsIgnoreCase("prev")) {
                List<Object> objects = (List<Object>) CommandCache.deleteCache(context.getGuild(), "searchResults");
                YouTubeSearchResults results;
                String keyword;

                if (objects != null
                        && objects.get(0) instanceof YouTubeSearchResults
                        && objects.get(1) instanceof String) {
                    results = (YouTubeSearchResults) objects.get(0);
                    keyword = (String) objects.get(1);
                } else {
                    context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.searchfirst")).queue();
                    return;
                }

                try {
                    if (StringUtils.isEmpty(results.getPrevPageToken())) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.nopage")).queue();
                        return;
                    }
                    results = client.searchVideos(keyword, results.getPrevPageToken());

                    if (results == null || results.getItems().length == 0) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.notfound")).queue();
                        return;
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
                    context.getChannel().sendMessage(message.toString()).queue(send ->
                            CommandCache.registerCache(context.getGuild(), "searchResults", Arrays.asList(finalResults,
                                    finalKeyword, context.getMessage(), send)));
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(context.getGuild(), e, MessageManager.getMessage("command.play.search.failed"));
                }
            } else {
                StringBuilder builder = new StringBuilder();
                for (String arg : context.getArgs())
                    builder.append(arg + " ");
                try {
                    YouTubeSearchResults result =
                            new YouTubeAPIClient(launcher.getConfig().getAdvancedConfig().getGoogleAPIToken()).searchVideos(builder.toString());
                    if (result == null || result.getItems().length == 0) {
                        context.getChannel().sendMessage(MessageManager.getMessage("command.play.search.notfound")).queue();
                        return;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(MessageManager.getMessage("command.play.search.found"));
                    int count = 1;
                    for (SearchItem item : result.getItems()) {
                        message.append("\n`[" + count + "]` " + item.getSnippet().getTitle() + "");
                        count++;
                    }
                    message.append("\n\n" + MessageManager.getMessage("command.play.search.select"));

                    context.getChannel().sendMessage(message.toString()).queue(send ->
                            CommandCache.registerCache(context.getGuild(), "searchResults", Arrays.asList(result, builder.toString(), context.getMessage(), send)));
                } catch (IOException e) {
                    ExceptionUtil.sendStackTrace(context.getGuild(), e, MessageManager.getMessage("command.play.search.failed"));
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Do a search on YouTube for keywords.";
    }

    @Override
    public String getHelp() {
        return getName() + " [options]\n----\n" +
                "[<SearchKeyword>]: Search by the specified keyword.\n" +
                "[next]: Displays the next page of search results.\n" +
                "[prev]: Displays the previous page of search results.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
