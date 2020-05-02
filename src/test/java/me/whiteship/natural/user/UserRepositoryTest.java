package me.whiteship.natural.user;

import me.whiteship.natural.common.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        userRepository.deleteAll();
    }

    @Description("Save new user and check generated id")
    @Test
    public void saveUser() {
        // When
        User newUser = userRepository.save(user());


        // Then
        assertThat(newUser.getId()).isNotNull();
        assertThat(userRepository.findAll().size()).isEqualTo(1);
    }

    private User user() {
        return User.builder()
                    .email("keesun@email.com")
                    .password("password")
                    .roles(Set.of(UserRole.ADMIN, UserRole.USER))
                    .build();
    }

    @Description("Find existing use by email")
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