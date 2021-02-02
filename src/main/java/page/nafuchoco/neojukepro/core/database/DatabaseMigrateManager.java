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

package page.nafuchoco.neojukepro.core.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import page.nafuchoco.neojukepro.core.Launcher;
import page.nafuchoco.neojukepro.core.MessageManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DatabaseMigrateManager {
    private final Map<String, DatabaseTable> databaseTables = new HashMap<>();
    private final Map<Integer, MigrateConfig> migrateConfig;

    public DatabaseMigrateManager(Launcher launcher, DatabaseTable... tables) throws IOException {
        for (DatabaseTable table : tables) {
            databaseTables.put(
                    table.getTablename().replace(launcher.getConfig().getBasicConfig().getDatabase().getTablePrefix(), ""),
                    table);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        migrateConfig = mapper.readValue(ClassLoader.getSystemResourceAsStream("DatabaseMigrate.yaml"),
                new TypeReference<>() {
                });
        Arrays.sort(migrateConfig.keySet().toArray());

        log.debug("Found Migrate Config: {}", migrateConfig.keySet().toArray());
    }

    public void migrate(int begin) {
        log.debug("Migrate start from {}.", begin);
        for (int i = begin; i < migrateConfig.size(); i++) {
            log.debug("Running migrate: {}", i);
            MigrateConfig config = migrateConfig.get(i);
            MigrateConfig.MigrateAction action = config.getAction();
            String[] options = config.getOption().split("\\p{javaSpaceChar}+");
            switch (action) {
                case ADD: {
                    DatabaseTable table = databaseTables.get(config.getTable());
                    try {
                        table.createTableColumn(options[0], options[1]);
                    } catch (SQLException e) {
                        log.error(MessageManager.getMessage("system.db.migrate.error"), e);
                    }
                    break;
                }

                case REMOVE: {
                    DatabaseTable table = databaseTables.get(config.getTable());
                    try {
                        table.dropTableColumn(options[0]);
                    } catch (SQLException e) {
                        log.error(MessageManager.getMessage("system.db.migrate.error"), e);
                    }
                    break;
                }

                case SCRIPT: {
                    DatabaseTable table = databaseTables.get(config.getTable());
                    String syntax = config.getOption().replaceAll("%TABLENAME%", table.getTablename());
                    try (Connection connection = table.getConnector().getConnection();
                         PreparedStatement ps = connection.prepareStatement(
                                 syntax)) {
                        ps.execute();
                    } catch (SQLException e) {
                        log.error(MessageManager.getMessage("system.db.migrate.error"), e);
                    }
                    break;
                }
            }
        }
        NeoJukeTable neoJukeTable = (NeoJukeTable) databaseTables.get("neojuke");
        try {
            neoJukeTable.setOption("migrate", migrateConfig.keySet().toArray()[migrateConfig.size() - 1].toString());
        } catch (SQLException e) {
            log.error(MessageManager.getMessage("system.db.migrate.error"));
        }
    }
}
