Feature: Manage Match Results
  As a developer
  I want to verify that the match results repository is exposed correctly

  Scenario: MatchResults endpoint is working
    Given I'm not logged in
    When I request the match results list
    Then The response code is 200