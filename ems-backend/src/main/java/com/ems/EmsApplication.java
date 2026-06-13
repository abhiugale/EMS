package com.ems;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(com.ems.modules.user.repository.UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("==================================================");
            String hash = passwordEncoder.encode("Admin@123");
            System.out.println("DYNAMICALY GENERATED HASH FOR Admin@123: " + hash);
            System.out.println("==================================================");

            userRepository.findByEmail("admin@ems.local").ifPresent(u -> {
                u.setPasswordHash(hash);
                userRepository.save(u);
                System.out.println("RESET SUCCESS: admin@ems.local password set to Admin@123");
            });
            userRepository.findByEmail("manager@ems.local").ifPresent(u -> {
                u.setPasswordHash(hash);
                userRepository.save(u);
                System.out.println("RESET SUCCESS: manager@ems.local password set to Admin@123");
            });
            userRepository.findByEmail("supervisor@ems.local").ifPresent(u -> {
                u.setPasswordHash(hash);
                userRepository.save(u);
                System.out.println("RESET SUCCESS: supervisor@ems.local password set to Admin@123");
            });
            userRepository.findByEmail("viewer@ems.local").ifPresent(u -> {
                u.setPasswordHash(hash);
                userRepository.save(u);
                System.out.println("RESET SUCCESS: viewer@ems.local password set to Admin@123");
            });
        };
    }
}
