/*
 * Copyright (C) 2022 Vaticle
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

package com.vaticle.typedb.core.graph.vertex;

import com.vaticle.typedb.core.graph.GraphManager;
import com.vaticle.typedb.core.graph.ThingGraph;
import com.vaticle.typedb.core.graph.adjacency.ThingAdjacency;
import com.vaticle.typedb.core.graph.common.Encoding;
import com.vaticle.typedb.core.graph.iid.VertexIID;

public interface ThingVertex extends Vertex<VertexIID.Thing, Encoding.Vertex.Thing> {

    /**
     * Returns the {@code ThingGraph} containing all {@code ThingVertex}.
     *
     * @return the {@code ThingGraph} containing all {@code ThingVertex}
     */
    ThingGraph graph();

    /**
     * Returns the {@code GraphManager} containing both {@code TypeGraph} and {@code ThingGraph}.
     *
     * @return the {@code GraphManager} containing both {@code TypeGraph} and {@code ThingGraph}
     */
    GraphManager graphs();

    /**
     * Returns the {@code TypeVertex} in which this {@code ThingVertex} is an instance of.
     *
     * @return the {@code TypeVertex} in which this {@code ThingVertex} is an instance of
     */
    TypeVertex type();

    /**
     * Returns the {@code ThingAdjacency} set of outgoing edges.
     *
     * @return the {@code ThingAdjacency} set of outgoing edges
     */
    ThingAdjacency.Out outs();

    /**
     * Returns the {@code ThingAdjacency} set of incoming edges.
     *
     * @return the {@code ThingAdjacency} set of incoming edges
     */
    ThingAdjacency.In ins();

    /**
     * Returns true if this {@code ThingVertex} is a result of inference.
     *
     * @return true if this {@code ThingVertex} is a result of inference
     */
    boolean isInferred();

    /**
     * Returns true if this {@code ThingVertex} is an instance of {@code AttributeVertex}.
     *
     * @return true if this {@code ThingVertex} is an instance of {@code AttributeVertex}
     */
    boolean isAttribute();

    /**
     * Casts this {@code ThingVertex} into an {@code AttributeVertex} if it is one.
     *
     * @return this object as an {@code AttributeVertex}
     */
    AttributeVertex<?> asAttribute();

    boolean isWrite();

    ThingVertex.Write asWrite();

    ThingVertex.Write toWrite();

    interface Write extends ThingVertex {

        /**
         * Returns the {@code ThingAdjacency} set of outgoing edges.
         *
         * @return the {@code ThingAdjacency} set of outgoing edges
         */
        ThingAdjacency.Write.Out outs();

        /**
         * Returns the {@code ThingAdjacency} set of incoming edges.
         *
         * @return the {@code ThingAdjacency} set of incoming edges
         */
        ThingAdjacency.Write.In ins();

        void setModified();

        void delete();

        boolean isDeleted();

        void commit();

        @Override
        AttributeVertex.Write<?> asAttribute();

    }

}
