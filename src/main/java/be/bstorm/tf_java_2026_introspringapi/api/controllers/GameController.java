package be.bstorm.tf_java_2026_introspringapi.api.controllers;

import be.bstorm.tf_java_2026_introspringapi.api.model.game.requests.GameRequest;
import be.bstorm.tf_java_2026_introspringapi.api.model.game.responses.GameResponse;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.UserContext;
import be.bstorm.tf_java_2026_introspringapi.bll.services.GameService;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Endpoints REST pour la gestion des Games.
 * Expose les opérations CRUD avec support de pagination et recherche.
 * Les opérations sensibles (POST, PUT, DELETE) sont protégées selon leur contexte.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    /**
     * Récupère une page de Games avec filtres optionnels.
     * Accepte page, size et paramètres de recherche dynamiques.
     * @param page numéro de page (0-indexed, défaut 0)
     * @param size taille page (défaut 10)
     * @param params filtres additionnels (name, gt_releaseYear, etc.)
     * @return Page de GameResponse, 200 OK
     */
    @GetMapping
    public ResponseEntity<Page<GameResponse>> find(
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @RequestParam(name = "size", required = false, defaultValue = "10") int size,
        @RequestParam Map<String,String> params
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Game> games = gameService.find(params, pageable);
        Page<GameResponse> responsePage = games.map(GameResponse::fromGame);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Récupère un Game par son ID.
     * @param id identifiant du Game
     * @return GameResponse, 200 OK ou 404 si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> findById(
            @PathVariable Integer id
    ) {
        Game game = gameService.findById(id);

        GameResponse gameResponse = GameResponse.fromGame(game);

        return ResponseEntity.ok(gameResponse);
    }

    /**
     * Crée un nouveau Game avec upload image optionnel.
     * Supporte application/json + multipart/form-data.
     * @param gameRequest données du Game
     * @param image fichier image optionnel
     * @return 201 Created avec Location header
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> save(
            @Valid @RequestPart("gameRequest") GameRequest gameRequest,
            @RequestPart(name = "image", required = false) MultipartFile image
    ) {

        Game game = gameRequest.toGame();

        Game response = gameService.save(game, image);

        URI uri =  ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    /**
     * Modifie un Game existant.
     * Nécessite le rôle admin.
     * @param id identifiant du Game
     * @param gameRequest données mises à jour
     * @param image nouvelle image optionnelle
     * @return 204 No Content
     */
    @PreAuthorize("hasAuthority('admin')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void>  update(
            @PathVariable Integer id,
            @Valid @RequestPart(name = "gameRequest") GameRequest gameRequest,
            @RequestPart(name = "image", required = false) MultipartFile image
    ) {
        Game game = gameRequest.toGame();

        gameService.update(id, game, image);

        return ResponseEntity.noContent().build();
    }

    /**
     * Supprime un Game (soft-delete logique).
     * Nécessite le rôle admin.
     * @param id identifiant du Game
     * @return 204 No Content
     */
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id
    ){
        gameService.delete(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Ajoute/retire un Game de la wishlist (placeholder).
     * Nécessite d'être authentifié.
     * @param gameId identifiant du Game
     * @param user contexte utilisateur connecté
     * @return 204 No Content
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/wishlist/{gameId}")
    public ResponseEntity<Void> wishlist(
            @PathVariable Integer gameId,
            @AuthenticationPrincipal UserContext user
    ) {
        
        return ResponseEntity.noContent().build();
    }
}
