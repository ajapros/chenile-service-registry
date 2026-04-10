package org.chenile.service.registry.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ChenileController(value = "serviceregistryService", serviceName = "_serviceregistryService_",
		healthCheckerName = "serviceregistryHealthChecker")
public class ServiceRegistryController extends ControllerSupport{
	Logger logger = LoggerFactory.getLogger(ServiceRegistryController.class);
    @PostMapping("/serviceregistry")
    public ResponseEntity<GenericResponse<ChenileRemoteServiceDefinition>> save(
        HttpServletRequest httpServletRequest,
        @RequestBody ChenileRemoteServiceDefinition entity){
        return process(httpServletRequest,entity);
        }

    @GetMapping("/serviceregistry/{id}/{version}")
    public ResponseEntity<GenericResponse<ChenileRemoteServiceDefinition>> retrieveByIdVersion(
        HttpServletRequest httpServletRequest,
        @PathVariable("id") String id,
        @PathVariable("version") String version){
        logger.info("id = " + id + " version = " + version);
        return process(httpServletRequest,id,version);
    }

    @GetMapping("/serviceregistry/{id}")
    public ResponseEntity<GenericResponse<ChenileRemoteServiceDefinition>> retrieveById(
            HttpServletRequest httpServletRequest,
            @PathVariable("id") String id){
        logger.info("id = " + id);
        return process(httpServletRequest,id);
    }
}
