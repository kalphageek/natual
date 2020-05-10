package me.kalpha.natural.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

public class UserServiceTest {

    @DisplayName("Load existing user")
    @Test
    public void loadUserByUsername() {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserService userService = new UserService();
        userService.userRepository = userRepository;
        String email = "keesun@email.com";
        String password = "pass";
        Set<UserRole> h = new HashSet<>(Arrays.asList(UserRole.ADMIN, UserRole.USER));
        User user = User.builder()
                .email(email)
                .password(password)
                .roles(h)
                .build();
        Mockito.when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userService.loadUserByUsername(email);

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
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserService userService = new UserService();
        userService.userRepository = userRepository;
        Mockito.when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("admin@email.com")
        );

        //Then
        //assertTrue(exception.getMessage().contains("UsernameNotFoundException"));
    }
}