Feature: Team Management Robustness

  Background:
    Given the system is empty

  @HappyPath
  Scenario: Create a complete team with multiple members
    Given I create a team named "LegoStars" from "Igualada"
    And I add a member named "Pol" with role "Captain"
    And I add a member named "Marc" with role "Programmer"
    When I save the team
    Then the team "LegoStars" should exist in the system
    And the team should have 2 members

  @Validation
  Scenario: Name is too short
    When I try to create an invalid team named "Ab" from "Igualada"
    Then the system should reject the team with an error

  @BusinessRules
  Scenario: Cannot exceed 10 members limit
    Given I have a team named "DreamTeam" with 10 members
    When I try to add another member
    Then I should receive an error "A team cannot have more than 10 members"

  @Integrity
  Scenario: Cascade delete members
    Given I have a team "LegoStars" with a member "Pol"
    When I delete the team "LegoStars"
    Then the team "LegoStars" should not exist
    And no members should exist in the system

  @CRUD
  Scenario: Update team details
    Given I create a team named "BetaBot" from "Lleida"
    And I save the team
    When I change the city to "Barcelona"
    Then the team "BetaBot" should be in "Barcelona"

  @Search
  Scenario: Search team by city
    Given I create a team named "GammaBot" from "Girona"
    And I save the team
    When I search for teams in "Girona"
    Then I should find 1 team in the list