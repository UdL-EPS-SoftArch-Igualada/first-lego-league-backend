Feature: Match table assignment
	As a tournament organizer
	I want to assign matches to competition tables
	So that schedules remain conflict-free

	Background:
		Given the match table assignment system is empty
		And There is a registered user with username "user" and password "password" and email "user@sample.app"
		And I login as "user" with password "password"

	Scenario: Valid assignment
		Given a scheduled match without table exists from "11:00" to "11:20"
		And a competition table with identifier "Table-1" exists
		When I assign that match to table identifier "Table-1"
		Then The response code is 200
		And the assigned table identifier is "Table-1"

	Scenario: Table not found
		Given a scheduled match without table exists from "11:00" to "11:20"
		When I assign that match to table identifier "Table-404"
		Then The response code is 404

	Scenario: Overlap conflict
		Given a competition table with identifier "Table-1" exists
		And a scheduled match assigned to table "Table-1" exists from "11:10" to "11:30"
		And a scheduled match without table exists from "11:00" to "11:20"
		When I assign that match to table identifier "Table-1"
		Then The response code is 409

	Scenario: Match not found
		Given a competition table with identifier "Table-1" exists
		When I assign non existing match id "999999" to table identifier "Table-1"
		Then The response code is 404

	Scenario: Reassign match to another table
		Given a competition table with identifier "Table-1" exists
		And a competition table with identifier "Table-2" exists
		And a target scheduled match assigned to table "Table-1" exists from "11:00" to "11:20"
		When I assign that match to table identifier "Table-2"
		Then The response code is 200
		And the assigned table identifier is "Table-2"

	Scenario: Invalid match schedule
		Given a scheduled match without table exists from "11:20" to "11:00"
		And a competition table with identifier "Table-1" exists
		When I assign that match to table identifier "Table-1"
		Then The response code is 422
