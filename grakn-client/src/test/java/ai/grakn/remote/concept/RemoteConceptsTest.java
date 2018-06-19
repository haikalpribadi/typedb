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

package ai.grakn.remote.concept;

import ai.grakn.GraknTxType;
import ai.grakn.Keyspace;
import ai.grakn.concept.Attribute;
import ai.grakn.concept.AttributeType;
import ai.grakn.concept.AttributeType.DataType;
import ai.grakn.concept.Concept;
import ai.grakn.concept.ConceptId;
import ai.grakn.concept.Entity;
import ai.grakn.concept.EntityType;
import ai.grakn.concept.Label;
import ai.grakn.concept.Relationship;
import ai.grakn.concept.RelationshipType;
import ai.grakn.concept.Role;
import ai.grakn.concept.SchemaConcept;
import ai.grakn.concept.Thing;
import ai.grakn.concept.Type;
import ai.grakn.graql.Pattern;
import ai.grakn.remote.GrpcServerMock;
import ai.grakn.remote.RemoteGraknSession;
import ai.grakn.remote.RemoteGraknTx;
import ai.grakn.remote.rpc.RequestBuilder;
import ai.grakn.rpc.RolePlayer;
import ai.grakn.rpc.generated.GrpcGrakn.TxResponse;
import ai.grakn.rpc.util.ConceptBuilder;
import ai.grakn.util.SimpleURI;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static ai.grakn.graql.Graql.var;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author Felix Chapman
 */
public class RemoteConceptsTest {

    private static final ConceptId ID = ConceptId.of("V123");
    private static final Pattern PATTERN = var("x").isa("person");

    private static final ConceptId A = ConceptId.of("A");
    private static final ConceptId B = ConceptId.of("B");
    private static final ConceptId C = ConceptId.of("C");

    @Rule
    public final GrpcServerMock server = GrpcServerMock.create();

    private RemoteGraknSession session;
    private RemoteGraknTx tx;
    private static final SimpleURI URI = new SimpleURI("localhost", 999);
    private static final Label LABEL = Label.of("too-tired-for-funny-test-names-today");

    private SchemaConcept schemaConcept;
    private Type type;
    private EntityType entityType;
    private AttributeType<String> attributeType;
    private RelationshipType relationshipType;
    private Role role;
    private ai.grakn.concept.Rule rule;
    private Entity entity;
    private Attribute<String> attribute;
    private Relationship relationship;
    private Thing thing;
    private Concept concept;

    @Before
    public void setUp() {
        session = RemoteGraknSession.create(Keyspace.of("whatever"), URI, server.channel());
        tx = session.open(GraknTxType.WRITE);
        verify(server.requests()).onNext(any()); // The open request

        entityType = RemoteConcepts.createEntityType(tx, ID);
        attributeType = RemoteConcepts.createAttributeType(tx, ID);
        relationshipType = RemoteConcepts.createRelationshipType(tx, ID);
        role = RemoteConcepts.createRole(tx, ID);
        rule = RemoteConcepts.createRule(tx, ID);
        schemaConcept = role;
        type = entityType;

        entity = RemoteConcepts.createEntity(tx, ID);
        attribute = RemoteConcepts.createAttribute(tx, ID);
        relationship = RemoteConcepts.createRelationship(tx, ID);
        thing = entity;
        concept = entity;
    }

    @After
    public void closeTx() {
        tx.close();
    }

    @After
    public void closeSession() {
        session.close();
    }

    @Test @Ignore
    public void whenGettingLabel_ReturnTheExpectedLabel() {
        //mockConceptMethod(ConceptMethod.getLabel, LABEL);
        assertEquals(LABEL, schemaConcept.getLabel());
    }

    @Test @Ignore
    public void whenCallingIsImplicit_GetTheExpectedResult() {
        //mockConceptMethod(isImplicit, true);
        assertTrue(schemaConcept.isImplicit());

        //mockConceptMethod(isImplicit, false);
        assertFalse(schemaConcept.isImplicit());
    }

