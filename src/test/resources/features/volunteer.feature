Feature: Volunteer and Floater Management

  Background:
    Given the volunteer system is empty

  # Floater Tests

  @HappyPath
  Scenario: Create a floater with valid data
    When I create a floater with name "John Doe", email "john@example.com", phone "123456789" and student code "STU001"
    And I save the floater
    Then the floater with student code "STU001" should exist in the system

  @HappyPath
  Scenario: Create multiple floaters
    When I create a floater with name "Alice Smith", email "alice@example.com", phone "111222333" and student code "STU002"
    And I save the floater
    And I create a floater with name "Bob Johnson", email "bob@example.com", phone "444555666" and student code "STU003"
    And I save the floater
    Then there should be 2 floaters in the system

  @Validation
  Scenario: Cannot create floater without name
    When I try to create a floater with name "" and email "test@example.com" and phone "123456789" and student code "STU004"
    Then the floater creation should fail with validation error

  @Validation
  Scenario: Cannot create floater without email
    When I try to create a floater with name "Test User" and email "" and phone "123456789" and student code "STU005"
    Then the floater creation should fail with validation error

  @Validation
  Scenario: Cannot create floater with invalid email format
    When I try to create a floater with name "Test User" and email "invalid-email" and phone "123456789" and student code "STU006"
    Then the floater creation should fail with validation error

  @Validation
  Scenario: Cannot create floater without phone number
    When I try to create a floater with name "Test User" and email "test@example.com" and phone "" and student code "STU007"
    Then the floater creation should fail with validation error

  @Validation
  Scenario: Cannot create floater without student code
    When I try to create a floater with name "Test User" and email "test2@example.com" and phone "123456789" and student code ""
    Then the floater creation should fail with validation error

  @CRUD
  Scenario: Update floater details
    Given I create a floater with name "Original Name", email "original@example.com", phone "111111111" and student code "STU010"
    And I save the floater
    When I update the floater phone number to "999999999"
    Then the floater with student code "STU010" should have phone "999999999"

  @CRUD
  Scenario: Delete a floater
    Given I create a floater with name "ToDelete", email "delete@example.com", phone "777888999" and student code "STU011"
    And I save the floater
    When I delete the floater with student code "STU011"
    Then the floater with student code "STU011" should not exist

  @Search
  Scenario: Find floater by student code
    Given I create a floater with name "Searchable", email "search@example.com", phone "123123123" and student code "SEARCH001"
    And I save the floater
    When I search for floaters with student code "SEARCH001"
    Then I should find 1 floater in the results

  @Search
  Scenario: Search floaters by name containing text
    Given I create a floater with name "TestFloater Alpha", email "alpha@example.com", phone "111111111" and student code "ALPHA001"
    And I save the floater
    And I create a floater with name "TestFloater Beta", email "beta@example.com", phone "222222222" and student code "BETA001"
    And I save the floater
    When I search for floaters with name containing "TestFloater"
    Then I should find 2 floaters in the results

  # Team-Floater Relationship Tests

  @BusinessRules
  Scenario: Assign floater to a team
    Given I create a floater with name "Team Helper", email "helper@example.com", phone "333333333" and student code "HELPER01"
    And I save the floater
    And a team named "RoboTeam" from city "Barcelona" exists for floater assignment
    When I assign the floater "HELPER01" to team "RoboTeam"
    Then the team "RoboTeam" should have 1 floater assigned

  @BusinessRules
  Scenario: A team can have maximum 2 floaters
    Given I create a floater with name "Floater One", email "f1@example.com", phone "100000001" and student code "F001"
    And I save the floater
    And I create a floater with name "Floater Two", email "f2@example.com", phone "100000002" and student code "F002"
    And I save the floater
    And I create a floater with name "Floater Three", email "f3@example.com", phone "100000003" and student code "F003"
    And I save the floater
    And a team named "FullTeam" from city "Madrid" exists for floater assignment
    And I assign the floater "F001" to team "FullTeam"
    And I assign the floater "F002" to team "FullTeam"
    When I try to assign the floater "F003" to team "FullTeam"
    Then I should receive the error "A team cannot have more than 2 floaters"

  @BusinessRules
  Scenario: A floater can assist multiple teams
    Given I create a floater with name "Multi Helper", email "multi@example.com", phone "555555555" and student code "MULTI01"
    And I save the floater
    And a team named "TeamA" from city "Valencia" exists for floater assignment
    And a team named "TeamB" from city "Sevilla" exists for floater assignment
    When I assign the floater "MULTI01" to team "TeamA"
    And I assign the floater "MULTI01" to team "TeamB"
    Then the floater "MULTI01" should assist 2 teams

  @CRUD
  Scenario: Remove floater from team
    Given I create a floater with name "Removable", email "remove@example.com", phone "666666666" and student code "REM001"
    And I save the floater
    And a team named "RemoveTeam" from city "Bilbao" exists for floater assignment
    And I assign the floater "REM001" to team "RemoveTeam"
    When I remove the floater "REM001" from team "RemoveTeam"
    Then the team "RemoveTeam" should have 0 floaters assigned


