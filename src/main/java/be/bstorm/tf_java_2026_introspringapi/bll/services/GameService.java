package be.bstorm.tf_java_2026_introspringapi.bll.services;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Gère la logique métier des Games: création, recherche, modification.
 * Applique les validations métier et coordonne l'accès aux données.
 * Le cache Redis réduit les requêtes BDD pour find() et findById().
 */
public interface GameService {

    /**
     * Trouve une page de Games selon les critères et pagination.
     * Utilise des Specifications pour les recherches dynamiques.
     * @param params filtres de recherche (name, minPrice, etc.)
     * @param pageable numéro + taille page + tri
     * @return Page de Games trouvés
     */
    Page<Game> find(Map<String, String> params, Pageable pageable);

    /**
     * Trouve un Game par son ID.
     * Résultat mis en cache pour les accès répétés.
     * @param id identifiant du Game
     * @return le Game trouvé, exception si absent
     */
    Game findById(Integer id);

    /**
     * Crée un nouveau Game avec upload d'image optionnel.
     * @param game données du Game (name, releaseYear, price)
     * @param image fichier image optionnel
     * @return le Game créé avec ID généré
     */
    Game save(Game game, MultipartFile image);

    /**
     * Modifie un Game existant et son image.
     * @param id identifiant du Game à modifier
     * @param game données mises à jour
     * @param image nouvelle image optionnelle
     */
    void update(Integer id, Game game, MultipartFile image);

    /**
     * Supprime un Game (logiquement, via SoftDelete).
     * @param id identifiant du Game à supprimer
     */
    void delete(Integer id);
}
