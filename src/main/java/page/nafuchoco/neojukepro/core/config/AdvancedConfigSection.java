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

package page.nafuchoco.neojukepro.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AdvancedConfigSection {
    @JsonProperty("googleAPIToken")
    private String googleAPIToken;
    @JsonProperty("updateInfoUrl")
    private String updateInfoUrl;
    @JsonProperty("useNodeServer")
    private boolean useNodeServer;
    @JsonProperty("nodesInfo")
    private List<LavalinkConfigSection> nodesInfo;
    @JsonProperty("sentryDsn")
    private String sentryDsn;

    public String getGoogleAPIToken() {
        return googleAPIToken;
    }

    public String getUpdateInfoUrl() {
        return updateInfoUrl;
    }

    public boolean isUseNodeServer() {
        return useNodeServer;
    }

    public List<LavalinkConfigSection> getNodesInfo() {
        return nodesInfo;
    }

    public String getSentryDsn() {
        return sentryDsn;
    }
}
