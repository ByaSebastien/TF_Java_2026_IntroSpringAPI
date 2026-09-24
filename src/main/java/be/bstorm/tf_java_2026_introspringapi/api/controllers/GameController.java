package be.bstorm.tf_java_2026_introspringapi.api.controllers;

import be.bstorm.tf_java_2026_introspringapi.api.model.game.requests.GameRequest;
import be.bstorm.tf_java_2026_introspringapi.api.model.game.responses.GameResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

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

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> findById(
            @PathVariable Integer id
    ) {
        Game game = gameService.findById(id);

        GameResponse gameResponse = GameResponse.fromGame(game);

        return ResponseEntity.ok(gameResponse);
    }

//    @PreAuthorize("hasAuthority('admin')")
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

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id
    ){
        gameService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/wishlist/{gameId}")
    public ResponseEntity<Void> wishlist(
            @PathVariable Integer gameId,
            @AuthenticationPrincipal User user
    ) {
        
        return ResponseEntity.noContent().build();
    }
}
