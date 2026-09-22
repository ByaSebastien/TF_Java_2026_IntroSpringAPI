# Specifications JPA : Filtres Dynamiques Avancés

## Introduction : Filtrer sans SQL

Vous maîtrisez déjà :
- Les requêtes JPQL simples avec `@Query`
- Les dérivées de noms de méthodes avec Spring Data (`findByNameAndPrice`)

Mais que faire quand vous avez besoin de **filtres dynamiques** ? Par exemple :
- L'utilisateur peut filtrer par nom, prix, catégorie, date, etc.
- Tous les filtres sont **optionnels**
- Les combinaisons sont **infinies**

### Le problème

```java
// ❌ Créer une méthode pour chaque combinaison ?
gameRepository.findByNameAndPrice(...);
gameRepository.findByNameAndCategory(...);
gameRepository.findByPrice(...);
gameRepository.findByPriceGreaterThan(...);
// ... Des centaines de combinaisons !
```

### La solution : Specifications JPA

**Les Specifications** permettent de **construire des conditions dynamiques** en SQL avec une API fluide et réutilisable.

```java
// ✅ Une seule requête dynamique
Specification<Game> spec = Specification.allOf(SearchSpecification.search(params));
gameRepository.findAll(spec, pageable);
```

---

## 1. Qu'est-ce qu'une Specification JPA ?

### Définition simple

Une `Specification<T>` est un **objet qui représente une condition SQL** sans l'exécuter immédiatement.

```java
// Une Specification est une fonction qui prend 3 paramètres
Specification<Game> = (root, query, criteriaBuilder) -> {
    // root = accès aux colonnes de la table
    // query = requête en cours de construction
    // criteriaBuilder = outils pour construire les conditions
    
    // Retourne une condition (ex: WHERE name LIKE '%witcher%')
    return criteriaBuilder.like(root.get("name"), "%witcher%");
};
```

### Pourquoi "Specification" ?

Le mot **"specification"** signifie **"spécification, cahier des charges"**. Vous spécifiez une condition que les données doivent respecter.

---

## 2. Les 3 Paramètres de la Lambda

### À retenir absolument

```java
(root, query, criteriaBuilder) -> { ... }
 ↑     ↑      ↑
 │     │      └─ Outils pour construire les conditions
 │     └──────── Requête JPA en cours
 └────────────── Accès aux colonnes
```

| Paramètre | Type | Utilité |
|-----------|------|---------|
| **root** | `Root<T>` | Accès aux colonnes de l'entité (`root.get("name")`) |
| **query** | `CriteriaQuery<?>` | Requête en cours (moins utilisé) |
| **criteriaBuilder** | `CriteriaBuilder` | Outils SQL (`like`, `equal`, `greaterThan`, etc.) |

### Exemples

#### 1. Simple : Égalité
```java
// SELECT * FROM game WHERE name = 'Witcher 3'
Specification<Game> spec = (root, query, cb) ->
    cb.equal(root.get("name"), "Witcher 3");
```

#### 2. LIKE : Contient du texte
```java
// SELECT * FROM game WHERE name LIKE '%witcher%'
Specification<Game> spec = (root, query, cb) ->
    cb.like(root.get("name"), "%witcher%");
```

#### 3. Comparaison numérique
```java
// SELECT * FROM game WHERE price > 30
Specification<Game> spec = (root, query, cb) ->
    cb.greaterThan(root.get("price"), new BigDecimal("30"));
```

#### 4. Combinaisons avec AND
```java
// SELECT * FROM game WHERE name LIKE '%witcher%' AND price > 30
Specification<Game> spec1 = (root, query, cb) ->
    cb.like(root.get("name"), "%witcher%");

Specification<Game> spec2 = (root, query, cb) ->
    cb.greaterThan(root.get("price"), new BigDecimal("30"));

Specification<Game> combined = spec1.and(spec2);

gameRepository.findAll(combined);
```

---

## 3. CriteriaBuilder : Les Outils SQL

