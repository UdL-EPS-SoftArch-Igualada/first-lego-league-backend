Feature: Match referee assignment
	As a tournament organizer
	I want to assign referees to matches
	So that assignments are valid and conflict-free

	Background:
		Given the match assignment system is empty
		And There is a registered user with username "user" and password "password" and email "user@sample.app"
		And I login as "user" with password "password"

	Scenario: Assign referee to match successfully
		Given a scheduled match exists from "10:00" to "11:00"
		And a referee volunteer exists
		When I assign that referee to that match
		Then The response code is 200
		And assignment response status is "ASSIGNED"
		And the match is assigned to that referee

	Scenario: Match not found
		Given a referee volunteer exists
		When I assign referee id "1" to match id "99999"
		Then The response code is 404
		And assignment error code is "MATCH_NOT_FOUND"

	Scenario: Referee not found
		Given a scheduled match exists from "10:00" to "11:00"
		When I assign referee id "99999" to that match
		Then The response code is 404
		And assignment error code is "REFEREE_NOT_FOUND"

	Scenario: Invalid role
		Given a scheduled match exists from "10:00" to "11:00"
		And a floater volunteer exists
		When I assign that floater to that match
		Then The response code is 422
		And assignment error code is "INVALID_ROLE"

	Scenario: Match already has referee
		Given a referee volunteer exists
		And another referee volunteer exists
		And a scheduled match exists from "10:00" to "11:00" assigned to the first referee
		When I assign the second referee to that match
		Then The response code is 409
		And assignment error code is "MATCH_ALREADY_HAS_REFEREE"

	Scenario: Invalid match state
		Given a finished match exists from "10:00" to "11:00"
		And a referee volunteer exists
		When I assign that referee to that match
		Then The response code is 422
		And assignment error code is "INVALID_MATCH_STATE"

	Scenario: Availability conflict
		Given a referee volunteer exists
		And a scheduled match exists from "10:00" to "11:00" assigned to the referee
		And a scheduled match exists from "10:30" to "11:30"
		When I assign that referee to that match
		Then The response code is 409
		And assignment error code is "AVAILABILITY_CONFLICT"

	Scenario: Invalid ID format
		When I assign referee id "abc" to match id "xyz"
		Then The response code is 422
		And assignment error code is "INVALID_ID_FORMAT"

	Scenario: Batch assignment succeeds for a round
		Given a referee volunteer exists
		And another referee volunteer exists
		And a round with two scheduled matches exists from "10:00"-"11:00" and "11:00"-"12:00"
		When I assign referees in batch for that round
		Then The response code is 200
		And both batch matches are assigned to their referees

	Scenario: Batch assignment fails on intra-batch availability conflict and rolls back
		Given a referee volunteer exists
		And a round with overlapping scheduled matches exists from "10:00"-"11:00" and "10:30"-"11:30"
		When I assign the same referee in batch to both matches
		Then The response code is 409
		And assignment error code is "BATCH_ASSIGNMENT_FAILED"
		And batch assignment error cause is "AVAILABILITY_CONFLICT"
		And none of the batch matches should have a referee assigned

	Scenario: Batch assignment fails on invalid role and rolls back
		Given a referee volunteer exists
		And a floater volunteer exists
		And a round with two scheduled matches exists from "10:00"-"11:00" and "11:00"-"12:00"
		When I assign one referee and one floater in batch for that round
		Then The response code is 422
		And assignment error code is "BATCH_ASSIGNMENT_FAILED"
		And batch assignment error cause is "INVALID_ROLE"
		And none of the batch matches should have a referee assigned

	Scenario: Batch assignment fails when round does not exist
		Given a referee volunteer exists
		When I assign referees in batch for round id "99999"
		Then The response code is 404
		And assignment error code is "ROUND_NOT_FOUND"
