Feature: Manage Coach
    In order to manage the competition staff
    As an admin
    I want to be able to create, retrieve, update and delete coaches

    Background:
        Given There is a registered user with username "admin" and password "admin" and email "admin@fll.udl.cat"
        And I login as "admin" with password "admin"

    Scenario: Create a coach
        When I create a new coach with name "Sergio Gomez", email "sergio@example.com" and phone "123456789"
        Then The response code is 201
        And It has been created a coach with name "Sergio Gomez" and email "sergio@example.com"

    Scenario: Retrieve a coach
        Given There is a coach with name "Sergio Gomez" and email "sergio@example.com"
        When I retrieve the coach with email "sergio@example.com"
        Then The response code is 200
        And The coach name is "Sergio Gomez"

    Scenario: Update a coach
        Given There is a coach with name "Sergio Gomez" and email "sergio@example.com"
        When I update the coach "sergio@example.com" with new phone "999888777"
        Then The response code is 200
        And The coach "sergio@example.com" has phone "999888777"

    Scenario: Delete a coach
        Given There is a coach with name "Sergio Gomez" and email "sergio@example.com"
        When I delete the coach with email "sergio@example.com"
        Then The response code is 204
        When I retrieve the coach with email "sergio@example.com"
        Then The response code is 404