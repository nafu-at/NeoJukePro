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

import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manage temporary data about the guild.
 *
 * @since v2.0
 */
public class NeoGuildTempRegistry {
    private final Map<String, Object> tempRegistry = new HashMap<>();

    public void registerTemp(String key, Object value) {
        tempRegistry.put(key, value);
    }

    public Object getTemp(String key) {
        return tempRegistry.get(key);
    }

    public Set<Map.Entry<String, Object>> getTemps(Guild guild) {
        return tempRegistry.entrySet();
    }

    public Object deleteTemp(String key) {
        return tempRegistry.remove(key);
    }
}
