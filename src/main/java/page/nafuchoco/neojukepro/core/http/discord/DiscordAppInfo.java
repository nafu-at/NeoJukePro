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

package page.nafuchoco.neojukepro.core.http.discord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscordAppInfo {
    @JsonProperty("bot_public")
    private boolean botPublic;
    @JsonProperty("bot_require_code_grant")
    private boolean botRequireCodeGrant;
    @JsonProperty("cover_image")
    private String coverImage;
    @JsonProperty("description")
    private String description;
    @JsonProperty("guild_id")
    private String guildID;
    @JsonProperty("icon")
    private Object icon;
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("owner")
    private Owner owner;
    @JsonProperty("primary_sku_id")
    private String primarySkuID;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("summary")
    private String summary;
    @JsonProperty("team")
    private Team team;
    @JsonProperty("verify_key")
    private String verifyKey;

    public boolean getBotPublic() {
        return botPublic;
    }

    public boolean getBotRequireCodeGrant() {
        return botRequireCodeGrant;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public String getDescription() {
        return description;
    }

    public String getGuildID() {
        return guildID;
    }

    public Object getIcon() {
        return icon;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Owner getOwner() {
        return owner;
    }

    public String getPrimarySkuID() {
        return primarySkuID;
    }

    public String getSlug() {
        return slug;
    }

    public String getSummary() {
        return summary;
    }

    public Team getTeam() {
        return team;
    }

    public String getVerifyKey() {
        return verifyKey;
    }
}
