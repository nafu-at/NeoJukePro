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
    private final BlockingQueue<LoadedTrackContext> queue = new LinkedBlockingQueue<>();
    private final NeoGuildPlayer audioPlayer;

    public GuildTrackProvider(NeoGuildPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    /**
     * 登録されているすべてのキューを返します。
     *
     * @return 登録されているすべてのキューを格納したList
     */
    public List<LoadedTrackContext> getQueues() {
        return new ArrayList<>(queue);
    }

    public LoadedTrackContext provideTrack() {
        return queue.poll();
    }

    /**
     * 指定番号が1以下の場合は先頭に追加。
     */
    public void queue(LoadedTrackContext trackContext) {
        List<LoadedTrackContext> tracks = getQueues();

        if (trackContext.getInterruptNumber() <= 1 || trackContext.getInterruptNumber() > tracks.size()) {
            queue.offer(trackContext);
            if (audioPlayer.getNeoGuild().getSettings().getPlayerOptions().isShuffle())
                shuffle();
        } else {
            List<LoadedTrackContext> before;
            List<LoadedTrackContext> after;
            if (trackContext.getInterruptNumber() == 1) {
                before = new ArrayList<>(); // empty list
                after = tracks;
            } else {
                before = tracks.subList(0, trackContext.getInterruptNumber() - 1);
                after = tracks.subList(trackContext.getInterruptNumber() - 1, tracks.size());
            }

            queue.clear();
            queue.addAll(before);
            queue.add(trackContext);
            queue.addAll(after);
        }
    }

    public void queue(List<LoadedTrackContext> contextList) {
        List<LoadedTrackContext> tracks = getQueues();
        int interruptNumber = contextList.get(0).getInterruptNumber();

        if (interruptNumber <= 1 || interruptNumber > tracks.size()) {
            queue.addAll(contextList);
            if (audioPlayer.getNeoGuild().getSettings().getPlayerOptions().isShuffle())
                shuffle();
        } else {
            List<LoadedTrackContext> before;
            List<LoadedTrackContext> after;
            if (interruptNumber == 1) {
                before = new ArrayList<>(); // empty list
                after = tracks;
            } else {
                before = tracks.subList(0, interruptNumber - 1);
                after = tracks.subList(interruptNumber - 1, tracks.size());
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

        List<LoadedTrackContext> queues = getQueues();

        Collections.shuffle(queues);
        queue.clear();
        queues.forEach(queue::add);
    }

    public void clearTracks() {
        queue.clear();
    }

    public List<LoadedTrackContext> skip(int below) {
        return skip(below, queue.size());
    }

    public List<LoadedTrackContext> skip(int from, int to) {
        List<LoadedTrackContext> tracks = getQueues();
        List<LoadedTrackContext> toDelete = new ArrayList<>();
        for (int index = from - 1; index < to; index++)
            toDelete.add(tracks.get(index));
        toDelete.forEach(queue::remove);
        return toDelete;
    }

    public List<LoadedTrackContext> skip(Member invoker) {
        List<LoadedTrackContext> tracks = getQueues();
        List<LoadedTrackContext> toDelete = new ArrayList<>();
        tracks.forEach(track -> {
            if (track.getInvoker().equals(invoker))
                toDelete.add(track);
        });
        toDelete.forEach(queue::remove);
        return toDelete;
    }
}
