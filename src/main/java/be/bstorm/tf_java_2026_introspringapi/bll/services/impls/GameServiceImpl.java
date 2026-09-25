package be.bstorm.tf_java_2026_introspringapi.bll.services.impls;

import be.bstorm.tf_java_2026_introspringapi.bll.services.GameService;
import be.bstorm.tf_java_2026_introspringapi.bll.specifications.GameSpecification;
import be.bstorm.tf_java_2026_introspringapi.bll.specifications.SearchSpecification;
import be.bstorm.tf_java_2026_introspringapi.bll.utils.FileUtils;
import be.bstorm.tf_java_2026_introspringapi.dal.repositories.GameRepository;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Implémentation de la logique métier pour les Games (jeux vidéo).
 * 
 * Responsabilités principales:
 * - Coordonne l'accès aux données via GameRepository
 * - Gère la persistance des images via FileUtils
 * - Optimise les performances avec le cache Redis (@Cacheable/@CacheEvict)
 * 
 * CACHING avec Redis:
 * - @Cacheable: si le résultat existe en cache, le retourner sans requête BDD
 * - @CacheEvict: invalider le cache quand les données changent (update/delete)
 * - Clé du cache: construction dynamique (pageNumber + pageSize + sort)
 * 
 * RECHERCHE DYNAMIQUE:
 * - SearchSpecification.search(params) construit des critères SQL dynamiquement
 * - Les params (name, minPrice, etc.) viennent de l'URL et deviennent WHERE clauses
 * 
 * IMAGE UPLOAD:
 * - Vérifie que le fichier n'est pas vide avant sauvegarde
 * - FileUtils.saveFile() retourne l'URL du fichier stocké
 * - URL est persistée dans la colonne Game.imageUrl en BDD
 * 
 * @see GameRepository pour les requêtes BDD
 * @see FileUtils pour la gestion des fichiers
 * @see SearchSpecification pour la recherche dynamique
 */
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final FileUtils fileUtils;

    @Override
    @Cacheable(cacheNames = "games", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<Game> find(Map<String, String> params, Pageable pageable) {
        // RECHERCHE DYNAMIQUE: construire les critères SQL à partir des params
        // params = {name: "Mario", minPrice: "10"} → WHERE name LIKE "%Mario%" AND price >= 10
        // SearchSpecification.search() analyse la Map et crée des Specification dynamiquement
        Specification<Game> specs = Specification.allOf(SearchSpecification.search(params));

        // Requête BDD avec pagination et critères construits dynamiquement
        // Pageable définit: numéro de page, taille, ordre de tri (name ASC, price DESC, etc.)
        // CACHE: résultat stocké en Redis avec la clé = pageNumber + pageSize + sort
        // Prochaine requête identique? Retour immédiat du cache sans requête BDD
        return  gameRepository.findAll(specs, pageable);
    }

    @Override
    @Cacheable(cacheNames = "game", key = "#id")
    public Game findById(Integer id) {
        // Récupérer un Game par son ID
        // findById retourne Optional.empty() si pas trouvé
        // orElseThrow() lève une exception si vide (déjà gérée par le contrôleur)
        // CACHE: ce Game est stocké en Redis avec clé = id
        // Prochaine recherche du même id? Retour immédiat du cache
        return gameRepository.findById(id)
                .orElseThrow();
    }

    @Override
    @CacheEvict(cacheNames = "games", allEntries = true)
    public Game save(Game game, MultipartFile image) {
        // Gestion optionnelle de l'image
        if(image != null && !image.isEmpty()){
            // Image fournie ET non vide → la sauvegarder
            // FileUtils.saveFile() écrit le fichier sur disque et retourne son URL
            String imageUrl = fileUtils.saveFile(image);
            // Stocker l'URL en base (pour retrouver le fichier plus tard)
            game.setImageUrl(imageUrl);
        }
        // Sinon: pas d'image fournie, imageUrl reste null ou sa valeur actuelle

        // Sauvegarder le Game en BDD
        // CACHE EVICT: invalider le cache "games" car les données ont changé
        // (une nouvelle entrée a été ajoutée → la pagination peut être différente)
        return gameRepository.save(game);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "game", key = "#id"),
        @CacheEvict(value = "games", allEntries = true)
    })
    public void update(Integer id, Game game, MultipartFile image) {
        // Vérification 1: chercher le Game existant (sinon exception)
        Game existing = gameRepository.findById(id)
                .orElseThrow();

        // Vérification 2: mettre à jour le nom
        existing.setName(game.getName());

        // Vérification 3: gérer optionnellement la nouvelle image
        if(image != null && !image.isEmpty()){
            // Nouvelle image fournie → sauvegarder et remplacer l'ancienne URL
            String imageUrl = fileUtils.saveFile(image);
            existing.setImageUrl(imageUrl);
        }
        // Sinon: garder l'image existante

        // Sauvegarder les modifications en BDD
        // CACHE EVICT (double): 
        // - "game" avec clé id: invalider le Game spécifique modifié
        // - "games" avec allEntries: invalider toutes les pages (le tri/contenu peut changer)
        gameRepository.save(existing);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "game", key = "#id"),
        @CacheEvict(value = "games", allEntries = true)
    })
    public void delete(Integer id) {
        // Vérification: le Game existe-t-il?
        // Utiliser existsById() (plus efficace qu'une requête SELECT complète)
        if(!gameRepository.existsById(id)){
            throw new RuntimeException("Game with id " + id + " does not exist");
        }

        // Supprimer le Game en BDD via son ID
        // Note: si @SoftDelete est actif (Game entity), ce sera un UPDATE (is_enable = false)
        // sinon, ce sera un vrai DELETE
        gameRepository.deleteById(id);
        
        // CACHE EVICT (double):
        // - "game" avec clé id: invalider le Game supprimé
        // - "games" avec allEntries: invalider toutes les pages (une entrée a disparu)
    }
}
