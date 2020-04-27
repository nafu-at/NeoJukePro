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

package page.nafuchoco.neojukepro.core.discord.guild;

public class GuildSettings {
    private String prefix;
    private int volume;
    private REPEATTYPE repeat;
    private boolean shuffle;
    private boolean autoLeave;
    private boolean announce;

    public String getPrefix() {
        return prefix;
    }

    public int getVolume() {
        return volume;
    }

    public REPEATTYPE getRepeat() {
        return repeat;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public boolean isAutoLeave() {
        return autoLeave;
    }

    public boolean isAnnounce() {
        return announce;
    }

    public enum REPEATTYPE {
        NONE, SINGLE, ALL
    }
}
