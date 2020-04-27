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
import page.nafuchoco.neojukepro.core.config.NeoJukeConfig;

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
                result = true;
                log.info("The configuration file was not found, so a new file was created.");
                log.debug("Configuration file location: {}", configFile.getPath());
            } catch (IOException e) {
                result = false;
                log.error("The correct configuration file could not be retrieved from the executable.\n" +
                        "If you have a series of problems, please contact the developer.", e);
            }
        }
        return result;
    }

    public void reloadConfig() {
        try (FileInputStream configInput = new FileInputStream(configFile)) {
            config = MAPPER.readValue(configInput, NeoJukeConfig.class);
            log.info("The configuration file has been successfully loaded.");
        } catch (FileNotFoundException e) {
            log.error("The configuration file could not be found. Do not delete the configuration file after starting the program.\n" +
                    "If you don't know what it is, please report it to the developer.", e);
        } catch (IOException e) {
            log.error("An error occurred while loading the configuration file.", e);
        }
    }

    public NeoJukeConfig getConfig() {
        return config;
    }
}
