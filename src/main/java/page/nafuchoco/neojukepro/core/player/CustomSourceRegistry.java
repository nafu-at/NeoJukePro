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

package page.nafuchoco.neojukepro.core.player;

import page.nafuchoco.neobot.api.module.NeoModule;

import java.util.*;
import java.util.stream.Collectors;

public class CustomSourceRegistry {
    private final Map<NeoModule, List<CustomAudioSourceManager>> audioSourceManagers = new LinkedHashMap<>();

    public void registerCustomAudioSource(CustomAudioSourceManager sourceManager, NeoModule module) {
        List<CustomAudioSourceManager> reg = audioSourceManagers.computeIfAbsent(module, key -> new ArrayList<>());
        reg.add(sourceManager);
    }

    public List<CustomAudioSourceManager> getSources() {
        return audioSourceManagers.values().stream().flatMap(Collection::stream).distinct().collect(Collectors.toList());
    }
}
