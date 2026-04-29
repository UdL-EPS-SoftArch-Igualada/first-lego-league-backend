@EditionMatches
Feature: Get matches by edition

  Background:
    Given There is an edition with id for matches

  @EditionMatchesHappy
  Scenario: Retrieve matches for an edition with teams
    Given the edition has teams with matches
    When I get matches for the edition
    Then the match response status should be 200
    And the edition matches response should contain 2 matches
    And the edition matches should not contain duplicates

  @EditionMatchesEmpty
  Scenario: Retrieve matches for an edition with no teams
    When I get matches for the edition
    Then the match response status should be 200
    And the edition matches response should be empty

  @EditionMatchesShared
  Scenario: Match with both teams in the same edition appears only once
    Given the edition has two teams in the same match
    When I get matches for the edition
    Then the match response status should be 200
    And the edition matches response should contain 1 matches
    And the edition matches should not contain duplicates

  @EditionMatchesNotFound
  Scenario: Returns error when edition does not exist
    When I get matches for edition 99999
    Then the match response status should be 404
    And the edition matches error should be "EDITION_NOT_FOUND"
