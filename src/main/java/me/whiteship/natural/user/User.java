package me.whiteship.natural.user;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Natural_User")
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id @GeneratedValue
    public Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

}
