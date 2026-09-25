package be.bstorm.tf_java_2026_introspringapi.dl.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.io.Serializable;

/**
 * Représente un jeu en base de données.
 * Le @SoftDelete supprime logiquement via is_enable=false (pas de DELETE physique).
 * Cela permet de conserver l'historique tout en cachant les données logiquement supprimées.
 */
@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
@SoftDelete(columnName = "is_enable", strategy = SoftDeleteType.ACTIVE)
public class Game implements Serializable {

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

    @Getter @Setter
    @Column
    private String imageUrl;

    /**
     * Constructeur de commodité pour créer un jeu sans ID.
     * @param name nom du jeu
     * @param releaseYear année de sortie
     * @param price prix en cents
     */
    public Game(String name, int releaseYear, int price) {
        this.name = name;
        this.releaseYear = releaseYear;
        this.price = price;
    }
}
