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
    public ResponseEntity<GenericResponse<ChenileRemoteServiceDefinition>> retrieve(
    HttpServletRequest httpServletRequest,
    @PathVariable("id") String id,
    @PathVariable("version") String version){
    return process(httpServletRequest,id,version);
    }
}
