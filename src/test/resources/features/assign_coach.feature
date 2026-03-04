Feature: Assign coach to team

	Scenario: Successfully assign coach to team
		Given a team "TeamA" exists
		And a coach with id 1 exists
		When I assign coach 1 to team "TeamA"
		Then the assignment is successful

	Scenario: Team not found
		Given a coach with id 1 exists
		When I assign coach 1 to team "UnknownTeam"
		Then the error TEAM_NOT_FOUND is returned

	Scenario: Coach not found
		Given a team "TeamA" exists
		When I assign coach 99 to team "TeamA"
		Then the error COACH_NOT_FOUND is returned

	Scenario: Team already has maximum coaches
		Given a team "TeamA" exists
		And a coach with id 1 exists
		And a coach with id 2 exists
		And coach 1 is assigned to team "TeamA"
		And coach 2 is assigned to team "TeamA"
		And a coach with id 3 exists
		When I assign coach 3 to team "TeamA"
		Then the error MAX_COACHES_PER_TEAM_REACHED is returned

	Scenario: Coach already trains two teams
		Given a coach with id 1 exists
		And a team "TeamA" exists
		And a team "TeamB" exists
		And a team "TeamC" exists
		And coach 1 is assigned to team "TeamA"
		And coach 1 is assigned to team "TeamB"
		When I assign coach 1 to team "TeamC"
		Then the error MAX_TEAMS_PER_COACH_REACHED is returned

	Scenario: Coach already assigned to team
		Given a team "TeamA" exists
		And a coach with id 1 exists
		And coach 1 is assigned to team "TeamA"
		When I assign coach 1 to team "TeamA"
		Then the error COACH_ALREADY_ASSIGNED is returned