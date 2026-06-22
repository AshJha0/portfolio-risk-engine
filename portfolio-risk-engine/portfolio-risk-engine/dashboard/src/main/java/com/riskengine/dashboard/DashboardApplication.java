package com.riskengine.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the static risk dashboard. Serves {@code index.html} (and supporting
 * assets) from {@code src/main/resources/static}, which calls the rest-api module's
 * endpoints ({@code /risk/var}, {@code /risk/stress}) via fetch() to populate the UI.
 *
 * <p>Run alongside rest-api (default port 8080); the dashboard listens on port 8081
 * by default — see application.yml.</p>
 */
@SpringBootApplication
public class DashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }
}