### Les méthodes principales du CriteriaBuilder

| Méthode | SQL | Exemple |
|---------|-----|---------|
| `equal(x, y)` | `x = y` | `cb.equal(root.get("name"), "Witcher")` |
| `notEqual(x, y)` | `x != y` | `cb.notEqual(root.get("name"), "Elden")` |
| `greaterThan(x, y)` | `x > y` | `cb.greaterThan(root.get("price"), 30)` |
| `ge(x, y)` | `x >= y` | `cb.ge(root.get("price"), 30)` |
| `lessThan(x, y)` | `x < y` | `cb.lessThan(root.get("price"), 60)` |
| `le(x, y)` | `x <= y` | `cb.le(root.get("price"), 60)` |
| `like(x, y)` | `x LIKE y` | `cb.like(root.get("name"), "%Witcher%")` |
| `isNull(x)` | `x IS NULL` | `cb.isNull(root.get("description"))` |
| `isNotNull(x)` | `x IS NOT NULL` | `cb.isNotNull(root.get("description"))` |
| `in(x, ...)` | `x IN (...)` | `root.get("status").in("active", "pending")` |
| `not(x)` | `NOT x` | `cb.not(cb.equal(...))` |

### Exemples concrets

```java
// Chercher les jeux avec "witcher" dans le nom
cb.like(root.get("name"), "%witcher%")
// SQL: WHERE LOWER(name) LIKE '%witcher%'

// Chercher les jeux coûtant plus de 50€
cb.greaterThan(root.get("price"), new BigDecimal("50"))
// SQL: WHERE price > 50

// Chercher les jeux avec description vide
cb.isNull(root.get("description"))
// SQL: WHERE description IS NULL

// Chercher les jeux dont le statut est 'active' ou 'pending'
root.get("status").in("active", "pending")
// SQL: WHERE status IN ('active', 'pending')
```

---

## 4. Combinaison de Specifications : AND / OR

### Combiner avec AND

```java
Specification<Game> spec1 = (root, query, cb) ->
    cb.like(root.get("name"), "%witcher%");

Specification<Game> spec2 = (root, query, cb) ->
    cb.greaterThan(root.get("price"), new BigDecimal("30"));

// SQL: WHERE name LIKE '%witcher%' AND price > 30
Specification<Game> combined = spec1.and(spec2);
```

### Combiner avec OR

```java
Specification<Game> spec1 = (root, query, cb) ->
    cb.equal(root.get("status"), "active");

Specification<Game> spec2 = (root, query, cb) ->
    cb.equal(root.get("status"), "premium");

// SQL: WHERE status = 'active' OR status = 'premium'
Specification<Game> combined = spec1.or(spec2);
```

### Combiner plusieurs avec Specification.allOf()

```java
List<Specification<Game>> specifications = List.of(
    (root, query, cb) -> cb.like(root.get("name"), "%witcher%"),
    (root, query, cb) -> cb.greaterThan(root.get("price"), new BigDecimal("30")),
    (root, query, cb) -> cb.notNull(root.get("description"))
);

// SQL: WHERE name LIKE '%witcher%' AND price > 30 AND description IS NOT NULL
Specification<Game> combined = Specification.allOf(specifications);
gameRepository.findAll(combined, pageable);
```

---

## 5. Utiliser une Specification avec le Repository

### Step 1 : Faire hériter le Repository de JpaSpecificationExecutor

```java
@Repository
public interface GameRepository extends JpaRepository<Game, Integer>, JpaSpecificationExecutor<Game> {
    // JpaSpecificationExecutor fournit :
    // - findAll(Specification<T> spec)
    // - findOne(Specification<T> spec)
    // - count(Specification<T> spec)
    // - etc.
}
```

### Step 2 : Utiliser dans le Service

