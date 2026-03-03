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

  Scenario: Direct POST to matchResults is disabled
    Given There is a registered user with username "score-user-3" and password "password" and email "score-user-3@sample.app"
    And I login as "score-user-3" with password "password"
    And There is a finished match ready for score registration
    When I try to create a match result directly with score 10
    Then The response code is 405
