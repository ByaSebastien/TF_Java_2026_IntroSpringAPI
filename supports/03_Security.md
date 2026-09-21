# Spring Security avec JWT

## ⚠️ Important : Mise en Place vs Utilisation

**Avant de commencer**, il faut clarifier quelque chose :

> **La mise en place de Spring Security avec JWT est COMPLEXE.**
> **Mais vous n'avez PAS besoin de la maîtriser entièrement.**

Divisez votre apprentissage en deux parties :
- **Mise en place** : Vous l'avez, elle fonctionne, c'est compliqué → Ce n'est PAS prioritaire de retenir par cœur
- **Utilisation** : `@PreAuthorize`, `@AuthenticationPrincipal`, etc. → C'est CELLE-CI qui est essentiel à 100%

Ce document vous montre les deux, mais **concentrez-vous sur la section "Utilisation"**.

---

## Du Stateful au Stateless

### 1. Approche Stateful (MVC - Ce que vous faisiez avant)

```mermaid
sequenceDiagram
    participant User as 👤 User<br/>(Browser)
    participant Server as 🖥️ Serveur MVC
    
    User->>Server: 1️⃣ POST /login<br/>username + password
    activate Server
    Server->>Server: Valider credentials
    Server->>Server: ✓ Créer session<br/>SessionID = abc123
    Server-->>User: Set-Cookie:<br/>SessionID=abc123<br/>(HttpOnly)
    deactivate Server
    
    Note over User,Server: Requête suivante
    User->>Server: 2️⃣ GET /games<br/>Cookie: SessionID=abc123
    activate Server
    Server->>Server: Chercher session<br/>SessionID=abc123
    Server->>Server: ✓ Session trouvée!<br/>User = Alice
    Server-->>User: 200 OK + HTML
    deactivate Server
```

**Ce qui se passe** :
- Le serveur **crée et stocke** une session en mémoire
- Le client reçoit un cookie HttpOnly (pas accessible au JS)
- À chaque requête, le client renvoie le cookie
- Le serveur cherche la session correspondante

**Avantages** :
- ✅ Simple à comprendre
- ✅ Sécurisé (cookie HttpOnly)

