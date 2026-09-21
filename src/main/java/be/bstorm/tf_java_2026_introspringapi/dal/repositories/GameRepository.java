package be.bstorm.tf_java_2026_introspringapi.dal.repositories;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
}
