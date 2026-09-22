package be.bstorm.tf_java_2026_introspringapi.api.model.game.requests;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GameRequest(
        @NotBlank String name,
        @NotNull Integer releaseYear,
        @NotNull Integer price
) {

    public Game toGame() {
        return new Game(
                name, releaseYear, price
        );
    }
}
