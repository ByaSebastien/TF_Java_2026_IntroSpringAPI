# Les APIs REST avec Spring

## Introduction : De MVC à REST

Vous maîtrisez déjà Spring MVC avec Thymeleaf. Nous passons maintenant à une approche différente : les **APIs REST**. Contrairement au MVC classique où le serveur génère des pages HTML complètes, une API REST envoie uniquement des **données structurées** (JSON) que le client utilise pour afficher l'interface comme il le souhaite.

### Tableau comparatif : MVC vs API REST

| Aspect | MVC (Thymeleaf) | API REST |
|--------|-----------------|----------|
| **Retour** | Page HTML complète | Données JSON |
| **Annotation classe** | `@Controller` | `@RestController` |
| **Paramètre entrée** | `@ModelAttribute` | `@RequestBody` |
| **Paramètre model** | `Model model` (obligatoire) | Aucun `Model` (pas nécessaire) |
| **Format réponse** | Template HTML | Objet sérialisé en JSON |
| **Code HTTP** | Souvent implicite | Explicite via `ResponseEntity` |

---

## 1. @RestController vs @Controller

### En MVC (ce que vous connaissiez)
```java
@Controller
@RequestMapping("/game")
public class GameController {
    
    @GetMapping
    public String listGames(Model model) {
        List<Game> games = gameService.find();
        model.addAttribute("games", games);
        return "games/list";  // Nom du template Thymeleaf
    }
}
```

La méthode retourne le **nom d'un template** qui sera rendu en HTML par Thymeleaf.

### En API REST (ce que vous faites maintenant)
```java
@RestController
@RequestMapping("/Game")
public class GameController {
    
    @GetMapping
    public ResponseEntity<Page<GameResponse>> find(
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @RequestParam(name = "size", required = false, defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Game> games = gameService.find(pageable);
        Page<GameResponse> responsePage = games.map(GameResponse::fromGame);

        return ResponseEntity.ok(responsePage);
    }
}
```

**Différence clé** : 
- `@RestController` indique au serveur : *"Sérialise directement l'objet retourné en JSON"*
- Pas de modèle HTML à retourner
- L'objet retourné est automatiquement converti en JSON par Spring

---

## 2. Format d'échange : JSON

Une API REST utilise **JSON** (JavaScript Object Notation) comme format d'échange universel.

### Exemple de réponse JSON
```json
{
  "content": [
    {
      "id": 1,
      "name": "The Witcher 3",
      "price": 29.99
    },
    {
      "id": 2,
      "name": "Cyberpunk 2077",
      "price": 39.99
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

Spring convertit automatiquement vos objets Java en JSON grâce à la librairie **Jackson**.

---

## 3. Recevoir des données : @RequestBody

### En MVC (ce que vous connaissiez)
```java
@PostMapping("/game")
public String saveGame(@ModelAttribute GameRequest gameRequest, Model model) {
    Game game = gameRequest.toGame();
    gameService.save(game);
    return "redirect:/game";
}
```

`@ModelAttribute` prenait les données du formulaire HTML (application/x-www-form-urlencoded).

### En API REST (ce que vous faites maintenant)
```java
@PostMapping
public ResponseEntity<Void> save(@Valid @RequestBody GameRequest gameRequest) {
    Game game = gameRequest.toGame();
    Game response = gameService.save(game);
    
    URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
    
    return ResponseEntity.created(uri).build();
}
```

**Différences clés** :
- `@RequestBody` : Spring désérialise le JSON reçu en objet Java
- `@Valid` : Valide les données reçues contre les règles du DTO
- Pas d'appel à `model.addAttribute()`
- Retour d'une `ResponseEntity` avec le code HTTP approprié

### Comment fonctionne @RequestBody ?

Le client envoie ceci :
```json
{
  "name": "Elden Ring",
  "price": 49.99,
  "genre": "Action RPG"
}
```

Spring désérialise automatiquement ce JSON dans votre objet `GameRequest` :
```java
public class GameRequest {
    private String name;
    private BigDecimal price;
    private String genre;
    
