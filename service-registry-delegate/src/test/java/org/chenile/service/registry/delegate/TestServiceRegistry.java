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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringConfig.class,webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("unittest")
public class TestServiceRegistry {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8089);
   @Autowired
   ServiceRegistryService serviceRegistry;
      
    @Test public void test1Param() throws ClassNotFoundException {
		ChenileRemoteServiceDefinition sd = serviceRegistry.retrieveById("abc");
		assertEquals("abc",sd.serviceId);
		assertEquals("v8",sd.serviceVersion);
		assertEquals("m1",sd.moduleName);
		assertNotNull(sd.operations);
		assertEquals(1,sd.operations.size());
		ParameterizedTypeReference<List<String>> ptr = new ParameterizedTypeReference<List<String>>() {};
		assertEquals(ptr,sd.operations.get(0).outputAsParameterizedReference);
		assertEquals(List.class,Class.forName( sd.operations.get(0).output));
    }

	@Test public void test2Params() throws ClassNotFoundException {
		ChenileRemoteServiceDefinition sd = serviceRegistry.retrieveByIdVersion("abc","v1");
		assertEquals("abc",sd.serviceId);
		assertEquals("v1",sd.serviceVersion);
		assertEquals("m1",sd.moduleName);
		assertNotNull(sd.operations);
		assertEquals(1,sd.operations.size());
		ParameterizedTypeReference<Map<String,String>> ptr = new ParameterizedTypeReference<Map<String,String>>() {};
		assertEquals(ptr,sd.operations.get(0).outputAsParameterizedReference);
		assertEquals(Map.class,Class.forName(sd.operations.get(0).output));
	}

	@Test public void test2ParamsInvalidInput() {
		ChenileRemoteServiceDefinition sd = serviceRegistry.retrieveByIdVersion("abc","v2");
		assertNull(sd);
	}

}
