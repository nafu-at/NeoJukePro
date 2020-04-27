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

package page.nafuchoco.neojukepro.core.http.youtube;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Snippet {
    private String publishedAt;
    private String channelID;
    private String title;
    private String description;
    private String customUrl;
    private Thumbnails thumbnails;
    private String channelTitle;
    private String[] tags;
    private String categoryID;
    private String liveBroadcastContent;
    private String defaultLanguage;
    private Localized localized;
    private String defaultAudioLanguage;
    private String country;

    @JsonProperty("publishedAt")
    public String getPublishedAt() {
        return publishedAt;
    }

    @JsonProperty("channelId")
    public String getChannelID() {
        return channelID;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("customUrl")
    public String getCustomUrl() {
        return customUrl;
    }

    @JsonProperty("thumbnails")
    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    @JsonProperty("channelTitle")
    public String getChannelTitle() {
        return channelTitle;
    }

    @JsonProperty("tags")
    public String[] getTags() {
        return tags;
    }

    @JsonProperty("categoryId")
    public String getCategoryID() {
        return categoryID;
    }

    @JsonProperty("liveBroadcastContent")
    public String getLiveBroadcastContent() {
        return liveBroadcastContent;
    }

    @JsonProperty("defaultLanguage")
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    @JsonProperty("localized")
    public Localized getLocalized() {
        return localized;
    }

    @JsonProperty("defaultAudioLanguage")
    public String getDefaultAudioLanguage() {
        return defaultAudioLanguage;
    }

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }
}
