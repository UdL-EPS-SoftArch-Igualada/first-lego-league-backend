Feature: Manage Awards
	As a developer
	I want to verify that the awards repository is exposed correctly

	Scenario: Awards endpoint is working
		Given I'm not logged in
		When I request the awards list
		Then The response code is 200

	Scenario: Create a valid Award and retrieve it
		Given I'm not logged in
		And The dependencies exist
		When I create an award with name "Best Innovation"
		Then The response code is 201
		When I request the awards list
		Then The response code is 200

	Scenario: Create an award missing required fields
		Given I'm not logged in
		And The dependencies exist
		When I create an award with no name
		Then The response code is 400
		And The error message is "must not be blank"

	Scenario: Search returns awards by partial winner name
		Given I'm not logged in
		And A team exists with name "Lego Stars" and an award "Best Innovation"
		When I search awards by winner name containing "Lego"
		Then The response code is 200
		And The award search response should contain 1 result
		And The award search response should include award named "Best Innovation"

	Scenario: Returns empty list when no match
		Given I'm not logged in
		And A team exists with name "Lego Stars" and an award "Best Innovation"
		When I search awards by winner name containing "Unknown"
		Then The response code is 200
		And The award search response should contain 0 results