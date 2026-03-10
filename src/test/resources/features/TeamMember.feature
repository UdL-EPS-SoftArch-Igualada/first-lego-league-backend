Feature: TeamMember REST CRUD
    In order to manage team members through REST
    As an authenticated user
    I want to create, validate, retrieve, list and delete team members

    Background:
        Given the team member API system is empty
        And There is a registered user with username "user" and password "password" and email "user@sample.app"
        And I login as "user" with password "password"

    Scenario: Successfully creating a team member linked to an existing team
        Given a team with name "AlphaTeam" exists for team member management
        When I create a team member with name "Pol" birth date "2010-01-01" and role "Programmer" for team "AlphaTeam"
        Then The response code is 201
        And The created team member has name "Pol" and role "Programmer"
        And The created team member is linked to team "AlphaTeam"

    Scenario Outline: Failing to create a team member with a missing mandatory field
        Given a team with name "ValidationTeam" exists for team member management
        When I try to create a team member missing "<field>" for team "ValidationTeam"
        Then The response code is 400
        And The error message is "<message>"

        Examples:
            | field     | message                   |
            | name      | Member name is mandatory  |
            | birthDate | Birth date cannot be null |
            | role      | Role is mandatory         |

    Scenario: Retrieving a team member by ID
        Given a team with name "RetrieveTeam" exists for team member management
        And I create a team member with name "Anna" birth date "2011-02-03" and role "Builder" for team "RetrieveTeam"
        And The response code is 201
        When I retrieve the created team member by id
        Then The response code is 200
        And The response contains team member name "Anna" and role "Builder"

    Scenario: Retrieving the team member collection
        Given a team with name "ListTeam" exists for team member management
        And I create a team member with name "Mia" birth date "2010-06-10" and role "Captain" for team "ListTeam"
        And The response code is 201
        And I create a team member with name "Noah" birth date "2011-07-11" and role "Strategist" for team "ListTeam"
        And The response code is 201
        When I list all team members
        Then The response code is 200
        And The team member list contains name "Mia"
        And The team member list contains name "Noah"

    Scenario: Retrieve team members by role returns results
        Given a team with name "RoleTeam" exists for team member management
        And I create a team member with name "Pol" birth date "2010-01-01" and role "Captain" for team "RoleTeam"
        And The response code is 201
        And I create a team member with name "Nora" birth date "2011-05-05" and role "Designer" for team "RoleTeam"
        And The response code is 201
        When I search team members by role "Captain"
        Then The response code is 200
        And The team member list contains name "Pol"

    Scenario: Retrieve team members by role returns empty list when none match
        Given a team with name "EmptyRoleTeam" exists for team member management
        And I create a team member with name "Lia" birth date "2010-08-08" and role "Programmer" for team "EmptyRoleTeam"
        And The response code is 201
        When I search team members by role "Unknown"
        Then The response code is 200
        And The team member list is empty

    Scenario: Deleting a team member
        Given a team with name "DeleteTeam" exists for team member management
        And I create a team member with name "Eva" birth date "2010-09-09" and role "Researcher" for team "DeleteTeam"
        And The response code is 201
        When I delete the created team member
        Then The response code is 204
        When I retrieve the deleted team member by id
        Then The response code is 404
