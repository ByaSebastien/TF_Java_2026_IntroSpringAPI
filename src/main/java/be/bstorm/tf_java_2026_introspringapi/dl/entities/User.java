package be.bstorm.tf_java_2026_introspringapi.dl.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user_")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public class User implements UserDetails {

    @Getter
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Getter @Setter
    @Column(nullable = false)
    private String password;

    @Getter @Setter
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(role.getName())
        );
    }
}
