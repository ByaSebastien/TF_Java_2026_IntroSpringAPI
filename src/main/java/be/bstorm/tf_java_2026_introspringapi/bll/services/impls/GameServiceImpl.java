package be.bstorm.tf_java_2026_introspringapi.bll.services.impls;

import be.bstorm.tf_java_2026_introspringapi.bll.services.GameService;
import be.bstorm.tf_java_2026_introspringapi.dal.repositories.GameRepository;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    @Override
    public Page<Game> find(Pageable pageable) {
        return  gameRepository.findAll(pageable);
    }

    @Override
    public Game findById(Integer id) {
        return gameRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public Game save(Game game) {
        return gameRepository.save(game);
    }

    @Override
    public void update(Integer id, Game game) {
        Game existing = gameRepository.findById(id)
                .orElseThrow();

        existing.setName(game.getName());

        gameRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        if(!gameRepository.existsById(id)){
            throw new RuntimeException("Game with id " + id + " does not exist");
        }

        gameRepository.deleteById(id);
    }
}