**Inconvénients** :
- ❌ Pas scalable (si 2 serveurs, les sessions sont perdues)
- ❌ CORS difficile (les cookies ne se partagent pas cross-domain)
- ❌ Mobile difficile (gérer des cookies c'est compliqué)

---

### 2. Approche Stateless (JWT - Ce que vous faites maintenant)

```mermaid
sequenceDiagram
    participant User as 👤 User<br/>(Frontend/App)
    participant Server as 🖥️ Serveur API
    
    User->>Server: 1️⃣ POST /login<br/>username + password
    activate Server
    Server->>Server: Valider credentials
    Server->>Server: Créer token signé<br/>JWT = eyJhbGc...
    Server-->>User: { user, token }
    deactivate Server
    
    Note over User: 💾 Stocke le token
    
    User->>Server: 2️⃣ GET /games<br/>Authorization: Bearer eyJhbGc...
    activate Server
    Server->>Server: Valider la signature<br/>du token
    Server->>Server: ✓ Token valide!<br/>User = Alice
    Server-->>User: 200 OK + JSON
    deactivate Server
```

**Ce qui se passe** :
- Le serveur **signe et retourne** un token
- Le client le stocke (localStorage, sessionStorage, etc.)
- À chaque requête, le client l'envoie dans le header
- Le serveur valide juste la signature (pas d'accès à une base de données)

**Avantages** :
- ✅ Scalable (pas besoin de stocker les sessions)
- ✅ CORS-friendly (header standard HTTP)
- ✅ Mobile-friendly (facile à gérer)
- ✅ Multi-serveur (chaque serveur peut valider)

**Inconvénients** :
- ❌ Plus complexe à mettre en place
- ❌ Révocation difficile (token reste valide jusqu'à expiration)

---

## Architecture Sécurité JWT

### Flux Complet : Login et Requête Protégée

```mermaid
sequenceDiagram
    participant User as 👤 Client<br/>(Frontend/Mobile)
    participant Auth as 🔓 AuthController<br/>POST /login
    participant Service as ⚙️ AuthService
    participant JWT as 🔑 JwtUtils
    participant Filter as 🔒 JwtFilter<br/>(Chaque requête)
    participant Context as 📋 SecurityContext
    participant Game as 🎮 GameController<br/>GET /games
    
    User->>Auth: 1️⃣ Envoie username + password
    activate Auth
    Auth->>Service: Valider credentials
    activate Service
    Service->>Service: Chercher utilisateur en DB
    Service-->>Auth: ✓ Utilisateur trouvé (Alice)
    deactivate Service
    Auth->>JWT: Générer token
    activate JWT
    JWT->>JWT: Signer le token<br/>avec secret
    JWT-->>Auth: Token = eyJhbGc...
    deactivate JWT
    Auth-->>User: 2️⃣ Retourner<br/>{ user, token }
    deactivate Auth
    
    Note over User: 💾 Stocke le token<br/>dans localStorage
    
    User->>Filter: 3️⃣ Nouvelle requête<br/>GET /games<br/>Authorization: Bearer eyJhbGc...
    activate Filter
    Filter->>Filter: Extraire token du header
    Filter->>JWT: Valider le token
    activate JWT
    JWT->>JWT: Vérifier la signature
    JWT-->>Filter: ✓ Valide!<br/>Claims: {user: Alice, id: 5}
    deactivate JWT
    Filter->>Context: 4️⃣ Mettre Alice en context
    activate Context
    Context->>Context: SecurityContext<br/>setAuthentication(alice)
    deactivate Context
    Filter->>Filter: Passer au filtre suivant
    deactivate Filter
    
    Context->>Game: 5️⃣ Appeler le contrôleur
    activate Game
    Note over Game: @PreAuthorize<br/>("isAuthenticated()")<br/>✓ OK, Alice est auth
    Note over Game: @AuthenticationPrincipal<br/>User user → user = Alice
    Game->>Game: Récupérer les jeux
    Game-->>User: 6️⃣ 200 OK + JSON
    deactivate Game
```

**Ce qu'il faut comprendre** :
- ✅ **Step 1-2** : Login → Client reçoit token
- ✅ **Step 3-4** : Prochaine requête → Filter valide le token et met l'utilisateur en context
- ✅ **Step 5-6** : Contrôleur accède à l'utilisateur via `@AuthenticationPrincipal`

---

## Composants Clés

```mermaid
graph LR
    subgraph CLIENT["📱 Client"]
        direction LR
        Token["Token<br/>(stocké)"]
    end
    
    subgraph SERVER["🖥️ Serveur"]
        direction TB
        subgraph ENTRY["Entrée"]
            Filter["🔒 JwtFilter<br/>Chaque requête"]
        end
        
        subgraph VALIDATE["Validation"]
            JWT["🔑 JwtUtils<br/>validateToken"]
        end
        
        subgraph CONTEXT["Context Spring"]
            SC["📋 SecurityContext<br/>Stocke l'utilisateur"]
        end
        
        subgraph USE["Utilisation"]
            PA["@PreAuthorize<br/>@AuthenticationPrincipal"]
        end
        
        ENTRY --> VALIDATE
        VALIDATE --> CONTEXT
        CONTEXT --> USE
    end
    
    Token -->|Bearer token| Filter
    USE -->|Réponse| Token
    
    style CLIENT fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style SERVER fill:#7ED321,stroke:#5BA818,color:#fff
    style ENTRY fill:#F5A623,stroke:#B87C17,color:#fff
    style VALIDATE fill:#BD10E0,stroke:#8B0AA8,color:#fff
    style CONTEXT fill:#9013FE,stroke:#6A0BAA,color:#fff
    style USE fill:#FF6B6B,stroke:#C92A2A,color:#fff
```

---

## Mise en Place : Configuration (Lisez, mais pas besoin de retenir)

### 1. JwtUtils - Générer et Valider les Tokens

```java
@Component
@RequiredArgsConstructor
public class JwtUtils {
    
    @Value("${app.jwt.secret}")
    private String secret;
    
    @Value("${app.jwt.expiration}")
    private Long expiration;
    
    // Générer un token à partir d'un utilisateur
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("roles", user.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    // Valider un token et extraire les informations
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // Extraire le username du token
    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }
}
```

**Ce qu'il faut savoir** :
- `generateToken()` : Crée un token signé avec une clé secrète
- `validateToken()` : Vérifie que le token est valide et pas expiré
- Le token contient des "claims" (données) : username, ID, rôles, etc.

---

### 2. JwtFilter - Valider Chaque Requête

```java
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // 1. Extraire le token du header Authorization
            String token = extractToken(request);
            
            if (token != null) {
                // 2. Valider le token
                Claims claims = jwtUtils.validateToken(token);
                
                // 3. Récupérer le username des claims
                String username = claims.getSubject();
                
                // 4. Charger l'utilisateur depuis la base de données
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
                
                // 5. Créer une authentication et la mettre dans le context
                UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Token invalide : on ne fait rien, l'utilisateur ne sera pas authentifié
            SecurityContextHolder.clearContext();
        }
        
        // Passer au filtre suivant
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // Enlever "Bearer "
        }
        return null;
    }
}
```

**Ce qu'il faut savoir** :
- Le filter s'exécute sur CHAQUE requête
- Il extrait le token du header `Authorization: Bearer <token>`
- Il valide le token
- Il met l'utilisateur dans le `SecurityContext` de Spring

---

### 3. SecurityConfig - Activer le Filter et Définir les Règles

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtFilter jwtFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF (normal pour une API REST stateless)
            .csrf(csrf -> csrf.disable())
            
            // Ajouter le JwtFilter avant le filtre d'authentification
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Autoriser les endpoints publics
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/login", "/register").permitAll()
                    .anyRequest().authenticated()
            )
            
            // Stateless : pas de sessions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .build();
    }
}
```

**Ce qu'il faut savoir** :
- `.addFilterBefore()` : Ajoute notre JwtFilter dans la chaîne de filtres Spring
- `.sessionCreationPolicy(STATELESS)` : Dit à Spring de ne pas créer de sessions
- `.permitAll()` : Les endpoints publics n'ont besoin de token
- `.authenticated()` : Les autres endpoints demandent une authentification

---

## ✅ Utilisation : C'est CELLE-CI Qu'il Faut Maîtriser à 100%

### 1. `@PreAuthorize` - Contrôler l'Accès

**Qu'est-ce que c'est** : Annotation pour restreindre l'accès à une méthode selon les rôles/droits.

#### Exemple 1 : Vérifier un rôle simple

```java
@RestController
@RequestMapping("/Game")
public class GameController {
    
