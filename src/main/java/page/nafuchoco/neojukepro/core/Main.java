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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.Console;

@Slf4j
public class Main {
    private static boolean debugMode;
    private static NeoJukeLauncher launcher;

    public static void main(String[] args) {
        log.info("\n _   _                _       _        \n" +
                "| \\ | | ___  ___     | |_   _| | _____ \n" +
                "|  \\| |/ _ \\/ _ \\ _  | | | | | |/ / _ \\\n" +
                "| |\\  |  __/ (_) | |_| | |_| |   <  __/\n" +
                "|_| \\_|\\___|\\___/ \\___/ \\__,_|_|\\_\\___|\n" +
                "                                       \n");
        log.info("Welcome to NeoJukePro. Starting v" + Main.class.getPackage().getImplementationVersion() + ".");

        for (String prop : args) {
            switch (prop.toLowerCase()) {
                case "debug":
                    debugMode = true;
                    BootOptions.setDebug(true);
                    break;

                case "nodb":
                    BootOptions.setNoDb(true);
                    break;

                case "nologin":
                    BootOptions.setNoLogin(true);
                    break;

                default:
                    if (prop.startsWith("lang=")) {
                        String[] s = prop.split("=");
                        MessageManager.setDefaultLocale(s[1]);
                    }
                    break;
            }
        }

        if (debugMode) {
            Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            Logger jdaLogger = (Logger) LoggerFactory.getLogger("net.dv8tion");
            Logger cpLogger = (Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
            root.setLevel(Level.DEBUG);
            jdaLogger.setLevel(Level.DEBUG);
            cpLogger.setLevel(Level.DEBUG);
        }

        launcher = new Launcher();
        launcher.launch();

        new Thread(() -> {
            Console console = System.console();
            while (true) {
                switch (console.readLine()) {
                    case "exit":
                    case "stop":
                        Runtime.getRuntime().exit(0);
                        break;

                    case "threadList":
                        for (Thread thread : Thread.getAllStackTraces().keySet()) {
                            log.debug("Found active thread: {} ({})", thread, thread.getClass().getClassLoader());
                        }
                        break;
                }
            }
        }).start();
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static NeoJukeLauncher getLauncher() {
        return launcher;
    }
}
