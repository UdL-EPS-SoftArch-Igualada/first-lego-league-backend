Feature: Edition Volunteers Overview

  Scenario: Authenticated user retrieves volunteers grouped by type for one edition
    Given I login as "demo" with password "password"
    And an edition volunteer overview dataset exists
    When I request volunteers grouped by type for the target edition
    Then The response code is 200
    And the volunteer overview contains 1 referee 1 judge and 1 floater
    And the volunteer overview does not include volunteers from other editions

  Scenario: Authenticated user retrieves empty grouped lists for an edition without volunteers
    Given I login as "demo" with password "password"
    And an empty edition exists for volunteer overview
    When I request volunteers grouped by type for the target edition
    Then The response code is 200
    And the volunteer overview contains 0 referee 0 judge and 0 floater

  Scenario: Anonymous user cannot retrieve edition volunteers
    Given I'm not logged in
    And an empty edition exists for volunteer overview
    When I request volunteers grouped by type for the target edition
    Then The response code is 401

  Scenario: Retrieving volunteers for a non existing edition returns not found
    Given I login as "demo" with password "password"
    When I request volunteers grouped by type for edition id 999999
    Then The response code is 404
    And the volunteer overview error is "EDITION_NOT_FOUND"
