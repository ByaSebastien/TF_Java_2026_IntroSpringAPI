package be.bstorm.tf_java_2026_introspringapi.dl.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente un rôle d'accès en base de données.
 * Utilisé pour contrôler les droits via Spring Security (ex: USER, ADMIN).
 */
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

    /**
     * Constructeur de commodité.
     * @param name nom du rôle (ex: USER, ADMIN)
     */
    public Role(String name) {
        this.name = name;
    }
}
