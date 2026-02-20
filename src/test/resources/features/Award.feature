Feature: Manage Awards
  As a developer
  I want to verify that the awards repository is exposed correctly

  Scenario: Awards endpoint is working
    Given I'm not logged in
    When I request the awards list
    Then The response code is 200