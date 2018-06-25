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

package ai.grakn.client;

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
import ai.grakn.client.concept.RemoteAttribute;
import ai.grakn.client.concept.RemoteAttributeType;
import ai.grakn.client.concept.RemoteEntity;
import ai.grakn.client.concept.RemoteEntityType;
import ai.grakn.client.concept.RemoteMetaType;
import ai.grakn.client.concept.RemoteRelationship;
import ai.grakn.client.concept.RemoteRelationshipType;
import ai.grakn.client.concept.RemoteRole;
import ai.grakn.client.concept.RemoteRule;
import ai.grakn.client.rpc.ConceptBuilder;
import ai.grakn.client.rpc.RequestBuilder;
import ai.grakn.rpc.generated.GrpcGrakn.TxResponse;
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
public class RemoteConceptTest {

    private static final ConceptId ID = ConceptId.of("V123");
    private static final Pattern PATTERN = var("x").isa("person");

    private static final ConceptId A = ConceptId.of("A");
    private static final ConceptId B = ConceptId.of("B");
    private static final ConceptId C = ConceptId.of("C");

    @Rule
    public final ServerRPCMock server = ServerRPCMock.create();

    private Grakn.Session session;
    private Grakn.Transaction tx;
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
        session = Grakn.session(URI, Keyspace.of("whatever"));
        tx = session.transaction(GraknTxType.WRITE);
        verify(server.requests()).onNext(any()); // The open request

        entityType = RemoteEntityType.create(tx, ID);
        attributeType = RemoteAttributeType.create(tx, ID);
        relationshipType = RemoteRelationshipType.create(tx, ID);
        role = RemoteRole.create(tx, ID);
        rule = RemoteRule.create(tx, ID);
        schemaConcept = role;
        type = entityType;

        entity = RemoteEntity.create(tx, ID);
        attribute = RemoteAttribute.create(tx, ID);
        relationship = RemoteRelationship.create(tx, ID);
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
        Attribute<String> attribute = RemoteAttribute.create(tx, A);

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

    @Test @Ignore
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
        Type mySuper = RemoteEntityType.create(tx, A);
        Type mySupersSuper = RemoteEntityType.create(tx, B);
        Type metaType = RemoteMetaType.create(tx, C);

        //mockConceptMethod(ConceptMethod.getSuperConcepts, Stream.of(me, mySuper, mySupersSuper, metaType));

