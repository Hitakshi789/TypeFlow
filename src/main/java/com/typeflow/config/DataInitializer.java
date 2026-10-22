package com.typeflow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataInitializer maintains database cleanliness.
 * No fabricated competitors, fake typing attempts, or placeholder leaderboard scores are seeded.
 * Only genuine tests completed by registered users populate the leaderboard.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // No automatic fake users or fake typing scores.
        // Leaderboard reflects only genuine user-completed tests.
    }
}
