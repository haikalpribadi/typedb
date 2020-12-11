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


  ##################
  # SCHEMA QUERIES #
  ##################

#  Scenario: 'sub' can be used to match the specified type and all its supertypes, including indirect supertypes
#    Given graql define
#      """
#      define
#      writer sub person;
#      scifi-writer sub writer;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match writer sub $x;
#      """
#    And concept identifiers are
#      |     | check | value  |
#      | WRI | label | writer |
#      | PER | label | person |
#      | ENT | label | entity |
#    Then uniquely identify answer concepts
#      | x   |
#      | WRI |
#      | PER |
#      | ENT |


#  Scenario: 'sub' can be used to retrieve all instances of types that are subtypes of a given type
#    Given graql define
#      """
#      define
#
#      child sub person;
#      worker sub person;
#      retired-person sub person;
#      construction-worker sub worker;
#      bricklayer sub construction-worker;
#      crane-driver sub construction-worker;
#      telecoms-worker sub worker;
#      mobile-network-researcher sub telecoms-worker;
#      smartphone-designer sub telecoms-worker;
#      telecoms-business-strategist sub telecoms-worker;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $a isa child, has name "Alfred", has ref 0;
#      $b isa retired-person, has name "Barbara", has ref 1;
#      $c isa bricklayer, has name "Charles", has ref 2;
#      $d isa crane-driver, has name "Debbie", has ref 3;
#      $e isa mobile-network-researcher, has name "Edmund", has ref 4;
#      $f isa telecoms-business-strategist, has name "Felicia", has ref 5;
#      $g isa worker, has name "Gary", has ref 6;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match
#        $x isa $type;
#        $type sub worker;
#      """
#    When concept identifiers are
#      |     | check | value                        |
#      | CHA | key   | ref:2                        |
#      | DEB | key   | ref:3                        |
#      | EDM | key   | ref:4                        |
#      | FEL | key   | ref:5                        |
#      | GAR | key   | ref:6                        |
#      | CON | label | construction-worker          |
#      | BRI | label | bricklayer                   |
#      | CRA | label | crane-driver                 |
#      | TEL | label | telecoms-worker              |
#      | MNR | label | mobile-network-researcher    |
#      | TBS | label | telecoms-business-strategist |
#      | WOR | label | worker                       |
#    # Alfred and Barbara are not retrieved, as they aren't subtypes of worker
#    Then uniquely identify answer concepts
#      | x   | type |
#      | CHA | BRI  |
#      | CHA | CON  |
#      | CHA | WOR  |
#      | DEB | CRA  |
#      | DEB | CON  |
#      | DEB | WOR  |
#      | EDM | MNR  |
#      | EDM | TEL  |
#      | EDM | WOR  |
#      | FEL | TBS  |
#      | FEL | TEL  |
#      | FEL | WOR  |
#      | GAR | WOR  |


#  Scenario: 'sub!' matches the specified type and its direct subtypes
#    Given graql define
#      """
#      define
#      writer sub person;
#      scifi-writer sub writer;
#      musician sub person;
#      flutist sub musician;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x sub! person;
#      """
#    And concept identifiers are
#      |     | check | value    |
#      | WRI | label | writer   |
#      | MUS | label | musician |
#    Then uniquely identify answer concepts
#      | x   |
#      | WRI |
#      | MUS |


#  Scenario: 'sub!' can be used to match the specified type and its direct supertype
#    Given graql define
#      """
#      define
#      writer sub person;
#      scifi-writer sub writer;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match writer sub! $x;
#      """
#    And concept identifiers are
#      |     | check | value  |
#      | PER | label | person |
#    Then uniquely identify answer concepts
#      | x   |
#      | PER |

#
#
#  #############
#  # RELATIONS #
#  #############
#

  Scenario: matching a chain of relations only returns answers if there is a chain of the required length
    Given graql define
      """
      define

      gift-delivery sub relation,
        relates sender,
        relates recipient;

      person plays gift-delivery:sender,
        plays gift-delivery:recipient;
      """
    Given transaction commits
    Given the integrity is validated
    Given connection close all sessions
    Given connection open data session for database: grakn
    Given session opens transaction of type: write
    Given graql insert
      """
      insert
      $x1 isa person, has name "Soroush", has ref 0;
      $x2a isa person, has name "Martha", has ref 1;
      $x2b isa person, has name "Patricia", has ref 2;

      (sender: $x1, recipient: $x2a) isa gift-delivery;
      """
