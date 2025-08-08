package org.chenile.service.registry.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.base.exception.NotFoundException;
import org.chenile.base.response.GenericResponse;
import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@PropertySource("classpath:${chenile.properties:chenile.properties}")
public class ServiceRegistryClientImpl implements ServiceRegistryService {
    Logger logger = LoggerFactory.getLogger(getClass());
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
    @CachePut("xxx")
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
                logger.warn("Invalid status code returned from remote service registry. Code =  " + response.getStatusCode());
                return null;
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing payload to JSON", e);
        }
    }

    @Override
    public ChenileRemoteServiceDefinition retrieveByIdVersion(String serviceId, String serviceVersion) {
        ChenileRemoteServiceDefinition csrd = serviceRegistryCache.retrieve(serviceId, serviceVersion);
        if (csrd == null){
            csrd = retrieveFromRemote(serviceId,serviceVersion);
            if(csrd != null)
                serviceRegistryCache.store(csrd);
        }
        return csrd;
    }

    @Override
    public ChenileRemoteServiceDefinition retrieveById(String serviceId) {
        ChenileRemoteServiceDefinition csrd = serviceRegistryCache.retrieve(serviceId);
        if(csrd == null){
            csrd = retrieveFromRemote(serviceId);
            if(csrd != null)
                serviceRegistryCache.store(csrd);
        }
        return csrd;
    }

    private ChenileRemoteServiceDefinition retrieveFromRemote(String serviceId, String serviceVersion) {
        String url = chenileRemoteServiceRegistry + "/serviceregistry/" + serviceId;
        if (serviceVersion != null) url = url + "/" + serviceVersion ;
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
                logger.warn("Error status code from remote. Code = " + response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            logger.warn("Error status code from remote. ",e);
            return null;
        }
    }

    private ChenileRemoteServiceDefinition retrieveFromRemote(String serviceId){
        return retrieveFromRemote(serviceId,null);
    }
}
