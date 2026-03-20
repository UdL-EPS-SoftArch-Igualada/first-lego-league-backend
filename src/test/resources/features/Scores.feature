Feature: Manage Round Scores
  As a referee
  I want to submit and list scores for a round
  So that final rankings can be generated

  Scenario: Referee submits a score successfully
    Given There is a registered user with username "referee", password "password", email "referee@test.com" and roles "ROLE_USER,ROLE_REFEREE"
    And I login as "referee" with password "password"
    And a round exists with id 1 and a team "TeamA" participates in round 1
    When I submit 215 points for team "/teams/TeamA" in round 1
    Then The response code is 201

  Scenario: Non-referee cannot submit a score
    Given There is a registered user with username "user", password "password", email "user@test.com" and roles "ROLE_USER"
    And I login as "user" with password "password"
    And a round exists with id 1 and a team "TeamA" participates in round 1
    When I submit 215 points for team "/teams/TeamA" in round 1
    Then The response code is 403

  Scenario: Team not registered in the competition (round) returns 400
    Given There is a registered user with username "referee", password "password", email "referee@test.com" and roles "ROLE_USER,ROLE_REFEREE"
    And I login as "referee" with password "password"
    And a round exists with id 1
    And a team "TeamB" exists but does not participate in round 1
    When I submit 215 points for team "/teams/TeamB" in round 1
    Then The response code is 400

  Scenario: A team can only have one score per round
    Given There is a registered user with username "referee", password "password", email "referee@test.com" and roles "ROLE_USER,ROLE_REFEREE"
    And I login as "referee" with password "password"
    And a round exists with id 1 and a team "TeamA" participates in round 1
    When I submit 215 points for team "/teams/TeamA" in round 1
    Then The response code is 201
    When I submit 300 points for team "/teams/TeamA" in round 1
    Then The response code is 400

  Scenario: List scores for a round
    Given There is a registered user with username "referee", password "password", email "referee@test.com" and roles "ROLE_USER,ROLE_REFEREE"
    And I login as "referee" with password "password"
    And a round exists with id 1 and a team "TeamA" participates in round 1
    When I submit 215 points for team "/teams/TeamA" in round 1
    Then The response code is 201
    When I request the scores for round 1
    Then The response code is 200

  Scenario: List scores for a non-existent round returns 404
    Given There is a registered user with username "referee", password "password", email "referee@test.com" and roles "ROLE_USER,ROLE_REFEREE"
    And I login as "referee" with password "password"
    When I request the scores for round 999999
    Then The response code is 404
