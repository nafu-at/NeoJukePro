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

package page.nafuchoco.neojukepro.core.command;

import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandCache {
    private static final Map<Guild, Map<String, Object>> cacheRegistry = new HashMap<>();

    private CommandCache() {
        throw new UnsupportedOperationException();
    }

    public static void registerCache(Guild guild, String key, Object value) {
        Map<String, Object> guildCache = cacheRegistry.computeIfAbsent(guild, k -> new HashMap<>());
        guildCache.put(key, value);
    }

    public static Object getCache(Guild guild, String key) {
        return cacheRegistry.computeIfAbsent(guild, k -> new HashMap<>()).get(key);
    }

    public static Set<Map.Entry<String, Object>> getCaches(Guild guild) {
        return cacheRegistry.computeIfAbsent(guild, k -> new HashMap<>()).entrySet();
    }

    public static Object deleteCache(Guild guild, String key) {
        Map<String, Object> map = cacheRegistry.get(guild);
        if (map != null)
            return map.remove(key);
        return null;
    }

    public static void deleteAllCache(Guild guild) {
        cacheRegistry.remove(guild);
    }
}
