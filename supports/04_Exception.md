# Gestion des Exceptions dans une API REST Spring

## Introduction : Pourquoi gérer les exceptions ?

Vous maîtrisez déjà les exceptions Java de base. Mais dans une **API REST**, les exceptions doivent être traitées différemment du MVC classique.

### En MVC (ce que vous faisiez avant)
Une exception non capturée → Page d'erreur Thymeleaf → HTML retourné

### En API REST (ce que vous faites maintenant)
Une exception non capturée → ??? → JSON retourné avec code HTTP approprié

**Le problème** : Comment transformer une exception Java en réponse JSON structurée avec le bon code HTTP ?

**La solution** : Une architecture d'exceptions bien pensée + un gestionnaire centralisé d'exceptions.

---

## 1. Les Tenants et Aboutissants des Exceptions

### Qu'est-ce qu'une Exception ?

Une exception est un **événement anormal** qui interrompt le flux normal d'exécution d'un programme.

```java
public void findUser(Integer id) {
    if (id == null) {
        throw new IllegalArgumentException("ID ne peut pas être null");
        // ↑ Interruption du flux normal
    }
    
    User user = repository.findById(id);
    if (user == null) {
        throw new UserNotFoundException("User not found");
        // ↑ Interruption du flux normal
    }
    
    System.out.println("Utilisateur trouvé : " + user.getName());
    // Cette ligne ne s'exécutera que si pas d'exception
}
```

### Checked vs Unchecked

| Aspect | Checked | Unchecked (Runtime) |
|--------|---------|-------------------|
| **Héritage** | `extends Exception` | `extends RuntimeException` |
| **Vérification** | À la **compilation** | À l'**exécution** |
| **Gestion** | Obligatoire (`try-catch` ou `throws`) | Optionnelle |
| **Cas d'usage** | Erreurs prévisibles (I/O, fichiers) | Erreurs de programmation (null, index) |

### Pourquoi les RuntimeException dans une API REST ?

Dans une API REST, nous utilisons principalement des **RuntimeException** (exceptions non cochées) car :

1. **Plus simple** : Pas besoin de déclarer `throws` partout
2. **Cohérent** : Spring gère bien les RuntimeException
3. **Flexible** : Le gestionnaire centralisé les capture toutes
4. **Flux métier** : Les exceptions représentent des états anormaux (utilisateur non trouvé, etc.)

```java
// ❌ Compliqué : Checked exceptions
public User findUser(Integer id) throws UserNotFoundException {
    // ...
}

// ✅ Mieux : Unchecked exceptions
public User findUser(Integer id) {
    // ...
    throw new UserNotFoundException("User not found");
}
```

---

## 2. Hiérarchie d'Exceptions : Conception par Héritage

### Principes de conception

Une bonne hiérarchie d'exceptions permet de :
- **Capturer spécifiquement** les erreurs appropriées
- **Partager du code** via héritage
- **Structurer** les erreurs par domaine

### La hiérarchie du projet

```
Exception
└── RuntimeException
    └── IntroSpringApiException (abstraite)
        ├── UserException (abstraite)
        │   ├── UserNotFoundException
        │   ├── UserAlreadyExistException
        │   └── UserInvalidPasswordException
        └── RoleException (abstraite)
            └── RoleNotFoundException
```

### La classe de base : IntroSpringApiException

```java
@EqualsAndHashCode(callSuper = false) @ToString
public abstract class IntroSpringApiException extends RuntimeException {
    
    @Getter
    private String section;           // "user", "role", etc.
    
    @Getter
    private final HttpStatus status;  // Code HTTP (404, 409, 400, etc.)
    
    @Getter
    private final Object body;        // Contenu de la réponse JSON
    
    public IntroSpringApiException(HttpStatus status, Object body, String section) {
        super();
        this.status = status;
        this.body = body;
        this.section = section;
    }
}
```

**Pourquoi cette structure ?**

- **HttpStatus** : Le gestionnaire d'exceptions sait quel code HTTP retourner
- **body** : Les données à retourner au client (String, Map, objet, etc.)
- **section** : Pour tracer quelle partie du système a généré l'erreur

### Exceptions spécifiques par domaine

#### UserException (classe abstraite)
```java
public abstract class UserException extends IntroSpringApiException {
    
    public UserException(HttpStatus status, Object body) {
        super(status, body, "user");  // Section = "user"
    }
}
```