    // getters, setters, etc.
}
```

---

## 4. Les paramètres d'entrée

### @RequestParam : Paramètres de requête (query string)

Utilisé pour les paramètres dans l'URL après le `?`

```java
@GetMapping
public ResponseEntity<Page<GameResponse>> find(
    @RequestParam(name = "page", required = false, defaultValue = "0") int page,
    @RequestParam(name = "size", required = false, defaultValue = "10") int size
) {
    // ...
}
```

**Exemples d'appels** :
- `GET /Game` → page=0, size=10 (valeurs par défaut)
- `GET /Game?page=1&size=20` → page=1, size=20
- `GET /Game?page=2` → page=2, size=10 (taille par défaut)

| Propriété | Signification |
|-----------|---------------|
| `name` | Nom du paramètre dans l'URL |
| `required` | Est-il obligatoire ? (false = optionnel) |
| `defaultValue` | Valeur par défaut si absent |

### @PathVariable : Paramètres dans le chemin

Utilisé pour les paramètres variables dans le chemin de l'URL

```java
@GetMapping("/{id}")
public ResponseEntity<GameResponse> findById(@PathVariable Integer id) {
    Game game = gameService.findById(id);
    GameResponse gameResponse = GameResponse.fromGame(game);
    return ResponseEntity.ok(gameResponse);
}
```

**Exemples d'appels** :
- `GET /Game/1` → id=1
- `GET /Game/42` → id=42

---

## 5. Les codes HTTP et ResponseEntity

### Qu'est-ce qu'une ResponseEntity ?

`ResponseEntity` vous permet de contrôler précisément :
- Le **code de statut HTTP** (200, 201, 204, 404, etc.)
- Les **headers** de la réponse
- Le **body** (corps) de la réponse

### Les codes HTTP courants

| Code | Classe | Signification | Utilisation |
|------|--------|---------------|------------|
| **200** | 2xx Succès | OK - Requête réussie | Récupération de données |
| **201** | 2xx Succès | Created - Ressource créée | Après un POST |
| **204** | 2xx Succès | No Content - Succès sans corps | Après une modification (PUT/PATCH/DELETE) |
| **400** | 4xx Client | Bad Request - Requête invalide | Données manquantes/invalides |
| **404** | 4xx Client | Not Found - Ressource inexistante | L'ID n'existe pas |
| **500** | 5xx Serveur | Internal Server Error | Erreur côté serveur |

### Exemples tirés de votre code

#### GET - Récupérer des données
```java
@GetMapping
public ResponseEntity<Page<GameResponse>> find(...) {
    // ...
    return ResponseEntity.ok(responsePage);  // 200 OK
}
```

#### POST - Créer une ressource
```java
@PostMapping
public ResponseEntity<Void> save(@Valid @RequestBody GameRequest gameRequest) {
    Game response = gameService.save(game);
    
    URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
    
    return ResponseEntity.created(uri).build();  // 201 Created
}
```

**Point important** : 
- Le code `201 Created` indique que la ressource a bien été créée
- L'en-tête `Location` retourné pointe vers la nouvelle ressource créée
- Le body est vide (`.build()`)

#### PUT - Modifier une ressource
```java
@PutMapping("/{id}")
public ResponseEntity<Void> update(
    @PathVariable Integer id,
    @Valid @RequestBody GameRequest gameRequest
) {
    Game game = gameRequest.toGame();
    gameService.update(id, game);
    
    return ResponseEntity.noContent().build();  // 204 No Content
}
```

#### DELETE - Supprimer une ressource
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Integer id) {
    gameService.delete(id);
    return ResponseEntity.noContent().build();  // 204 No Content
}
```

#### PATCH - Modification partielle
```java
@PatchMapping("/wishlist/{gameId}")
public ResponseEntity<Void> wishlist(@PathVariable Integer gameId, ...) {
    // Logique métier
    return ResponseEntity.noContent().build();  // 204 No Content
}
```

---

## 6. Validation des données : @Valid

### Qu'est-ce que @Valid ?

`@Valid` demande à Spring de valider l'objet reçu contre les contraintes définies dans votre DTO.

```java
@PostMapping
public ResponseEntity<Void> save(@Valid @RequestBody GameRequest gameRequest) {
    // gameRequest a déjà été validé ici
}
```

### Exemple de DTO avec validations
```java
public class GameRequest {
    
    @NotBlank(message = "Le nom ne peut pas être vide")
    private String name;
    
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal price;
    
    @Size(min = 3, max = 50, message = "Le genre doit entre 3 et 50 caractères")
    private String genre;
}
```

**Si les données ne respectent pas les règles** :
- Spring rejette la requête avec un code `400 Bad Request`
- Les erreurs de validation sont retournées au client en JSON

---

## 7. Conversion entre Entités et DTOs

### Pourquoi des DTOs ?

Vos **entités JPA** contiennent souvent des données que vous ne voulez pas exposer via l'API (relations complexes, données sensibles, etc.).

Les **DTOs** (Data Transfer Objects) sont des objets simplifiés pour le transfert de données.

### Exemple : GameRequest (DTO d'entrée)

