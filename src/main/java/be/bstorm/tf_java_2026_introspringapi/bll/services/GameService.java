package be.bstorm.tf_java_2026_introspringapi.bll.services;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GameService {

    Page<Game> find(Pageable pageable);
    Game findById(Integer id);
    Game save(Game game);
    void update(Integer id, Game game);
    void delete(Integer id);
}
