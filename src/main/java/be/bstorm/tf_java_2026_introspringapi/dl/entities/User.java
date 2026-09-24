package be.bstorm.tf_java_2026_introspringapi.dl.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user_")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
@SoftDelete(columnName = "is_enable", strategy = SoftDeleteType.ACTIVE)
public class User implements UserDetails {

    @Getter
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Getter @Setter
    @Column
    private LocalDate birthday;

    @Getter @Setter
    @Column(nullable = false)
    private String password;

    @Getter @Setter
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    private Role role;

    @Getter
    @Column(nullable = false, name = "is_enable", insertable = false, updatable = false)
    private Boolean isEnable = true;

    public User(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, LocalDate birthday) {
        this(username, password);
        this.birthday = birthday;
    }

    public User(String username, String password, Role role) {
        this(username, password);
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(role.getName())
        );
    }

    @Override
    public boolean isEnabled() {
        return isEnable;
    }
}