#  $x2c isa person, has name "Lily", has ref 3;
#      (sender: $x1, recipient: $x2b) isa gift-delivery;
#  (sender: $x1, recipient: $x2c) isa gift-delivery;
#  (sender: $x2a, recipient: $x2b) isa gift-delivery;
    Given transaction commits
    Given the integrity is validated
    Given session opens transaction of type: read
    When get answers of graql query
      """
      match
        (sender: $a, recipient: $b) isa gift-delivery;
        $a isa person, has name "Soroush"; $b isa person, has name "Martha";
        $c isa person, has name "Patricia";
      """
#  (sender: $b, recipient: $c) isa gift-delivery;
    When concept identifiers are
      |     | check | value |
      | SOR | key   | ref:0 |
      | MAR | key   | ref:1 |
      | PAT | key   | ref:2 |
#    Then uniquely identify answer concepts
#      | a   | b   |
#      | SOR | MAR |
    Then uniquely identify answer concepts
      | a   | b   | c   |
      | SOR | MAR | PAT |
#    When get answers of graql query
#      """
#      match
#        (sender: $a, recipient: $b) isa gift-delivery;
#        (sender: $b, recipient: $c) isa gift-delivery;
#        (sender: $c, recipient: $d) isa gift-delivery;
#      """
#    Then answer size is: 0
#
#
#  Scenario: when multiple relation instances exist with the same roleplayer, matching that player returns just 1 answer
#    Given graql define
#      """
#      define
#      residency sub relation,
#        relates resident,
#        owns ref @key;
#      person plays residency:resident;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has ref 0;
#      $e (employee: $x) isa employment, has ref 1;
#      $f (friend: $x) isa friendship, has ref 2;
#      $r (resident: $x) isa residency, has ref 3;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    Given concept identifiers are
#      |     | check | value |
#      | PER | key   | ref:0 |
#      | EMP | key   | ref:1 |
#      | FRI | key   | ref:2 |
#      | RES | key   | ref:3 |
#    Given get answers of graql query
#      """
#      match $r isa relation;
#      """
#    Given uniquely identify answer concepts
#      | r   |
#      | EMP |
#      | FRI |
#      | RES |
#    When get answers of graql query
#      """
#      match ($x) isa relation;
#      """
#    Then uniquely identify answer concepts
#      | x   |
#      | PER |
#    When get answers of graql query
#      """
#      match ($x);
#      """
#    Then uniquely identify answer concepts
#      | x   |
#      | PER |
#
#
#  Scenario: an error is thrown when matching an entity type as if it were a role type
#    Then graql match; throws exception
#      """
#      match (person: $x) isa relation;
#      """
#    Then the integrity is validated
#
#
#  Scenario: an error is thrown when matching an entity as if it were a relation
#    Then graql match; throws exception
#      """
#      match ($x) isa person;
#      """
#    Then the integrity is validated
#
#
#  Scenario: an error is thrown when matching a non-existent type label as if it were a relation type
#    Then graql match; throws exception
#      """
#      match ($x) isa bottle-of-rum;
#      """
#    Then the integrity is validated
#
#
#  Scenario: when matching a role type that doesn't exist, an error is thrown
#    Then graql match; throws exception
#      """
#      match (rolein-rolein-rolein: $rolein) isa relation;
#      """
#    Then the integrity is validated
#
#
#  Scenario: when matching a role in a relation type that doesn't have that role, an empty result is returned
#    When get answers of graql query
#      """
#      match (friend: $x) isa employment;
#      """
#    Then answer size is: 0
#
#
#  Scenario: when matching a roleplayer in a relation that can't actually play that role, an empty result is returned
#    When get answers of graql query
#      """
#      match
#        $x isa company;
#        ($x) isa friendship;
#      """
#    Then answer size is: 0
#
#
#  Scenario: when querying for a non-existent relation type iid, an empty result is returned
#    When get answers of graql query
#      """
#      match ($x, $y) isa $type; $type iid 0x83cb2;
#      """
#    Then answer size is: 0
#    When get answers of graql query
#      """
#      match $r ($x, $y) isa $type; $r iid 0x4ba92;
#      """
#    Then answer size is: 0
#
#
#  ##############
#  # ATTRIBUTES #
#  ##############
#
#  Scenario Outline: '<type>' attributes can be matched by value
#    Given graql define
#      """
#      define <attr> sub attribute, value <type>, owns ref @key;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert $n <value> isa <attr>, has ref 0;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $a <value>;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | ATT | key   | ref:0 |
#    Then uniquely identify answer concepts
#      | a   |
#      | ATT |
#
#    Examples:
#      | attr        | type     | value      |
#      | colour      | string   | "Green"    |
#      | calories    | long     | 1761       |
#      | grams       | double   | 9.6        |
#      | gluten-free | boolean  | false      |
#      | use-by-date | datetime | 2020-06-16 |
#
#
#  Scenario Outline: when matching a '<type>' attribute by a value that doesn't exist, an empty answer is returned
#    Given graql define
#      """
#      define <attr> sub attribute, value <type>, owns ref @key;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $a <value>;
#      """
#    Then answer size is: 0
#
#    Examples:
#      | attr        | type     | value      |
#      | colour      | string   | "Green"    |
#      | calories    | long     | 1761       |
#      | grams       | double   | 9.6        |
#      | gluten-free | boolean  | false      |
#      | use-by-date | datetime | 2020-06-16 |
#
#
#  Scenario: 'contains' matches strings that contain the specified substring
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x "Seven Databases in Seven Weeks" isa name;
#      $y "Four Weddings and a Funeral" isa name;
#      $z "Fun Facts about Space" isa name;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x contains "Fun";
#      """
#    And concept identifiers are
#      |     | check | value                            |
#      | FOU | value | name:Four Weddings and a Funeral |
#      | FUN | value | name:Fun Facts about Space       |
#    Then uniquely identify answer concepts
#      | x   |
#      | FOU |
#      | FUN |
#
#
#  Scenario: 'contains' performs a case-insensitive match
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x "The Phantom of the Opera" isa name;
#      $y "Pirates of the Caribbean" isa name;
#      $z "Mr. Bean" isa name;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x contains "Bean";
#      """
#    And concept identifiers are
#      |     | check | value                         |
#      | PIR | value | name:Pirates of the Caribbean |
#      | MRB | value | name:Mr. Bean                 |
#    Then uniquely identify answer concepts
#      | x   |
#      | PIR |
#      | MRB |
#
#
#  Scenario: 'like' matches strings that match the specified regex
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x "ABC123" isa name;
#      $y "123456" isa name;
#      $z "9" isa name;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x like "^[0-9]+$";
#      """
#    And concept identifiers are
#      |     | check | value       |
#      | ONE | value | name:123456 |
#      | NIN | value | name:9      |
#    Then uniquely identify answer concepts
#      | x   |
#      | ONE |
#      | NIN |
#
#
#  Scenario: when querying for a non-existent attribute type iid, an empty result is returned
#    When get answers of graql query
#      """
#      match $x has name $y; $x iid 0x83cb2;
#      """
#    Then answer size is: 0
#    When get answers of graql query
#      """
#      match $x has name $y; $y iid 0x83cb2;
#      """
#    Then answer size is: 0
#
#
#  #######################
#  # ATTRIBUTE OWNERSHIP #
#  #######################
#
#  Scenario: 'has' can be used to match things that own any instance of the specified attribute
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Leila", has ref 0;
#      $y isa person, has ref 1;
#      $c isa company, has name "Grakn", has ref 2;
#      $d isa company, has ref 3;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has name $y; get $x;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | LEI | key   | ref:0 |
#      | GRA | key   | ref:2 |
#    Then uniquely identify answer concepts
#      | x   |
#      | LEI |
#      | GRA |
#
#
#  Scenario: using the 'attribute' meta label, 'has' can match things that own any attribute with a specified value
#    Given graql define
#      """
#      define
#      shoe-size sub attribute, value long;
#      person owns shoe-size;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has age 9, has ref 0;
#      $y isa person, has shoe-size 9, has ref 1;
#      $z isa person, has age 12, has shoe-size 12, has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has attribute 9;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | AG9 | key   | ref:0 |
#      | SS9 | key   | ref:1 |
#    Then uniquely identify answer concepts
#      | x   |
#      | AG9 |
#      | SS9 |
#
#
#  Scenario: when an attribute instance is fully specified, 'has' matches its owners
#    Given graql define
#      """
#      define
#      friendship owns age;
#      graduation-date sub attribute, value datetime, owns age, owns ref @key;
#      person owns graduation-date;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Zoe", has age 21, has graduation-date 2020-06-01, has ref 0;
#      $y (friend: $x) isa friendship, has age 21, has ref 1;
#      $z 2020-06-01 isa graduation-date, has age 21, has ref 2;
#      $w isa person, has ref 3;
#      $v (friend: $x, friend: $w) isa friendship, has age 7, has ref 4;
#      $u 2019-06-03 isa graduation-date, has age 22, has ref 5;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has age 21;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | PER | key   | ref:0 |
#      | FRI | key   | ref:1 |
#      | GRA | key   | ref:2 |
#    Then uniquely identify answer concepts
#      | x   |
#      | PER |
#      | FRI |
#      | GRA |
#
#
#  Scenario: 'has' matches an attribute's owner even if it owns more attributes of the same type
#    Given graql define
#      """
#      define
#      lucky-number sub attribute, value long;
#      person owns lucky-number;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has lucky-number 10, has lucky-number 20, has lucky-number 30, has ref 0;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has lucky-number 20;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | PER | key   | ref:0 |
#    Then uniquely identify answer concepts
#      | x   |
#      | PER |
#
#  Scenario: 'has' can match instances that have themselves
#    Given graql define
#      """
#      define
#      unit sub attribute, value string, owns unit owns ref;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x "meter" isa unit, has $x, has ref 0;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has $x;
#      """
#    And concept identifiers are
#      |       | check | value  |
#      | METER | key   | ref:0  |
#    Then uniquely identify answer concepts
#      | x     |
#      | METER |