```java
public class GameRequest {
    private String name;
    private BigDecimal price;
    private String genre;
    
    public Game toGame() {
        Game game = new Game();
        game.setName(this.name);
        game.setPrice(this.price);
        game.setGenre(this.genre);
        return game;
    }
}
```

### Exemple : GameResponse (DTO de sortie)

```java
public class GameResponse {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String genre;
    
    public static GameResponse fromGame(Game game) {
        return new GameResponse(
            game.getId(),
            game.getName(),
            game.getPrice(),
            game.getGenre()
        );
    }
}
```

**Note** : Dans le contrôleur, vous convertissez systématiquement :
```java
@GetMapping("/{id}")
public ResponseEntity<GameResponse> findById(@PathVariable Integer id) {
    Game game = gameService.findById(id);                    // Récupérer l'entité
    GameResponse gameResponse = GameResponse.fromGame(game); // Convertir en DTO
    return ResponseEntity.ok(gameResponse);                  // Retourner le DTO
}
```

---

## 8. Résumé des annotations du contrôleur

| Annotation | Endroit | Rôle |
|-----------|---------|------|
| `@RestController` | Classe | Active le mode API REST (sérialisation JSON) |
| `@RequestMapping("/Game")` | Classe | Chemin de base pour toutes les méthodes |
| `@GetMapping` | Méthode | Récupérer une ressource (HTTP GET) |
| `@PostMapping` | Méthode | Créer une ressource (HTTP POST) |
| `@PutMapping` | Méthode | Modifier entièrement une ressource (HTTP PUT) |
| `@PatchMapping` | Méthode | Modifier partiellement une ressource (HTTP PATCH) |
| `@DeleteMapping` | Méthode | Supprimer une ressource (HTTP DELETE) |
| `@RequestBody` | Paramètre | Le paramètre contient le corps JSON de la requête |
| `@RequestParam` | Paramètre | Le paramètre vient de la query string (?param=valeur) |
| `@PathVariable` | Paramètre | Le paramètre vient du chemin de l'URL (/resource/{id}) |
| `@Valid` | Paramètre | Valide l'objet reçu contre ses contraintes |

---

## 9. Flux complet d'une requête API REST

### Exemple : Créer un nouveau jeu

#### 1. Le client envoie une requête HTTP
```http
POST /Game HTTP/1.1
Content-Type: application/json

{
  "name": "Baldur's Gate 3",
  "price": 59.99,
  "genre": "RPG"
}
```

#### 2. Spring reçoit la requête et l'achemine
Spring voit `POST` et `/Game` → Appelle `save()`

#### 3. Désérialisation du JSON
```java
@RequestBody GameRequest gameRequest
// Spring convertit le JSON en objet GameRequest
// gameRequest.name = "Baldur's Gate 3"
// gameRequest.price = 59.99
// gameRequest.genre = "RPG"
```

#### 4. Validation
```java
@Valid @RequestBody GameRequest gameRequest
// Spring valide que name n'est pas vide, que price > 0, etc.
// Si validation échoue → 400 Bad Request
```

#### 5. Traitement métier
```java
Game game = gameRequest.toGame();        // Convertir DTO → Entité
Game response = gameService.save(game);  // Sauvegarder
```

#### 6. Construction de la réponse
```java
URI uri = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(response.getId())  // /Game/5 par exemple
        .toUri();

return ResponseEntity.created(uri).build();  // 201 Created
```

#### 7. Le client reçoit
```http
HTTP/1.1 201 Created
Location: /Game/5
```

Le client sait que la ressource a été créée et peut la récupérer à l'URL `Location`.

---

## 10. Points clés à retenir

✅ **Une API REST c'est** :
- Envoyer et recevoir des **données JSON**
- Utiliser les **codes HTTP** pour indiquer le résultat de l'opération
- Utiliser les **verbes HTTP** (GET, POST, PUT, PATCH, DELETE) pour l'action
- Utiliser les **chemins URL** pour identifier les ressources

✅ **Différences principales avec MVC** :
- `@RestController` au lieu de `@Controller`
- `@RequestBody` au lieu de `@ModelAttribute`
- Pas de `Model model` à passer
- Pas de templates à retourner
- `ResponseEntity` pour contrôler les codes HTTP

✅ **Toujours convertir** :
- Entrée : JSON → DTO via `@RequestBody`
- Sortie : Entité JPA → DTO avant retour

✅ **La validation c'est important** :
- `@Valid` sur le `@RequestBody`
- Defines les contraintes dans le DTO avec des annotations (`@NotBlank`, `@Positive`, etc.)
