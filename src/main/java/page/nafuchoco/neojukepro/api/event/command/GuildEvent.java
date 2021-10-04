/*
 * Copyright 2021 NAFU_at.
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

package page.nafuchoco.neojukepro.api.event.command;

import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neojukepro.api.event.Event;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;
import page.nafuchoco.neojukepro.core.module.RegisteredListener;

import java.util.ArrayList;
import java.util.List;

public class GuildEvent extends Event {
    private static final List<RegisteredListener> handlerList = new ArrayList<>();

    private final NeoGuild guild;

    public GuildEvent(@NotNull NeoGuild guild) {
        this.guild = guild;
    }

    @Override
    public List<RegisteredListener> getHandlerList() {
        return null;
    }
}
