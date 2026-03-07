Feature: Validate match time consistency and prevent table time overlaps

	Background:
		Given I login as "admin" with password "admin"
		And a competition table "Table-01" exists
		And a valid match exists on "Table-01" from "10:00:00" to "11:00:00"

	Scenario: Invalid Time Range
		When I request to create a match on "Table-01" from "12:00:00" to "11:00:00"
		Then the match scheduling response status should be 422
		And the match scheduling response error should be "INVALID_TIME_RANGE"

	Scenario: Table Time Overlap (Creation)
		When I request to create a match on "Table-01" from "10:30:00" to "11:30:00"
		Then the match scheduling response status should be 409
		And the match scheduling response error should be "TABLE_TIME_OVERLAP"

	Scenario: Valid Match Creation
		When I request to create a match on "Table-01" from "12:00:00" to "13:00:00"
		Then the match scheduling response status should be 201