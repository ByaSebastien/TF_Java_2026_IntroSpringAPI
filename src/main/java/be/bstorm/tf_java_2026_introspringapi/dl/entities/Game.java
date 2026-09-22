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

    @Getter @Setter
    @Column(nullable = false)
    private int releaseYear;

    @Getter @Setter
    @Column(nullable = false)
    private int price;

    public Game(String name, int releaseYear, int price) {
        this.name = name;
        this.releaseYear = releaseYear;
        this.price = price;
    }
}
