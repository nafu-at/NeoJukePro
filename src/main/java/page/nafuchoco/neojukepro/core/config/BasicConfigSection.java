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

import java.util.List;

public class BasicConfigSection {
    @JsonProperty("discordToken")
    private String discordToken;
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("botAdmins")
    private List<Long> botAdmins;
    @JsonProperty("musicSource")
    private MusicSourceSection musicSource;
    @JsonProperty("database")
    private DatabaseSection database;

    public String getDiscordToken() {
        return discordToken;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<Long> getBotAdmins() {
        return botAdmins;
    }

    public MusicSourceSection getMusicSource() {
        return musicSource;
    }

    public DatabaseSection getDatabase() {
        return database;
    }
}
