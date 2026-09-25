package be.bstorm.tf_java_2026_introspringapi.bll.services.impls;

import be.bstorm.tf_java_2026_introspringapi.bll.utils.FileUtils;
import be.bstorm.tf_java_2026_introspringapi.dal.repositories.GameRepository;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameServiceImpl Tests")
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private FileUtils fileUtils;

    @InjectMocks
    private GameServiceImpl gameService;

    private Game testGame;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testGame = new Game("Test Game", 2024, 49);
        testGame.setImageUrl("test-image.jpg");

        testPageable = PageRequest.of(0, 10);
    }

    // ==================== find() Tests ====================

    @Test
    @DisplayName("find() - devrait retourner une Page de Games avec paramètres vides")
    void testFindWithEmptyParams() {
        Page<Game> expectedPage = new PageImpl<>(List.of(testGame), testPageable, 1);
        when(gameRepository.findAll((Specification<Game>) any(), eq(testPageable))).thenReturn(expectedPage);

        Page<Game> result = gameService.find(new HashMap<>(), testPageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testGame.getName(), result.getContent().get(0).getName());
        verify(gameRepository, times(1)).findAll((Specification<Game>) any(), eq(testPageable));
    }

    @Test
    @DisplayName("find() - devrait retourner une Page vide quand aucun résultat")
    void testFindWithEmptyResult() {
        Page<Game> emptyPage = new PageImpl<>(new ArrayList<>(), testPageable, 0);
        when(gameRepository.findAll((Specification<Game>) any(), eq(testPageable))).thenReturn(emptyPage);

        Page<Game> result = gameService.find(new HashMap<>(), testPageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("find() - devrait fonctionner avec différents numéros de page")
    void testFindWithVariousPageNumbers(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<Game> gameList = List.of(testGame);
        Page<Game> expectedPage = new PageImpl<>(gameList, pageable, gameList.size());
        when(gameRepository.findAll((Specification<Game>) any(), eq(pageable))).thenReturn(expectedPage);

        Page<Game> result = gameService.find(new HashMap<>(), pageable);

        assertNotNull(result);
        verify(gameRepository, times(1)).findAll((Specification<Game>) any(), eq(pageable));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 20, 50})
    @DisplayName("find() - devrait fonctionner avec différentes tailles de page")
    void testFindWithVariousPageSizes(int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<Game> expectedPage = new PageImpl<>(List.of(testGame), pageable, 1);
        when(gameRepository.findAll((Specification<Game>) any(), eq(pageable))).thenReturn(expectedPage);

        Page<Game> result = gameService.find(new HashMap<>(), pageable);

        assertNotNull(result);
        verify(gameRepository, times(1)).findAll((Specification<Game>) any(), eq(pageable));
    }

    @Test
    @DisplayName("find() - devrait fonctionner avec des paramètres de filtrage")
    void testFindWithFilterParams() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "Test");
        params.put("minPrice", "10");
        Page<Game> expectedPage = new PageImpl<>(List.of(testGame), testPageable, 1);
        when(gameRepository.findAll((Specification<Game>) any(), eq(testPageable))).thenReturn(expectedPage);

        Page<Game> result = gameService.find(params, testPageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(gameRepository, times(1)).findAll((Specification<Game>) any(), eq(testPageable));
    }

    // ==================== findById() Tests ====================

    @Test
    @DisplayName("findById() - devrait retourner un Game quand il existe")
    void testFindByIdSuccess() {
        when(gameRepository.findById(1)).thenReturn(Optional.of(testGame));

        Game result = gameService.findById(1);

        assertNotNull(result);
        assertEquals(testGame.getId(), result.getId());
        assertEquals(testGame.getName(), result.getName());
        verify(gameRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("findById() - devrait lancer une exception quand le Game n'existe pas")
    void testFindByIdNotFound() {
        when(gameRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> gameService.findById(999));
        verify(gameRepository, times(1)).findById(999);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 100, 999})
    @DisplayName("findById() - devrait fonctionner avec différents IDs")
    void testFindByIdWithVariousIds(int id) {
        Game gameWithId = new Game("Game " + id, 2024, 50);
        when(gameRepository.findById(id)).thenReturn(Optional.of(gameWithId));

        Game result = gameService.findById(id);

        assertNotNull(result);
        assertEquals("Game " + id, result.getName());
        verify(gameRepository, times(1)).findById(id);
    }

    // ==================== save() Tests ====================

    @Test
    @DisplayName("save() - devrait sauvegarder un Game sans image")
    void testSaveGameWithoutImage() {
        when(gameRepository.save(testGame)).thenReturn(testGame);

        Game result = gameService.save(testGame, null);

        assertNotNull(result);
        assertEquals(testGame.getName(), result.getName());
        verify(gameRepository, times(1)).save(testGame);
        verify(fileUtils, never()).saveFile(any());
    }

    @Test
    @DisplayName("save() - devrait sauvegarder un Game avec image valide")
    void testSaveGameWithImage() {
        MultipartFile mockImage = mock(MultipartFile.class);
        when(mockImage.isEmpty()).thenReturn(false);
        when(fileUtils.saveFile(mockImage)).thenReturn("saved-image.jpg");
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        Game result = gameService.save(testGame, mockImage);

        assertNotNull(result);
        verify(fileUtils, times(1)).saveFile(mockImage);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    @DisplayName("save() - devrait ignorer une image vide")
    void testSaveGameWithEmptyImage() {
        MultipartFile mockImage = mock(MultipartFile.class);
        when(mockImage.isEmpty()).thenReturn(true);
        when(gameRepository.save(testGame)).thenReturn(testGame);

        Game result = gameService.save(testGame, mockImage);

        assertNotNull(result);
        verify(fileUtils, never()).saveFile(any());
        verify(gameRepository, times(1)).save(testGame);
    }

    @Test
    @DisplayName("save() - devrait définir l'imageUrl correctement après sauvegarder le fichier")
    void testSaveGameSetsImageUrlFromFileUtils() {
        MultipartFile mockImage = mock(MultipartFile.class);
        String imageUrl = "path/to/saved/image.png";
        when(mockImage.isEmpty()).thenReturn(false);
        when(fileUtils.saveFile(mockImage)).thenReturn(imageUrl);
        Game savedGame = new Game("Test Game", 2024, 49);
        savedGame.setImageUrl(imageUrl);
        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);

        Game result = gameService.save(testGame, mockImage);

        assertEquals(imageUrl, result.getImageUrl());
        verify(fileUtils, times(1)).saveFile(mockImage);
    }

    // ==================== update() Tests ====================

    @Test
    @DisplayName("update() - devrait mettre à jour un Game existant sans image")
    void testUpdateGameWithoutImage() {
        Game updatedGame = new Game("Updated Game", 2024, 60);
        when(gameRepository.findById(1)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(updatedGame);

        gameService.update(1, updatedGame, null);

        verify(gameRepository, times(1)).findById(1);
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(fileUtils, never()).saveFile(any());
    }

    @Test
    @DisplayName("update() - devrait mettre à jour un Game avec image valide")
    void testUpdateGameWithImage() {
        MultipartFile mockImage = mock(MultipartFile.class);
        Game updatedGame = new Game("Updated Game", 2024, 60);
        when(mockImage.isEmpty()).thenReturn(false);
        when(gameRepository.findById(1)).thenReturn(Optional.of(testGame));
        when(fileUtils.saveFile(mockImage)).thenReturn("new-image.jpg");
        when(gameRepository.save(any(Game.class))).thenReturn(updatedGame);

        gameService.update(1, updatedGame, mockImage);

        verify(gameRepository, times(1)).findById(1);
        verify(fileUtils, times(1)).saveFile(mockImage);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    @DisplayName("update() - devrait lancer une exception si le Game n'existe pas")
    void testUpdateGameNotFound() {
        when(gameRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> gameService.update(999, testGame, null));
        verify(gameRepository, times(1)).findById(999);
        verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("update() - devrait ignorer une image vide")
    void testUpdateGameWithEmptyImage() {
        MultipartFile mockImage = mock(MultipartFile.class);
        Game updatedGame = new Game("Updated Game", 2024, 60);
        when(mockImage.isEmpty()).thenReturn(true);
        when(gameRepository.findById(1)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(updatedGame);

        gameService.update(1, updatedGame, mockImage);

        verify(fileUtils, never()).saveFile(any());
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 50, 999})
    @DisplayName("update() - devrait mettre à jour avec différents IDs")
    void testUpdateGameWithVariousIds(int id) {
        Game existingGame = new Game("Existing Game", 2024, 50);
        Game updatedGame = new Game("Updated Game", 2024, 60);
        when(gameRepository.findById(id)).thenReturn(Optional.of(existingGame));
        when(gameRepository.save(any(Game.class))).thenReturn(updatedGame);

        gameService.update(id, updatedGame, null);

        verify(gameRepository, times(1)).findById(id);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    // ==================== delete() Tests ====================

    @Test
    @DisplayName("delete() - devrait supprimer un Game existant")
    void testDeleteGameSuccess() {
        when(gameRepository.existsById(1)).thenReturn(true);

        gameService.delete(1);

        verify(gameRepository, times(1)).existsById(1);
        verify(gameRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("delete() - devrait lancer une exception si le Game n'existe pas")
    void testDeleteGameNotFound() {
        when(gameRepository.existsById(999)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gameService.delete(999));
        assertTrue(exception.getMessage().contains("does not exist"));
        verify(gameRepository, times(1)).existsById(999);
        verify(gameRepository, never()).deleteById(999);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 999})
    @DisplayName("delete() - devrait supprimer avec différents IDs")
    void testDeleteGameWithVariousIds(int id) {
        when(gameRepository.existsById(id)).thenReturn(true);

        gameService.delete(id);

        verify(gameRepository, times(1)).existsById(id);
        verify(gameRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("delete() - le message d'erreur devrait contenir l'ID")
    void testDeleteGameErrorMessageContainsId() {
        when(gameRepository.existsById(42)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gameService.delete(42));
        assertTrue(exception.getMessage().contains("42"));
    }

}