```java
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    
    private final GameRepository gameRepository;
    
    public Page<Game> find(Map<String, String> params, Pageable pageable) {
        // Créer une Specification à partir des paramètres
        Specification<Game> spec = Specification.allOf(
            SearchSpecification.search(params)  // Toolbox personnalisée (voir après)
        );
        
        // Exécuter la requête dynamique
        return gameRepository.findAll(spec, pageable);
    }
}
```

### Step 3 : Appeler depuis le Contrôleur

```java
@GetMapping
public ResponseEntity<Page<GameResponse>> find(
    @RequestParam(name = "page", required = false, defaultValue = "0") int page,
    @RequestParam(name = "size", required = false, defaultValue = "10") int size,
    @RequestParam Map<String, String> params  // Tous les paramètres de requête
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
    
    // Le service construit la Specification
    Page<Game> games = gameService.find(params, pageable);
    
    // Convertir et retourner
    Page<GameResponse> responsePage = games.map(GameResponse::fromGame);
    return ResponseEntity.ok(responsePage);
}
```

### Utilisation finale

```
GET /game?page=0&size=10&name=witcher&price_gt=30&release_date_gte=2023-01-01

↓ Spring collecte les paramètres dans Map<String, String>
↓ Le service les transforme en Specifications
↓ gameRepository.findAll(spec, pageable)
↓ Base de données retourne les résultats filtrés
↓ JSON retourné au client
```

---

## 6. La Toolbox Personnalisée : SearchSpecification + SearchOperator + SearchParam

### Le problème résolu

Sans la toolbox, créer une Specification pour chaque filtre :

```java
// ❌ Trop verbeux et répétitif
Specification<Game> spec1 = (root, query, cb) ->
    cb.like(root.get("name"), "%witcher%");

Specification<Game> spec2 = (root, query, cb) ->
    cb.greaterThan(root.get("price"), new BigDecimal("30"));

Specification<Game> spec3 = (root, query, cb) ->
    cb.lessThan(root.get("releaseDate"), LocalDate.of(2024, 1, 1));
```

Avec la toolbox :

```java
// ✅ Magique et concis
Map<String, String> params = Map.of(
    "name", "witcher",
    "GT_price", "30",
    "LT_releaseDate", "2024-01-01"
);

Specification<Game> spec = Specification.allOf(SearchSpecification.search(params));
```

### Component 1 : SearchOperator (l'Enum)

```java
public enum SearchOperator {
    EQ,        // Égal à
    NE,        // Pas égal à
    GT,        // Greater Than (>)
    GTE,       // Greater Than or Equal (>=)
    LT,        // Less Than (<)
    LTE,       // Less Than or Equal (<=)
    START,     // Commence par
    END,       // Finit par
    CONTAINS,  // Contient
    IN,        // Dans une liste
    NIN        // Pas dans une liste
}
```

**Utilisation dans l'URL** :

```
GET /game?name=witcher                    # EQ (opérateur par défaut)
GET /game?GT_price=30                      # Greater Than
GET /game?CONTAINS_description=adventur    # Contains
GET /game?IN_status=active,premium         # In
GET /game?START_name=eld                    # Starts with
```

### Component 2 : SearchParam (le parseur)

```java
@AllArgsConstructor
public class SearchParam<T> {
    
    private String field;          // Nom du champ (ex: "price")
    private SearchOperator op;     // Opérateur (ex: GT)
    private Object value;          // Valeur (ex: "30")
    
    // Parseur magique : transforme "GT_price=30" en SearchParam
    private static <T> SearchParam<T> create(Map.Entry<String, String> entry) {
        String field;
        SearchOperator op;
        String value;
        
        String[] parts = entry.getKey().split("_");
        
        if (parts.length == 1) {
            // Pas d'opérateur spécifié → utiliser EQ
            field = parts[0];
            op = SearchOperator.EQ;
        } else if (parts.length == 2) {
            // Format: "OPERATEUR_champ"
            op = SearchOperator.valueOf(parts[0].toUpperCase());
            field = parts[1];
        } else {
            throw new IllegalArgumentException("Invalid search parameter: " + entry.getKey());
        }
        
        value = entry.getValue();
        
        return new SearchParam<T>(field, op, value);
    }
    
    // Convertit Map entière en List<SearchParam>
    public static <T> List<SearchParam<T>> create(Map<String, String> params) {
        return params.entrySet().stream()
            .filter(e -> !e.getKey().equals("page") 
                      && !e.getKey().equals("size") 
                      && !e.getKey().equals("sort"))  // Ignorer les paramètres de pagination
            .map(SearchParam::<T>create)
            .toList();
    }
}
```

