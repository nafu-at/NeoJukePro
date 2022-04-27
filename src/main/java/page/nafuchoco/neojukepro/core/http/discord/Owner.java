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

import com.fasterxml.jackson.annotation.JsonProperty;

public class Owner {
    @JsonProperty("avatar")
    private String avatar;
    @JsonProperty("avatar_decoration")
    private String avatarDecoration;
    @JsonProperty("discriminator")
    private String discriminator;
    @JsonProperty("flags")
    private Long flags;
    @JsonProperty("id")
    private String id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("public_flags")
    private int publicFlags;

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarDecoration() {
        return avatarDecoration;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public Long getFlags() {
        return flags;
    }

    public String getID() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getPublicFlags() {
        return publicFlags;
    }
}
