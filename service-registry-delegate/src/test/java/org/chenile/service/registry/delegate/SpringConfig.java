package org.chenile.service.registry.delegate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@SpringBootApplication(scanBasePackages = { "org.chenile.configuration" ,"org.chenile.service.registry.configuration"})
@PropertySource("classpath:org/chenile/service/registry/delegate/TestChenileProxy.properties")
@PropertySource("classpath:application-fixedport.properties")
@ActiveProfiles("unittest")
public class SpringConfig extends SpringBootServletInitializer{
	public static void main(String[] args) {
		SpringApplication.run(SpringConfig.class, args);
	}

}

