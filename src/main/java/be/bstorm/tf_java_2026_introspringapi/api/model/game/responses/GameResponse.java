package be.bstorm.tf_java_2026_introspringapi.api.model.game.responses;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;

/**
 * Game sérialisé pour la réponse HTTP.
 * Limite les infos retournées au client (ex: pas d'imageUrl en REST).
 * Peut être utilisé par la pagination ou les affichages détail/liste.
 */
public record GameResponse(
        Integer id,
        String name
) {

    /**
     * Convertit une entité Game en DTO de réponse.
     * @param game entité Game de la BDD
     * @return GameResponse sans infos sensibles
     */
    public static GameResponse fromGame(Game game) {
        return new GameResponse(
                game.getId(),
                game.getName()
        );
    }
}
