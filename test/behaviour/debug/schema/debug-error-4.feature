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

    # TODO this does not work on types anymore - types cannot be specified by IID
#  Scenario: subtype hierarchy satisfies transitive sub assertions
#    Given graql define
#      """
#      define
#      sub1 sub entity;
#      sub2 sub sub1;
#      sub3 sub sub1;
#      sub4 sub sub2;
#      sub5 sub sub4;
#      sub6 sub sub5;
#      """
#    Given transaction commits
#    Given the integrity is validated
#    Given session opens transaction of type: read
#    When get answers of graql query
#      """
#      match
#        $x sub $y;
#        $y sub $z;
#        $z sub sub1;
#      """
#    Then each answer satisfies
#      """
#      match $x sub $z; $x iid <answer.x.iid>; $z iid <answer.z.iid>;
#      """