    // Seuls les admins peuvent créer un jeu
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody GameRequest gameRequest) {
        // ...
    }
}
```

**Comment ça fonctionne** :
- Si l'utilisateur n'a pas le rôle `admin` → 403 Forbidden automatiquement
- Si l'utilisateur a le rôle → La méthode s'exécute

#### Exemple 2 : Vérifier que l'utilisateur est connecté

```java
@PreAuthorize("isAuthenticated()")
@PatchMapping("/wishlist/{gameId}")
public ResponseEntity<Void> addToWishlist(@PathVariable Integer gameId) {
    // Cette méthode nécessite une authentification
    // Peu importe le rôle, juste que le token soit valide
}
```

#### Exemple 3 : Plusieurs conditions

```java
// L'utilisateur doit être connecté ET avoir le rôle admin OU moderator
@PreAuthorize("isAuthenticated() and (hasAuthority('admin') or hasAuthority('moderator'))")
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Integer id) {
    // ...
}
```

#### Expressions `@PreAuthorize` courantes

| Expression | Signification |
|-----------|---------------|
| `isAnonymous()` | L'utilisateur n'est pas connecté |
| `isAuthenticated()` | L'utilisateur est connecté |
| `hasAuthority('admin')` | L'utilisateur a le rôle 'admin' |
| `hasAnyAuthority('admin', 'mod')` | L'utilisateur a l'un de ces rôles |
| `hasRole('ADMIN')` | Raccourci pour `hasAuthority('ROLE_ADMIN')` |
| `permitAll()` | N'importe qui peut accéder |
| `denyAll()` | Personne ne peut accéder |

---

### 2. `@AuthenticationPrincipal` - Récupérer l'Utilisateur Connecté

**Qu'est-ce que c'est** : Annotation pour injecter l'utilisateur actuellement authentifié dans vos méthodes.

#### Exemple 1 : Simple - Récupérer l'utilisateur

```java
@PreAuthorize("isAuthenticated()")
@PatchMapping("/wishlist/{gameId}")
public ResponseEntity<Void> addToWishlist(
        @PathVariable Integer gameId,
        @AuthenticationPrincipal User user
) {
    // user est l'utilisateur connecté
    Integer userId = user.getId();
    String username = user.getUsername();
    
    // Utiliser ces informations pour votre logique métier
    wishlistService.addToWishlist(userId, gameId);
    
    return ResponseEntity.noContent().build();
}
```

**Ce qui se passe** :
- `@AuthenticationPrincipal User user` : Spring injecte automatiquement l'utilisateur du `SecurityContext`
- Pas besoin de demander au client l'ID de l'utilisateur
- Impossible de tricher : On prend l'ID du token validé

#### Exemple 2 : Récupérer seulement le username

```java
@AuthenticationPrincipal User user
// ou
@AuthenticationPrincipal(expression = "username") String username
```

#### Exemple 3 : Utiliser dans un service

```java
@RestController
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/my-games")
    public ResponseEntity<List<GameResponse>> getMyGames(
            @AuthenticationPrincipal User user
    ) {
        // Passer l'utilisateur au service
        List<Game> games = gameService.findByCreator(user);
        List<GameResponse> responses = games.stream()
                .map(GameResponse::fromGame)
                .toList();
        
        return ResponseEntity.ok(responses);
    }
}
```

---

## Flux Complet : Exemple Réel

### Scénario : Ajouter un jeu à la wishlist

#### 1. Le Frontend se Connecte

```
POST /login
{
  "username": "alice",
  "password": "password123"
}

