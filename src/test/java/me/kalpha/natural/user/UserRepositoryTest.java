package me.kalpha.natural.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @DisplayName("Save new user and check generated id")
    @Test
    public void saveUser() {
        // When
        User newUser = userRepository.save(user());


        // Then
        assertThat(newUser.getId()).isNotNull();
        assertThat(userRepository.findAll().size()).isEqualTo(1);
    }

    private User user() {
        Set<UserRole> h = new HashSet<>(Arrays.asList(UserRole.ADMIN, UserRole.USER));
        return User.builder()
                    .email("keesun@email.com")
                    .password("password")
                    .roles(h)
                    .build();
    }

    @DisplayName("Find existing use by email")
    @Test
    public void findByEmail() {
        // Given
        User user = user();
        User existingUser = userRepository.save(user);

        // When
        Optional<User> byEmail = userRepository.findByEmailIgnoreCase(user.getEmail());

        // Then
        assertThat(byEmail).isNotEmpty();
        assertThat(byEmail).hasValue(existingUser);
    }

}