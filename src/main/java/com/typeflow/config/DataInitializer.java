package com.typeflow.config;

import com.typeflow.entity.KeyError;
import com.typeflow.entity.TypingResult;
import com.typeflow.entity.User;
import com.typeflow.repository.UserRepository;
import com.typeflow.repository.TypingResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TypingResultRepository typingResultRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           TypingResultRepository typingResultRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.typingResultRepository = typingResultRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            // Seed a sample demonstration user for viva and presentation
            User demo = new User(
                    "demouser",
                    "demo@typeflow.com",
                    passwordEncoder.encode("password123"),
                    "D"
            );
            userRepository.save(demo);

            User alex = new User(
                    "alex_typist",
                    "alex@typeflow.com",
                    passwordEncoder.encode("password123"),
                    "A"
            );
            userRepository.save(alex);

            // Seed qualifying scores within the 24-hour window
            LocalDateTime now = LocalDateTime.now();

            TypingResult r1 = new TypingResult();
            r1.setUser(demo);
            r1.setDuration(60);
            r1.setRawWpm(88.0);
            r1.setNetWpm(84.4);
            r1.setAccuracy(96.0);
            r1.setCorrectChars(422);
            r1.setIncorrectChars(18);
            r1.setTotalChars(440);
            r1.setBackspaces(12);
            r1.setCompletionTimestamp(now.minusHours(2));
            r1.setExpirationTimestamp(now.minusHours(2).plusHours(24));
            r1.addKeyError(new KeyError("e", "r", "CASE_A_SUBSTITUTION", 3));
            r1.addKeyError(new KeyError("t", "y", "CASE_A_SUBSTITUTION", 2));
            typingResultRepository.save(r1);

            TypingResult r2 = new TypingResult();
            r2.setUser(alex);
            r2.setDuration(60);
            r2.setRawWpm(105.0);
            r2.setNetWpm(101.2);
            r2.setAccuracy(97.5);
            r2.setCorrectChars(506);
            r2.setIncorrectChars(19);
            r2.setTotalChars(525);
            r2.setBackspaces(8);
            r2.setCompletionTimestamp(now.minusHours(5));
            r2.setExpirationTimestamp(now.minusHours(5).plusHours(24));
            r2.addKeyError(new KeyError("c", "v", "CASE_A_SUBSTITUTION", 2));
            typingResultRepository.save(r2);

            // Seed a score older than 24 hours to demonstrate 24-hour expiration filter
            TypingResult expired = new TypingResult();
            expired.setUser(demo);
            expired.setDuration(60);
            expired.setRawWpm(120.0);
            expired.setNetWpm(115.0);
            expired.setAccuracy(98.0);
            expired.setCorrectChars(575);
            expired.setIncorrectChars(25);
            expired.setTotalChars(600);
            expired.setBackspaces(5);
            expired.setCompletionTimestamp(now.minusHours(26)); // 26 hours ago!
            expired.setExpirationTimestamp(now.minusHours(26).plusHours(24)); // expired 2 hours ago!
            typingResultRepository.save(expired);
        }
    }
}