Response:
{
  "user": {
    "id": 5,
    "username": "alice",
    "roles": ["user"]
  },
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZSIsImlkIjo1LCJleHAiOjE2OTQ4OTIwMDB9..."
}
```

Le frontend **stocke le token**.

#### 2. Le Frontend Envoie une Requête Protégée

```
PATCH /Game/wishlist/42
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZSIsImlkIjo1LCJleHAiOjE2OTQ4OTIwMDB9...
```

#### 3. Le Serveur Traite la Requête

**Étape 1 : JwtFilter Intercepte**
```
Le filter extrait le token du header Authorization
```

**Étape 2 : JwtUtils Valide**
```
JwtUtils.validateToken(token)
→ ✓ Token valide
→ Claims extraits : { subject: "alice", id: 5, roles: ["user"] }
```

**Étape 3 : Filter Met l'Utilisateur dans le Context**
```java
User user = userRepository.findById(5);  // Alice
UsernamePasswordAuthenticationToken auth = 
    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
SecurityContextHolder.getContext().setAuthentication(auth);
```

**Étape 4 : Le Contrôleur S'Exécute**
```java
@PreAuthorize("isAuthenticated()")
@PatchMapping("/wishlist/{gameId}")
public ResponseEntity<Void> addToWishlist(
        @PathVariable Integer gameId,
        @AuthenticationPrincipal User user  // user = Alice (injecté)
) {
    // Alice a le token valide et est connectée
    // On peut faire confiance à son ID
    wishlistService.addToWishlist(user.getId(), gameId);
    return ResponseEntity.noContent().build();
}
```

**Étape 5 : Réponse**
```
204 No Content
```

---

## Différences Stateful vs Stateless

### Comparaison

| Aspect | Stateful (MVC) | Stateless (JWT) |
|--------|-------|---------|
| **Où est stockée l'auth** | Serveur (session) | Client (token) |
| **Vérification** | Serveur cherche la session | Serveur valide la signature |
| **Révocation** | Immédiate (supprime session) | Difficile (token reste valide) |
| **Scalabilité** | Difficile (sticky sessions) | Facile (no server state) |
| **CORS** | Compliqué (cookies) | Simple (header) |
| **Mobile** | Difficile (cookies) | Naturel (header) |
| **Sécurité du token** | N/A | Signature (impossible de tricher) |

---

## Pour Résumer : Mise en Place vs Utilisation

### ❌ Mise en Place (Vous n'avez PAS besoin de maîtriser à 100%)

La mise en place c'est :
- JwtUtils (générer/valider tokens)
- JwtFilter (valider chaque requête)
- SecurityConfig (configuration Spring)

**Vous l'avez, elle fonctionne, c'est configuré.**

Vous pouvez :
- La modifier si besoin
- La copier/coller sur un autre projet
- Chercher sur Google si ça casse

**Mais ce n'est pas prioritaire de la maîtriser par cœur.**

---

### ✅ Utilisation (Vous DEVEZ maîtriser à 100%)

L'utilisation c'est :
- `@PreAuthorize("hasAuthority('admin')")` : Restreindre l'accès
- `@AuthenticationPrincipal User user` : Récupérer l'utilisateur

**C'est simple, c'est logique, et vous l'utiliserez TOUT LE TEMPS.**

Exemples réalistes :

```java
// Exemple 1 : Seuls les admins peuvent créer
@PreAuthorize("hasAuthority('admin')")
@PostMapping
public ResponseEntity<Void> save(@Valid @RequestBody GameRequest gameRequest) { }

