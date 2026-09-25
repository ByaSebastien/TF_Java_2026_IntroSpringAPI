package be.bstorm.tf_java_2026_introspringapi.bll.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Utilitaire de gestion des fichiers uploadés.
 * Sauvegarde les fichiers sur disque avec UUID pour éviter les collisions.
 * Charge et sert les fichiers via le serveur.
 */
@Component
public class FileUtils {

    private final Path uploadDir = Paths.get("uploads");

    /**
     * Sauvegarde un fichier uploadé dans le répertoire uploads/.
     * Génère un UUID pour renommer le fichier et éviter les collisions.
     * @param file fichier MultipartFile du client
     * @return chemin relatif du fichier sauvegardé (/uploads/uuid_filename)
     * @throws RuntimeException si l'écriture échoue
     */
    public String saveFile(MultipartFile file) {

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier", e);
        }
    }

    /**
     * Charge un fichier sous forme de bytes.
     * @param filename nom du fichier dans uploads/
     * @return contenu du fichier en bytes
     * @throws RuntimeException si le fichier n'existe pas
     */
    public byte[] loadFile(String filename) {
        Path filePath = Paths.get("uploads", filename);

        byte[] fileContent = null;

        try {
            fileContent = Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fileContent;
    }

    /**
     * Charge un fichier comme Spring Resource.
     * Permet le téléchargement/affichage direct via ResponseEntity.
     * @param filename nom du fichier dans uploads/
     * @return Resource Spring
     * @throws RuntimeException si le fichier n'existe pas
     */
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier introuvable : " + filename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du fichier : " + filename, e);
        }
    }
}
