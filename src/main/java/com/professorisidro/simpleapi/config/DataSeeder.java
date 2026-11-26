package com.professorisidro.simpleapi.config;

import com.professorisidro.simpleapi.user.Role;
import com.professorisidro.simpleapi.user.User;
import com.professorisidro.simpleapi.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Seeds the default test user (isidro/isidro) on startup if it does not exist yet. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("isidro")) {
            User user = new User(
                    "isidro",
                    passwordEncoder.encode("isidro"),
                    Role.ROLE_ADMIN,
                    true);
            userRepository.save(user);
        }
    }
}
