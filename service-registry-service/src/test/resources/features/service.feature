Feature: Tests the serviceregistry Service using a REST client.
 
  Scenario: Save the Chenile Remote Service Definition first.
    Given that "tenant" equals "tenant0"
    And that "employee" equals "E1"
    And that "serviceId" equals "service1"
    And that "serviceVersion" equals "serviceVersion1"
    When I construct a REST request with header "x-chenile-tenant-id" and value "${tenant}"
    And I construct a REST request with header "x-chenile-eid" and value "${employee}"
    And I POST a REST request to URL "/serviceregistry" with payload
    """json
    {
      "serviceId": "${serviceId}",
      "serviceVersion": "${serviceVersion}",
      "moduleName": "module1",
      "operations": [
        {
          "name":  "op1",
          "description": "op1_description",
          "consumes": "JSON",
          "params": [
            {
              "name": "param1",
              "description": "param1_description",
              "paramClassName": "org.chenile.model.DummyModel",
              "type": "BODY"
            }
          ]
        }
      ]
	}
	"""
	Then success is true
    And store "$.payload.id" from response to "id"
    And the REST response key "serviceId" is "${serviceId}"
    And the REST response key "serviceVersion" is "${serviceVersion}"
    # And the REST response key "createdBy" is "${employee}"

  Scenario: Retrieve the saved Remote Service
    Given that "entity" equals "serviceregistry"
    And I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I GET a REST request to URL "/${entity}/${serviceId}/${serviceVersion}"
    Then success is true
    And the REST response key "id" is "${id}"
    And the REST response key "serviceId" is "${serviceId}"
    And the REST response key "serviceVersion" is "${serviceVersion}"

  Scenario: Save a serviceregistry using an ID that already is determined
  Given that "id" equals "123"
    And that "serviceId" equals "service2"
    And that "serviceVersion" equals "serviceVersion2"
    And I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I construct a REST request with header "x-chenile-eid" and value "E1"
    And I POST a REST request to URL "/serviceregistry" with payload
  """json
  {
      "serviceId": "${serviceId}",
      "id": "${id}",
      "serviceVersion": "${serviceVersion}",
      "moduleName": "module1",
      "operations": [
        {
          "name":  "op1",
          "description": "op1_description",
          "consumes": "JSON",
          "params": [
            {
              "name": "param1",
              "description": "param1_description",
              "paramClassName": "org.chenile.model.DummyModel",
              "type": "BODY"
            }
          ]
        }
      ]
	}
  """
    Then success is true
    And the REST response key "id" is "${id}"
    And the REST response key "serviceId" is "${serviceId}"
    And the REST response key "serviceVersion" is "${serviceVersion}"

  Scenario: Retrieve the saved serviceregistry
  Given that "entity" equals "serviceregistry"
    And I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I GET a REST request to URL "/${entity}/${serviceId}/${serviceVersion}"
    Then success is true
    And the REST response key "id" is "${id}"
    And the REST response key "serviceId" is "${serviceId}"
    And the REST response key "serviceVersion" is "${serviceVersion}"


    