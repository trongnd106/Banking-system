package com.trongdev.banking_system.configuration;

import com.trongdev.banking_system.entity.User;
import com.trongdev.banking_system.enums.Role;
import com.trongdev.banking_system.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){
        log.info("Init application...");
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){
                var roles = new HashSet<String>();
                // todo: add role "ADMIN" for admin
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .build();

                userRepository.save(user);
                log.warn("Admin user has been created with default password: admin, please change it.");
            }
        };
    }
}
