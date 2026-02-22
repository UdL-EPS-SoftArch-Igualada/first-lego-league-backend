Feature: Manage Scientific Project
    In order to manage scientific project evaluations
    As a user
    I want to be able to create and search scientific projects

    Scenario: Create a scientific project
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        When I create a new scientific project with score 85 and comments "Great innovation"
        Then The response code is 201

    Scenario: Find scientific projects by minimum score
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        And There is a scientific project with score 90 and comments "Excellent research"
        When I search for scientific projects with minimum score 85
        Then The response code is 200

    Scenario: Find scientific projects with minimum score returns no results
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        And There is a scientific project with score 70 and comments "Average work"
        When I search for scientific projects with minimum score 85
        Then The response code is 200
