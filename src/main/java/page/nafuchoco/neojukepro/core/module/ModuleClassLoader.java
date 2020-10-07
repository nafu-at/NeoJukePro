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

import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.module.exception.InvalidModuleException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ModuleClassLoader extends URLClassLoader {
    private final NeoJukeLauncher launcher;
    private final ModuleDescription description;
    private final NeoModule module;

    protected ModuleClassLoader(NeoJukeLauncher launcher, File file, ModuleDescription description, ClassLoader parent)
            throws MalformedURLException, InvalidModuleException {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.launcher = launcher;
        this.description = description;

        Class<?> jarClass;
        try {
            jarClass = Class.forName(description.getMain(), true, this);
        } catch (ClassNotFoundException e) {
            throw new InvalidModuleException("The main class could not be found.");
        }
        Class<? extends NeoModule> moduleClass;
        try {
            moduleClass = jarClass.asSubclass(NeoModule.class);
        } catch (ClassCastException e) {
            throw new InvalidModuleException("The main class must be extended from \"NeoModule\".");
        }

        try {
            module = moduleClass.getDeclaredConstructor().newInstance();
        } catch (IllegalArgumentException | InstantiationException | NoSuchMethodException e) {
            throw new InvalidModuleException("Module format is not correct.");
        } catch (IllegalAccessException e) {
            throw new InvalidModuleException("The constructor is not public.");
        } catch (InvocationTargetException e) {
            throw new InvalidModuleException("An error occurred during initialization of the constructor.", e);
        }
    }

    public NeoModule getModule() {
        return module;
    }

    public void initialize(NeoModule module) {
        module.init(launcher, description);
    }
}
