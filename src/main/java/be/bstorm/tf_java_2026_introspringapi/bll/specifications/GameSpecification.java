package be.bstorm.tf_java_2026_introspringapi.bll.specifications;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import org.springframework.data.jpa.domain.Specification;

/**
 * Spécifications pré-construites pour filtrer les Games.
 * Utilisées pour l'optimisation des requêtes complexes.
 */
public class GameSpecification {

    /**
     * Filtre les Games par nom (recherche case-insensitive).
     * @param name fragment du nom à chercher
     * @return Specification pour filtering JPA
     */
    public static Specification<Game> hasName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")), 
                "%" + name.toLowerCase() + "%"
        );
    }
}
