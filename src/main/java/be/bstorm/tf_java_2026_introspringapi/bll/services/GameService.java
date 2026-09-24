package be.bstorm.tf_java_2026_introspringapi.bll.services;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface GameService {

    Page<Game> find(Map<String, String> params, Pageable pageable);
    Game findById(Integer id);
    Game save(Game game, MultipartFile image);
    void update(Integer id, Game game, MultipartFile image);
    void delete(Integer id);
}
