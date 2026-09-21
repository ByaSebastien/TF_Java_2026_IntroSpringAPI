package be.bstorm.tf_java_2026_introspringapi.api.model.game.responses;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;

public record GameResponse(
        Integer id,
        String name
) {

    public static GameResponse fromGame(Game game) {
        return new GameResponse(
                game.getId(),
                game.getName()
        );
    }
}
