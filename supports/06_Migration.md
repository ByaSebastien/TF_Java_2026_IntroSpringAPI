# Guide complet Flyway - Migrations de base de données

## 📋 Table des matières
1. [Introduction](#introduction)
2. [Installation et configuration](#installation-et-configuration)
3. [Concepts fondamentaux](#concepts-fondamentaux)
4. [Stratégies de nommage](#stratégies-de-nommage)
5. [Scénarios pratiques](#scénarios-pratiques)
6. [Problèmes courants et solutions](#problèmes-courants-et-solutions)
7. [Bonnes pratiques](#bonnes-pratiques)

---

## Introduction

**Flyway** est un outil de gestion des migrations de schéma de base de données. Il permet de versionner et d'automatiser les changements de BD de manière reproductible et traçable.

### Avantages
- ✅ Version control pour la BD
- ✅ Déploiement automatisé et reproductible
- ✅ Historique complet des changements
- ✅ Prévention de conflits de versions
- ✅ Rollback facile (pour les migrations undo)

---

## Installation et configuration

### 1. Ajouter les dépendances dans `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>

<!-- Support PostgreSQL 17+ -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <version>10.15.0</version>
</dependency>

<!-- Support MySQL -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
    <version>10.15.0</version>
</dependency>
```

### 2. Configuration dans `application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/game_db
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Important: ne pas utiliser 'create' ni 'update'
    show-sql: false
  
  flyway:
    enabled: true
    baseline-on-migrate: false  # true si BD existante sans Flyway
    out-of-order: false         # true pour exécuter migrations hors ordre
    locations: classpath:migration  # Dossier des migrations
```

### 3. Structure du projet

```
src/
├── main/
│   ├── java/
│   └── resources/
│       ├── application.yaml
│       └── migration/          ← Dossier des migrations
│           ├── V1__InitDb.sql
│           ├── V2__SeedData.sql
│           ├── V3__AddUserColumns.sql
│           └── U1__CreateFunction.sql  (undo)
```

---

## Concepts fondamentaux

### Version des migrations

| Nom | Format | Exemple | Utilisation |
|-----|--------|---------|-------------|
| **Versioned** | `V<version>__<description>.sql` | `V1__InitDb.sql` | Migration principale (ascendante) |
| **Undo** | `U<version>__<description>.sql` | `U1__DropTable.sql` | Annuler une migration |
| **Repeatable** | `R__<description>.sql` | `R__CreateViews.sql` | Exécutée à chaque changement |

### Le fichier `flyway_schema_history`

Flyway crée automatiquement cette table pour tracker les migrations exécutées:

```sql
-- Créée automatiquement
CREATE TABLE flyway_schema_history (
    installed_rank INTEGER,
    version VARCHAR(50),
    description VARCHAR(255),
    type VARCHAR(20),
    script VARCHAR(1000),
    checksum INTEGER,
    installed_by VARCHAR(100),
    installed_on TIMESTAMP,
    execution_time INTEGER,
    success BOOLEAN
);
```

---

## Stratégies de nommage

### 1. Versioning linéaire (RECOMMANDÉ)

```
V1__Create_tables.sql
V2__SeedData.sql
V3__Add_user_status.sql
V4__Create_indexes.sql
V5__Add_wishlist_feature.sql
```

**Avantages:**
- Ordre d'exécution clair
- Pas de conflits
- Facile à merger en git

**Inconvénients:**
- Peut créer de longs numéros

### 2. Versioning par date

```
V2024_09_24_1__Create_tables.sql
V2024_09_24_2__SeedData.sql
V2024_09_25_1__Add_columns.sql
```

**Avantages:**
- Facile de savoir quand un changement a eu lieu
- Merge-friendly en parallèle

**Inconvénients:**
- Format plus long

### 3. Versioning sémantique (MOINS RECOMMANDÉ)

```
V1_0__Initial_schema.sql
V1_1__Add_user_role.sql
V1_2__Add_game_columns.sql
V2_0__Refactor_tables.sql
```

**Problème:** Les migrations doivent être séquentielles (V1.1 avant V2.0)

---

## Scénarios pratiques

### Scénario 1: Projet de zéro

#### Jour 1 - Créer le schéma initial

**Fichier:** `V1__InitDb.sql`

```sql
-- Roles
CREATE TABLE role_ (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Users
CREATE TABLE user_ (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id INTEGER NOT NULL REFERENCES role_(id),
    is_enable BOOLEAN NOT NULL DEFAULT true,
    birthday DATE
);

-- Games
CREATE TABLE game (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    release_year INTEGER NOT NULL,
    price INTEGER NOT NULL,
    image_url VARCHAR(255),
    is_enable BOOLEAN NOT NULL DEFAULT true
);

-- Wishlist
CREATE TABLE wishlist (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_(id) ON DELETE CASCADE,
    game_id INTEGER NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, game_id)
);

-- Indexes
CREATE INDEX idx_user_username ON user_(username);
CREATE INDEX idx_wishlist_user ON wishlist(user_id);
CREATE INDEX idx_game_name ON game(name);
```

#### Jour 2 - Seed les données

**Fichier:** `V2__SeedData.sql`

```sql
-- Insert roles
INSERT INTO role_ (name) VALUES ('user');
INSERT INTO role_ (name) VALUES ('admin');

-- Insert users
INSERT INTO user_ (username, password, role_id, is_enable) 
VALUES 
    ('user', '$2a$10$...', 1, true),
    ('admin', '$2a$10$...', 2, true);

-- Insert games
INSERT INTO game (name, release_year, price, is_enable) 
VALUES 
    ('Devil may cry', 2001, 5000, true),
    ('Onimusha', 2001, 3000, true),
    ('Resident evil', 1996, 10000, true),
    ('League of legend', 2009, 1000000, true);
```

---

### Scénario 2: Ajouter une nouvelle colonne

**Contexte:** Vous voulez ajouter un champ `description` aux jeux.

**Fichier:** `V3__Add_game_description.sql`

```sql
-- Ajouter la colonne (avec contrainte NOT NULL et valeur par défaut)
ALTER TABLE game 
ADD COLUMN description VARCHAR(500) DEFAULT '';

-- Mettre à jour les données existantes
UPDATE game SET description = 'Description du jeu' WHERE name = 'Devil may cry';

-- Rendre la colonne NOT NULL après les données
ALTER TABLE game 
MODIFY COLUMN description VARCHAR(500) NOT NULL;
```

**Résultat:** Les anciens enregistrements obtiennent une description vide, les nouvelles lignes doivent fournir une description.

---

### Scénario 3: Renommer une colonne

**Contexte:** Renommer `release_year` en `year_of_release`.

**Fichier:** `V4__Rename_game_release_year.sql`

```sql
-- PostgreSQL
ALTER TABLE game RENAME COLUMN release_year TO year_of_release;

-- MySQL
ALTER TABLE game CHANGE COLUMN release_year year_of_release INT NOT NULL;

-- SQL Server
EXEC sp_rename 'game.release_year', 'year_of_release', 'COLUMN';
```

---

### Scénario 4: Supprimer une colonne (avec retour possible)

**Contexte:** Vous supprimez `birthday` mais vous devez pouvoir revenir en arrière.

**Fichier:** `V5__Remove_user_birthday.sql`

```sql
-- Migration principale (supprime la colonne)
ALTER TABLE user_ DROP COLUMN birthday;
```

**Fichier:** `U5__Restore_user_birthday.sql` (undo)

```sql
-- Undo: restaure la colonne
ALTER TABLE user_ ADD COLUMN birthday DATE;
```

**Activation de l'undo:**
```yaml
spring:
  flyway:
    mixed: true  # Autoriser migrations versioned + undo
```

---

### Scénario 5: Changer le type de données

**Contexte:** Passer `price` de `INTEGER` à `DECIMAL(10, 2)`.

**Fichier:** `V6__Change_game_price_type.sql`

```sql
-- PostgreSQL: créer colonne temporaire
ALTER TABLE game ADD COLUMN price_decimal DECIMAL(10, 2);

-- Copier et convertir les données
UPDATE game SET price_decimal = CAST(price AS DECIMAL(10, 2));

-- Supprimer l'ancienne colonne
ALTER TABLE game DROP COLUMN price;

-- Renommer la nouvelle
ALTER TABLE game RENAME COLUMN price_decimal TO price;

-- Ajouter une contrainte NOT NULL si nécessaire
ALTER TABLE game ALTER COLUMN price SET NOT NULL;
```

---

### Scénario 6: Ajouter une nouvelle table avec clé étrangère

**Contexte:** Ajouter une table `Review` pour les avis des jeux.

**Fichier:** `V7__Create_review_table.sql`

```sql
CREATE TABLE review (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_(id) ON DELETE CASCADE,
    game_id INTEGER NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, game_id)
);

CREATE INDEX idx_review_game ON review(game_id);
CREATE INDEX idx_review_user ON review(user_id);
```

---

### Scénario 7: Ajouter une contrainte CHECK

**Contexte:** Ajouter une validation que le prix doit être positif.

**Fichier:** `V8__Add_game_price_constraint.sql`

```sql
-- PostgreSQL
ALTER TABLE game ADD CONSTRAINT ck_game_price_positive CHECK (price > 0);

-- MySQL
ALTER TABLE game ADD CONSTRAINT ck_game_price_positive CHECK (price > 0);
```

---

### Scénario 8: Vue (Repeatable)

**Contexte:** Créer une vue pour les stats des jeux.

**Fichier:** `R__Create_game_stats_view.sql` (Repeatable)

```sql
-- Repeatable: exécutée à chaque changement du fichier
DROP VIEW IF EXISTS game_stats CASCADE;

CREATE VIEW game_stats AS
SELECT 
    g.id,
    g.name,
    COUNT(DISTINCT w.user_id) as wishlist_count,
    COUNT(DISTINCT r.id) as review_count,
    AVG(r.rating) as average_rating
FROM game g
LEFT JOIN wishlist w ON g.id = w.game_id
LEFT JOIN review r ON g.id = r.game_id
GROUP BY g.id, g.name;
```

---

## Problèmes courants et solutions

### Problème 1: "Unsupported Database: PostgreSQL 17.6"

**Cause:** Flyway version trop ancienne ne supporte pas PostgreSQL 17.

**Solution:**

```xml
<!-- Ajouter cette dépendance -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <version>10.15.0</version>
</dependency>
```

Ou mettre à jour Spring Boot à 4.2.x+.

---

### Problème 2: "Migration V1__InitDb.sql failed" - Conflit de contraintes

**Cause:** Vous essayez de créer une contrainte UNIQUE sur une colonne qui a des doublons.

**Solution:**

```sql
-- AVANT: Nettoyer les doublons
DELETE FROM user_ WHERE id NOT IN (
    SELECT MIN(id) FROM user_ GROUP BY username
);

-- PUIS: Ajouter la contrainte
ALTER TABLE user_ ADD CONSTRAINT uc_user_username UNIQUE (username);
```

---

### Problème 3: "Checksum mismatch" - Fichier modifié après exécution

**Cause:** Vous avez modifié un fichier déjà exécuté (ex: V1__InitDb.sql).

**Solution:** NE JAMAIS modifier les migrations exécutées. Créer une nouvelle migration:

```sql
-- MAUVAIS ❌
-- Modifier V1__InitDb.sql après exécution

-- BON ✅
-- Créer V2__FixInitDb.sql
ALTER TABLE user_ ADD CONSTRAINT uc_user_email UNIQUE (email);
```

---

### Problème 4: BD existante sans Flyway

**Cause:** Vous avez une BD existante et vous voulez commencer avec Flyway.

**Solution:** Utiliser baseline

```yaml
spring:
  flyway:
    baseline-on-migrate: true  # Crée flyway_schema_history
    baseline-version: 0        # Version de départ
```

Ou en ligne de commande:
```bash
mvn flyway:baseline -Dflyway.baselineVersion=0
```

---

### Problème 5: Migration bloquée (out of order)

**Cause:** Vous avez créé V1, V3 mais pas V2. Flyway refuse d'exécuter V3.

**Solution 1:** Créer V2 (recommandé)

```sql
-- V2__MissingMigration.sql
-- Ajouter ici ce qui manquait
```

**Solution 2:** Autoriser les migrations hors ordre (déconseillé)

```yaml
spring:
  flyway:
    out-of-order: true
```

---

### Problème 6: Erreur lors d'une migration - Flyway bloqué

**Cause:** Une migration V3 a échoué. Flyway refuse de continuer.

**Solution:** 

1. Vérifier l'erreur dans les logs
2. Corriger la BD manuellement SI NÉCESSAIRE
3. Mettre à jour `flyway_schema_history` pour marquer comme succès:

```sql
UPDATE flyway_schema_history 
SET success = true 
WHERE version = '3' AND success = false;
```

---

### Problème 7: Performance - Grosse migration bloque l'app

**Cause:** Une migration V5 fait un UPDATE sur 1M de lignes et ça prend 10 min.

**Solution:**

```sql
-- V5__Bulk_update_users.sql

-- Mettre à jour par batch
UPDATE user_ SET status = 'active' WHERE id <= 100000;
UPDATE user_ SET status = 'active' WHERE id > 100000 AND id <= 200000;
-- ... etc

-- Ou créer un index AVANT pour accélérer
CREATE INDEX idx_user_status ON user_(status);

-- Puis mettre à jour
UPDATE user_ SET status = 'active';
```

---

## Bonnes pratiques

### ✅ À FAIRE

1. **Une migration = un changement logique**
   ```sql
   -- BON: V3__Add_user_status.sql (un seul changement)
   ALTER TABLE user_ ADD COLUMN status VARCHAR(20);
   CREATE INDEX idx_user_status ON user_(status);
   ```

2. **Utiliser des noms descriptifs**
   ```
   V1__Create_initial_schema.sql        ✅
   V1__Schema.sql                       ❌
   ```

3. **Tester les migrations en local**
   ```bash
   mvn clean flyway:migrate
   ```

4. **Ne jamais modifier les migrations exécutées**
   ```
   Si erreur → créer V_NEXT pour corriger
   ```

5. **Documenter les changements complexes**
   ```sql
   -- Migration V5: Renomme price_usd en price
   -- Impact: Aucun changement applicatif (compatibilité JPA)
   ALTER TABLE game RENAME COLUMN price_usd TO price;
   ```

6. **Valider après chaque migration**
   ```bash
   # Vérifier que la BD démarre correctement
   mvn spring-boot:run
   ```

---

### ❌ À ÉVITER

1. **Modifier une migration déjà exécutée**
   ```sql
   ❌ Corriger V1__InitDb.sql après deployment
   ```

2. **Mélanger DDL et DML sans précaution**
   ```sql
   ❌ CREATE TABLE users; INSERT INTO users ...;
      ALTER TABLE users ...;  -- Peut échouer si INSERT échoue
   ```

3. **Grandes migrations sans index**
   ```sql
   ❌ UPDATE table SET column = value;  -- Sur 10M de lignes
   ✅ CREATE INDEX; UPDATE; ANALYZE;
   ```

4. **Dépendre d'un état applicatif**
   ```sql
   ❌ DELETE FROM users WHERE status = 'inactive';
      -- L'app change le statut, les données disparaissent!
   ```

5. **Ignorer les cascades**
   ```sql
   ❌ DELETE FROM users;  -- Cascade supprime wishlist, reviews...
   ✅ DELETE FROM users WHERE condition; -- Précis
   ```

---

## Commandes utiles

```bash
# Afficher l'état des migrations
mvn flyway:info

# Exécuter les migrations
mvn flyway:migrate

# Baseline une BD existante
mvn flyway:baseline

# Nettoyer flyway_schema_history (⚠️ DANGEREUX)
mvn flyway:clean

# Valider les migrations sans les exécuter
mvn flyway:validate

# Réparation (si echec)
mvn flyway:repair
```

---

## Exemple complet: Évolution d'un projet réel

### Mois 1: MVP (V1-V2)
```
V1__InitDb.sql              → Schéma initial (users, roles, games, wishlist)
V2__SeedData.sql            → Données de test
```

### Mois 2: Feature wishlist (V3)
```
V3__Add_review_table.sql    → Nouveau système d'avis
```

### Mois 3: Bug fixes (V4-V5)
```
V4__Fix_user_unique_constraint.sql   → Corriger doublons
V5__Add_audit_columns.sql            → created_at, updated_at
```

### Mois 4: Performance (V6-V7)
```
V6__Create_game_stats_view.sql       → Vue pour stats
V7__Add_missing_indexes.sql          → Optimiser requêtes
```

### Mois 5: Refactor (V8-V9)
```
V8__Rename_legacy_columns.sql        → Nettoyage technique
V9__Add_soft_delete.sql              → is_active au lieu de DELETE
```

---

## Résumé

| Aspect | Recommandation |
|--------|----------------|
| **Versioning** | Linéaire (V1, V2, V3...) |
| **Nommage** | `V<N>__<Description>.sql` |
| **Localisation** | `src/main/resources/migration/` |
| **ddl-auto** | `validate` (jamais `create` ou `update`) |
| **Modifications** | Jamais modifier une migration exécutée |
| **Tests** | Exécuter localement avant push |
| **Taille** | Une migration = un changement logique |
| **Documentation** | Commenter les changements complexes |

---

**Flyway = Paix d'esprit pour la gestion de votre schéma BD! 🚀**
