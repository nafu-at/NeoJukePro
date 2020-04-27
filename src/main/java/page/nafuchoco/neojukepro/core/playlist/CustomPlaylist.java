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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CustomPlaylist {
    @JsonProperty("listname")
    private final String listname;
    @JsonProperty("items")
    private final List<PlaylistItem> items;

    public CustomPlaylist(String listname, List<PlaylistItem> items) {
        this.listname = listname;
        this.items = items;
    }

    public String getListname() {
        return listname;
    }

    public List<PlaylistItem> getItems() {
        return items;
    }
}
