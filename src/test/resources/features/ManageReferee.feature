Feature: Manage Referees
  In order to manage referees
  As a user
  I want to be able to create, retrieve, update and delete referees

  Background:
    Given There is a registered user with username "admin" and password "admin" and email "admin@fll.udl.cat"
    And I login as "admin" with password "admin"

  Scenario: Create a referee
    Given There is no referee with email "john@example.com"
    When I create a new referee with name "John Doe", email "john@example.com", phone "123456789" and expert "true"
    Then The response code is 201
    And A referee with name "John Doe", email "john@example.com", phone "123456789" and expert "true" exists

  Scenario: Retrieve a referee
    Given There is no referee with email "read@example.com"
    And There is a referee with name "Jane Smith", email "read@example.com", phone "987654321" and expert "false"
    When I retrieve the referee with email "read@example.com"
    Then The response code is 200
    And The response contains referee name "Jane Smith" and email "read@example.com"

  Scenario: Update a referee
    Given There is no referee with email "update@example.com"
    And There is a referee with name "Bob Johnson", email "update@example.com", phone "555123456" and expert "false"
    When I update the referee with email "update@example.com" to expert "true"
    Then The response code is 200
    And The referee with email "update@example.com" is expert

  Scenario: Delete a referee
    Given There is no referee with email "delete@example.com"
    And There is a referee with name "Alice White", email "delete@example.com", phone "555987654" and expert "true"
    When I delete the referee with email "delete@example.com"
    Then The response code is 204
    And No referee with email "delete@example.com" exists
