Feature: Manage Match Results
  As a developer
  I want to verify that the match results repository is exposed correctly

  Scenario: MatchResults endpoint is working
    Given I'm not logged in
    When I request the match results list
    Then The response code is 200

  Scenario: Create a valid MatchResult and retrieve it
    Given I'm not logged in
	And The dependencies exist
    When I create a match result with score 10
    Then The response code is 201
    When I request the match results list
    Then The response code is 200

  Scenario: Create a match result with negative score
    Given I'm not logged in
	And The dependencies exist
    When I create a match result with score -5
    Then The response code is 400
    And The error message is "Score cannot be negative"