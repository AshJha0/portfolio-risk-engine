package com.riskengine.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Risk Engine REST API.
 *
 * <p>Run with: {@code mvn -pl rest-api spring-boot:run} from the project root,
 * or {@code java -jar rest-api/target/rest-api-1.0.0-SNAPSHOT.jar} after a full build.
 * Listens on port 8080 by default (configurable in application.yml).</p>
 */
@SpringBootApplication
public class RiskEngineApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskEngineApiApplication.class, args);
    }
}
