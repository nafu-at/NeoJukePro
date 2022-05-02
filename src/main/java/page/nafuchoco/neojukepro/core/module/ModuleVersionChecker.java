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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleVersionChecker {
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
                    "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)" +
                    "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))" +
                    "?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    /**
     * Compares the two modules and returns true if arg2 is new.
     *
     * @param arg1
     * @param arg2
     * @return Returns true if arg2 is new.
     */
    public static boolean isNew(NeoModule arg1, NeoModule arg2) {
        return isNew(arg1.getDescription().getVersion(), arg2.getDescription().getVersion());
    }

    /**
     * Compares the two input values and returns true if arg2 is new.
     *
     * @param arg1
     * @param arg2
     * @return Returns true if arg2 is new.
     */
    public static boolean isNew(String arg1, String arg2) {
        if (arg1 == null || arg2 == null)
            return true;

        Matcher arg1Matcher = VERSION_PATTERN.matcher(arg1);
        Matcher arg2Matcher = VERSION_PATTERN.matcher(arg2);

        if (!(arg1Matcher.matches() && arg2Matcher.matches()))
            throw new IllegalArgumentException("The version format must conform to Semantic Versioning 2.0.0.");

        return Integer.parseInt(arg1Matcher.group(3)) < Integer.parseInt(arg2Matcher.group(3))
                || Integer.parseInt(arg1Matcher.group(2)) < Integer.parseInt(arg2Matcher.group(2))
                || Integer.parseInt(arg1Matcher.group(1)) < Integer.parseInt(arg2Matcher.group(1));
    }
}
