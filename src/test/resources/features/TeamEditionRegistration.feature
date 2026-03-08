Feature: Team Edition Registration
  As an admin
  I want to register teams to editions
  So that I can manage which teams participate in each edition

  Background:
    Given There is a registered user with username "user" and password "password" and email "user@sample.app"
    And I login as "user" with password "password"

  @HappyPath
  Scenario: Successfully register a team to an edition
    Given There is an edition with year 2025, venue "Igualada" and description "FLL 2025"
    And There is a team named "LegoStars" from "Igualada" with category "Challenge"
    When I register team "LegoStars" to the current edition
    Then The response code is 201
    And The response has status "REGISTERED"

  @Validation
  Scenario: Cannot register a team to a non-existent edition
    Given There is a team named "LegoStars" from "Igualada" with category "Challenge"
    When I register team "LegoStars" to edition with id 9999
    Then The response code is 404
    And The response has error "EDITION_NOT_FOUND"
    And The response has a non-empty message

  @Validation
  Scenario: Cannot register a non-existent team to an edition
    Given There is an edition with year 2025, venue "Igualada" and description "FLL 2025"
    When I register team "NonExistentTeam" to the current edition
    Then The response code is 404
    And The response has error "TEAM_NOT_FOUND"
    And The response has a non-empty message

  @BusinessRules
  Scenario: Cannot register a team already registered in the edition
    Given There is an edition with year 2025, venue "Igualada" and description "FLL 2025"
    And There is a team named "LegoStars" from "Igualada" with category "Challenge"
    And Team "LegoStars" is already registered in the current edition
    When I register team "LegoStars" to the current edition
    Then The response code is 409
    And The response has error "TEAM_ALREADY_REGISTERED"
    And The response has a non-empty message

  @BusinessRules
  Scenario: Cannot exceed maximum of 18 teams per edition
    Given There is an edition with year 2025, venue "Igualada" and description "FLL 2025"
    And The current edition already has 18 teams registered
    And There is a team named "Team19" from "Igualada" with category "Challenge"
    When I register team "Team19" to the current edition
    Then The response code is 409
    And The response has error "MAX_TEAMS_REACHED"
    And The response has a non-empty message

  @Concurrency
  Scenario: Concurrent registrations beyond capacity return 409 instead of 500
    Given There is an edition with year 2025, venue "Igualada" and description "FLL 2025"
    And The current edition already has 17 teams registered
    And There is a team named "RacerA" from "Igualada" with category "Challenge"
    And There is a team named "RacerB" from "Igualada" with category "Challenge"
    When I register teams "RacerA" and "RacerB" concurrently to the current edition
    Then One registration succeeds with code 201 and the other fails with code 409

