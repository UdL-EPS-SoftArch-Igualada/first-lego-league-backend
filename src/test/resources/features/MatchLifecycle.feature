	Feature: Match Lifecycle

	Background:
		Given There is a registered user with username "referee", password "password", email "referee@test.com" and roles "ROLE_USER,ROLE_REFEREE"
		And There is a match in state "SCHEDULED"

	Scenario: Admin transitions match from SCHEDULED to IN_PROGRESS
		Given I login as "admin" with password "password"
		When I change the match state to "IN_PROGRESS"
		Then The response code is 200
		And The match transition response has previous state "SCHEDULED" and new state "IN_PROGRESS"

	Scenario: Referee transitions match from SCHEDULED to IN_PROGRESS
		Given I login as "referee" with password "password"
		When I change the match state to "IN_PROGRESS"
		Then The response code is 200
		And The match transition response has previous state "SCHEDULED" and new state "IN_PROGRESS"

	Scenario: Invalid transition from SCHEDULED to FINISHED returns 400
		Given I login as "admin" with password "password"
		When I change the match state to "FINISHED"
		Then The response code is 400
		And The response has error "INVALID_MATCH_STATE_TRANSITION"

	Scenario: Unauthenticated user cannot transition match state
		Given I'm not logged in
		When I change the match state to "IN_PROGRESS"
		Then The response code is 401

	Scenario: Transition match that does not exist returns 404
		Given I login as "admin" with password "password"
		When I change match with id 99999 state to "IN_PROGRESS"
		Then The response code is 404
		And The response has error "MATCH_NOT_FOUND"

	Scenario: Non-admin non-referee authenticated user cannot transition match state
		Given There is a registered user with username "coach", password "password", email "coach@test.com" and roles "ROLE_USER"
		And I login as "coach" with password "password"
		When I change the match state to "IN_PROGRESS"
		Then The response code is 403