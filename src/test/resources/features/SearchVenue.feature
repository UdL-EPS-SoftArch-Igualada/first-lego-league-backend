Feature: Search Venue by partial name

  Background:
    Given I login as "user" with password "password"
    And the volunteer system is empty
    Given a venue exists with name "Sports Center Barcelona" and city "Barcelona"

  Scenario: Search returns venues with partial name match (case-insensitive)
    When I search for a venue by partial name "sport"
    Then the venue search API response status should be 200
    And the search response should contain a venue with name "Sports Center Barcelona"

  Scenario: Search returns empty list when no match
    When I search for a venue by partial name "Unknown"
    Then the venue search API response status should be 200
    And the search response should be empty