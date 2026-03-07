Feature: Manage Scientific Project
    In order to manage scientific project evaluations
    As a user
    I want to be able to create and search scientific projects

    Scenario: Create a scientific project with an associated team
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        When I create a new scientific project with score 85 and comments "Great innovation" for team "LegoStars"
        Then The response code is 201
        And The response has a team link
        And The latest scientific project has a team relation endpoint

    Scenario: Reject a scientific project without team
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        When I create a new scientific project with score 80 and comments "Missing team" without team
        Then The response code is 400
        And The error code is "TEAM_REQUIRED"
        And The error message is "A scientific project must have an associated team"

    Scenario: Reject a scientific project with non-existent team
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        When I create a new scientific project with score 82 and comments "Unknown team" and invalid team
        Then The response code is 400
        And The error code is "TEAM_NOT_FOUND"
        And The error message is "The referenced team does not exist"

    Scenario: Find scientific projects by minimum score
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        And There is a scientific project with score 90 and comments "Excellent research" for team "AlphaTeam"
        When I search for scientific projects with minimum score 85
        Then The response code is 200
        And The response contains 1 scientific project(s)

    Scenario: Find scientific projects with minimum score returns no results
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        And There is a scientific project with score 70 and comments "Average work" for team "BetaTeam"
        When I search for scientific projects with minimum score 85
        Then The response code is 200
        And The response contains 0 scientific project(s)

    Scenario: Find scientific projects by team name
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"
        And There is a scientific project with score 88 and comments "Robotics focus" for team "SearchTeam"
        And There is a scientific project with score 75 and comments "Another team project" for team "OtherTeam"
        When I search for scientific projects by team name "SearchTeam"
        Then The response code is 200
        And The response contains 1 scientific project(s)
