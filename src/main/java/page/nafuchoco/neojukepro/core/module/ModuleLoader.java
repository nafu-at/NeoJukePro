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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.module.exception.InvalidDescriptionException;
import page.nafuchoco.neojukepro.core.module.exception.InvalidModuleException;
import page.nafuchoco.neojukepro.core.module.exception.UnknownDependencyException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Slf4j
public class ModuleLoader {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final ModuleRegistry moduleRegistry;
    private final File dir;

    public ModuleLoader(ModuleRegistry moduleRegistry, String moduleDir) {
        this.moduleRegistry = moduleRegistry;
        this.dir = new File(moduleDir);
    }

    /**
     * Get the list of JAR files stored in the module folder.
     * If the folder doesn't exist or is not from, return an empty List.
     *
     * @return The list of JAR files stored in the module folder.
     */
    public List<File> searchModules() {
        if (dir.exists())
            return Arrays.stream(dir.listFiles()).filter(file -> file.getName().endsWith(".jar")).collect(Collectors.toList());
        else
            dir.mkdirs();
        return new ArrayList<>();
    }

    /**
     * Load the specified file as a module.
     *
     * @param file Module file to load.
     * @return Loaded modules
     * @throws InvalidModuleException     Thrown if the format of the module is not correct.
     * @throws UnknownDependencyException Thrown if the specified dependency cannot be resolved.
     */
    public NeoModule loadModule(File file) throws InvalidModuleException {
        if (!file.exists())
            throw new InvalidModuleException("The specified file does not exist.");

        ModuleDescription description;
        try {
            description = loadModuleDescription(file);
        } catch (InvalidDescriptionException e) {
            throw new InvalidModuleException(e);
        }

        int currentVersion = parseVersion(Main.class.getPackage().getImplementationVersion());
        int requiredVersion = parseVersion(description.getRequiredVersion());
        if (requiredVersion > currentVersion)
            throw new InvalidModuleException("The current version of NeoJukePro in use is unavailable " +
                    "because it falls below the version required by the module.");

        if (!CollectionUtils.isEmpty(description.getDependency())) {
            for (String dep : description.getDependency()) {
                if (moduleRegistry.getModule(dep) == null)
                    throw new UnknownDependencyException("The dependency could not be resolved.");
            }
        }

        ModuleClassLoader classLoader;
        try {
            classLoader = new ModuleClassLoader(file, description, getClass().getClassLoader());
        } catch (MalformedURLException e) {
            throw new InvalidModuleException(e);
        }

        File parent = file.getParentFile();
        File dataFolder = new File(parent, description.getName());

        if (!dataFolder.exists())
            dataFolder.mkdirs();

        return classLoader.getModule();
    }

    /**
     * Reads the detailed information registered in the module.
     *
     * @param file Module file to be load.
     * @return Registered details
     * @throws InvalidModuleException      Thrown if the format of the module is not correct.
     * @throws InvalidDescriptionException Thrown if the specified dependency cannot be resolved.
     */
    public ModuleDescription loadModuleDescription(File file) throws InvalidModuleException {
        if (!file.exists())
            throw new InvalidModuleException("The specified file does not exist.");

        InputStream inputStream = null;
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("module.yaml");

            if (entry == null)
                throw new InvalidDescriptionException("\"module.yaml\" could not be found.");

            inputStream = jar.getInputStream(entry);
            return mapper.readValue(inputStream, ModuleDescription.class);
        } catch (IOException e) {
            throw new InvalidModuleException("The module could not be loaded.", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private int parseVersion(String version) {
        String[] sa = version.split("-");
        try {
            return Integer.parseInt(sa[0].replaceAll(".", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
