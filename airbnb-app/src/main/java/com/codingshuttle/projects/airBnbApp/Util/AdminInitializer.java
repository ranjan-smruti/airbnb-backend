package com.codingshuttle.projects.airBnbApp.Util;

import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.Entity.enums.AccountType;
import com.codingshuttle.projects.airBnbApp.Entity.enums.Roles;
import com.codingshuttle.projects.airBnbApp.Repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test") // avoid in test env
public class AdminInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name:ADMIN}") // default value
    private String adminName;

    //If multiple instances of app start simultaneously (say in Kubernetes), there is risk of creating duplicate admin users.
    @Transactional
    @PostConstruct
    public void initAdmin(){

        if (adminEmail == null || adminPassword == null) {
            throw new IllegalStateException("Admin credentials not configured!");
        }

        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        if(admin == null)
        {
            log.info("Admin user not present. Creating Admin user..");
            admin = new User();
            admin.setName("ADMIN");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(Roles.ADMIN));
            admin.setAccountType(AccountType.ADMIN);
            userRepository.save(admin);
        }
    }
}
