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

package page.nafuchoco.neojukepro.core.playlist;

import java.util.List;

public class CustomPlaylist {
    private final String uuid;
    private final long guildId;
    private final String listname;
    private final List<PlaylistItem> items;

    public CustomPlaylist(String uuid, long guildId, String listname, List<PlaylistItem> items) {
        this.uuid = uuid;
        this.guildId = guildId;
        this.listname = listname;
        this.items = items;
    }

    public String getUuid() {
        return uuid;
    }

    public long getGuildId() {
        return guildId;
    }

    public String getName() {
        return listname;
    }

    public List<PlaylistItem> getItems() {
        return items;
    }
}
