package me.kalpha.natural.init;

import me.kalpha.natural.common.AppSecurityProperties;
import me.kalpha.natural.user.User;
import me.kalpha.natural.user.UserRole;
import me.kalpha.natural.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DefaultUsersInitializer implements ApplicationRunner {

    @Autowired
    UserService userService;

    @Autowired
    AppSecurityProperties appSecurityProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Set<UserRole> h = new HashSet<>(Arrays.asList(UserRole.USER, UserRole.ADMIN));
        User admin = User.builder()
                .email(appSecurityProperties.getAdminUsername())
                .password(appSecurityProperties.getAdminPassword())
                .roles(h)
                .build();

        userService.createUser(admin);

        Set<UserRole> h2 = new HashSet<>(Arrays.asList(UserRole.USER));
        User user = User.builder()
                .email(appSecurityProperties.getUserUsername())
                .password(appSecurityProperties.getUserPassword())
                .roles(h2)
                .build();

        userService.createUser(user);
    }

}
