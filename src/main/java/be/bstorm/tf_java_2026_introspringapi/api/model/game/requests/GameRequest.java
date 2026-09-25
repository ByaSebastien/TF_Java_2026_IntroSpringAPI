package be.bstorm.tf_java_2026_introspringapi.api.model.game.requests;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Données envoyées lors de la création d'un Game.
 * Le serveur valide les contraintes puis persiste en BDD.
 * Les validations sont appliquées par Jakarta Validation.
 */
public record GameRequest(
        @NotBlank(message = "Game name is required") String name,
        @NotNull(message = "Release year is required") Integer releaseYear,
        @NotNull(message = "Price is required") Integer price
) {

    /**
     * Convertit ce DTO en entité Game pour la persistance.
     * @return Game sans ID (sera généré à l'insertion)
     */
    public Game toGame() {
        return new Game(
                name, releaseYear, price
        );
    }
}