**Avantage** : Toutes les exceptions liées aux utilisateurs héritent de `UserException`. Le gestionnaire peut les capturer toutes avec un seul `@ExceptionHandler(UserException.class)`.

#### Cas concrets

**UserNotFoundException** : L'utilisateur n'existe pas
```java
public class UserNotFoundException extends UserException {
    
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND,  // 404
              "User not found");      // Message simple
    }
    
    public UserNotFoundException(String body) {
        super(HttpStatus.NOT_FOUND, body);
    }
}
```

**UserAlreadyExistException** : Tentative de créer un utilisateur existant
```java
public class UserAlreadyExistException extends UserException {
    
    public UserAlreadyExistException() {
        super(HttpStatus.CONFLICT,    // 409 Conflict
              new HashMap<>(
                  Map.of("username", "Username already exists")
              ));
    }
}
```

**UserInvalidPasswordException** : Mot de passe incorrect
```java
public class UserInvalidPasswordException extends UserException {
    
    public UserInvalidPasswordException() {
        super(HttpStatus.BAD_REQUEST,  // 400
              "Invalid password");
    }
    
    public UserInvalidPasswordException(String body) {
        super(HttpStatus.BAD_REQUEST, body);
    }
}
```

### Quand lancer une exception ?

```java
// Dans la couche BLL (Business Logic Layer)
@Service
public class UserService {
    
    public User findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException());
            // ↑ Lève l'exception si l'utilisateur n'existe pas
    }
    
    public void register(UserRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistException();
            // ↑ Empêche la création d'un utilisateur en doublon
        }
        // ... Créer l'utilisateur
    }
}
```

---

## 3. @RestControllerAdvice et @ExceptionHandler

### Le problème : Comment gérer les exceptions au niveau API REST ?

Imaginez ceci :

```java
@RestController
@RequestMapping("/User")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Integer id) {
        User user = userService.findById(id);  // Peut lever UserNotFoundException
        // ❓ Que faire si UserNotFoundException est levée ?
        // Le client reçoit quoi ?
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }
}
```

**Sans gestionnaire centralisé** :
- L'exception remonte au framework Spring
- Spring retourne une page d'erreur HTML (500 Internal Server Error)
- Le client reçoit du HTML au lieu du JSON attendu ❌

**Avec un gestionnaire centralisé** :
- L'exception est interceptée et transformée en réponse JSON
- Le client reçoit une réponse cohérente avec le bon code HTTP ✅

### @RestControllerAdvice : Le gestionnaire centralisé

La classe `ExceptionHandlers` est annotée avec `@RestControllerAdvice` :

```java
@RestControllerAdvice
@Slf4j  // Logging (expliqué après)
public class ExceptionHandlers {
    
    @ExceptionHandler(IntroSpringApiException.class)
    public ResponseEntity<?> handleIntroSpringApiException(IntroSpringApiException ex) {
        // Cette méthode est appelée quand une IntroSpringApiException est levée
        // N'importe où dans l'application
        
        return ResponseEntity
            .status(ex.getStatus())      // Code HTTP de l'exception
            .body(ex.getBody());          // Contenu de la réponse
    }
}
```

**Comment ça fonctionne ?**

```
Contrôleur
    ↓
userService.findById(id)  // Lève UserNotFoundException
    ↓
❌ UserNotFoundException remonte
    ↓
Spring intercepte l'exception
    ↓
🔍 Cherche un @ExceptionHandler pour UserNotFoundException
    ↓
UserNotFoundException extends UserException
UserException extends IntroSpringApiException
    ↓
✅ Trouve @ExceptionHandler(IntroSpringApiException.class)
    ↓
Appelle handleIntroSpringApiException(ex)
    ↓
Retourne ResponseEntity.status(404).body("User not found")
    ↓
Client reçoit JSON : "User not found" avec code 404 ✅
```

### Hiérarchie de capture des exceptions

Spring teste les `@ExceptionHandler` **du plus spécifique au plus général** :

