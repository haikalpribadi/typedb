/*
 * Copyright (C) 2021 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.core.common.producer;

import grakn.core.common.iterator.ResourceIterator;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static grakn.core.common.concurrent.ExecutorService.forkJoinPool;

@ThreadSafe
public class BaseProducer<T> implements Producer<T> {

    private final ResourceIterator<T> iterator;
    private final AtomicBoolean isDone;
    private CompletableFuture<Void> future;

    BaseProducer(ResourceIterator<T> iterator) {
        this.iterator = iterator;
        this.isDone = new AtomicBoolean(false);
        this.future = CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized void produce(Queue<T> queue, int request) {
        if (isDone.get()) return;
        future = future.thenRunAsync(() -> {
            try {
                int i = 0;
                for (; i < request && iterator.hasNext() && !isDone.get(); i++) queue.put(iterator.next());
                if (i < request && !isDone.get()) done(queue);
            } catch (Throwable e) {
                queue.done(e);
            }
        }, forkJoinPool());
    }

    private void done(Queue<T> queue) {
        if (isDone.compareAndSet(false, true)) {
            queue.done();
        }
    }

    private void done(Queue<T> queue, Throwable e) {
        if (isDone.compareAndSet(false, true)) {
            queue.done(e);
        }
    }

    @Override
    public synchronized void recycle() {
        iterator.recycle();
    }
}
