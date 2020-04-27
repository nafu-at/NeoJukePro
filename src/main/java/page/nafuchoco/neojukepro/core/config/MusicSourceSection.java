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

package page.nafuchoco.neojukepro.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MusicSourceSection {
    @JsonProperty("youtube")
    private boolean youtube;
    @JsonProperty("soundcloud")
    private boolean soundcloud;
    @JsonProperty("bandcamp")
    private boolean bandcamp;
    @JsonProperty("vimeo")
    private boolean vimeo;
    @JsonProperty("twitch")
    private boolean twitch;
    @JsonProperty("http")
    private boolean http;
    @JsonProperty("local")
    private boolean local;

    public boolean enableYoutube() {
        return youtube;
    }

    public boolean enableSoundCloud() {
        return soundcloud;
    }

    public boolean enableBandCamp() {
        return bandcamp;
    }

    public boolean enableVimeo() {
        return vimeo;
    }

    public boolean enableTwitch() {
        return twitch;
    }

    public boolean enableHttp() {
        return http;
    }

    public boolean enableLocal() {
        return local;
    }
}
