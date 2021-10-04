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

package page.nafuchoco.neojukepro.core.module;

import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neojukepro.api.event.Event;
import page.nafuchoco.neojukepro.api.event.EventListener;
import page.nafuchoco.neojukepro.core.module.exception.EventException;

public class RegisteredListener {
    private final EventListener listener;
    private final NeoModule module;
    private final EventExecutor executor;

    public RegisteredListener(@NotNull EventListener listener, @NotNull NeoModule module, @NotNull EventExecutor executor) {
        this.listener = listener;
        this.module = module;
        this.executor = executor;
    }

    public EventListener getListener() {
        return listener;
    }

    public NeoModule getModule() {
        return module;
    }

    public void callEvent(@NotNull final Event event) throws EventException {
        executor.execute(listener, event);
    }
}
