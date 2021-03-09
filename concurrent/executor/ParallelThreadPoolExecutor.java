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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class ParallelThreadPoolExecutor implements Executor {

    private static final Logger LOG = LoggerFactory.getLogger(ParallelThreadPoolExecutor.class);

    private final Executor[] executors;
    private final AtomicInteger nextIndex;

    public ParallelThreadPoolExecutor(int executors, NamedThreadFactory threadFactory) {
        this.executors = new Executor[executors];
        this.nextIndex = new AtomicInteger(0);
        for (int i = 0; i < executors; i++) this.executors[i] = newFixedThreadPool(1, threadFactory);
    }

    private Executor next() {
        return executors[nextIndex.getAndUpdate(i -> {
            i++; if (i % executors.length == 0) i = 0; return i;
        })];
    }

    @Override
    public void execute(@Nonnull Runnable runnable) {
        next().execute(runnable);
    }
}
