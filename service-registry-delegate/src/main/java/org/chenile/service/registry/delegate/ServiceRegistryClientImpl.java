package org.chenile.service.registry.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.base.exception.NotFoundException;
import org.chenile.base.response.GenericResponse;
import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@PropertySource("classpath:${chenile.properties:chenile.properties}")
public class ServiceRegistryClientImpl implements ServiceRegistryService {

    @Value("${chenile.remote.service.registry:}")
    private String chenileRemoteServiceRegistry;
    private final RestTemplate restTemplate;
    ObjectMapper objectMapper = new ObjectMapper();
    /**
     * The cache here acts like a near cache.
     */
    @Autowired
    ServiceRegistryCache serviceRegistryCache;

    public ServiceRegistryClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    @Override
    public ChenileRemoteServiceDefinition save(ChenileRemoteServiceDefinition serviceDefinition) {
        serviceDefinition = writeToRemote(serviceDefinition);
        serviceRegistryCache.store(serviceDefinition);
        return serviceDefinition;
    }



    private ChenileRemoteServiceDefinition writeToRemote(ChenileRemoteServiceDefinition serviceDefinition) {
        String url = chenileRemoteServiceRegistry + "/serviceregistry";
        HttpHeaders headers = headers();
        try {
            String jsonPayload = objectMapper.writeValueAsString(serviceDefinition);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<GenericResponse<ChenileRemoteServiceDefinition>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<GenericResponse<ChenileRemoteServiceDefinition>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GenericResponse<ChenileRemoteServiceDefinition> responseBody = response.getBody();
                return responseBody.getData();
            } else {
                throw new RuntimeException("Master with error code "+response.getStatusCode());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing payload to JSON", e);
        }
    }

    @Override
    public ChenileRemoteServiceDefinition retrieve(String serviceId, String serviceVersion) {
        ChenileRemoteServiceDefinition csrd = serviceRegistryCache.retrieve(serviceId, serviceVersion);
        if (csrd == null){
            csrd = retrieveFromRemote(serviceId,serviceVersion);
            if (csrd == null){
                throw new NotFoundException(1501,"Unable to retrieve service with service ID = " +
                        serviceId + " and version = " + serviceVersion);
            }
            serviceRegistryCache.store(csrd);
        }
        return csrd;
    }

    private ChenileRemoteServiceDefinition retrieveFromRemote(String serviceId, String serviceVersion) {
        String url = chenileRemoteServiceRegistry + "/serviceregistry/" + serviceId + "/" + serviceVersion ;
        HttpHeaders headers = headers();
        try {
            HttpEntity<Object> entity = new HttpEntity<>(headers);
            ResponseEntity<GenericResponse<ChenileRemoteServiceDefinition>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<GenericResponse<ChenileRemoteServiceDefinition>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GenericResponse<ChenileRemoteServiceDefinition> responseBody = response.getBody();
                return responseBody.getData();
            } else {
                throw new RuntimeException("Master with error code "+response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error in Getting results from remote service registry", e);
        }
    }
}
