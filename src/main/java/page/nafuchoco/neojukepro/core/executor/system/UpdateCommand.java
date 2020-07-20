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

package page.nafuchoco.neojukepro.core.executor.system;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;

@Slf4j
public class UpdateCommand extends CommandExecutor {

    public UpdateCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        String file;
        if (context.getArgs().length >= 1) {
            file = context.getArgs()[0];
        } else {
            if (context.getMessage().getAttachments().isEmpty())
                return;
            file = (context.getMessage().getAttachments().get(0)).getUrl();
        }

        try {
            context.getChannel().sendMessage(":warning: Execute the update operation. Quit the program when you're done.").queue();
            update(file);
        } catch (IOException e) {
            context.getChannel().sendMessage("Failed to rewrite the file.").queue();
            log.error("Failed to rewrite the file.", e);
        } catch (URISyntaxException e) {
            context.getChannel().sendMessage("Failed to get the location of the JAR file.").queue();
        }

        Runtime.getRuntime().exit(0);
        return;
    }

    private void update(String file) throws IOException, URISyntaxException {
        File temp = new File("update.jar");
        FileUtils.copyURLToFile(new URL(file), temp);

        String temp_sha3 = DigestUtils.sha3_256Hex(new FileInputStream(temp));
        String original_sha3 =
                DigestUtils.sha3_256Hex(new FileInputStream(getApplicationPath(Main.class).toFile()));
        if (original_sha3.equals(temp_sha3)) {
            FileUtils.forceDelete(temp);
            return;
        }

        FileUtils.forceDelete(getApplicationPath(Main.class).toFile());
        FileUtils.moveFile(temp, getApplicationPath(Main.class).toFile());
        return;
    }

    private Path getApplicationPath(Class<?> cls) throws URISyntaxException {
        ProtectionDomain domain = cls.getProtectionDomain();
        CodeSource source = domain.getCodeSource();
        URL location = source.getLocation();
        URI uri = location.toURI();
        Path path = Paths.get(uri);
        return path;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public int getRequiredPerm() {
        return 255;
    }
}
