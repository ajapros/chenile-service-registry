package org.chenile.service.registry.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ChenileController(value = "serviceregistryService", serviceName = "_serviceregistryService_",
		healthCheckerName = "serviceregistryHealthChecker")
public class ServiceRegistryController extends ControllerSupport{
	
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
        System.out.println("id = " + id + " version = " + version);
        return process(httpServletRequest,id,version);
    }

    @GetMapping("/serviceregistry/{id}")
    public ResponseEntity<GenericResponse<ChenileRemoteServiceDefinition>> retrieveById(
            HttpServletRequest httpServletRequest,
            @PathVariable("id") String id){
        System.out.println("id = " + id);
        return process(httpServletRequest,id);
    }
}
