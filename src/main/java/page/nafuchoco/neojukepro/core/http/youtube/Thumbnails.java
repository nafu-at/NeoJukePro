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

package page.nafuchoco.neojukepro.core.http.youtube;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Thumbnails {
    @JsonProperty("default")
    private ThumbnailDefault thumbnailsDefault;
    @JsonProperty("medium")
    private ThumbnailDefault medium;
    @JsonProperty("high")
    private ThumbnailDefault high;
    @JsonProperty("standard")
    private ThumbnailDefault standard;
    @JsonProperty("maxres")
    private ThumbnailDefault maxres;

    public ThumbnailDefault getThumbnailsDefault() {
        return thumbnailsDefault;
    }

    public ThumbnailDefault getMedium() {
        return medium;
    }

    public ThumbnailDefault getHigh() {
        return high;
    }

    public ThumbnailDefault getStandard() {
        return standard;
    }

    public ThumbnailDefault getMaxres() {
        return maxres;
    }


    public static class ThumbnailDefault {
        @JsonProperty("url")
        private String url;
        @JsonProperty("width")
        private long width;
        @JsonProperty("height")
        private long height;

        public String getURL() {
            return url;
        }

        public long getWidth() {
            return width;
        }

        public long getHeight() {
            return height;
        }
    }
}
