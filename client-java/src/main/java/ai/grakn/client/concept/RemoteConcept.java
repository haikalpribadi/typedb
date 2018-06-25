/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016-2018 Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 */

package ai.grakn.client.concept;

import ai.grakn.Keyspace;
import ai.grakn.concept.Concept;
import ai.grakn.concept.ConceptId;
import ai.grakn.exception.GraknTxOperationException;
import ai.grakn.client.Grakn;
import ai.grakn.client.rpc.ConceptBuilder;
import ai.grakn.client.rpc.RequestIterator;
import ai.grakn.rpc.generated.GrpcConcept;
import ai.grakn.rpc.generated.GrpcGrakn;
import ai.grakn.rpc.generated.GrpcIterator;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Felix Chapman
 */
abstract class RemoteConcept<SomeConcept extends Concept> implements Concept {

    abstract Grakn.Transaction tx();

    @Override
    public abstract ConceptId getId();

    @Override
    public final Keyspace keyspace() {
        return tx().keyspace();
    }

    @Override
    public final void delete() throws GraknTxOperationException {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setDelete(GrpcConcept.Unit.getDefaultInstance());
        runMethod(method.build());
    }

    @Override
    public final boolean isDeleted() {
        return tx().getConcept(getId()) == null;
    }

    protected final Stream<? extends Concept> runMethodToConceptStream(GrpcConcept.ConceptMethod method) {
        GrpcIterator.IteratorId iteratorId = runMethod(method).getConceptResponse().getIteratorId();
        Iterable<? extends Concept> iterable = () -> new RequestIterator<>(
                tx(), iteratorId, res -> ConceptBuilder.concept(res.getConcept(), tx())
        );

        return StreamSupport.stream(iterable.spliterator(), false);
    }
    protected final GrpcGrakn.TxResponse runMethod(GrpcConcept.ConceptMethod method) {
        return runMethod(getId(), method);
    }

    protected final GrpcGrakn.TxResponse runMethod(ConceptId id, GrpcConcept.ConceptMethod method) {
        return tx().runConceptMethod(id, method);
    }

    abstract SomeConcept asCurrentBaseType(Concept other);

}