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

package grakn.core.concurrent.executor;

import grakn.common.concurrent.NamedThreadFactory;
import grakn.core.common.exception.GraknException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import static grakn.core.common.exception.ErrorMessage.Internal.UNEXPECTED_INTERRUPTION;

public class ParallelThreadPoolExecutor implements Executor {

    private static final Logger LOG = LoggerFactory.getLogger(ParallelThreadPoolExecutor.class);

    private final RunnableExecutor[] executors;

    public ParallelThreadPoolExecutor(int executors, NamedThreadFactory threadFactory) {
        this.executors = new RunnableExecutor[executors];
        for (int i = 0; i < executors; i++) {
            this.executors[i] = new RunnableExecutor(threadFactory);
        }
    }

    private RunnableExecutor next() {
        int next = 0, smallest = Integer.MAX_VALUE;
        for (int i = 0; i < executors.length; i++) {
            if (executors[i].queue.size() < smallest) {
                smallest = executors[i].queue.size();
                next = i;
            }
        }
        return executors[next];
    }

    @Override
    public void execute(@Nonnull Runnable runnable) {
        next().execute(runnable);
    }

    private static class RunnableExecutor implements Executor {

        private final BlockingQueue<Runnable> queue;

        private RunnableExecutor(NamedThreadFactory threadFactory) {
            this.queue = new LinkedBlockingQueue<>();
            threadFactory.newThread(this::run).start();
        }

        @Override
        public void execute(@Nonnull Runnable runnable) {
            try {
                queue.put(runnable);
            } catch (InterruptedException e) {
                throw GraknException.of(UNEXPECTED_INTERRUPTION);
            }
        }

        private void run() {
            while (true) {
                try {
                    Runnable runnable = queue.take();
                    runnable.run();
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                }
            }
        }
    }
}
