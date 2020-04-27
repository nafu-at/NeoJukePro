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

import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class ResourcesManager {
    private static final Map<Guild, GuildResources> resourcesMap = new HashMap<>();

    public static void registerResources(Guild guild, GuildResources resources) {
        resourcesMap.put(guild, resources);
    }

    public static GuildResources getResources(Guild guild) {
        return resourcesMap.computeIfAbsent(guild, k -> new GuildResources());
    }

    public static void removeResources(Guild guild) {
        resourcesMap.remove(guild);
    }
}