    @Test @Ignore
    public void whenCallingIsInferred_GetTheExpectedResult() {
        //mockConceptMethod(isInferred, true);
        assertTrue(thing.isInferred());

        //mockConceptMethod(isInferred, false);
        assertFalse(thing.isInferred());
    }

    @Test @Ignore
    public void whenCallingIsAbstract_GetTheExpectedResult() {
        //mockConceptMethod(IS_ABSTRACT, true);
        assertTrue(type.isAbstract());

        //mockConceptMethod(IS_ABSTRACT, false);
        assertFalse(type.isAbstract());
    }

    @Test @Ignore
    public void whenCallingGetValue_GetTheExpectedResult() {
        //mockConceptMethod(GET_VALUE, 123);
        assertEquals(123, ((Attribute<?>) attribute).getValue());
    }

    @Test @Ignore
    public void whenCallingGetDataTypeOnAttributeType_GetTheExpectedResult() {
        //mockConceptMethod(getDataTypeOfType, Optional.of(DataType.LONG));
        assertEquals(DataType.LONG, ((AttributeType<?>) attributeType).getDataType());
    }

    @Test @Ignore
    public void whenCallingGetDataTypeOnAttribute_GetTheExpectedResult() {
        //mockConceptMethod(getDataTypeOfAttribute, DataType.LONG);
        assertEquals(DataType.LONG, ((Attribute<?>) attribute).dataType());
    }

    @Test @Ignore
    public void whenCallingGetRegex_GetTheExpectedResult() {
        //mockConceptMethod(getRegex, Optional.of("hello"));
        assertEquals("hello", attributeType.getRegex());
    }

    @Test @Ignore
    public void whenCallingGetAttribute_GetTheExpectedResult() {
        String value = "Dunstan again";
        Attribute<String> attribute = RemoteConcepts.createAttribute(tx, A);

        //mockConceptMethod(ConceptMethod.getAttribute(value), Optional.of(attribute));

        assertEquals(attribute, attributeType.getAttribute(value));
    }

    @Test @Ignore
    public void whenCallingGetAttributeWhenThereIsNoResult_ReturnNull() {
        String value = "Dunstan > Oliver";
        //mockConceptMethod(ConceptMethod.getAttribute(value), Optional.empty());
        assertNull(attributeType.getAttribute(value));
    }

    @Test @Ignore
    public void whenCallingGetWhen_GetTheExpectedResult() {
        //mockConceptMethod(getWhen, Optional.of(PATTERN));
        assertEquals(PATTERN, rule.getWhen());
    }

    @Test @Ignore
    public void whenCallingGetThen_GetTheExpectedResult() {
        //mockConceptMethod(getThen, Optional.of(PATTERN));
        assertEquals(PATTERN, rule.getThen());
    }

    @Test
    public void whenCallingIsDeleted_GetTheExpectedResult() {
        TxResponse response = TxResponse.newBuilder().setConcept(ConceptBuilder.concept(concept)).build();

        server.setResponse(RequestBuilder.getConcept(ID), response);

        assertFalse(entity.isDeleted());

        TxResponse nullResponse = TxResponse.newBuilder().setNoResult(true).build();

        server.setResponse(RequestBuilder.getConcept(ID), nullResponse);

        assertTrue(entity.isDeleted());
    }

    @Test @Ignore
    public void whenCallingSups_GetTheExpectedResult() {
        Type me = entityType;
        Type mySuper = RemoteConcepts.createEntityType(tx, A);
        Type mySupersSuper = RemoteConcepts.createEntityType(tx, B);
        Type metaType = RemoteConcepts.createMetaType(tx, C);

        //mockConceptMethod(ConceptMethod.getSuperConcepts, Stream.of(me, mySuper, mySupersSuper, metaType));

        Set<Type> sups = entityType.sups().collect(toSet());
        assertThat(sups, containsInAnyOrder(me, mySuper, mySupersSuper));
        assertThat(sups, not(hasItem(metaType)));
    }

