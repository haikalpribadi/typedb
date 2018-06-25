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
import ai.grakn.concept.ConceptId;
import ai.grakn.concept.Relationship;
import ai.grakn.concept.RelationshipType;
import ai.grakn.concept.Role;
import ai.grakn.concept.Thing;
import ai.grakn.client.Grakn;
import ai.grakn.client.rpc.ConceptBuilder;
import ai.grakn.client.rpc.RequestIterator;
import ai.grakn.rpc.generated.GrpcConcept;
import ai.grakn.rpc.generated.GrpcGrakn;
import ai.grakn.rpc.generated.GrpcIterator;
import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Felix Chapman
 */
@AutoValue
public abstract class RemoteRelationship extends RemoteThing<Relationship, RelationshipType> implements Relationship {

    public static RemoteRelationship create(Grakn.Transaction tx, ConceptId id) {
        return new AutoValue_RemoteRelationship(tx, id);
    }

    @Override // TODO: Weird. Why is this not a stream, while other collections are returned as stream
    public final Map<Role, Set<Thing>> allRolePlayers() {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        method.setGetRolePlayers(GrpcConcept.Unit.getDefaultInstance());

        GrpcIterator.IteratorId iteratorId = runMethod(method.build()).getConceptResponse().getIteratorId();
        Iterable<GrpcConcept.RolePlayer> rolePlayers = () -> new RequestIterator<>(
                tx(), iteratorId, GrpcGrakn.TxResponse::getRolePlayer
        );

        Map<Role, Set<Thing>> rolePlayerMap = new HashMap<>();
        for (GrpcConcept.RolePlayer rolePlayer : rolePlayers) {
            Role role = ConceptBuilder.concept(rolePlayer.getRole(), tx()).asRole();
            Thing player = ConceptBuilder.concept(rolePlayer.getPlayer(), tx()).asThing();
            if (rolePlayerMap.containsKey(role)) {
                rolePlayerMap.get(role).add(player);
            } else {
                rolePlayerMap.put(role, Collections.singleton(player));
            }
        }

        return rolePlayerMap;
    }

    @Override // TODO: remove (roles.length==0){...} behavior as it is semantically the same as allRolePlayers() above
    public final Stream<Thing> rolePlayers(Role... roles) {
        GrpcConcept.ConceptMethod.Builder method = GrpcConcept.ConceptMethod.newBuilder();
        if (roles.length == 0) {
            method.setGetRolePlayers(GrpcConcept.Unit.getDefaultInstance());
        } else {
            method.setGetRolePlayersByRoles(ConceptBuilder.concepts(Stream.of(roles)));
        }

        GrpcIterator.IteratorId iteratorId = runMethod(method.build()).getConceptResponse().getIteratorId();
        Iterable<Thing> rolePlayers = () -> new RequestIterator<>(
                tx(), iteratorId, res -> ConceptBuilder.concept(res.getRolePlayer().getPlayer(), tx()).asThing()
        );

        return StreamSupport.stream(rolePlayers.spliterator(), false);
    }

    @Override
    public final Relationship addRolePlayer(Role role, Thing player) {
        GrpcConcept.RolePlayer rolePlayer = GrpcConcept.RolePlayer.newBuilder()
                .setRole(ConceptBuilder.concept(role))
                .setPlayer(ConceptBuilder.concept(player))
                .build();

        runMethod(GrpcConcept.ConceptMethod.newBuilder().setSetRolePlayer(rolePlayer).build());
        return asCurrentBaseType(this);
    }

    @Override
    public final void removeRolePlayer(Role role, Thing player) {
        GrpcConcept.RolePlayer rolePlayer = GrpcConcept.RolePlayer.newBuilder()
                .setRole(ConceptBuilder.concept(role))
                .setPlayer(ConceptBuilder.concept(player))
                .build();

        runMethod(GrpcConcept.ConceptMethod.newBuilder().setUnsetRolePlayer(rolePlayer).build());
    }

    @Override
    final RelationshipType asCurrentType(Concept concept) {
        return concept.asRelationshipType();
    }

    @Override
    final Relationship asCurrentBaseType(Concept other) {
        return other.asRelationship();
    }
}