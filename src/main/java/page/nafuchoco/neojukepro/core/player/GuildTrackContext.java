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

package page.nafuchoco.neojukepro.core.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class GuildTrackContext {
    private final Guild guild;
    private final Member invoker;
    private final int startPosition;
    private final AudioTrack track;

    public GuildTrackContext(Guild guild, Member invoker, int startPosition, AudioTrack track) {
        this.guild = guild;
        this.invoker = invoker;
        this.startPosition = startPosition;
        this.track = track;
    }

    public Guild getGuild() {
        return guild;
    }

    public Member getInvoker() {
        return invoker;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public GuildTrackContext makeClone() {
        return new GuildTrackContext(guild, invoker, startPosition, track.makeClone());
    }

    @Override
    public String toString() {
        return track.getInfo().title;
    }
}
