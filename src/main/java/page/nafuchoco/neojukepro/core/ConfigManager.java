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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;

@Slf4j
public class ConfigManager {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final File configFile = new File("NeoJukeConfig.yaml");
    private NeoJukeConfig config;

    public boolean existsConfig(boolean generateFile) {
        boolean result = configFile.exists();
        if (!result && generateFile) {
            try (InputStream original = ClassLoader.getSystemResourceAsStream("NeoJukeConfig.yaml")) {
                Files.copy(original, configFile.toPath());
                log.info(MessageManager.getMessage("system.config.generate"));
                log.debug(MessageManager.getMessage("system.config.generate.debug"), configFile.getPath());
            } catch (IOException e) {
                log.error(MessageManager.getMessage("system.config.generate.failed"), e);
            }
        }
        return result;
    }

    public void reloadConfig() {
        try (var configInput = new FileInputStream(configFile)) {
            config = MAPPER.readValue(configInput, NeoJukeConfig.class);
            log.info(MessageManager.getMessage("system.config.load.success"));
        } catch (FileNotFoundException e) {
            log.error(MessageManager.getMessage("system.config.load.notfound"), e);
        } catch (IOException e) {
            log.error(MessageManager.getMessage("system.config.load.error"), e);
        }
    }

    public NeoJukeConfig getConfig() {
        return config;
    }
}
