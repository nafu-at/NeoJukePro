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

package page.nafuchoco.neojukepro.core.http.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Slf4j
public class DiscordAPIClient {
    private static final String APP_URL = "https://discord.com/api/v6/oauth2/applications/@me";

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    public DiscordAppInfo getBotApplicationInfo(@NonNull String dicordToken) throws IOException {
        HttpUrl.Builder builder = HttpUrl.parse(APP_URL).newBuilder();
        Request request = new Request.Builder().url(builder.build())
                .header("Authorization", "Bot " + dicordToken).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body().string(), DiscordAppInfo.class);
            }
            return null;
        }
    }
}
