@env=go_client,scala_client
Feature: Create a pop LAO

  Background:

    # This feature will be called to test LAO creation
    # Call read(...) makes this feature and the called feature share the same scope
    # Meaning they share def variables, configurations ...
    # Especially JS functions defined in the called features can be directly used here thanks to Karate shared scopes
    * call read('classpath:be/utils/server.feature')
    * call read('classpath:be/mockClient.feature')
    * call read('classpath:be/constants.feature')
    * def organizer = call createMockClient
    * def validLao = organizer.createValidLao()

  Scenario: Create Lao request with empty lao name should fail with an error response
    Given def lao = validLao.setName('')
    And def badLaoReq =
      """
        {
          "object": "lao",
          "action": "create",
          "id": "#(lao.id)",
          "name": "#(lao.name)",
          "creation": "#(lao.creation)",
          "organizer": "#(lao.organizerPk)",
          "witnesses": "#(lao.witnesses)"
        }
      """
    When organizer.publish(badLaoReq, rootChannel)
    And json answer = organizer.getBackendResponse(badLaoReq)
    Then match answer contains INVALID_MESSAGE_FIELD
    And match organizer.receiveNoMoreResponses() == true

  Scenario: Create Lao request with negative creation time should fail with an error response
    Given def lao = validLao.setCreation(-1)
    And def badLaoReq =
      """
        {
          "object": "lao",
          "action": "create",
          "id": "#(lao.creation)",
          "name": "#(lao.name)",
          "creation": "#(lao.creation)",
          "organizer": "#(lao.organizerPk)",
          "witnesses": "#(lao.witnesses)"
        }
      """
    When organizer.publish(badLaoReq, rootChannel)
    And json answer = organizer.getBackendResponse(badLaoReq)
    Then match answer contains INVALID_MESSAGE_FIELD
    And match organizer.receiveNoMoreResponses() == true

  Scenario: Create Lao request with invalid id hash should fail with an error response
    Given def badLaoReq =
      """
        {
          "object": "lao",
          "action": "create",
          "id": '#(random.generateLaoId())',
          "name": "#(validLao.name)",
          "creation": "#(validLao.creation)",
          "organizer": "#(validLao.organizerPk)",
          "witnesses": "#(lao.witnesses)"
        }
      """
    When organizer.publish(badLaoReq, rootChannel)
    And json answer = organizer.getBackendResponse(badLaoReq)
    Then match answer contains INVALID_MESSAGE_FIELD
    And match organizer.receiveNoMoreResponses() == true

  Scenario: Valid Create Lao request should succeed
    Given def laoCreateRequest =
      """
        {
          "object": "lao",
          "action": "create",
          "id": '#(validLao.id)',
          "name": '#(validLao.name)',
          "creation": '#(validLao.creation)',
          "organizer": '#(validLao.organizerPk)',
          "witnesses": "#(validLao.witnesses)"
        }
      """
    When organizer.publish(laoCreateRequest, rootChannel)
    And json answer = organizer.getBackendResponse(laoCreateRequest)
    Then match answer contains VALID_MESSAGE
    And match organizer.receiveNoMoreResponses() == true

  Scenario: Create Lao request with invalid signature should fail
    Given def laoCreateRequest =
      """
        {
          "object": "lao",
          "action": "create",
          "id": '#(validLao.id)',
          "name": '#(validLao.name)',
          "creation": '#(validLao.creation)',
          "organizer": '#(validLao.organizerPk)',
          "witnesses": "#(lao.witnesses)"
        }
      """
    When organizer.useWrongSignature()
    And organizer.publish(laoCreateRequest, rootChannel)
    And json answer = organizer.getBackendResponse(laoCreateRequest)
    Then match answer contains INVALID_MESSAGE_FIELD
    And match organizer.receiveNoMoreResponses() == true

  Scenario: Create Lao request with public key different from the sender public key should fail
    Given def notOrganizer = call createMockClient
    And def laoCreateRequest =
      """
        {
          "object": "lao",
          "action": "create",
          "id": '#(validLao.id)',
          "name": '#(validLao.name)',
          "creation": '#(validLao.creation)',
          "organizer": '#(validLao.organizerPk)',
          "witnesses": "#(lao.witnesses)"
        }
      """

    When notOrganizer.publish(laoCreateRequest, rootChannel)
    And json answer = notOrganizer.getBackendResponse(laoCreateRequest)
    Then match answer contains ACCESS_DENIED
    And match notOrganizer.receiveNoMoreResponses() == true