// Exemple 2 : Seul l'utilisateur connecté peut voir ses jeux
@PreAuthorize("isAuthenticated()")
@GetMapping("/mine")
public ResponseEntity<List<GameResponse>> getMyGames(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(gameService.findByUserId(user.getId()));
}

// Exemple 3 : Seul l'utilisateur qui a créé peut supprimer
@PreAuthorize("isAuthenticated()")
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal User user) {
    gameService.deleteIfOwner(id, user.getId());
    return ResponseEntity.noContent().build();
}
```

**C'est ÇA qu'on vous demandera en interview, en test, en entreprise.**

---

## Cas d'Usage Courants

### ✅ Cas 1 : Endpoint Anonyme (Public)

```java
@PostMapping("/register")
public ResponseEntity<UserTokenResponse> register(@Valid @RequestBody RegisterRequest request) {
    // Pas d'annotation @PreAuthorize
    // Tout le monde peut accéder
    User user = authService.register(request.toUser());
    return ResponseEntity.ok(mapUser(user));
}
```

**Quand l'utiliser** : Login, register, endpoints publics

---

### ✅ Cas 2 : Endpoint Authentifié Uniquement

```java
@PreAuthorize("isAuthenticated()")
@PatchMapping("/wishlist/{gameId}")
public ResponseEntity<Void> addToWishlist(
        @PathVariable Integer gameId,
        @AuthenticationPrincipal User user
) {
    wishlistService.addToWishlist(user.getId(), gameId);
    return ResponseEntity.noContent().build();
}
```

**Quand l'utiliser** : N'importe quel endpoint que seuls les utilisateurs connectés doivent voir

---

### ✅ Cas 3 : Endpoint Admin Uniquement

```java
@PreAuthorize("hasAuthority('admin')")
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

**Quand l'utiliser** : Endpoints sensibles (création, suppression, modifications système)

---

### ✅ Cas 4 : Vérifier que C'est le Propriétaire

```java
@PreAuthorize("isAuthenticated()")
@PutMapping("/{id}")
public ResponseEntity<Void> update(
        @PathVariable Integer id,
        @Valid @RequestBody GameRequest gameRequest,
        @AuthenticationPrincipal User user
) {
    // Vérifier que l'utilisateur connecté est le propriétaire du jeu
    Game game = gameService.findById(id);
    
    if (!game.getCreatedBy().getId().equals(user.getId())) {
        throw new ForbiddenException("Vous ne pouvez modifier que vos propres jeux");
    }
    
    gameService.update(id, gameRequest.toGame());
    return ResponseEntity.noContent().build();
}
```

**Quand l'utiliser** : Quand un utilisateur ne peut modifier que ses propres données

---

## Points Clés à Retenir

✅ **Stateless vs Stateful** :
- Ancien : Sessions stockées sur le serveur
- Nouveau : Token signé stocké sur le client

✅ **Le flow simple** :
1. Login → Reçoit un token
2. Chaque requête → Envoie le token
3. Filter → Valide le token et met l'utilisateur en context
4. Controller → Accède à l'utilisateur via `@AuthenticationPrincipal`

✅ **Mise en Place** :
- JwtUtils, JwtFilter, SecurityConfig
- C'est complexe mais c'est fait
- Pas de panique si vous ne comprenez pas tout

✅ **Utilisation** :
- `@PreAuthorize()` : Restreindre l'accès
- `@AuthenticationPrincipal User user` : Récupérer l'utilisateur
- **C'est simples, logique, et prioritaire**

✅ **Sécurité** :
- Le token est signé → impossible de tricher
- L'utilisateur ne peut accéder qu'à ses propres données
- Le serveur valide chaque requête

---

## Ressources

Si vous voulez en savoir plus :
- Spring Security Docs: https://spring.io/projects/spring-security
- JWT.io: https://jwt.io (décoder/voir les tokens)
- OAuth 2.0: Comprendre les standards modernes

**Mais pour ce cours** : Concentrez-vous sur `@PreAuthorize` et `@AuthenticationPrincipal`. C'est suffisant. 🎯
