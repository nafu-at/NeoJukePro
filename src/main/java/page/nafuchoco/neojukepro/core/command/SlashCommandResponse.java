/*
 * Copyright 2022 NAFU_at.
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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

public class SlashCommandResponse {
    private final InteractionHook hook;

    private boolean executorResponded = false;

    public SlashCommandResponse(InteractionHook hook) {
        this.hook = hook;
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendMessage(@NotNull String content) {
        executorResponded = true;
        return hook.sendMessage(content);
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendMessage(@NotNull Message message) {
        executorResponded = true;
        return hook.sendMessage(message);
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendMessageFormat(@NotNull String format, @NotNull Object... args) {
        executorResponded = true;
        return hook.sendMessageFormat(format, args);
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendMessageEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        executorResponded = true;
        return hook.sendMessageEmbeds(embeds);
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendMessageEmbeds(@NotNull MessageEmbed embed, @NotNull MessageEmbed... embeds) {
        executorResponded = true;
        return hook.sendMessageEmbeds(embed, embeds);
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendFile(@NotNull InputStream data, @NotNull String name, @NotNull AttachmentOption... options) {
        executorResponded = true;
        return hook.sendFile(data, name, options);
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendFile(@NotNull File file, @NotNull AttachmentOption... options) {
        executorResponded = true;
        return hook.sendFile(file, options);
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendFile(@NotNull File file, @NotNull String name, @NotNull AttachmentOption... options) {
        executorResponded = true;
        return hook.sendFile(file, name, options);
    }

    @CheckReturnValue
    @Nonnull
    public WebhookMessageAction<Message> sendFile(@NotNull byte[] data, @NotNull String name, @NotNull AttachmentOption... options) {
        executorResponded = true;
        return hook.sendFile(data, name, options);
    }

    public boolean isExecutorResponded() {
        return executorResponded;
    }
}
