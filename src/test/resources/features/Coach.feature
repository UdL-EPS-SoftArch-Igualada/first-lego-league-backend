Feature: Manage Coaches
  In order to manage the FIRST Lego League competition
  As an administrator
  I want to be able to create and retrieve coaches

  Background:
    # first we create the user at the bbdd and then we login
    Given There is a registered user with username "admin" and password "admin" and email "admin@fll.udl.cat"
    And I login as "admin" with password "admin"

  Scenario: Create a new coach
    # This sentence must match the regex in your CoachStepDefs.java
    When I create a new coach with name "Sergio Gomez", email "sergio@example.com" and phone "123456789"
    Then The response code is 201
    And It has been created a coach with name "Sergio Gomez" and email "sergio@example.com"