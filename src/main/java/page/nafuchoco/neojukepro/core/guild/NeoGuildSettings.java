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

package page.nafuchoco.neojukepro.core.guild;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import page.nafuchoco.neojukepro.module.NeoJuke;

@Slf4j
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NeoGuildSettings {
    private static final Gson gson = new Gson();
    private final long guildId;
    private final NeoGuildPlayerOptions playerOptions;

    public void setVolumeLevel(int volumeLevel) {
        getPlayerOptions().setVolumeLevel(volumeLevel);
        NeoJuke.getInstance().getGuildRegistry().getNeoGuild(guildId).getAudioPlayer().setVolume(volumeLevel);

        NeoJuke.getInstance().getSettingsStore().saveStoreData(guildId, "player_options", gson.toJson(getPlayerOptions()));
    }

    public void setRepeatMode(NeoGuildPlayerOptions.RepeatMode repeatMode) {
        getPlayerOptions().setRepeatMode(repeatMode);
        NeoJuke.getInstance().getSettingsStore().saveStoreData(guildId, "player_options", gson.toJson(getPlayerOptions()));
    }

    public void setShuffle(boolean shuffle) {
        getPlayerOptions().setShuffle(shuffle);
        if (shuffle)
            NeoJuke.getInstance().getGuildRegistry().getNeoGuild(guildId).getAudioPlayer().getTrackProvider().shuffle();
        NeoJuke.getInstance().getSettingsStore().saveStoreData(guildId, "player_options", gson.toJson(getPlayerOptions()));
    }

    public void disableSource(String sourceName) {
        getPlayerOptions().disableSource(sourceName);
        NeoJuke.getInstance().getSettingsStore().saveStoreData(guildId, "player_options", gson.toJson(getPlayerOptions()));
    }

    public void enableSource(String sourceName) {
        getPlayerOptions().enableSource(sourceName);
        NeoJuke.getInstance().getSettingsStore().saveStoreData(guildId, "player_options", gson.toJson(getPlayerOptions()));
    }
}
