Feature: Register Match Score
  As an authenticated user
  I want to register match scores through the custom endpoint

  Scenario: Register final score successfully
    Given There is a registered user with username "score-user" and password "password" and email "score-user@sample.app"
    And I login as "score-user" with password "password"
    And There is a finished match ready for score registration
    When I register a final score of 120 for team A and 95 for team B
    Then The response code is 200
    And The register response contains successful flags

  Scenario: Register final score with negative value
    Given There is a registered user with username "score-user-2" and password "password" and email "score-user-2@sample.app"
    And I login as "score-user-2" with password "password"
    And There is a finished match ready for score registration
    When I register a final score of -1 for team A and 95 for team B
    Then The response code is 400
    And The error message is "Score cannot be negative"

  Scenario: Register score for non existing match
    Given There is a registered user with username "score-user-4" and password "password" and email "score-user-4@sample.app"
    And I login as "score-user-4" with password "password"
    And There is a finished match ready for score registration
    When I register a score for a non existing match
    Then The response code is 404
    And The error message is "does not exist"

  Scenario: Register score for unfinished match
    Given There is a registered user with username "score-user-5" and password "password" and email "score-user-5@sample.app"
    And I login as "score-user-5" with password "password"
    And There is an unfinished match ready for score registration
    When I register a final score of 120 for team A and 95 for team B
    Then The response code is 400
    And The error message is "Match must be finished before registering the result"

  Scenario: Register score for match with invalid time range
    Given There is a registered user with username "score-user-6" and password "password" and email "score-user-6@sample.app"
    And I login as "score-user-6" with password "password"
    And There is a match with invalid time range ready for score registration
    When I register a final score of 120 for team A and 95 for team B
    Then The response code is 400
    And The error message is "Match end time cannot be before start time"

  Scenario: Register score when match result already exists
    Given There is a registered user with username "score-user-7" and password "password" and email "score-user-7@sample.app"
    And I login as "score-user-7" with password "password"
    And There is a finished match ready for score registration
    And There is already a registered score for that match
    When I register a final score of 120 for team A and 95 for team B
    Then The response code is 400
    And The error message is "A result has already been registered for this match"

  Scenario: Register score with mismatched teams
    Given There is a registered user with username "score-user-8" and password "password" and email "score-user-8@sample.app"
    And I login as "score-user-8" with password "password"
    And There is a finished match ready for score registration
    When I register a final score with mismatched teams
    Then The response code is 400
    And The error message is "Provided team IDs do not match the teams assigned to the match"

  Scenario: Register score using same team in both sides
    Given There is a registered user with username "score-user-9" and password "password" and email "score-user-9@sample.app"
    And I login as "score-user-9" with password "password"
    And There is a finished match ready for score registration
    When I register a final score using the same team for both sides
    Then The response code is 400
    And The error message is "A match result requires two different teams"

  Scenario: Register score with null score payload
    Given There is a registered user with username "score-user-10" and password "password" and email "score-user-10@sample.app"
    And I login as "score-user-10" with password "password"
    And There is a finished match ready for score registration
    When I register a final score with null score payload
    Then The response code is 400
    And The error message is "Invalid score payload"

  Scenario: Register score with invalid score format
    Given There is a registered user with username "score-user-11" and password "password" and email "score-user-11@sample.app"
    And I login as "score-user-11" with password "password"
    And There is a finished match ready for score registration
    When I register a final score with invalid score format
    Then The response code is 400
    And The error message is "Invalid score payload"

  Scenario: Direct POST to matchResults is disabled
    Given There is a registered user with username "score-user-3" and password "password" and email "score-user-3@sample.app"
    And I login as "score-user-3" with password "password"
    And There is a finished match ready for score registration
    When I try to create a match result directly with score 10
    Then The response code is 405
