/*
 * Copyright 2022 NAFU_at.
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

package page.nafuchoco.neojukepro.core.command;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandValueOption implements CommandOption {
    private final OptionType optionType;
    private final String optionName;
    private final String optionDescription;
    private final boolean required;
    private final boolean autoComplete;

    public CommandValueOption(OptionType optionType,
                              String optionName, String optionDescription, boolean required,
                              boolean autoComplete) {
        this.optionType = optionType;
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.required = required;
        this.autoComplete = autoComplete;
    }


    public SlashCommandData addCommandOption(SlashCommandData slashCommandData) {
        return slashCommandData.addOption(optionType, optionName, optionDescription, required, autoComplete);
    }

    @Override
    public OptionType optionType() {
        return optionType;
    }

    @Override
    public String optionName() {
        return optionName;
    }

    @Override
    public String optionDescription() {
        return optionDescription;
    }

    public boolean required() {
        return required;
    }

    public boolean autoComplete() {
        return autoComplete;
    }
}
