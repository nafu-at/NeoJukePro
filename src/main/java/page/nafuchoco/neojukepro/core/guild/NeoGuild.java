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

package page.nafuchoco.neojukepro.core.guild;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lavalink.client.io.Link;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.database.GuildUsersPermTable;
import page.nafuchoco.neojukepro.core.guild.user.NeoGuildMemberRegistry;
import page.nafuchoco.neojukepro.core.player.NeoGuildPlayer;

import java.util.List;

/**
 * @since v2.0
 */
@Getter
@EqualsAndHashCode(exclude = {"lastJoinedChannel", "audioPlayer"})
public class NeoGuild {
    private final NeoJukePro neoJukePro;
    private final long discordGuildId;
    private final NeoGuildSettings settings;
    private final NeoGuildMemberRegistry guildMemberRegistry;
    private final NeoGuildTempRegistry guildTempRegistry;
    private final AudioPlayerManager audioPlayerManager;

    // TODO: 2020/12/08 ギルド固有のモジュールやカスタムコマンドなんかの実装の検討

    private TextChannel lastJoinedChannel;
    protected NeoGuildPlayer audioPlayer;

    public NeoGuild(NeoJukePro neoJukePro, long discordGuildId, NeoGuildSettings settings, GuildUsersPermTable permTable) {
        this.neoJukePro = neoJukePro;
        this.discordGuildId = discordGuildId;
        this.settings = settings;
        this.guildMemberRegistry = new NeoGuildMemberRegistry(neoJukePro, permTable, this);
        guildTempRegistry = new NeoGuildTempRegistry();
        audioPlayerManager = new DefaultAudioPlayerManager();

        audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
        audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new GetyarnAudioSourceManager());

        neoJukePro.getCustomSourceRegistry().getSources().forEach(audioPlayerManager::registerSourceManager);

        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public Guild getJDAGuild() {
        return getNeoJukePro().getShardManager().getGuildById(discordGuildId);
    }

    public void setLastJoinedChannel(TextChannel lastJoinedChannel) {
        this.lastJoinedChannel = lastJoinedChannel;
    }

    public void deleteMessage(TextChannel channel, List<Member> members, int maxDelete, boolean checkPrefix) {
        if (maxDelete > 50 || maxDelete < 1)
            maxDelete = 50;

        channel.getHistory().retrievePast(maxDelete).queue(messages -> {
            for (Message message : messages) {
                Member member = getGuildMemberRegistry().getNeoGuildMember(message.getAuthor().getIdLong()).getJDAMember();

                if (member == null)
                    continue;

                if (checkPrefix) {
                    if (members.contains(member) || message.getContentRaw().startsWith(settings.getCommandPrefix()))
                        message.delete().submit();
                } else {
                    if (members.contains(member))
                        message.delete().submit();
                }
            }
        });
    }

    public NeoGuildPlayer getAudioPlayer() {
        if (audioPlayer == null) {
            audioPlayer = new NeoGuildPlayer(neoJukePro, this,
                    getNeoJukePro().getLavaLink() != null ? getNeoJukePro().getLavaLink().getLink(getJDAGuild()) : null);
            getJDAGuild().getAudioManager().setSendingHandler(audioPlayer.getSendHandler());
            audioPlayer.setVolume(getSettings().getPlayerOptions().getVolumeLevel());
        }
        return audioPlayer;
    }

    public void destroyAudioPlayer() {
        if (audioPlayer != null) {
            if (audioPlayer.getLink() == null) {
                audioPlayer.stop();
                audioPlayer.leaveChannel();
            } else if (audioPlayer.getLink().getState() == Link.State.DESTROYED) {
                sendMessageToLatest(MessageManager.getMessage(settings.getLang(), "player.destroyed"));
            } else {
                audioPlayer.destroy();
            }
            audioPlayer = null;
        }
    }

    public void sendMessageToLatest(String message) {
        lastJoinedChannel.sendMessage(message).queue();
    }

    public void sendMessageToLatest(Message message) {
        lastJoinedChannel.sendMessage(message).queue();
    }

    public void sendMessageToLatest(MessageEmbed embed) {
        lastJoinedChannel.sendMessageEmbeds(embed).queue();
    }
}
