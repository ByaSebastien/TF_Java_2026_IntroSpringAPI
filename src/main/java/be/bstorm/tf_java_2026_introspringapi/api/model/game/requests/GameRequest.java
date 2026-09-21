package be.bstorm.tf_java_2026_introspringapi.api.model.game.requests;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import jakarta.validation.constraints.NotBlank;

public record GameRequest(
        @NotBlank String name
) {

    public Game toGame() {
        return new Game(
                name
        );
    }
}
