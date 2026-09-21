package be.bstorm.tf_java_2026_introspringapi.dl.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public class Game {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(nullable = false, length = 50)
    private String name;

    public Game(String name) {
        this.name = name;
    }
}
