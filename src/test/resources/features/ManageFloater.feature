Feature: Manage Floater REST API

  Background:
    Given I login as "user" with password "password"
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