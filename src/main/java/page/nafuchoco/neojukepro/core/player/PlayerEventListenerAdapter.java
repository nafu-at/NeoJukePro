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

/*
 * This source code is a modification of part of the source code licensed by Frederik Ar. Mikkelsen & NoobLance.
 * The original source code license is as follows.
 *
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package page.nafuchoco.neojukepro.core.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.*;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public interface PlayerEventListenerAdapter extends AudioEventListener {

    /**
     * @param player Audio player
     */
    void onPlayerPause(AudioPlayer player);

    /**
     * @param player Audio player
     */
    void onPlayerResume(AudioPlayer player);

    /**
     * @param player Audio player
     * @param track  Audio track that started
     */
    void onTrackStart(AudioPlayer player, AudioTrack track);

    /**
     * @param player    Audio player
     * @param track     Audio track that ended
     * @param endReason The reason why the track stopped playing
     */
    void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason);

    /**
     * @param player    Audio player
     * @param track     Audio track where the exception occurred
     * @param exception The exception that occurred
     */
    void onTrackException(AudioPlayer player, AudioTrack track, Exception exception);

    /**
     * @param player      Audio player
     * @param track       Audio track where the exception occurred
     * @param thresholdMs The wait threshold that was exceeded for this event to trigger
     */
    void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs);

    /**
     * This code may cause problems in future specification changes.
     */
    @Override
    default void onEvent(AudioEvent event) {
        if (event instanceof PlayerPauseEvent) {
            onPlayerPause(event.player);
        } else if (event instanceof PlayerResumeEvent) {
            onPlayerResume(event.player);
        } else if (event instanceof TrackStartEvent) {
            onTrackStart(event.player,
                    ((com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent) event).track);
        } else if (event instanceof TrackEndEvent) {
            onTrackEnd(event.player,
                    ((com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent) event).track, ((com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent) event).endReason);
        } else if (event instanceof TrackExceptionEvent) {
            onTrackException(event.player,
                    ((com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent) event).track, ((com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent) event).exception);
        } else if (event instanceof TrackStuckEvent) {
            onTrackStuck(event.player,
                    ((TrackStuckEvent) event).track, ((TrackStuckEvent) event).thresholdMs);
        }
    }
}
