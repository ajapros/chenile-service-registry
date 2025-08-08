package org.chenile.service.registry.delegate;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringConfig.class,webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("unittest")
public class TestServiceRegistry {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8089);
   @Autowired
   ServiceRegistryService serviceRegistry;
      
    @Test public void test1Param() {
		ChenileRemoteServiceDefinition sd = serviceRegistry.retrieveById("abc");
		assertEquals("abc",sd.serviceId);
		assertEquals("v1",sd.serviceVersion);
		assertEquals("m1",sd.moduleName);
    }

	@Test public void test2Params() {
		ChenileRemoteServiceDefinition sd = serviceRegistry.retrieveByIdVersion("abc","v1");
		assertEquals("abc",sd.serviceId);
		assertEquals("v1",sd.serviceVersion);
		assertEquals("m1",sd.moduleName);
	}

	@Test public void test2ParamsInvalidInput() {
		ChenileRemoteServiceDefinition sd = serviceRegistry.retrieveByIdVersion("abc","v2");
		assertNull(sd);
	}

}
