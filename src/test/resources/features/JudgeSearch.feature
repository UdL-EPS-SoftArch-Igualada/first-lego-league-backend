Feature: Judge search by name
  As an organizer
  I want to search judges by name
  So I can find them quickly in large lists

  Scenario: Search returns matching judges (200)
    Given judges exist with names "John Smith" and "Alice Brown"
    When I search judges by name containing "john"
    Then The response code is 200
    And the judges search response should contain 1 result
    And the judges search response should include judge named "John Smith"

  Scenario: Search returns empty list when no match (200)
    Given judges exist with names "Mark White" and "Anna Green"
    When I search judges by name containing "NoJudgeWillMatchThisToken"
    Then The response code is 200
    And the judges search response should contain 0 results
