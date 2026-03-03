Feature: Assign a Judge to a ProjectRoom

	# --- CASOS D'ÈXIT ---

	Scenario: Successful assignment of a Manager
		Given a project room "ROOM_1" exists
		And a judge "10" exists
		When I request to assign judge "10" to room "ROOM_1" with isManager true
		Then the response status should be 200
		And the response role should be "MANAGER"

	Scenario: Successful assignment of a Panelist
		Given a project room "ROOM_2" exists
		And a judge "20" exists
		When I request to assign judge "20" to room "ROOM_2" with isManager false
		Then the response status should be 200
		And the response role should be "PANELIST"

	# --- CASOS D'ERROR ---

	Scenario: Validation fails when room does not exist
		Given a judge "30" exists
		When I request to assign judge "30" to room "NON_EXISTENT_ROOM" with isManager true
		Then the response status should be 400
		And the response error should be "ROOM_NOT_FOUND"

	Scenario: Validation fails when judge does not exist
		Given a project room "ROOM_3" exists
		When I request to assign judge "999" to room "ROOM_3" with isManager false
		Then the response status should be 400
		And the response error should be "JUDGE_NOT_FOUND"

	Scenario: Validation fails when room already has a manager
		Given a project room "ROOM_4" exists
		And the room "ROOM_4" already has a manager
		And a judge "40" exists
		When I request to assign judge "40" to room "ROOM_4" with isManager true
		Then the response status should be 400
		And the response error should be "ROOM_ALREADY_HAS_MANAGER"

	Scenario: Validation fails when maximum panelists are reached
		Given a project room "ROOM_5" exists
		And the room "ROOM_5" already has 3 panelists
		And a judge "50" exists
		When I request to assign judge "50" to room "ROOM_5" with isManager false
		Then the response status should be 400
		And the response error should be "MAX_PANELISTS_REACHED"

	Scenario: Validation fails when judge is already assigned to the room
		Given a project room "ROOM_6" exists
		And a judge "60" exists
		And judge "60" is already assigned to room "ROOM_6"
		When I request to assign judge "60" to room "ROOM_6" with isManager false
		Then the response status should be 400
		And the response error should be "JUDGE_ALREADY_ASSIGNED"