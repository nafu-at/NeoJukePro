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

import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GuildTrackProvider {
    private final BlockingQueue<GuildTrackContext> queue = new LinkedBlockingQueue<>();
    private final GuildAudioPlayer audioPlayer;

    public GuildTrackProvider(GuildAudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    /**
     * 登録されているすべてのキューを返します。
     *
     * @return 登録されているすべてのキューを格納したList
     */
    public List<GuildTrackContext> getQueues() {
        return new ArrayList<>(queue);
    }

    public GuildTrackContext provideTrack() {
        return queue.poll();
    }

    /**
     * 指定番号が1以下の場合は先頭に追加。
     *
     * @param context
     * @param desiredNumber 指定された数字から-1して登録すること。
     */
    public void queue(GuildTrackContext context, int desiredNumber) {
        List<GuildTrackContext> tracks = getQueues();

        if (desiredNumber <= 1 || desiredNumber > tracks.size()) {
            queue.offer(context);
            if (audioPlayer.isShuffle())
                shuffle();
        } else {
            List<GuildTrackContext> before;
            List<GuildTrackContext> after;
            if (desiredNumber == 1) {
                before = new ArrayList<>(); // empty list
                after = tracks;
            } else {
                before = tracks.subList(0, desiredNumber - 1);
                after = tracks.subList(desiredNumber - 1, tracks.size());
            }

            queue.clear();
            queue.addAll(before);
            queue.add(context);
            queue.addAll(after);
        }
    }

    public void queue(List<GuildTrackContext> contextList, int desiredNumber) {
        List<GuildTrackContext> tracks = getQueues();

        if (desiredNumber <= 1 || desiredNumber > tracks.size()) {
            queue.addAll(contextList);
            if (audioPlayer.isShuffle())
                shuffle();
        } else {
            List<GuildTrackContext> before;
            List<GuildTrackContext> after;
            if (desiredNumber == 1) {
                before = new ArrayList<>(); // empty list
                after = tracks;
            } else {
                before = tracks.subList(0, desiredNumber - 1);
                after = tracks.subList(desiredNumber - 1, tracks.size());
            }

            queue.clear();
            queue.addAll(before);
            queue.addAll(contextList);
            queue.addAll(after);
        }
    }

    public void shuffle() {
        if (queue.isEmpty())
            return;

        List<GuildTrackContext> queues = getQueues();

        Collections.shuffle(queues);
        queue.clear();
        queues.forEach(queue::add);
    }

    public void clearTracks() {
        queue.clear();
    }

    public List<GuildTrackContext> skip(int below) {
        return skip(below, queue.size());
    }

    public List<GuildTrackContext> skip(int from, int to) {
        List<GuildTrackContext> tracks = getQueues();
        List<GuildTrackContext> toDelete = new ArrayList<>();
        for (int index = from - 1; index < to; index++) {
            toDelete.add(tracks.get(index));
        }
        toDelete.forEach(queue::remove);
        return toDelete;
    }

    public List<GuildTrackContext> skip(Member invoker) {
        List<GuildTrackContext> tracks = getQueues();
        List<GuildTrackContext> toDelete = new ArrayList<>();
        tracks.forEach(track -> {
            if (track.getInvoker().equals(invoker))
                toDelete.add(track);
        });
        toDelete.forEach(queue::remove);
        return toDelete;
    }
}
