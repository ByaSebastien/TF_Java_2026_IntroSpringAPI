//package be.bstorm.tf_java_2026_introspringapi.api.configs;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
///**
// * Configuration Web MVC (actuellement désactivée).
// * Expose le répertoire uploads/ comme ressource statique.
// * Permet d'accéder aux fichiers via /uploads/filename directement.
// */
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    /**
//     * Ajoute un handler pour servir les fichiers du répertoire uploads/.
//     * @param registry registre des handlers de ressources
//     */
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        Path uploadDir = Paths.get("uploads");
//        String uploadPath = uploadDir.toFile().getAbsolutePath();
//
//        registry.addResourceHandler("/uploads/**")
//                .addResourceLocations("file:" + uploadPath + "/");
//    }
//}