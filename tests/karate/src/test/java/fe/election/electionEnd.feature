Feature:

  Scenario: Casting a vote sends the correct message to the backend
    # Do all the steps up until (and including) opening an election
    Given call read('classpath:fe/utils/simpleScenarios.feature@name=cast_vote')

    # Setup an election
    When def election_page_object = 'classpath:fe/utils/<env>.feature@name=end_election'
    * replace election_page_object.env = karate.env
    And call read(election_page_object)

    # Retrieving sent messages
    * json election_json = buffer.takeTimeout(timeout)
    * string election_string = election_json

    # General message verification
    Then match election_json contains deep { method: 'publish' }
    * match messageVerification.verifyMessageIdField(election_string) == true
    And match messageVerification.verifyMessageSignature(election_string) == true

    # Election specific verification - ballot index is 1 (second option)
    And match verificationUtils.getObject(election_string) == constants.ELECTION
    * match verificationUtils.getAction(election_string) == constants.END

    And match backend.receiveNoMoreResponses() == true