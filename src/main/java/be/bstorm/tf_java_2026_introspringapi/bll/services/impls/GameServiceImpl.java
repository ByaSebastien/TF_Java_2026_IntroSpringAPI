package be.bstorm.tf_java_2026_introspringapi.bll.services.impls;

import be.bstorm.tf_java_2026_introspringapi.bll.services.GameService;
import be.bstorm.tf_java_2026_introspringapi.bll.specifications.GameSpecification;
import be.bstorm.tf_java_2026_introspringapi.bll.specifications.SearchSpecification;
import be.bstorm.tf_java_2026_introspringapi.bll.utils.FileUtils;
import be.bstorm.tf_java_2026_introspringapi.dal.repositories.GameRepository;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final FileUtils fileUtils;

    @Override
    public Page<Game> find(Map<String, String> params, Pageable pageable) {

        // Au cas ou besoin de jointure
//        Specification<Game> specs = GameSpecification.joinCategory().and(
//                Specification.allOf(SearchSpecification.search(params))
//        );

        Specification<Game> specs = Specification.allOf(SearchSpecification.search(params));

        return  gameRepository.findAll(specs, pageable);
    }

    @Override
    public Game findById(Integer id) {
        return gameRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public Game save(Game game, MultipartFile image) {

        if(image != null && !image.isEmpty()){

            String imageUrl = fileUtils.saveFile(image);

            game.setImageUrl(imageUrl);
        }

        return gameRepository.save(game);
    }

    @Override
    public void update(Integer id, Game game, MultipartFile image) {
        Game existing = gameRepository.findById(id)
                .orElseThrow();

        existing.setName(game.getName());

        if(image != null && !image.isEmpty()){

            String imageUrl = fileUtils.saveFile(image);

            existing.setImageUrl(imageUrl);
        }

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
