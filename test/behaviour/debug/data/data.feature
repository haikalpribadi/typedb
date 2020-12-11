#
# Copyright (C) 2020 Grakn Labs
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

#noinspection CucumberUndefinedStep
Feature: Graql Match Query

  Background: Open connection and create a simple extensible schema
    Given connection has been opened
    Given connection does not have any database
    Given connection create database: grakn
    Given connection open schema session for database: grakn
    Given session opens transaction of type: write
    Given the integrity is validated
    Given graql define
      """
      define
      person sub entity,
        plays friendship:friend,
        plays employment:employee,
        owns name,
        owns age,
        owns ref @key;
      company sub entity,
        plays employment:employer,
        owns name,
        owns ref @key;
      friendship sub relation,
        relates friend,
        owns ref @key;
      employment sub relation,
        relates employee,
        relates employer,
        owns ref @key;
      name sub attribute, value string;
      age sub attribute, value long;
      ref sub attribute, value long;
      """
    Given transaction commits
    Given the integrity is validated
    Given session opens transaction of type: write

#  ##########
#  # THINGS #
#  ##########




  # TODO will be fixed by splitting vertex procedure from graph procedure
#  Scenario: 'iid' matches the instance with the specified internal iid
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has ref 0;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x isa person;
#      """
#    Then each answer satisfies
#      """
#      match $x iid <answer.x.iid>;
#      """



  # TODO should throw exceptions when labels don't exist
#  Scenario: when matching by a type whose label doesn't exist, an error is thrown
#    Then graql match; throws exception
#      """
#      match $x isa ganesh;
#      """
#    Then the integrity is validated
#

  # TODO should throw exceptions when labels don't exist
#  Scenario: when matching by a relation type whose label doesn't exist, an error is thrown
#    Then graql match; throws exception
#      """
#      match ($x, $y) isa $type; $type type jakas-relacja;
#      """
#    Then the integrity is validated

  # TODO should throw exceptions when labels don't exist
#  Scenario: when matching a non-existent type label to a variable from a generic 'isa' query, an error is thrown
#    Then graql match; throws exception
#      """
#      match $x isa $type; $type type polok;
#      """
#    Then the integrity is validated
#

#
#
#  #############
#  # RELATIONS #
#  #############
#


  # TODO unknown reason for failure
#  Scenario: a relation is matchable from role players without specifying relation type
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has ref 0;
#      $y isa company, has ref 1;
#      $r (employee: $x, employer: $y) isa employment,
#         has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    Then get answers of graql query
#      """
#      match $x isa person; $r (employee: $x) isa relation;
#      """
#    And concept identifiers are
#      |      | check | value |
#      | REF0 | key   | ref:0 |
#      | REF1 | key   | ref:1 |
#      | REF2 | key   | ref:2 |
#    Then uniquely identify answer concepts
#      | x    | r    |
#      | REF0 | REF2 |
#    When get answers of graql query
#      """
#      match $y isa company; $r (employer: $y) isa relation;
#      """
#    Then uniquely identify answer concepts
#      | y    | r    |
#      | REF1 | REF2 |

  # TODO this fails because we use relation:someplayer as a `label` property to create an iterator over. This label does
  # TODO not exist as a type!
#  Scenario: duplicate role players are retrieved singly when queried doubly
#    Given graql define
#      """
#      define
#      some-entity sub entity, plays symmetric:someplayer, owns ref @key;
#      symmetric sub relation, relates someplayer, owns ref @key;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert $x isa some-entity, has ref 0; (someplayer: $x, someplayer: $x) isa symmetric, has ref 1;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $r (someplayer: $x) isa relation;
#      """
#    And concept identifiers are
#      |      | check | value |
#      | REF0 | key   | ref:0 |
#      | REF1 | key   | ref:1 |
#    Then uniquely identify answer concepts
#      | x    | r    |
#      | REF0 | REF1 |

  # TODO this fails because we use relation:someplayer as a `label` property to create an iterator over. This label does
  # TODO not exist as a type!
#  Scenario: duplicate role players are retrieved singly when queried singly
#    Given graql define
#      """
#      define
#      some-entity sub entity, plays symmetric:player, owns ref @key;
#      symmetric sub relation, relates player, owns ref @key;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert $x isa some-entity, has ref 0; (player: $x, player: $x) isa symmetric, has ref 1;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $r (player: $x) isa relation;
#      """
#    And concept identifiers are
#      |      | check | value |
#      | REF0 | key   | ref:0 |
#      | REF1 | key   | ref:1 |
#    Then uniquely identify answer concepts
#      | x    | r    |
#      | REF0 | REF1 |