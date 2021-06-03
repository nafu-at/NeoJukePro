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

import java.util.regex.Pattern;

public class MessageUtil {
    private static final Pattern VAR_PATTERN = Pattern.compile("(\\{\\d+\\})");

    private MessageUtil() {
        throw new IllegalStateException();
    }

    public static String format(String message, Object... args) {
        String result = message;
        var matcher = VAR_PATTERN.matcher(result);
        while (matcher.find()) {
            String index = matcher.group().replace("{", "").replace("}", "");
            if (Integer.parseInt(index) < args.length)
                result = result.replace(matcher.group(), String.valueOf(args[Integer.parseInt(index)]));
        }
        return result;
    }

    public static String formatTime(long millis) {
        if (millis == Long.MAX_VALUE)
            return "LIVE";

        long t = millis / 1000L;
        int sec = (int) (t % 60L);
        int min = (int) ((t % 3600L) / 60L);
        int hrs = (int) (t / 3600L);

        String timestamp;

        if (hrs != 0)
            timestamp = forceTwoDigits(hrs) + ":" + forceTwoDigits(min) + ":" + forceTwoDigits(sec);
        else
            timestamp = forceTwoDigits(min) + ":" + forceTwoDigits(sec);
        return timestamp;
    }

    public static long parseTimeToMillis(String time) {
        var sec = 0;
        var min = 0;
        var hrs = 0;

        String[] split = time.split(":");
        if (split.length == 3) {
            hrs = Integer.parseInt(split[0]);
            min = Integer.parseInt(split[1]);
            sec = Integer.parseInt(split[2]);
        } else if (split.length == 2) {
            min = Integer.parseInt(split[0]);
            sec = Integer.parseInt(split[1]);
        } else {
            sec = Integer.parseInt(split[0].replace(":", ""));
        }

        return (hrs * 3600L + min * 60L + sec) * 1000L;
    }

    private static String forceTwoDigits(int i) {
        return i < 10 ? "0" + i : Integer.toString(i);
    }
}