**Comment ça fonctionne** :

```java
// Requête HTTP: GET /game?GT_price=30&name=witcher

Map<String, String> params = {
    "GT_price" -> "30",
    "name" -> "witcher",
    "page" -> "0",
    "size" -> "10"
};

// SearchParam.create(params) produit:
List<SearchParam<Game>> searchParams = [
    SearchParam(field="price", op=GT, value="30"),
    SearchParam(field="name", op=EQ, value="witcher")
    // page et size sont ignorés
]
```

### Component 3 : SearchSpecification (le générateur)

```java
public interface SearchSpecification {
    
    // Transforme UN SearchParam en Specification
    private static <T> Specification<T> search(SearchParam<T> searchParam) {
        return (root, query, cb) -> switch (searchParam.getOp()) {
            
            case EQ -> cb.equal(
                cb.lower(root.get(searchParam.getField())),
                searchParam.getValue().toString().toLowerCase()
            );
            // SQL: WHERE LOWER(field) = LOWER(value)
            
            case NE -> cb.notEqual(
                cb.lower(root.get(searchParam.getField())),
                searchParam.getValue().toString().toLowerCase()
            );
            
            case GT -> {
                try {
                    Number number = new BigDecimal(searchParam.getValue().toString());
                    yield cb.gt(root.get(searchParam.getField()), number);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Value must be a number");
                }
            }
            // SQL: WHERE field > value
            
            case GTE -> {
                try {
                    Number number = new BigDecimal(searchParam.getValue().toString());
                    yield cb.ge(root.get(searchParam.getField()), number);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Value must be a number");
                }
            }
            // SQL: WHERE field >= value
            
            case LT -> {
                try {
                    Number number = new BigDecimal(searchParam.getValue().toString());
                    yield cb.lt(root.get(searchParam.getField()), number);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Value must be a number");
                }
            }
            // SQL: WHERE field < value
            
            case LTE -> {
                try {
                    Number number = new BigDecimal(searchParam.getValue().toString());
                    yield cb.le(root.get(searchParam.getField()), number);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Value must be a number");
                }
            }
            // SQL: WHERE field <= value
            
            case START -> cb.like(
                cb.lower(root.get(searchParam.getField())),
                searchParam.getValue().toString().toLowerCase() + "%"
            );
            // SQL: WHERE LOWER(field) LIKE LOWER(value)%
            
            case END -> cb.like(
                cb.lower(root.get(searchParam.getField())),
                "%" + searchParam.getValue().toString().toLowerCase()
            );
            // SQL: WHERE LOWER(field) LIKE %LOWER(value)
            
            case CONTAINS -> cb.like(
                cb.lower(root.get(searchParam.getField())),
                "%" + searchParam.getValue().toString().toLowerCase() + "%"
            );
            // SQL: WHERE LOWER(field) LIKE %LOWER(value)%
            
            case IN -> root.get(searchParam.getField())
                .in(((String)searchParam.getValue()).split(","));
            // SQL: WHERE field IN (val1, val2, val3)
            
            case NIN -> cb.not(root.get(searchParam.getField()))
                .in(((String)searchParam.getValue()).split(","));
            // SQL: WHERE field NOT IN (val1, val2, val3)
        };
    }
    
    // Transforme PLUSIEURS SearchParam en List<Specification>
    static <T> List<Specification<T>> search(Map<String, String> params) {
        List<SearchParam<T>> searchParams = SearchParam.create(params);
        return searchParams.stream()
            .map(SearchSpecification::search)
            .toList();
    }
}
```

