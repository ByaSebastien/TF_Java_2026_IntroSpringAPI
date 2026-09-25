package be.bstorm.tf_java_2026_introspringapi.api.controllers;

import be.bstorm.tf_java_2026_introspringapi.bll.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Endpoints REST pour le téléchargement et l'affichage des fichiers.
 * Sert les fichiers uploadés (images, documents) via HTTP.
 * Gère le Content-Type automatiquement selon l'extension.
 */
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class FileController {

    private final FileUtils fileUtils;

    /**
     * Télécharge un fichier (force le navigateur à télécharger).
     * @param filename nom du fichier dans uploads/
     * @return 200 OK avec le contenu du fichier + header Content-Disposition: attachment
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {

        Resource file = fileUtils.loadFileAsResource(filename);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /**
     * Affiche un fichier dans le navigateur (inline).
     * Détecte le type MIME (image/png, image/jpeg, etc.).
     * @param filename nom du fichier dans uploads/
     * @return 200 OK avec le contenu du fichier + cache-control
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws IOException {

        Resource file = fileUtils.loadFileAsResource(filename);

        return ResponseEntity.ok()
                .contentType(getMediaType(filename))
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .header("Cache-Control", "public, max-age=3600")
                .body(file);
    }

    /**
     * Détecte le type MIME basé sur l'extension du fichier.
     * @param filename nom du fichier
     * @return MediaType approprié
     */
    private MediaType getMediaType(String filename) {
        if (filename.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (filename.endsWith(".jpg")) return MediaType.IMAGE_JPEG;
        if (filename.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if( filename.endsWith(".webp")) return MediaType.valueOf("image/webp");
        return MediaType.APPLICATION_OCTET_STREAM;
    }

}
