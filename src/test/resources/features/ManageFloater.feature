Feature: Manage Floater REST API

  Background:
    Given I login as "admin" with password "password"
    And the volunteer system is empty

  Scenario: Create a floater
    When I request to create a floater with name "Laura" and student code "FLL-001"
    Then the floater API response status should be 201
    And I request to retrieve that floater
    And the response should contain name "Laura" and student code "FLL-001"

  Scenario: Retrieve a floater
    Given a floater exists with name "Marc" and student code "FLL-002"
    When I request to retrieve that floater
    Then the floater API response status should be 200
    And the response should contain name "Marc" and student code "FLL-002"

  Scenario: Update a floater
    Given a floater exists with name "Anna" and student code "FLL-003"
    When I request to update the floater name to "Anna Updated"
    Then the floater API response status should be 204
    And I request to retrieve that floater 
    Then the response should contain name "Anna Updated" and student code "FLL-003"

  Scenario: Delete a floater
    Given a floater exists with name "Joan" and student code "FLL-004"
    When I request to delete that floater
    Then the floater API response status should be 204
    And I request to retrieve that floater
    Then the floater API response status should be 404

	Scenario: Cannot assign more than 2 floaters to a team
		Given I create a floater with name "First", email "f1@test.com", phone "100000001" and student code "FLL-010"
		And I save the floater
		And I create a floater with name "Second", email "f2@test.com", phone "100000002" and student code "FLL-011"
		And I save the floater
		And I create a floater with name "Third", email "f3@test.com", phone "100000003" and student code "FLL-012"
		And I save the floater
		And a team named "OverfullTeam" from city "Barcelona" exists for floater assignment
		And I assign the floater "FLL-010" to team "OverfullTeam"
		And I assign the floater "FLL-011" to team "OverfullTeam"
		When I try to assign the floater "FLL-012" to team "OverfullTeam"
		Then I should receive the error "A team cannot have more than 2 floaters"