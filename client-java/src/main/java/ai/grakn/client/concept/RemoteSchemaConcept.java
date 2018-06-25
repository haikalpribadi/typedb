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

import ai.grakn.concept.Concept;
import ai.grakn.concept.Label;
import ai.grakn.concept.LabelId;
import ai.grakn.concept.Rule;
import ai.grakn.concept.SchemaConcept;
import ai.grakn.client.rpc.ConceptBuilder;
import ai.grakn.rpc.generated.GrpcConcept;
import ai.grakn.rpc.generated.GrpcGrakn;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * @author Felix Chapman
 *
 * @param <SomeType> The exact type of this class
 */
abstract class RemoteSchemaConcept<SomeType extends SchemaConcept> extends RemoteConcept<SomeType> implements SchemaConcept {

    public final SomeType sup(SomeType type) {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setSetDirectSuperConcept(ConceptBuilder.concept(type));
        runMethod(method.build());

        return asCurrentBaseType(this);
    }

    public final SomeType sub(SomeType type) {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setSetDirectSuperConcept(ConceptBuilder.concept(this)).build();
        runMethod(type.getId(), method.build());

        return asCurrentBaseType(this);
    }

    @Override
    public final Label getLabel() {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setGetLabel(GrpcConcept.Unit.getDefaultInstance());
        GrpcGrakn.TxResponse response = runMethod(method.build());

        return Label.of(response.getConceptResponse().getLabel());
    }

    @Override
    public final Boolean isImplicit() {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setIsImplicit(GrpcConcept.Unit.getDefaultInstance());
        GrpcGrakn.TxResponse response = runMethod(method.build());

        return response.getConceptResponse().getIsImplicit();
    }

    @Override
    public final SomeType setLabel(Label label) {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setSetLabel(label.getValue());
        runMethod(method.build());

        return asCurrentBaseType(this);
    }

    @Nullable
    @Override
    public final SomeType sup() {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setGetDirectSuperConcept(GrpcConcept.Unit.getDefaultInstance());
        GrpcGrakn.TxResponse response = runMethod(method.build());

        if (response.getConceptResponse().getNoResult()) return null;

        Concept concept = ConceptBuilder.concept(response.getConceptResponse().getConcept(), tx());

        return equalsCurrentBaseType(concept) ? asCurrentBaseType(concept) : null;
    }

    @Override
    public final Stream<SomeType> sups() {
        return tx().admin().sups(this).filter(this::equalsCurrentBaseType).map(this::asCurrentBaseType);
    }

    @Override
    public final Stream<SomeType> subs() {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setGetSubConcepts(GrpcConcept.Unit.getDefaultInstance());
        return runMethodToConceptStream(method.build()).map(this::asCurrentBaseType);
    }

    @Override
    public final LabelId getLabelId() {
        throw new UnsupportedOperationException(); // TODO: remove from API
    }

    @Override
    public final Stream<Rule> getRulesOfHypothesis() {
        throw new UnsupportedOperationException(); // TODO: remove from API
    }

    @Override
    public final Stream<Rule> getRulesOfConclusion() {
        throw new UnsupportedOperationException(); // TODO: remove from API
    }

    abstract boolean equalsCurrentBaseType(Concept other);
}