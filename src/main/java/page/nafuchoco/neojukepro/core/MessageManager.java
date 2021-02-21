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

package page.nafuchoco.neojukepro.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MessageManager {
    private static final Map<String, ResourceBundle> lang = new HashMap<>();
    private static String defaultLocale = "en_US";

    private MessageManager() {
        throw new IllegalStateException();
    }

    /**
     * @param locale Language of the message to retrieve. This parameter can be Null and will return the default language.
     * @param index  Item name of the message to be retrieved
     * @return Retrieved messages
     */
    public static String getMessage(@Nullable String locale, @NotNull String index) {
        if (locale == null)
            locale = defaultLocale;
        ResourceBundle bundle = lang.computeIfAbsent(locale, key -> {
            String[] loc = key.split("_");
            return ResourceBundle.getBundle("languages/messages", new Locale(loc[0], loc[1]));
        });
        return bundle.getString(index);
    }

    public static String getMessage(@NotNull String index) {
        return getMessage(null, index);
    }

    public static void setDefaultLocale(@NotNull String locale) {
        defaultLocale = locale;
    }
}
