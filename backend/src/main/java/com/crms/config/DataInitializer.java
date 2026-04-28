package com.crms.config;

import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("No users found, creating default admin user...");
            
            User admin = User.builder()
                    .username("admin")
                    .email("admin@crms.local")
                    .password(passwordEncoder.encode("Admin123!"))
                    .firstName("System")
                    .lastName("Administrator")
                    .enabled(true)
                    .mustChangePassword(true)
                    .failedLoginAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            admin.addRole(Role.ROLE_ADMIN);
            admin.addRole(Role.ROLE_IT_ADMIN);
            
            userRepository.save(admin);
            log.info("Default admin user created successfully: admin@crms.local / Admin123!");
            
            // Create additional default users
            createDefaultUser("ops_director", "ops@crms.local", "Operations", "Director", 
                    passwordEncoder.encode("OpsDirector123!"), Role.ROLE_OPS_DIRECTOR);
            
            createDefaultUser("contracts_mgr", "contracts@crms.local", "Contracts", "Manager",
                    passwordEncoder.encode("ContractsMgr123!"), Role.ROLE_CONTRACTS_MANAGER);
            
            createDefaultUser("qs", "qs@crms.local", "Quantity", "Surveyor",
                    passwordEncoder.encode("QS123!"), Role.ROLE_QS);
            
            createDefaultUser("site_agent", "site@crms.local", "Site", "Agent",
                    passwordEncoder.encode("SiteAgent123!"), Role.ROLE_SITE_AGENT);
            
            createDefaultUser("engineer", "engineer@crms.local", "Site", "Engineer",
                    passwordEncoder.encode("Engineer123!"), Role.ROLE_ENGINEER);
            
            log.info("Default users created successfully");
        } else {
            log.info("Users already exist, skipping default user creation");
        }
    }
    
    private void createDefaultUser(String username, String email, String firstName, 
                                   String lastName, String password, Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .firstName(firstName)
                    .lastName(lastName)
                    .enabled(true)
                    .mustChangePassword(false)
                    .failedLoginAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            user.addRole(role);
            userRepository.save(user);
            log.info("Created default user: {}", username);
        }
    }
}