#
#  Scenario: an error is thrown when matching by attribute ownership, when the owned thing is actually an entity
#    Then graql match; throws exception
#      """
#      match $x has person "Luke";
#      """
#    Then the integrity is validated
#
#
#  Scenario: when matching by an attribute ownership, if the owner can't actually own it, an empty result is returned
#    When get answers of graql query
#      """
#      match $x isa company, has age $n;
#      """
#    Then answer size is: 0
#
#
#  Scenario: an error is thrown when matching by attribute ownership, when the owned type label doesn't exist
#    Then graql match; throws exception
#      """
#      match $x has bananananananana "rama";
#      """
#    Then the integrity is validated
#
#
#  ##############################
#  # ATTRIBUTE VALUE COMPARISON #
#  ##############################
#
#  Scenario: when things own attributes of different types but the same value, they match by equality, but not ownership
#    Given graql define
#      """
#      define
#      start-date sub attribute, value datetime;
#      graduation-date sub attribute, value datetime;
#      person owns graduation-date;
#      employment owns start-date;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "James", has ref 0, has graduation-date 2009-07-16;
#      $r (employee: $x) isa employment, has start-date 2009-07-16, has ref 1;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match
#        $x isa person, has graduation-date $date;
#        $r (employee: $x) isa employment, has start-date $date;
#      """
#    Then answer size is: 0
#    Then get answers of graql query
#      """
#      match
#        $x isa person, has graduation-date $date;
#        $r (employee: $x) isa employment, has start-date = $date;
#      """
#    Then answer size is: 1
#
#
#  Scenario: 'has $attr = $x' matches owners of any instance '$y' of '$attr' where '$y' and '$x' are equal by value
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Susie", has age 16, has ref 0;
#      $y isa person, has name "Donald", has age 25, has ref 1;
#      $z isa person, has name "Ralph", has age 18, has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has age = 16;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | SUS | key   | ref:0 |
#    Then uniquely identify answer concepts
#      | x   |
#      | SUS |
#
#
#  Scenario: 'has $attr > $x' matches owners of any instance '$y' of '$attr' where '$y > $x'
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Susie", has age 16, has ref 0;
#      $y isa person, has name "Donald", has age 25, has ref 1;
#      $z isa person, has name "Ralph", has age 18, has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has age > 18;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | DON | key   | ref:1 |
#    Then uniquely identify answer concepts
#      | x   |
#      | DON |
#
#
#  Scenario: 'has $attr < $x' matches owners of any instance '$y' of '$attr' where '$y < $x'
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Susie", has age 16, has ref 0;
#      $y isa person, has name "Donald", has age 25, has ref 1;
#      $z isa person, has name "Ralph", has age 18, has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has age < 18;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | SUS | key   | ref:0 |
#    Then uniquely identify answer concepts
#      | x   |
#      | SUS |
#
#
#  Scenario: 'has $attr != $x' matches owners of any instance '$y' of '$attr' where '$y != $x'
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Susie", has age 16, has ref 0;
#      $y isa person, has name "Donald", has age 25, has ref 1;
#      $z isa person, has name "Ralph", has age 18, has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has age != 18;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | DON | key   | ref:1 |
#      | SUS | key   | ref:0 |
#    Then uniquely identify answer concepts
#      | x   |
#      | DON |
#      | SUS |
#
#
#  Scenario: value comparisons can be performed between a 'double' and a 'long'
#    Given graql define
#      """
#      define
#      house-number sub attribute, value long;
#      length sub attribute, value double;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x 1 isa house-number;
#      $y 2.0 isa length;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match
#        $x isa house-number;
#        $x = 1.0;
#      """
#    Then answer size is: 1
#    When get answers of graql query
#      """
#      match
#        $x isa length;
#        $x = 2;
#      """
#    Then answer size is: 1
#    When get answers of graql query
#      """
#      match
#        $x isa house-number;
#        $x 1.0;
#      """
#    Then answer size is: 1
#    When get answers of graql query
#      """
#      match
#        $x isa length;
#        $x 2;
#      """
#    Then answer size is: 1
#    When get answers of graql query
#      """
#      match
#        $x isa attribute;
#        $x >= 1;
#      """
#    Then answer size is: 2
#    When get answers of graql query
#      """
#      match
#        $x isa attribute;
#        $x < 2.0;
#      """
#    Then answer size is: 1
#
#
#  Scenario: when a thing owns multiple attributes of the same type, a value comparison matches if any value matches
#    Given graql define
#      """
#      define
#      lucky-number sub attribute, value long;
#      person owns lucky-number;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has lucky-number 10, has lucky-number 20, has lucky-number 30, has ref 0;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match $x has lucky-number > 25;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | PER | key   | ref:0 |
#    Then uniquely identify answer concepts
#      | x   |
#      | PER |
#
#
#  Scenario: an attribute variable used in both '=' and '>=' predicates is correctly resolved
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Susie", has age 16, has ref 0;
#      $y isa person, has name "Donald", has age 25, has ref 1;
#      $z isa person, has name "Ralph", has age 18, has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match
#        $x has age = $z;
#        $z >= 17;
#        $z isa age;
#      get $x;
#      """
#    And concept identifiers are
#      |     | check | value |
#      | DON | key   | ref:1 |
#      | RAL | key   | ref:2 |
#    Then uniquely identify answer concepts
#      | x   |
#      | DON |
#      | RAL |
#
#
#  Scenario: when the answers of a value comparison include both a 'double' and a 'long', both answers are returned
#    Given graql define
#      """
#      define
#      length sub attribute, value double;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $a 24 isa age;
#      $b 19 isa age;
#      $c 20.9 isa length;
#      $d 19.9 isa length;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match
#        $x isa attribute;
#        $x > 20;
#      """
#    And concept identifiers are
#      |      | check | value       |
#      | A24  | value | age:24      |
#      | A19  | value | age:19      |
#      | L209 | value | length:20.9 |
#      | L199 | value | length:19.9 |
#    Then uniquely identify answer concepts
#      | x    |
#      | A24  |
#      | L209 |
#
#
#  Scenario: when one entity exists, and we match two variables with concept inequality, an empty answer is returned
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert $x isa person, has ref 0;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match
#        $x isa person;
#        $y isa person;
#        not { $x is $y; };
#      """
#    Then answer size is: 0
#
#
#  Scenario: concept comparison of unbound variables throws an error
#    Then graql match; throws exception
#      """
#      match not { $x is $y; };
#      """
#
#
#  Scenario: value comparison of unbound variables throws an error
#    Then graql match; throws exception
#      """
#      match $x != $y;
#      """
#
#
#  ############
#  # PATTERNS #
#  ############
#
#  Scenario: disjunctions return the union of composing query statements
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Jeff", has ref 0;
#      $y isa company, has name "Amazon", has ref 1;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match {$x isa person;} or {$x isa company;};
#      """
#    And concept identifiers are
#      |     | check | value |
#      | JEF | key   | ref:0 |
#      | AMA | key   | ref:1 |
#    Then uniquely identify answer concepts
#      | x   |
#      | JEF |
#      | AMA |
#
#
#  ##################
#  # VARIABLE TYPES #
#  ##################
#
#  Scenario: all instances and their types can be retrieved
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Bertie", has ref 0;
#      $y isa person, has name "Angelina", has ref 1;
#      $r (friend: $x, friend: $y) isa friendship, has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    Given get answers of graql query
#      """
#      match $x isa entity;
#      """
#    Given answer size is: 2
#    Given get answers of graql query
#      """
#      match $r isa relation;
#      """
#    Given answer size is: 1
#    Given get answers of graql query
#      """
#      match $x isa attribute;
#      """
#    Given answer size is: 5
#    When get answers of graql query
#      """
#      match $x isa $type;
#      """
#    # 2 entities x 3 types {person,entity,thing}
#    # 1 relation x 3 types {friendship,relation,thing}
#    # 5 attributes x 3 types {ref/name,attribute,thing}
#    Then answer size is: 24
#
#
#  Scenario: all relations and their types can be retrieved
#    Given connection close all sessions
#    Given connection open data session for database: grakn
#    Given session opens transaction of type: write
#    Given graql insert
#      """
#      insert
#      $x isa person, has name "Bertie", has ref 0;
#      $y isa person, has name "Angelina", has ref 1;
#      $r (friend: $x, friend: $y) isa friendship, has ref 2;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    Given get answers of graql query
#      """
#      match $r isa relation;
#      """
#    Given answer size is: 1
#    Given get answers of graql query
#      """
#      match ($x, $y) isa relation;
#      """
#    # 2 permutations of the roleplayers
#    Given answer size is: 2
#    When get answers of graql query
#      """
#      match ($x, $y) isa $type;
#      """
#    # 2 permutations x 3 types {friendship,relation,thing}
#    Then answer size is: 6
#
#
#  #######################
#  # NEGATION VALIDATION #
#  #######################
#
#  # Negation resolution is handled by Reasoner, but query validation is handled by the language.
#
#  Scenario: when the entire match clause is a negation, an error is thrown
#
#  At least one negated pattern variable must be bound outside the negation block, so this query is invalid.
#
#    Then graql match; throws exception
#      """
#      match not { $x has attribute "value"; };
#      """
#
#
#  Scenario: when matching a negation whose pattern variables are all unbound outside it, an error is thrown
#    Then graql match; throws exception
#      """
#      match
#        $r isa entity;
#        not {
#          ($r2, $i);
#          $i isa entity;
#        };
#      """
#
#
#  Scenario: the first variable in a negation can be unbound, as long as it is connected to a bound variable
#    Then get answers of graql query
#      """
#      match
#        $r isa attribute;
#        not {
#          $x isa entity, has attribute $r;
#        };
#      """
#
#
#  Scenario: negations cannot contain disjunctions
#    Then graql match; throws exception
#      """
#      match
#        $x isa entity;
#        not {
#          { $x has attribute 1; } or { $x has attribute 2; };
#        };
#      """
#
#  Scenario: when negating a negation redundantly, an error is thrown
#    Then graql match; throws exception
#      """
#      match
#        $x isa person, has name "Tim";
#        not {
#          not {
#            $x has age 55;
#          };
#        };
#      """
