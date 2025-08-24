Feature: Tests if a Local Service (such as ServiceRegistry Service) is present
  in the service registry. This tests start up capability.
 
  Scenario: Retrieve the Chenile Remote Service Definition first.
    Given that "tenant" equals "tenant0"
    And that "entity" equals "serviceregistry"
    And that "employee" equals "E1"
    And that "serviceId" equals "serviceregistryService"
    And that "serviceVersion" equals "unittest"
    When I construct a REST request with header "x-chenile-tenant-id" and value "${tenant}"
    And I construct a REST request with header "x-chenile-eid" and value "${employee}"
    And I GET a REST request to URL "/${entity}/${serviceId}/${serviceVersion}"
    Then success is true
    And the REST response key "serviceId" is "${serviceId}"
    And the REST response key "serviceVersion" is "${serviceVersion}"

  Scenario: Retrieve the Chenile Remote Service Definition first without the version
    Given that "tenant" equals "tenant0"
    And that "entity" equals "serviceregistry"
    And that "employee" equals "E1"
    And that "serviceId" equals "serviceregistryService"
    And that "serviceVersion" equals "unittest"
    When I construct a REST request with header "x-chenile-tenant-id" and value "${tenant}"
    And I construct a REST request with header "x-chenile-eid" and value "${employee}"
    And I GET a REST request to URL "/${entity}/${serviceId}"
    Then success is true
    And the REST response key "serviceId" is "${serviceId}"
    And the REST response key "serviceVersion" is "${serviceVersion}"
    And the REST response key "operations[0].outputAsStringReference" is "org.chenile.service.registry.model.ChenileRemoteServiceDefinition"
    And the REST response key "operations[0].output" is "org.chenile.service.registry.model.ChenileRemoteServiceDefinition"

