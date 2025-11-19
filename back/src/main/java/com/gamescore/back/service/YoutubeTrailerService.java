package com.gamescore.back.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamescore.back.model.Game;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class YoutubeTrailerService {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeTrailerService.class);
    // Ya no necesitamos RestTemplate para esta tarea.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    private final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/search";

    // ... (el método findTrailerUrl con la lógica de fallback se mantiene igual)
    public Optional<String> findTrailerUrl(Game game) {
        logger.info("=========================================================================");
        logger.info("--- INICIO BÚSQUEDA DE TRÁILER PARA: '{}' ---", game.getName());

        logger.info(">>> INTENTO 1: Búsqueda genérica ('official trailer').");
        String genericQuery = game.getName() + " teaser trailer";
        Optional<String> trailerUrl = executeSearchWithJsoup(genericQuery);

        if (trailerUrl.isPresent()) {
            logger.info("--- FIN BÚSQUEDA: ÉXITO en el primer intento. ---");
            return trailerUrl;
        }

        logger.warn(">>> INTENTO 1 FALLIDO. Realizando INTENTO 2 (Fallback): Búsqueda en español.");
        String specificQuery = game.getName() + " teaser trailer";
        Optional<String> fallbackUrl = executeSearchWithJsoup(specificQuery);

        if (fallbackUrl.isPresent()) {
            logger.info("--- FIN BÚSQUEDA: ÉXITO en el intento de fallback. ---");
        } else {
            logger.error("--- FIN BÚSQUEDA: FALLO TOTAL. No se encontró tráiler en ningún intento. ---");
        }
        logger.info("=========================================================================");

        return fallbackUrl;
    }

    /**
     * Ejecuta la búsqueda usando JSoup, que simula mejor a un navegador.
     */
    private Optional<String> executeSearchWithJsoup(String searchQuery) {
        logger.info("1. TÉRMINOS DE BÚSQUEDA (JSoup): '{}'", searchQuery);
        String url = UriComponentsBuilder.fromUriString(YOUTUBE_API_URL)
                .queryParam("key", youtubeApiKey)
                .queryParam("part", "id")
                .queryParam("q", searchQuery)
                .queryParam("type", "video")
                .queryParam("maxResults", 1)
                .queryParam("order", "relevance")
                .toUriString();

        logger.info("2. URL DE PETICIÓN (JSoup): {}", url);

        try {
            // --- CAMBIO CLAVE: Usamos Jsoup.connect() ---
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                    .timeout(10000) // 10 segundos de timeout
                    .ignoreContentType(true) // Importante para que acepte JSON
                    .execute();
            // --- FIN DEL CAMBIO ---

            String jsonResponse = response.body();
            logger.info("3. RESPUESTA JSON (JSoup): {}", jsonResponse);

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode videoIdNode = root.path("items").path(0).path("id").path("videoId");

            if (videoIdNode.isMissingNode() || !videoIdNode.isTextual()) {
                logger.warn("4. EXTRACCIÓN FALLIDA (JSoup): 'videoId' no encontrado.");
                return Optional.empty();
            }

            String videoId = videoIdNode.asText();
            logger.info("4. EXTRACCIÓN EXITOSA (JSoup): 'videoId' encontrado: '{}'", videoId);
            return Optional.of("https://www.youtube.com/watch?v=" + videoId);

        } catch (Exception e) {
            logger.error("!!! ERROR al ejecutar la búsqueda con JSoup para '{}'", searchQuery, e);
            return Optional.empty();
        }
    }

    // Mantén tu método de test con RestTemplate si quieres, pero ahora sabemos que
    // no es fiable
    // para el proceso de fondo. O mejor, actualízalo para usar JSoup también.
    public String testYoutubeSearch(String gameName) {
        // ... (puedes actualizar este método para que también use
        // executeSearchWithJsoup si quieres)
        String genericQuery = gameName + " teaser trailer";
        Optional<String> result = executeSearchWithJsoup(genericQuery);
        return result.orElse("No se encontró tráiler en la prueba con JSoup.");
    }
}