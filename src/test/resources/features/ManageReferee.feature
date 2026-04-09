Feature: Manage Referee REST API

  Background:
    Given I login as "admin" with password "password"
    And the volunteer system is empty

  Scenario: Create a referee
    When I request to create a referee with name "Jordi" and emailAddress "jordi@udl.cat" and phoneNumber "123456789" and expert "true"
    Then the referee API response status should be 201
    And I request to retrieve that referee
    And the referee response should contain name "Jordi" and emailAddress "jordi@udl.cat" and phoneNumber "123456789" and expert "true"

  Scenario: Retrieve a referee
    Given a referee exists with name "Marc" and emailAddress "marc@udl.cat" and phoneNumber "123456789" and expert "false"
    When I request to retrieve that referee
    Then the referee API response status should be 200
    And the referee response should contain name "Marc" and emailAddress "marc@udl.cat" and phoneNumber "123456789" and expert "false"

  Scenario: Update a referee
    Given a referee exists with name "Anna" and emailAddress "anna@udl.cat" and phoneNumber "123456789" and expert "false"
    When I request to update the referee name to "Anna Updated"
    Then the referee API response status should be 204
    And I request to retrieve that referee
    Then the referee response should contain name "Anna Updated" and emailAddress "anna@udl.cat" and phoneNumber "123456789" and expert "false"

  Scenario: Delete a referee
    Given a referee exists with name "Joan" and emailAddress "joan@udl.cat" and phoneNumber "123456789" and expert "false"
    When I request to delete that referee
    Then the referee API response status should be 204
    And I request to retrieve that referee
    Then the referee API response status should be 404
