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

package page.nafuchoco.neojukepro.core.module;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ModuleDescription {
    /**
     * @return Module name
     */
    @JsonProperty("name")
    private String name;
    /**
     * @return Module version
     */
    @JsonProperty("version")
    private String version;
    /**
     * @return Module Description
     */
    @JsonProperty(("description"))
    private String description;
    /**
     * @return Module author
     */
    @JsonProperty("authors")
    private List<String> authors;
    /**
     * @return Module author's website
     */
    @JsonProperty("website")
    private String website;

    /**
     * @return Module main class path
     */
    @JsonProperty("main")
    private String main;
    /**
     * @return Other modules required by this module
     */
    @JsonProperty("dependency")
    private List<String> dependency;
    /**
     * @return Modules that should be loaded before this module is loaded
     */
    @JsonProperty("loadBefore")
    private List<String> loadBefore;
    /**
     * @return The minimum version of NeoJukePro that this module requires
     */
    @JsonProperty("requiredVersion")
    private String requiredVersion;
}
