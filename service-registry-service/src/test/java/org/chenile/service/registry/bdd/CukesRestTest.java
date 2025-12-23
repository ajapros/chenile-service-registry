package org.chenile.service.registry.bdd;

import io.cucumber.junit.CucumberOptions;
import org.springframework.test.context.ActiveProfiles;


@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "org.chenile.service.registry.bdd",
                "org.chenile.cucumber.rest"
        },
        plugin = {"pretty"}
)
@ActiveProfiles("unittest")
public class CukesRestTest {

}