```java
@RestControllerAdvice
public class ExceptionHandlers {
    
    // 1️⃣ Capture UserNotFoundException en priorité
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
    
    // 2️⃣ Si pas de gestionnaire spécifique, capture UserException
    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> handleUserException(UserException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
    }
    
    // 3️⃣ Si pas de gestionnaire plus spécifique, capture IntroSpringApiException
    @ExceptionHandler(IntroSpringApiException.class)
    public ResponseEntity<?> handleIntroSpringApiException(IntroSpringApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
    }
    
    // 4️⃣ En dernier recours, capture n'importe quelle Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity.internalServerError().build();  // 500
    }
}
```

### Autres exceptions gérées dans le projet

```java
@RestControllerAdvice
@Slf4j
public class ExceptionHandlers {
    
    // Les exceptions métier (héritent d'IntroSpringApiException)
    @ExceptionHandler(IntroSpringApiException.class)
    public ResponseEntity<?> handleIntroSpringApiException(IntroSpringApiException ex) {
        log.error("IntroSpringApiException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
    }
    
    // Les erreurs de validation (données invalides du client)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex) {
        
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage(), ex);
        
        Map<String, List<String>> errors = ex.getBindingResult()
            .getFieldErrors().stream()
            .collect(Collectors.groupingBy(
                FieldError::getField,
                Collectors.mapping(
                    DefaultMessageSourceResolvable::getDefaultMessage,
                    Collectors.toList()
                )
            ));
        return ResponseEntity.badRequest().body(errors);  // 400
    }
    
    // Les erreurs de Spring Security
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.error("UsernameNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(404).body(ex.getMessage());
    }
    
    // Les erreurs d'accès à la base de données
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<?> handleInvalidDataAccessApiUsageException(
        InvalidDataAccessApiUsageException ex) {
        
        log.error("InvalidDataAccessApiUsageException: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
    
    // Le filet de sécurité : capture tout ce qui n'a pas été capturé avant
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        log.error("Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().build();  // 500
    }
}
```

---

## 4. Le Logging : Tracer ce qui se passe

### Pourquoi logger les exceptions ?

Imaginez ceci : Une exception est levée et l'utilisateur reçoit une réponse 404. Mais **vous**, en tant que développeur, ne savez pas :
- ❓ Quand s'est-elle produite ?
- ❓ Quel utilisateur était impliqué ?
- ❓ Quelle était la stack trace complète ?
- ❓ Est-ce un problème récurrent ?

**Le logging** répond à ces questions en enregistrant :
- Le timestamp de l'exception
- Le message d'erreur
- La stack trace complète
- Des contextes additionnels (utilisateur, ID requête, etc.)

### Les niveaux de log