### Flux Complet de la Toolbox

```
Requête HTTP
GET /game?GT_price=30&CONTAINS_name=witcher&page=0&size=10
                           ↓
                    Map<String, String>
                    {
                      "GT_price" -> "30",
                      "CONTAINS_name" -> "witcher",
                      "page" -> "0",
                      "size" -> "10"
                    }
                           ↓
         SearchParam.create(params)
                    ↓
         List<SearchParam<Game>>
         [
           SearchParam(field="price", op=GT, value="30"),
           SearchParam(field="name", op=CONTAINS, value="witcher")
         ]
                           ↓
       SearchSpecification.search(params)
                    ↓
       List<Specification<Game>>
       [
         (root, query, cb) -> cb.gt(root.get("price"), 30),
         (root, query, cb) -> cb.like(root.get("name"), "%witcher%")
       ]
                           ↓
     Specification.allOf(specifications)
                    ↓
     Specification<Game> combinée
     (avec AND implicite)
                           ↓
   gameRepository.findAll(spec, pageable)
                           ↓
     SELECT * FROM game
     WHERE price > 30
     AND LOWER(name) LIKE LOWER('%witcher%')
     LIMIT 10 OFFSET 0
                           ↓
                   Page<Game>
                           ↓
              JSON Response
```

---

## 7. Exemples Pratiques : Utilisation Réelle

### Exemple 1 : Chercher un jeu par nom

```
Requête :
GET /game?name=witcher

Map :
{ "name" -> "witcher" }

SearchParam :
SearchParam(field="name", op=EQ, value="witcher")

Specification :
cb.equal(root.get("name"), "witcher")

SQL généré :
SELECT * FROM game WHERE LOWER(name) = LOWER('witcher')
```

### Exemple 2 : Chercher les jeux chers (prix > 50€)

```
Requête :
GET /game?GT_price=50

Map :
{ "GT_price" -> "50" }

SearchParam :
SearchParam(field="price", op=GT, value="50")

Specification :
cb.gt(root.get("price"), new BigDecimal("50"))

SQL généré :
SELECT * FROM game WHERE price > 50
```

### Exemple 3 : Chercher les jeux dont le nom contient "elden"

```
Requête :
GET /game?CONTAINS_name=elden

Map :
{ "CONTAINS_name" -> "elden" }

SearchParam :
SearchParam(field="name", op=CONTAINS, value="elden")

Specification :
cb.like(root.get("name"), "%elden%")

SQL généré :
SELECT * FROM game WHERE LOWER(name) LIKE LOWER('%elden%')
```

### Exemple 4 : Chercher les jeux avec plusieurs conditions

```
Requête :
GET /game?GT_price=30&LT_price=60&CONTAINS_name=witcher&page=0&size=10

Map :
{
  "GT_price" -> "30",
  "LT_price" -> "60",
  "CONTAINS_name" -> "witcher",
  "page" -> "0",
  "size" -> "10"
}

SearchParams :
[
  SearchParam(field="price", op=GT, value="30"),
  SearchParam(field="price", op=LT, value="60"),
  SearchParam(field="name", op=CONTAINS, value="witcher")
]

SQL généré :
SELECT * FROM game
WHERE price > 30
  AND price < 60
  AND LOWER(name) LIKE LOWER('%witcher%')
LIMIT 10 OFFSET 0
```

### Exemple 5 : Chercher les jeux d'une certaine collection (IN)

```
Requête :
GET /game?IN_status=active,premium,beta

Map :
{ "IN_status" -> "active,premium,beta" }

SearchParam :
SearchParam(field="status", op=IN, value="active,premium,beta")

Specification :
root.get("status").in(["active", "premium", "beta"])

SQL généré :
SELECT * FROM game WHERE status IN ('active', 'premium', 'beta')
```

---

## 8. Cas Avancé : Utiliser GameSpecification Personnalisée

Pour certains filtres, vous voudrez des Specifications personnalisées plus complexes :

