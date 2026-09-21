package be.bstorm.tf_java_2026_introspringapi.dl.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public class Role {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
