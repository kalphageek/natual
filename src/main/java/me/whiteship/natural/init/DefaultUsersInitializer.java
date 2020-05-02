package me.whiteship.natural.init;

import me.whiteship.natural.common.AppSecurityProperties;
import me.whiteship.natural.user.User;
import me.whiteship.natural.user.UserRole;
import me.whiteship.natural.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DefaultUsersInitializer implements ApplicationRunner {

    @Autowired
    UserService userService;

    @Autowired
    AppSecurityProperties appSecurityProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        User admin = User.builder()
                .email(appSecurityProperties.getAdminUsername())
                .password(appSecurityProperties.getAdminPassword())
                .roles(Set.of(UserRole.ADMIN, UserRole.USER))
                .build();

        userService.createUser(admin);

        User user = User.builder()
                .email(appSecurityProperties.getUserUsername())
                .password(appSecurityProperties.getUserPassword())
                .roles(Set.of(UserRole.ADMIN, UserRole.USER))
                .build();

        userService.createUser(user);
    }

}