| Niveau | Intensité | Utilisation |
|--------|-----------|------------|
| **TRACE** | Plus verbeux | Déboguer les détails intimes (rarement utilisé) |
| **DEBUG** | Verbeux | Informations de débogage (utilisé en développement) |
| **INFO** | Normal | Informations importantes (flux d'application normal) |
| **WARN** | Avertissement | Situations inhabituelles (attention requise) |
| **ERROR** | Erreur | Erreurs sérieuses (exceptions, opérations échouées) |

### Comment logger dans le projet

#### 1. Ajouter la dépendance Lombok (déjà fait)
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

#### 2. Annoter la classe avec @Slf4j
```java
@RestControllerAdvice
@Slf4j  // ← Ajoute un logger automatiquement
public class ExceptionHandlers {
    // Le logger 'log' est maintenant disponible
}
```

#### 3. Utiliser le logger

```java
@ExceptionHandler(IntroSpringApiException.class)
public ResponseEntity<?> handleIntroSpringApiException(IntroSpringApiException ex) {
    
    // Enregistre l'exception avec la stack trace
    log.error("IntroSpringApiException: {}", ex.getMessage(), ex);
    //        ↑ Niveau          ↑ Message formaté        ↑ Exception (pour la stack trace)
    
    return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
}
```

### Exemple : Comprendre les logs

Quand le code ci-dessus s'exécute, voici ce qui s'affiche dans les logs :

```
[ERROR] 2026-09-22 12:08:37 - IntroSpringApiException: User not found
java.lang.Exception: User not found
    at be.bstorm.service.UserService.findById(UserService.java:42)
    at be.bstorm.api.UserController.findById(UserController.java:28)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    ...
```

**Ce qui s'affiche** :
- 🕐 **Timestamp** : 2026-09-22 12:08:37
- 📊 **Niveau** : [ERROR]
- 💬 **Message** : "IntroSpringApiException: User not found"
- 🔍 **Stack trace** : Montre exactement où l'exception a été levée et comment elle s'est propagée

### Les 3 types de logs dans le gestionnaire d'exceptions

```java
@ExceptionHandler(IntroSpringApiException.class)
public ResponseEntity<?> handleIntroSpringApiException(IntroSpringApiException ex) {
    
    // 1. ERROR : Grave, attention requise
    log.error("IntroSpringApiException: {}", ex.getMessage(), ex);
    //   ↑ Utiliser ERROR pour les vraies exceptions
    
    // 2. WARN : Situation anormale mais gérée
    log.warn("Tentative d'accès utilisateur inexistant: {}", ex.getSection());
    //   ↑ Utiliser WARN pour les cas aux limites (ex: tentative login échouée)
    
    // 3. INFO : Information utile
    log.info("Exception métier capturée et traitée");
    //   ↑ Utiliser INFO pour tracer les opérations importantes
    
    return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
}
```

### Configurer les logs : application.properties

```properties
# Niveau global : INFO (par défaut)
logging.level.root=INFO

# Niveau pour une classe spécifique
logging.level.be.bstorm.tf_java_2026_introspringapi.api.controllers.ExceptionHandlers=DEBUG

# Niveau pour un package entier
logging.level.be.bstorm.tf_java_2026_introspringapi.bll=DEBUG

# Chemin du fichier de logs
logging.file.name=logs/application.log

# Format personnalisé
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n
```

### Bonnes pratiques de logging

```java
@Service
@Slf4j
public class UserService {
    
    public User findById(Integer id) {
        log.debug("Searching for user with id: {}", id);  // Utile au débogage
        
        User user = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("User not found for id: {}", id);  // Attention
                return new UserNotFoundException();
            });
        
        log.info("User found successfully: {}", user.getId());  // Information
        return user;
    }
    
    public void register(UserRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            log.warn("Registration attempt with existing username: {}", 
                    request.getUsername());
            throw new UserAlreadyExistException();
        }
        
        User newUser = userMapper.toUser(request);
        User savedUser = repository.save(newUser);
        
        log.info("New user registered successfully: {} (id: {})",
                savedUser.getUsername(), savedUser.getId());
    }
}
```

---

## 5. Flux Complet : Du Contrôleur aux Logs

```
╔════════════════════════════════════════════════════════════════╗
║               Requête HTTP du client                           ║
║         GET /User/999 (utilisateur n'existe pas)             ║
╚════════════════════════════════════════════════════════════════╝
                              ↓
╔════════════════════════════════════════════════════════════════╗
║             UserController.findById(999)                       ║
║           - Spring injecte l'ID 999                           ║
╚════════════════════════════════════════════════════════════════╝
                              ↓
╔════════════════════════════════════════════════════════════════╗
║             userService.findById(999)                          ║
║   - Cherche en base de données                                ║
║   - L'ID 999 n'existe pas                                     ║
║   - Lève UserNotFoundException()                              ║
╚════════════════════════════════════════════════════════════════╝
                              ↓
             ❌ Exception propagée vers le haut
                              ↓
╔════════════════════════════════════════════════════════════════╗
║             Spring intercepte l'exception                      ║
║   - Cherche un @ExceptionHandler compatible                   ║
║   - Trouve handleIntroSpringApiException()                    ║
║     (car UserNotFoundException → UserException               ║
║                              → IntroSpringApiException)       ║
╚════════════════════════════════════════════════════════════════╝
                              ↓
╔════════════════════════════════════════════════════════════════╗
║        ExceptionHandlers.handleIntroSpringApiException()       ║
║   - Log : log.error("UserNotFoundException: User not found")  ║
║   - Crée ResponseEntity avec status 404                       ║
║   - Body : "User not found"                                   ║
╚════════════════════════════════════════════════════════════════╝
                              ↓
╔════════════════════════════════════════════════════════════════╗
║              Réponse HTTP au client                            ║
║            Status: 404 Not Found                               ║
║            Body: "User not found"                              ║
║            Content-Type: application/json                     ║
║                                                                ║
║        Les logs contiennent aussi :                           ║
║        [ERROR] 2026-09-22 12:08:37 - UserNotFoundException   ║
║        Stack trace avec tous les détails                      ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 6. Stratégies Populaires de Gestion d'Exceptions

### 1. **Try-Catch Local** (trop verbeux)
```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> findById(@PathVariable Integer id) {
    try {
        User user = userService.findById(id);
        return ResponseEntity.ok(UserResponse.fromUser(user));
    } catch (UserNotFoundException ex) {
        return ResponseEntity.status(404).body(null);
    } catch (Exception ex) {
        return ResponseEntity.status(500).build();
    }
}
```
❌ **Problème** : Du code de gestion d'erreur dans chaque contrôleur → Duplication massale

### 2. **@ExceptionHandler dans le contrôleur** (mieux)
```java
@RestController
@RequestMapping("/User")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Integer id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
```
⚠️ **Problème** : Si plusieurs contrôleurs, la gestion d'erreur se duplique partout

### 3. **@RestControllerAdvice GLOBAL** (meilleur ✅)
```java
@RestControllerAdvice
@Slf4j
public class ExceptionHandlers {
    
    @ExceptionHandler(IntroSpringApiException.class)
    public ResponseEntity<?> handleIntroSpringApiException(IntroSpringApiException ex) {
        log.error("Exception métier: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        log.error("Exception générique: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().build();
    }
}
```
✅ **Avantages** :
- Centralisation unique de la gestion d'erreur
- Pas de duplication
- Cohérence garantie partout
- Logging homogène

### 4. **Réponse d'erreur structurée** (professionnel)
```java
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    // constructeur, getters...
}

@RestControllerAdvice
@Slf4j
public class ExceptionHandlers {
    
    @ExceptionHandler(IntroSpringApiException.class)
    public ResponseEntity<ErrorResponse> handleIntroSpringApiException(
        IntroSpringApiException ex,
        HttpServletRequest request) {
        
        log.error("Exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }
}
```

**Réponse au client** :
```json
{
    "timestamp": "2026-09-22T12:08:37.467+02:00",
    "status": 404,
    "error": "Not Found",
    "message": "User not found",
    "path": "/User/999"
}
```

### 5. **Avec GlobalExceptionHandler et traits personnalisés** (avancé)
Combiner :
- Hiérarchie d'exceptions bien structurée (comme dans le projet ✅)
- Messages d'erreur personnalisés par domaine
- Logging contextualisé
- Codes d'erreur internes pour le suivi

```java
// Dans chaque exception
public class UserNotFoundException extends UserException {
    private static final String ERROR_CODE = "USER_001";
    
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, Map.of(
            "errorCode", ERROR_CODE,
            "message", "User not found"
        ));
    }
}

// Le gestionnaire extrait le code
@ExceptionHandler(IntroSpringApiException.class)
public ResponseEntity<?> handle(IntroSpringApiException ex) {
    log.error("[{}] {}", ex.getSection(), ex.getBody());
    return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
}
```

---

## Résumé : Architecture d'Exceptions Complète

### ✅ Ce que vous devez retenir

1. **Hiérarchie d'exceptions** :
   - Créez des exceptions spécifiques par domaine (UserException, RoleException)
   - Héritez d'une classe de base (IntroSpringApiException) avec HttpStatus et body

2. **@RestControllerAdvice** :
   - Centralisez la gestion d'erreur dans une seule classe
   - Utilisez `@ExceptionHandler` pour capturer spécifiquement les exceptions

3. **Logging avec @Slf4j** :
   - Enregistrez les erreurs avec le contexte (message, stack trace)
   - Utilisez les bons niveaux (ERROR, WARN, INFO)
   - Configurez les fichiers de logs

4. **Retournez du JSON** :
   - Utilisez `ResponseEntity` avec le bon code HTTP
   - Structurez vos réponses d'erreur (body cohérent)

### 📊 Comparaison des approches

| Stratégie | Centralisation | Maintenabilité | Cohérence |
|-----------|---|---|---|
| Try-Catch local | ❌ | ❌ | ❌ |
| @ExceptionHandler par contrôleur | ⚠️ | ⚠️ | ⚠️ |
| **@RestControllerAdvice global** | ✅ | ✅ | ✅ |
| Avec réponse structurée | ✅ | ✅ | ✅ |

**→ Utilisez toujours @RestControllerAdvice pour une API REST Spring !**
