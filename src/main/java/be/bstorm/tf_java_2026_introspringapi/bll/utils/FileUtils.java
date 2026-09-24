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

@Component
public class FileUtils {

    private final Path uploadDir = Paths.get("uploads");

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
