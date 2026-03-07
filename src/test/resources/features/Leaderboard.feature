Feature: Edition leaderboard retrieval
  As a competition organizer
  I want to retrieve a leaderboard for a specific edition
  So that team rankings are deterministic and paginated

  Scenario: Retrieve valid leaderboard for populated edition
    Given an edition with leaderboard data exists
    When I request leaderboard for that edition with page 0 and size 10
    Then The response code is 200
    And leaderboard should contain 3 items
    And leaderboard totalElements should be 3
    And leaderboard item at index 0 should have team "TeamA"
    And leaderboard item at index 1 should have team "TeamB"
    And leaderboard item at index 2 should have team "TeamC"

  Scenario: Score tie is resolved by matches played
    Given an edition with tie on score and different matches played exists
    When I request leaderboard for that edition with page 0 and size 10
    Then The response code is 200
    And leaderboard item at index 0 should have team "TeamAlpha"
    And leaderboard item at index 1 should have team "TeamBeta"

  Scenario: Full tie is resolved by team name ascending
    Given an edition with tie on score and matches played exists
    When I request leaderboard for that edition with page 0 and size 10
    Then The response code is 200
    And leaderboard item at index 0 should have team "TeamAlpha"
    And leaderboard item at index 1 should have team "TeamBeta"

  Scenario: Retrieve leaderboard for empty edition
    Given an empty edition exists
    When I request leaderboard for that edition with page 0 and size 10
    Then The response code is 200
    And leaderboard should contain 0 items
    And leaderboard totalElements should be 0

  Scenario: Non-existent edition returns not found
    When I request leaderboard for a non-existent edition with page 0 and size 10
    Then The response code is 404

  Scenario: Pagination returns second place on second page
    Given an edition with leaderboard data exists
    When I request leaderboard for that edition with page 1 and size 1
    Then The response code is 200
    And leaderboard should contain 1 item
    And leaderboard item at index 0 should have team "TeamB"
    And leaderboard item at index 0 should have position 2

  Scenario: Invalid pagination returns bad request
    Given an edition with leaderboard data exists
    When I request leaderboard for that edition with page 0 and size 0
    Then The response code is 400
