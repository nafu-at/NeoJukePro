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

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NeoGuildPlayerOptions {
    private int volumeLevel;
    private RepeatMode repeatMode;
    private boolean shuffle;
    private List<String> disabledSources;

    protected void enableSource(String sourceName) {
        disabledSources.remove(sourceName);
    }

    protected void disableSource(String sourceName) {
        if (!disabledSources.contains(sourceName))
            disabledSources.add(sourceName);
    }

    public List<String> getDisabledSources() {
        if (disabledSources == null)
            disabledSources = new ArrayList<>();
        return disabledSources;
    }

    public enum RepeatMode {
        NONE, SINGLE, ALL
    }
}
