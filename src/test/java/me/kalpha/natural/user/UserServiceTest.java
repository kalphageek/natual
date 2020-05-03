package me.kalpha.natural.user;

import me.kalpha.natural.common.Description;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

public class UserServiceTest {

    @Description("Load existing user")
    @Test
    public void loadUserByUsername() {
        // Given
        var userRepository = Mockito.mock(UserRepository.class);
        var userService = new UserService();
        userService.userRepository = userRepository;
        String email = "keesun@email.com";
        String password = "pass";
        User user = User.builder()
                .email(email)
                .password(password)
                .roles(Set.of(UserRole.ADMIN, UserRole.USER))
                .build();
        Mockito.when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // When
        var userDetails = userService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(userDetails.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN").contains("ROLE_USER");
    }
//    @Test(expected = UsernameNotFoundException.class)
//    public void usernameNotfoundException() {
//        // Given
//        var userRepository = Mockito.mock(UserRepository.class);
//        var userService = new UserService();
//        userService.userRepository = userRepository;
//        Mockito.when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
//
//        // When
//        userService.loadUserByUsername("keesun@email.com");
//    }

    @Test
    public void usernameNotfoundException() {
        // Given
        var userRepository = Mockito.mock(UserRepository.class);
        var userService = new UserService();
        userService.userRepository = userRepository;
        Mockito.when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("keesun@email.com")
        );
        System.out.println(exception.getMessage());

        //Then
        //assertTrue(exception.getMessage().contains("UsernameNotFoundException"));
    }
}