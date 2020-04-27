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

import java.util.List;

public class ModuleDescription {
    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private String version;
    @JsonProperty(("description"))
    private String description;
    @JsonProperty("authors")
    private List<String> authors;
    @JsonProperty("website")
    private String website;

    @JsonProperty("main")
    private String main;
    @JsonProperty("dependency")
    private List<String> dependency;
    @JsonProperty("loadBefore")
    private List<String> loadBefore;
    @JsonProperty("requiredVersion")
    private String requiredVersion;

    /**
     * @return Module name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Module version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return Module Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Module author
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * @return Module author's website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * @return Module main class path
     */
    public String getMain() {
        return main;
    }

    /**
     * @return Other modules required by this module
     */
    public List<String> getDependency() {
        return dependency;
    }

    /**
     * @return Modules that should be loaded before this module is loaded
     */
    public List<String> getLoadBefore() {
        return loadBefore;
    }

    /**
     * @return The minimum version of NeoJukePro that this module requires
     */
    public String getRequiredVersion() {
        return requiredVersion;
    }
}
