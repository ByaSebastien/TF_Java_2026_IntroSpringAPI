package be.bstorm.tf_java_2026_introspringapi.bll.specifications;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import org.springframework.data.jpa.domain.Specification;

public class GameSpecification {

    public static Specification<Game> hasName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

//    public static Specification<Game> joinCategory() {
//        return (root, query, criteriaBuilder) -> {
//            root.fetch("category");
//            return null;
//        };
//    }
}