```java
public class GameSpecification {
    
    // Recherche simple par nom avec LIKE
    public static Specification<Game> hasName(String name) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + name.toLowerCase() + "%"
            );
    }
    
    // Chercher avec jointure (si besoin de données related)
    public static Specification<Game> joinCategory() {
        return (root, query, criteriaBuilder) -> {
            root.fetch("category");  // JOIN la table category
            return null;
        };
    }
}
```

**Utilisation combinée** :

```java
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    
    private final GameRepository gameRepository;
    
    @Override
    public Page<Game> find(Map<String, String> params, Pageable pageable) {
        // Combiner la Specification personnalisée avec les filtres dynamiques
        Specification<Game> specs = GameSpecification.joinCategory()
            .and(Specification.allOf(SearchSpecification.search(params)));
        
        return gameRepository.findAll(specs, pageable);
    }
}
```

---

## 9. Bonnes Pratiques

### ✅ À FAIRE

1. **Ignorer les paramètres de pagination**
   ```java
   .filter(e -> !e.getKey().equals("page") 
            && !e.getKey().equals("size") 
            && !e.getKey().equals("sort"))
   ```

2. **Convertir en minuscules pour les recherches texte**
   ```java
   cb.equal(cb.lower(root.get("name")), value.toLowerCase())
   ```

3. **Valider les nombres**
   ```java
   try {
       Number number = new BigDecimal(value);
   } catch (NumberFormatException e) {
       throw new RuntimeException("Value must be a number");
   }
   ```

4. **Utiliser Specification.allOf() pour AND**
   ```java
   Specification.allOf(specifications)  // AND implicite
   ```

### ❌ À ÉVITER

1. **Pas de vérification SQL Injection**
   - Les Specifications utilisent PreparedStatements, c'est safe !

2. **Mélanger Specifications et @Query**
   ```java
   // Difficile à maintenir
   @Query("SELECT g FROM Game g WHERE ...")
   Page<Game> findComplex(String name, Pageable p);
   ```

3. **Créer trop de Specifications personnalisées**
   - Utiliser la toolbox pour la majorité des cas

---

## Résumé : Architecture des Specifications

### À retenir

1. **Specification<T>** = une fonction qui retourne une condition SQL

2. **CriteriaBuilder** = outils pour construire les conditions

3. **Toolbox SearchSpecification** :
   - `SearchOperator` : Les 11 opérateurs
   - `SearchParam` : Parse les paramètres de requête
   - `SearchSpecification` : Crée les Specifications

4. **Utilisation simple** :
   ```java
   // Dans le contrôleur
   @RequestParam Map<String, String> params
   
   // Dans le service
   Specification<T> spec = Specification.allOf(SearchSpecification.search(params));
   repository.findAll(spec, pageable);
   ```

5. **URL Magique** :
   ```
   GET /game?GT_price=30&CONTAINS_name=witcher&IN_status=active,premium
   ```
   Génère automatiquement la requête SQL appropriée !

### Flux Mémoire

```
URL params
    ↓ (Map<String, String>)
SearchParam.create()
    ↓ (List<SearchParam>)
SearchSpecification.search()
    ↓ (List<Specification>)
Specification.allOf()
    ↓ (Specification combinée)
Repository.findAll(spec)
    ↓
SQL + Résultats
```

### Opérateurs Courants

| Cas | Opérateur | Exemple |
|-----|-----------|---------|
| Recherche texte simple | `CONTAINS` | `?CONTAINS_name=witcher` |
| Filtre numérique | `GT`, `LT` | `?GT_price=30&LT_price=60` |
| Dates | `GTE`, `LTE` | `?GTE_releaseDate=2023-01-01` |
| Énumé/Catégorie | `IN` | `?IN_status=active,premium` |
| Début du texte | `START` | `?START_name=eld` |
| Fin du texte | `END` | `?END_name=ring` |

**C'est ça qui rend votre API puissante et flexible !** 🚀
