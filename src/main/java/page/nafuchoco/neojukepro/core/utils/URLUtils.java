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

package page.nafuchoco.neojukepro.core.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class URLUtils {

    public static URLStructure parseUrl(String url) throws MalformedURLException {
        return parseUrl(new URL(url));
    }

    public static URLStructure parseUrl(URL url) {
        String protocol;
        String userInfo;
        String username = null;
        String password = null;
        String authority;
        String host;
        int port;
        String path;
        Map<String, String> query = new LinkedHashMap<>();
        String fileName;
        String ref;

        protocol = url.getProtocol();
        userInfo = url.getUserInfo();
        if (userInfo != null) {
            String[] info = userInfo.split(":");
            if (info.length > 1)
                password = info[1];
            username = info[0];
        }
        authority = url.getAuthority();
        host = url.getHost();
        port = url.getPort();
        path = url.getPath();
        if (url.getQuery() != null) {
            String[] queries = url.getQuery().split("&");
            for (String q : queries) {
                String[] v = q.split("=");
                query.put(v[0], v[1]);
            }
        }
        fileName = url.getFile();
        ref = url.getRef();

        return new URLStructure(protocol, userInfo, username, password, authority, host, port, path, query, fileName, ref);
    }

    public static URL buildURL(URLStructure structure) {
        var urlBuilder = new StringBuilder();
        urlBuilder.append(structure.getProtocol());
        urlBuilder.append("://");
        urlBuilder.append(structure.getAuthority());
        urlBuilder.append(structure.getPath());
        if (!structure.getQuery().isEmpty()) {
            urlBuilder.append("?");
            urlBuilder.append(String.join("&",
                    structure.getQuery().entrySet().stream()
                            .map(query -> query.getKey() + "=" + query.getValue()).collect(Collectors.toSet())));
        }
        if (structure.getRef() != null) {
            urlBuilder.append("#");
            urlBuilder.append(structure.getRef());
        }

        try {
            return new URL(urlBuilder.toString());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @AllArgsConstructor
    @ToString
    public static class URLStructure {
        @Getter
        final String protocol;
        @Getter
        final String userInfo;
        @Getter
        final String username;
        @Getter
        final String password;
        @Getter
        final String authority;
        @Getter
        final String host;
        @Getter
        final int port;
        @Getter
        final String path;
        @Getter
        final Map<String, String> query;
        @Getter
        final String fileName;
        @Getter
        final String ref;
    }
}