        Set<Type> sups = entityType.sups().collect(toSet());
        assertThat(sups, containsInAnyOrder(me, mySuper, mySupersSuper));
        assertThat(sups, not(hasItem(metaType)));
    }

    @Test @Ignore
    public void whenCallingSubs_GetTheExpectedResult() {
        Type me = relationshipType;
        Type mySub = RemoteRelationshipType.create(tx, A);
        Type mySubsSub = RemoteRelationshipType.create(tx, B);

        //mockConceptMethod(ConceptMethod.getSubConcepts, Stream.of(me, mySub, mySubsSub));

        assertThat(relationshipType.subs().collect(toSet()), containsInAnyOrder(me, mySub, mySubsSub));
    }

    @Test @Ignore
    public void whenCallingSup_GetTheExpectedResult() {
        SchemaConcept sup = RemoteEntityType.create(tx, A);
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
        Type type = RemoteEntityType.create(tx, A);

        //mockConceptMethod(getDirectType, type);

        assertEquals(type, thing.type());
    }

    @Test @Ignore
    public void whenCallingAttributesWithNoArguments_GetTheExpectedResult() {
        Attribute<?> a = RemoteAttribute.create(tx, A);
        Attribute<?> b = RemoteAttribute.create(tx, B);
        Attribute<?> c = RemoteAttribute.create(tx, C);

        //mockConceptMethod(ConceptMethod.getAttributes, Stream.of(a, b, c));

        assertThat(thing.attributes().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingAttributesWithArguments_GetTheExpectedResult() {
        AttributeType<?> foo = RemoteAttributeType.create(tx, ConceptId.of("foo"));
        AttributeType<?> bar = RemoteAttributeType.create(tx, ConceptId.of("bar"));
        AttributeType<?> baz = RemoteAttributeType.create(tx, ConceptId.of("baz"));

        Attribute<?> a = RemoteAttribute.create(tx, A);
        Attribute<?> b = RemoteAttribute.create(tx, B);
        Attribute<?> c = RemoteAttribute.create(tx, C);

        //mockConceptMethod(ConceptMethod.getAttributesByTypes(foo, bar, baz), Stream.of(a, b, c));

        assertThat(thing.attributes(foo, bar, baz).collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingKeysWithNoArguments_GetTheExpectedResult() {
        Attribute<?> a = RemoteAttribute.create(tx, A);
        Attribute<?> b = RemoteAttribute.create(tx, B);
        Attribute<?> c = RemoteAttribute.create(tx, C);

        //mockConceptMethod(ConceptMethod.getKeys, Stream.of(a, b, c));

        assertThat(thing.keys().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingKeysWithArguments_GetTheExpectedResult() {
        AttributeType<?> foo = RemoteAttributeType.create(tx, ConceptId.of("foo"));
        AttributeType<?> bar = RemoteAttributeType.create(tx, ConceptId.of("bar"));
        AttributeType<?> baz = RemoteAttributeType.create(tx, ConceptId.of("baz"));

        Attribute<?> a = RemoteAttribute.create(tx, A);
        Attribute<?> b = RemoteAttribute.create(tx, B);
        Attribute<?> c = RemoteAttribute.create(tx, C);

        //mockConceptMethod(ConceptMethod.getKeysByTypes(foo, bar, baz), Stream.of(a, b, c));

        assertThat(thing.keys(foo, bar, baz).collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingPlays_GetTheExpectedResult() {
        Role a = RemoteRole.create(tx, A);
        Role b = RemoteRole.create(tx, B);
        Role c = RemoteRole.create(tx, C);

        //mockConceptMethod(ConceptMethod.getRolesPlayedByType, Stream.of(a, b, c));

        assertThat(type.plays().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingInstances_GetTheExpectedResult() {
        Thing a = RemoteRelationship.create(tx, A);
        Thing b = RemoteRelationship.create(tx, B);
        Thing c = RemoteRelationship.create(tx, C);

        //mockConceptMethod(ConceptMethod.getInstances, Stream.of(a, b, c));

        assertThat(relationshipType.instances().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingThingPlays_GetTheExpectedResult() {
        Role a = RemoteRole.create(tx, A);
        Role b = RemoteRole.create(tx, B);
        Role c = RemoteRole.create(tx, C);

        //mockConceptMethod(ConceptMethod.getRolesPlayedByThing, Stream.of(a, b, c));

        assertThat(thing.plays().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRelationshipsWithNoArguments_GetTheExpectedResult() {
        Relationship a = RemoteRelationship.create(tx, A);
        Relationship b = RemoteRelationship.create(tx, B);
        Relationship c = RemoteRelationship.create(tx, C);

        //mockConceptMethod(ConceptMethod.getRelationships, Stream.of(a, b, c));

        assertThat(thing.relationships().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRelationshipsWithRoles_GetTheExpectedResult() {
        Role foo = RemoteRole.create(tx, ConceptId.of("foo"));
        Role bar = RemoteRole.create(tx, ConceptId.of("bar"));
        Role baz = RemoteRole.create(tx, ConceptId.of("baz"));

        Relationship a = RemoteRelationship.create(tx, A);
        Relationship b = RemoteRelationship.create(tx, B);
        Relationship c = RemoteRelationship.create(tx, C);

        //mockConceptMethod(ConceptMethod.getRelationshipsByRoles(foo, bar, baz), Stream.of(a, b, c));

        assertThat(thing.relationships(foo, bar, baz).collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRelationshipTypes_GetTheExpectedResult() {
        RelationshipType a = RemoteRelationshipType.create(tx, A);
        RelationshipType b = RemoteRelationshipType.create(tx, B);
        RelationshipType c = RemoteRelationshipType.create(tx, C);

        //mockConceptMethod(ConceptMethod.getRelationshipTypesThatRelateRole, Stream.of(a, b, c));

        assertThat(role.relationshipTypes().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingPlayedByTypes_GetTheExpectedResult() {
        Type a = RemoteEntityType.create(tx, A);
        Type b = RemoteRelationshipType.create(tx, B);
        Type c = RemoteAttributeType.create(tx, C);

        //mockConceptMethod(ConceptMethod.getTypesThatPlayRole, Stream.of(a, b, c));

        assertThat(role.playedByTypes().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRelates_GetTheExpectedResult() {
        Role a = RemoteRole.create(tx, A);
        Role b = RemoteRole.create(tx, B);
        Role c = RemoteRole.create(tx, C);

        //mockConceptMethod(ConceptMethod.getRelatedRoles, Stream.of(a, b, c));

        assertThat(relationshipType.relates().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingAllRolePlayers_GetTheExpectedResult() {
        Role foo = RemoteRole.create(tx, ConceptId.of("foo"));
        Role bar = RemoteRole.create(tx, ConceptId.of("bar"));

        Thing a = RemoteEntity.create(tx, A);
        Thing b = RemoteRelationship.create(tx, B);
        Thing c = RemoteAttribute.create(tx, C);

//        Stream<RolePlayer> mockedResponse = Stream.of(
//                RolePlayer.create(foo, a),
//                RolePlayer.create(bar, b),
//                RolePlayer.create(bar, c)
//        );

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
        Role foo = RemoteRole.create(tx, ConceptId.of("foo"));

        Thing a = RemoteEntity.create(tx, A);
        Thing b = RemoteRelationship.create(tx, B);
        Thing c = RemoteAttribute.create(tx, C);

//        Stream<RolePlayer> expected = Stream.of(
//                RolePlayer.create(foo, a), RolePlayer.create(foo, b), RolePlayer.create(foo, c)
//        );

        //mockConceptMethod(ConceptMethod.getRolePlayers, expected);

        assertThat(relationship.rolePlayers().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingRolePlayersWithRoles_GetTheExpectedResult() {
        Role foo = RemoteRole.create(tx, ConceptId.of("foo"));
        Role bar = RemoteRole.create(tx, ConceptId.of("bar"));
        Role baz = RemoteRole.create(tx, ConceptId.of("baz"));

        Thing a = RemoteEntity.create(tx, A);
        Thing b = RemoteRelationship.create(tx, B);
        Thing c = RemoteAttribute.create(tx, C);

        //mockConceptMethod(ConceptMethod.getRolePlayersByRoles(foo, bar, baz), Stream.of(a, b, c));

        assertThat(relationship.rolePlayers(foo, bar, baz).collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingOwnerInstances_GetTheExpectedResult() {
        Thing a = RemoteEntity.create(tx, A);
        Thing b = RemoteRelationship.create(tx, A);
        Thing c = RemoteAttribute.create(tx, A);

        //mockConceptMethod(ConceptMethod.getOwners, Stream.of(a, b, c));

        assertThat(attribute.ownerInstances().collect(toSet()), containsInAnyOrder(a, b, c));
    }

    @Test @Ignore
    public void whenCallingAttributeTypes_GetTheExpectedResult() {

        ImmutableSet<AttributeType> attributeTypes = ImmutableSet.of(
                RemoteAttributeType.create(tx, A),
                RemoteAttributeType.create(tx, B),
                RemoteAttributeType.create(tx, C)
        );

        //mockConceptMethod(getAttributeTypes, attributeTypes.stream());

        assertEquals(attributeTypes, type.attributes().collect(toSet()));
    }

    @Test @Ignore
    public void whenCallingKeyTypes_GetTheExpectedResult() {

        ImmutableSet<AttributeType> keyTypes = ImmutableSet.of(
                RemoteAttributeType.create(tx, A),
                RemoteAttributeType.create(tx, B),
                RemoteAttributeType.create(tx, C)
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
        EntityType sup = RemoteEntityType.create(tx, A);
        assertEquals(entityType, entityType.sup(sup));
        //verifyConceptMethodCalled(ConceptMethod.setDirectSuper(sup));
    }

    @Test @Ignore
    public void whenSettingSub_ExecuteAConceptMethod() {
        EntityType sup = RemoteEntityType.create(tx, A);
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
        Role role = RemoteRole.create(tx, A);
        assertEquals(relationshipType, relationshipType.relates(role));
        //verifyConceptMethodCalled(ConceptMethod.setRelatedRole(role));
    }

    @Test @Ignore
    public void whenSettingPlays_ExecuteAConceptMethod() {
        Role role = RemoteRole.create(tx, A);
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
        AttributeType<?> attributeType = RemoteAttributeType.create(tx, A);
        assertEquals(type, type.attribute(attributeType));
        //verifyConceptMethodCalled(ConceptMethod.setAttributeType(attributeType));
    }

    @Test @Ignore
    public void whenSettingKeyType_ExecuteAConceptMethod() {
        AttributeType<?> attributeType = RemoteAttributeType.create(tx, A);
        assertEquals(type, type.key(attributeType));
        //verifyConceptMethodCalled(ConceptMethod.setKeyType(attributeType));
    }

    @Test @Ignore
    public void whenDeletingAttributeType_ExecuteAConceptMethod() {
        AttributeType<?> attributeType = RemoteAttributeType.create(tx, A);
        assertEquals(type, type.deleteAttribute(attributeType));
        //verifyConceptMethodCalled(ConceptMethod.unsetAttributeType(attributeType));
    }

    @Test @Ignore
    public void whenDeletingKeyType_ExecuteAConceptMethod() {
        AttributeType<?> attributeType = RemoteAttributeType.create(tx, A);
        assertEquals(type, type.deleteKey(attributeType));
        //verifyConceptMethodCalled(ConceptMethod.unsetKeyType(attributeType));
    }

    @Test @Ignore
    public void whenDeletingPlays_ExecuteAConceptMethod() {
        Role role = RemoteRole.create(tx, A);
        assertEquals(type, type.deletePlays(role));
        //verifyConceptMethodCalled(ConceptMethod.unsetRolePlayedByType(role));
    }

    @Test @Ignore
    public void whenCallingAddEntity_ExecuteAConceptMethod() {
        Entity entity = RemoteEntity.create(tx, A);
        //mockConceptMethod(ConceptMethod.addEntity, entity);
        assertEquals(entity, entityType.addEntity());
    }

    @Test @Ignore
    public void whenCallingAddRelationship_ExecuteAConceptMethod() {
        Relationship relationship = RemoteRelationship.create(tx, A);
        //mockConceptMethod(ConceptMethod.addRelationship, relationship);
        assertEquals(relationship, relationshipType.addRelationship());
    }

    @Test @Ignore
    public void whenCallingPutAttribute_ExecuteAConceptMethod() {
        String value = "Dunstan";
        Attribute<String> attribute = RemoteAttribute.create(tx, A);
        //mockConceptMethod(ConceptMethod.putAttribute(value), attribute);
        assertEquals(attribute, attributeType.putAttribute(value));
    }

    @Test @Ignore
    public void whenCallingDeleteRelates_ExecuteAConceptMethod() {
        Role role = RemoteRole.create(tx, A);
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
        Attribute<Long> attribute = RemoteAttribute.create(tx, A);
        Relationship relationship = RemoteRelationship.create(tx, C);
        //mockConceptMethod(ConceptMethod.setAttribute(attribute), relationship);

        assertEquals(thing, thing.attribute(attribute));

        //verifyConceptMethodCalled(ConceptMethod.setAttribute(attribute));
    }

    @Test @Ignore
    public void whenCallingAddAttributeRelationshipOnThing_ExecuteAConceptMethod() {
        Attribute<Long> attribute = RemoteAttribute.create(tx, A);
        Relationship relationship = RemoteRelationship.create(tx, C);
        //mockConceptMethod(ConceptMethod.setAttribute(attribute), relationship);
        assertEquals(relationship, thing.attributeRelationship(attribute));
    }

    @Test @Ignore
    public void whenCallingDeleteAttribute_ExecuteAConceptMethod() {
        Attribute<Long> attribute = RemoteAttribute.create(tx, A);
        assertEquals(thing, thing.deleteAttribute(attribute));
        //verifyConceptMethodCalled(ConceptMethod.unsetAttribute(attribute));
    }

    @Test @Ignore
    public void whenCallingAddRolePlayer_ExecuteAConceptMethod() {
        Role role = RemoteRole.create(tx, A);
        Thing thing = RemoteEntity.create(tx, B);
        assertEquals(relationship, relationship.addRolePlayer(role, thing));

        //verifyConceptMethodCalled(ConceptMethod.setRolePlayer(RolePlayer.create(role, thing)));
    }

    @Test @Ignore
    public void whenCallingRemoveRolePlayer_ExecuteAConceptMethod() {
        Role role = RemoteRole.create(tx, A);
        Thing thing = RemoteEntity.create(tx, B);
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