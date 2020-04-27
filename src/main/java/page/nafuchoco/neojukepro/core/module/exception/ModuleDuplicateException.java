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

package page.nafuchoco.neojukepro.core.module.exception;

/**
 * It is thrown if a module with the same name has already been registered.
 */
public class ModuleDuplicateException extends Exception {

    public ModuleDuplicateException() {
    }

    public ModuleDuplicateException(String message) {
        super(message);
    }

    public ModuleDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleDuplicateException(Throwable cause) {
        super(cause);
    }

    public ModuleDuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