    @Test @Ignore
    public void whenCallingSubs_GetTheExpectedResult() {
        Type me = relationshipType;
        Type mySub = RemoteConcepts.createRelationshipType(tx, A);
        Type mySubsSub = RemoteConcepts.createRelationshipType(tx, B);

        //mockConceptMethod(ConceptMethod.getSubConcepts, Stream.of(me, mySub, mySubsSub));

        assertThat(relationshipType.subs().collect(toSet()), containsInAnyOrder(me, mySub, mySubsSub));
    }

    @Test @Ignore
    public void whenCallingSup_GetTheExpectedResult() {
        SchemaConcept sup = RemoteConcepts.createEntityType(tx, A);
        //mockConceptMethod(getDirectSuper, Optional.of(sup));
        assertEquals(sup, entityType.sup());
    }

    @Test @Ignore
    public void whenCallingSupOnMetaType_GetNull() {
        //mockConceptMethod(getDirectSuper, Optional.empty());
        assertNull(schemaConcept.sup());
    }

    @Test @Ignore
    public void whenCallingType_GetTheExpectedResult() {
        Type type = RemoteConcepts.createEntityType(tx, A);

        //mockConceptMethod(getDirectType, type);

        assertEquals(type, thing.type());
    }

    @Test @Ignore
    public void whenCallingAttributesWithNoArguments_GetTheExpectedResult() {
        Attribute<?> a = RemoteConcepts.createAttribute(tx, A);
        Attribute<?> b = RemoteConcepts.createAttribute(tx, B);
        Attribute<?> c = RemoteConcepts.createAttribute(tx, C);

        //mockConceptMethod(ConceptMethod.getAttributes, Stream.of(a, b, c));

        assertThat(thing.attributes().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingAttributesWithArguments_GetTheExpectedResult() {
        AttributeType<?> foo = RemoteConcepts.createAttributeType(tx, ConceptId.of("foo"));
        AttributeType<?> bar = RemoteConcepts.createAttributeType(tx, ConceptId.of("bar"));
        AttributeType<?> baz = RemoteConcepts.createAttributeType(tx, ConceptId.of("baz"));

        Attribute<?> a = RemoteConcepts.createAttribute(tx, A);
        Attribute<?> b = RemoteConcepts.createAttribute(tx, B);
        Attribute<?> c = RemoteConcepts.createAttribute(tx, C);

        //mockConceptMethod(ConceptMethod.getAttributesByTypes(foo, bar, baz), Stream.of(a, b, c));

        assertThat(thing.attributes(foo, bar, baz).collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingKeysWithNoArguments_GetTheExpectedResult() {
        Attribute<?> a = RemoteConcepts.createAttribute(tx, A);
        Attribute<?> b = RemoteConcepts.createAttribute(tx, B);
        Attribute<?> c = RemoteConcepts.createAttribute(tx, C);

        //mockConceptMethod(ConceptMethod.getKeys, Stream.of(a, b, c));

        assertThat(thing.keys().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingKeysWithArguments_GetTheExpectedResult() {
        AttributeType<?> foo = RemoteConcepts.createAttributeType(tx, ConceptId.of("foo"));
        AttributeType<?> bar = RemoteConcepts.createAttributeType(tx, ConceptId.of("bar"));
        AttributeType<?> baz = RemoteConcepts.createAttributeType(tx, ConceptId.of("baz"));

        Attribute<?> a = RemoteConcepts.createAttribute(tx, A);
        Attribute<?> b = RemoteConcepts.createAttribute(tx, B);
        Attribute<?> c = RemoteConcepts.createAttribute(tx, C);

        //mockConceptMethod(ConceptMethod.getKeysByTypes(foo, bar, baz), Stream.of(a, b, c));

        assertThat(thing.keys(foo, bar, baz).collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingPlays_GetTheExpectedResult() {
        Role a = RemoteConcepts.createRole(tx, A);
        Role b = RemoteConcepts.createRole(tx, B);
        Role c = RemoteConcepts.createRole(tx, C);

        //mockConceptMethod(ConceptMethod.getRolesPlayedByType, Stream.of(a, b, c));

        assertThat(type.plays().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingInstances_GetTheExpectedResult() {
        Thing a = RemoteConcepts.createRelationship(tx, A);
        Thing b = RemoteConcepts.createRelationship(tx, B);
        Thing c = RemoteConcepts.createRelationship(tx, C);

        //mockConceptMethod(ConceptMethod.getInstances, Stream.of(a, b, c));

        assertThat(relationshipType.instances().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingThingPlays_GetTheExpectedResult() {
        Role a = RemoteConcepts.createRole(tx, A);
        Role b = RemoteConcepts.createRole(tx, B);
        Role c = RemoteConcepts.createRole(tx, C);

        //mockConceptMethod(ConceptMethod.getRolesPlayedByThing, Stream.of(a, b, c));

        assertThat(thing.plays().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRelationshipsWithNoArguments_GetTheExpectedResult() {
        Relationship a = RemoteConcepts.createRelationship(tx, A);
        Relationship b = RemoteConcepts.createRelationship(tx, B);
        Relationship c = RemoteConcepts.createRelationship(tx, C);

        //mockConceptMethod(ConceptMethod.getRelationships, Stream.of(a, b, c));

        assertThat(thing.relationships().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRelationshipsWithRoles_GetTheExpectedResult() {
        Role foo = RemoteConcepts.createRole(tx, ConceptId.of("foo"));
        Role bar = RemoteConcepts.createRole(tx, ConceptId.of("bar"));
        Role baz = RemoteConcepts.createRole(tx, ConceptId.of("baz"));

        Relationship a = RemoteConcepts.createRelationship(tx, A);
        Relationship b = RemoteConcepts.createRelationship(tx, B);
        Relationship c = RemoteConcepts.createRelationship(tx, C);

        //mockConceptMethod(ConceptMethod.getRelationshipsByRoles(foo, bar, baz), Stream.of(a, b, c));

        assertThat(thing.relationships(foo, bar, baz).collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRelationshipTypes_GetTheExpectedResult() {
        RelationshipType a = RemoteConcepts.createRelationshipType(tx, A);
        RelationshipType b = RemoteConcepts.createRelationshipType(tx, B);
        RelationshipType c = RemoteConcepts.createRelationshipType(tx, C);

        //mockConceptMethod(ConceptMethod.getRelationshipTypesThatRelateRole, Stream.of(a, b, c));

        assertThat(role.relationshipTypes().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingPlayedByTypes_GetTheExpectedResult() {
        Type a = RemoteConcepts.createEntityType(tx, A);
        Type b = RemoteConcepts.createRelationshipType(tx, B);
        Type c = RemoteConcepts.createAttributeType(tx, C);

        //mockConceptMethod(ConceptMethod.getTypesThatPlayRole, Stream.of(a, b, c));

        assertThat(role.playedByTypes().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRelates_GetTheExpectedResult() {
        Role a = RemoteConcepts.createRole(tx, A);
        Role b = RemoteConcepts.createRole(tx, B);
        Role c = RemoteConcepts.createRole(tx, C);

        //mockConceptMethod(ConceptMethod.getRelatedRoles, Stream.of(a, b, c));

        assertThat(relationshipType.relates().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingAllRolePlayers_GetTheExpectedResult() {
        Role foo = RemoteConcepts.createRole(tx, ConceptId.of("foo"));
        Role bar = RemoteConcepts.createRole(tx, ConceptId.of("bar"));

        Thing a = RemoteConcepts.createEntity(tx, A);
        Thing b = RemoteConcepts.createRelationship(tx, B);
        Thing c = RemoteConcepts.createAttribute(tx, C);

        Stream<RolePlayer> mockedResponse = Stream.of(
                RolePlayer.create(foo, a),
                RolePlayer.create(bar, b),
                RolePlayer.create(bar, c)
        );

        //TxResponse response = getRolePlayers.createTxResponse(server.grpcIterators(), mockedResponse);

        //server.setResponse(RequestBuilder.runConceptMethod(ID, getRolePlayers), response);

        Map<Role, Set<Thing>> allRolePlayers = relationship.allRolePlayers();

        Map<Role, Set<Thing>> expected = ImmutableMap.of(
                foo, ImmutableSet.of(a),
                bar, ImmutableSet.of(b, c)
        );

        assertEquals(expected, allRolePlayers);
    }

    @Test @Ignore
    public void whenCallingRolePlayersWithNoArguments_GetTheExpectedResult() {
        Role foo = RemoteConcepts.createRole(tx, ConceptId.of("foo"));

        Thing a = RemoteConcepts.createEntity(tx, A);
        Thing b = RemoteConcepts.createRelationship(tx, B);
        Thing c = RemoteConcepts.createAttribute(tx, C);

        Stream<RolePlayer> expected = Stream.of(
                RolePlayer.create(foo, a), RolePlayer.create(foo, b), RolePlayer.create(foo, c)
        );

        //mockConceptMethod(ConceptMethod.getRolePlayers, expected);

        assertThat(relationship.rolePlayers().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRolePlayersWithRoles_GetTheExpectedResult() {
        Role foo = RemoteConcepts.createRole(tx, ConceptId.of("foo"));
        Role bar = RemoteConcepts.createRole(tx, ConceptId.of("bar"));
        Role baz = RemoteConcepts.createRole(tx, ConceptId.of("baz"));

        Thing a = RemoteConcepts.createEntity(tx, A);
        Thing b = RemoteConcepts.createRelationship(tx, B);
        Thing c = RemoteConcepts.createAttribute(tx, C);

        //mockConceptMethod(ConceptMethod.getRolePlayersByRoles(foo, bar, baz), Stream.of(a, b, c));

        assertThat(relationship.rolePlayers(foo, bar, baz).collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingOwnerInstances_GetTheExpectedResult() {
        Thing a = RemoteConcepts.createEntity(tx, A);
        Thing b = RemoteConcepts.createRelationship(tx, A);
        Thing c = RemoteConcepts.createAttribute(tx, A);

        //mockConceptMethod(ConceptMethod.getOwners, Stream.of(a, b, c));

        assertThat(attribute.ownerInstances().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingAttributeTypes_GetTheExpectedResult() {

        ImmutableSet<AttributeType> attributeTypes = ImmutableSet.of(
                RemoteConcepts.createAttributeType(tx, A),
                RemoteConcepts.createAttributeType(tx, B),
                RemoteConcepts.createAttributeType(tx, C)
        );

        //mockConceptMethod(getAttributeTypes, attributeTypes.stream());

        assertEquals(attributeTypes, type.attributes().collect(toSet()));
    }

    @Test @Ignore
    public void whenCallingKeyTypes_GetTheExpectedResult() {

        ImmutableSet<AttributeType> keyTypes = ImmutableSet.of(
                RemoteConcepts.createAttributeType(tx, A),
                RemoteConcepts.createAttributeType(tx, B),
                RemoteConcepts.createAttributeType(tx, C)
        );

        //mockConceptMethod(getKeyTypes, keyTypes.stream());

        assertEquals(keyTypes, type.keys().collect(toSet()));
    }

    @Test @Ignore
    public void whenCallingDelete_ExecuteAConceptMethod() {
        concept.delete();
        //verifyConceptMethodCalled(ConceptMethod.delete);
    }

    @Test @Ignore
    public void whenSettingSuper_ExecuteAConceptMethod() {
        EntityType sup = RemoteConcepts.createEntityType(tx, A);
        assertEquals(entityType, entityType.sup(sup));
        //verifyConceptMethodCalled(ConceptMethod.setDirectSuper(sup));
    }

    @Test @Ignore
    public void whenSettingSub_ExecuteAConceptMethod() {
        EntityType sup = RemoteConcepts.createEntityType(tx, A);
        assertEquals(sup, sup.sub(entityType));
        //verifyConceptMethodCalled(ConceptMethod.setDirectSuper(sup));
    }

    @Test @Ignore
    public void whenSettingLabel_ExecuteAConceptMethod() {
        Label label = Label.of("Dunstan");
        assertEquals(schemaConcept, schemaConcept.setLabel(label));
        //verifyConceptMethodCalled(ConceptMethod.setLabel(label));
    }

    @Test @Ignore
    public void whenSettingRelates_ExecuteAConceptMethod() {
        Role role = RemoteConcepts.createRole(tx, A);
        assertEquals(relationshipType, relationshipType.relates(role));
        //verifyConceptMethodCalled(ConceptMethod.setRelatedRole(role));
    }

    @Test @Ignore
    public void whenSettingPlays_ExecuteAConceptMethod() {
        Role role = RemoteConcepts.createRole(tx, A);
        assertEquals(attributeType, attributeType.plays(role));
        //verifyConceptMethodCalled(ConceptMethod.setRolePlayedByType(role));
    }

    @Test @Ignore
    public void whenSettingAbstractOn_ExecuteAConceptMethod() {
        assertEquals(attributeType, attributeType.setAbstract(true));
        //verifyConceptMethodCalled(ConceptMethod.setAbstract(true));
    }

    @Test @Ignore
    public void whenSettingAbstractOff_ExecuteAConceptMethod() {
        assertEquals(attributeType, attributeType.setAbstract(false));
        //verifyConceptMethodCalled(ConceptMethod.setAbstract(false));
    }

    @Test @Ignore
    public void whenSettingAttributeType_ExecuteAConceptMethod() {
        AttributeType<?> attributeType = RemoteConcepts.createAttributeType(tx, A);
        assertEquals(type, type.attribute(attributeType));
        //verifyConceptMethodCalled(ConceptMethod.setAttributeType(attributeType));
    }

    @Test @Ignore
    public void whenSettingKeyType_ExecuteAConceptMethod() {
        AttributeType<?> attributeType = RemoteConcepts.createAttributeType(tx, A);
        assertEquals(type, type.key(attributeType));
        //verifyConceptMethodCalled(ConceptMethod.setKeyType(attributeType));
    }

    @Test @Ignore
    public void whenDeletingAttributeType_ExecuteAConceptMethod() {
        AttributeType<?> attributeType = RemoteConcepts.createAttributeType(tx, A);
        assertEquals(type, type.deleteAttribute(attributeType));
        //verifyConceptMethodCalled(ConceptMethod.unsetAttributeType(attributeType));
    }

    @Test @Ignore
    public void whenDeletingKeyType_ExecuteAConceptMethod() {
        AttributeType<?> attributeType = RemoteConcepts.createAttributeType(tx, A);
        assertEquals(type, type.deleteKey(attributeType));
        //verifyConceptMethodCalled(ConceptMethod.unsetKeyType(attributeType));
    }

    @Test @Ignore
    public void whenDeletingPlays_ExecuteAConceptMethod() {
        Role role = RemoteConcepts.createRole(tx, A);
        assertEquals(type, type.deletePlays(role));
        //verifyConceptMethodCalled(ConceptMethod.unsetRolePlayedByType(role));
    }

    @Test @Ignore
    public void whenCallingAddEntity_ExecuteAConceptMethod() {
        Entity entity = RemoteConcepts.createEntity(tx, A);
        //mockConceptMethod(ConceptMethod.addEntity, entity);
        assertEquals(entity, entityType.addEntity());
    }

    @Test @Ignore
    public void whenCallingAddRelationship_ExecuteAConceptMethod() {
        Relationship relationship = RemoteConcepts.createRelationship(tx, A);
        //mockConceptMethod(ConceptMethod.addRelationship, relationship);
        assertEquals(relationship, relationshipType.addRelationship());
    }

    @Test @Ignore
    public void whenCallingPutAttribute_ExecuteAConceptMethod() {
        String value = "Dunstan";
        Attribute<String> attribute = RemoteConcepts.createAttribute(tx, A);
        //mockConceptMethod(ConceptMethod.putAttribute(value), attribute);
        assertEquals(attribute, attributeType.putAttribute(value));
    }

    @Test @Ignore
    public void whenCallingDeleteRelates_ExecuteAConceptMethod() {
        Role role = RemoteConcepts.createRole(tx, A);
        assertEquals(relationshipType, relationshipType.deleteRelates(role));
        //verifyConceptMethodCalled(ConceptMethod.unsetRelatedRole(role));
    }

    @Test @Ignore
    public void whenSettingRegex_ExecuteAConceptMethod() {
        String regex = "[abc]";
        assertEquals(attributeType, attributeType.setRegex(regex));
        //verifyConceptMethodCalled(ConceptMethod.setRegex(Optional.of(regex)));
    }

    @Test @Ignore
    public void whenResettingRegex_ExecuteAQuery() {
        assertEquals(attributeType, attributeType.setRegex(null));
        //verifyConceptMethodCalled(ConceptMethod.setRegex(Optional.empty()));
    }

    @Test @Ignore
    public void whenCallingAddAttributeOnThing_ExecuteAConceptMethod() {
        Attribute<Long> attribute = RemoteConcepts.createAttribute(tx, A);
        Relationship relationship = RemoteConcepts.createRelationship(tx, C);
        //mockConceptMethod(ConceptMethod.setAttribute(attribute), relationship);

        assertEquals(thing, thing.attribute(attribute));

        //verifyConceptMethodCalled(ConceptMethod.setAttribute(attribute));
    }

    @Test @Ignore
    public void whenCallingAddAttributeRelationshipOnThing_ExecuteAConceptMethod() {
        Attribute<Long> attribute = RemoteConcepts.createAttribute(tx, A);
        Relationship relationship = RemoteConcepts.createRelationship(tx, C);
        //mockConceptMethod(ConceptMethod.setAttribute(attribute), relationship);
        assertEquals(relationship, thing.attributeRelationship(attribute));
    }

    @Test @Ignore
    public void whenCallingDeleteAttribute_ExecuteAConceptMethod() {
        Attribute<Long> attribute = RemoteConcepts.createAttribute(tx, A);
        assertEquals(thing, thing.deleteAttribute(attribute));
        //verifyConceptMethodCalled(ConceptMethod.unsetAttribute(attribute));
    }

    @Test @Ignore
    public void whenCallingAddRolePlayer_ExecuteAConceptMethod() {
        Role role = RemoteConcepts.createRole(tx, A);
        Thing thing = RemoteConcepts.createEntity(tx, B);
        assertEquals(relationship, relationship.addRolePlayer(role, thing));

        //verifyConceptMethodCalled(ConceptMethod.setRolePlayer(RolePlayer.create(role, thing)));
    }

    @Test @Ignore
    public void whenCallingRemoveRolePlayer_ExecuteAConceptMethod() {
        Role role = RemoteConcepts.createRole(tx, A);
        Thing thing = RemoteConcepts.createEntity(tx, B);
        relationship.removeRolePlayer(role, thing);
        //verifyConceptMethodCalled(ConceptMethod.removeRolePlayer(RolePlayer.create(role, thing)));
    }

//    private void verifyConceptMethodCalled(ConceptMethod<?> conceptMethod) {
//        verify(server.requests()).onNext(RequestBuilder.runConceptMethod(ID, conceptMethod));
//    }

//    private <T> void mockConceptMethod(ConceptMethod<T> property, T value) {
//        server.setResponse(
//                RequestBuilder.runConceptMethod(ID, property),
//                property.createTxResponse(server.grpcIterators(), value)
//        );
//    }
}