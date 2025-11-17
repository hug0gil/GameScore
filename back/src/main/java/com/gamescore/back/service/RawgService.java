package com.gamescore.back.service;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.Genre;
import com.gamescore.back.model.Platform;
import com.gamescore.back.repository.GameRepository;
import com.gamescore.back.repository.GenreRepository;
import com.gamescore.back.repository.PlatformRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RawgService {

    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final YoutubeTrailerService youtubeTrailerService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rawg.api.key}")
    private String apiKey;

    private final String RAWG_LIST_URL = "https://api.rawg.io/api/games";
    private final String RAWG_DETAIL_URL = "https://api.rawg.io/api/games/";

    @Transactional
    public void fetchAndSaveGames() {
        // PASO 1: Obtener solo los IDs de 25 juegos
        List<Long> gameIds = fetchGameIds(25);

        System.out.println("IDs obtenidos: " + gameIds);

        // PASO 2: Por cada ID, hacer la petición detallada y guardar
        for (Long rawgId : gameIds) {
            try {
                saveGameDetail(rawgId);
            } catch (Exception e) {
                // Captura de errores generales durante el guardado de un juego
                System.err.println("✗ Error fatal guardando juego con ID " + rawgId + ": " + e.getMessage());
                e.printStackTrace(); // Opcional: para depuración avanzada
            }
        }
    }

    private List<Long> fetchGameIds(int count) {
        String url = UriComponentsBuilder.fromUriString(RAWG_LIST_URL)
                .queryParam("key", apiKey)
                .queryParam("page_size", count)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("results")) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        return results.stream()
                .map(game -> Long.valueOf(String.valueOf(game.get("id"))))
                .collect(Collectors.toList());
    }

    private void saveGameDetail(Long rawgId) {
        // 1. Verificar si el juego ya existe para evitar duplicados.
        if (gameRepository.existsByRawgId(rawgId)) {
            System.out.println("-> Juego con rawgId " + rawgId + " ya existe. Saltando.");
            return;
        }

        // 2. Construir la URL de detalle y realizar la petición a la API.
        String detailUrl = UriComponentsBuilder.fromUriString(RAWG_DETAIL_URL + rawgId)
                .queryParam("key", apiKey)
                .toUriString();

        Map<String, Object> data = restTemplate.getForObject(detailUrl, Map.class);

        // 3. Verificación de seguridad: si no hay datos, no continuar.
        if (data == null) {
            System.err.println("✗ Error: No se recibieron datos para el rawgId " + rawgId);
            return;
        }

        // 4. Verificación de campo esencial: el nombre es obligatorio en la BBDD.
        Object nameObj = data.get("name");
        if (nameObj == null || nameObj.toString().trim().isEmpty()) {
            System.err.println("✗ Error: Juego con rawgId " + rawgId + " no tiene nombre. Saltando guardado.");
            return;
        }

        // 5. Creación y mapeo del objeto Game.
        Game game = new Game();

        // ---- MAPEANDO CAMPOS CON CONTROL DE NULOS ----

        game.setRawgId(rawgId);
        game.setName(nameObj.toString()); // Ya sabemos que no es nulo.

        // Campos String opcionales
        Object slugObj = data.get("slug");
        if (slugObj != null)
            game.setSlug(slugObj.toString());

        // Descripción: Priorizamos 'description_raw' (texto plano) sobre 'description'
        // (con HTML).
        Object descriptionObj = data.get("description_raw");
        if (descriptionObj == null) {
            descriptionObj = data.get("description"); // Fallback a la descripción con HTML
        }
        if (descriptionObj != null)
            game.setDescription(descriptionObj.toString());

        Object bgImageObj = data.get("background_image");
        if (bgImageObj != null)
            game.setBackgroundUrl(bgImageObj.toString());

        Object websiteObj = data.get("website");
        if (websiteObj != null)
            game.setWebsite(websiteObj.toString());

        // Fecha de lanzamiento (LocalDate)
        Object releaseDateObj = data.get("released");
        if (releaseDateObj != null) {
            try {
                game.setReleaseDate(LocalDate.parse(releaseDateObj.toString()));
            } catch (DateTimeParseException e) {
                System.err.println(
                        "⚠ Advertencia: Formato de fecha inválido para rawgId " + rawgId + ": " + releaseDateObj);
            }
        }

        // Campos numéricos (BigDecimal, Integer)
        Object ratingObj = data.get("rating");
        if (ratingObj instanceof Number) {
            game.setRating(BigDecimal.valueOf(((Number) ratingObj).doubleValue()));
        }

        Object metacriticObj = data.get("metacritic");
        if (metacriticObj instanceof Integer) {
            game.setMetacritic((Integer) metacriticObj);
        }

        // ---- MAPEANDO RELACIONES (Listas/Conjuntos) ----

        // Géneros
        List<Map<String, Object>> genresData = (List<Map<String, Object>>) data.get("genres");
        if (genresData != null) {
            Set<Genre> genreSet = genresData.stream()
                    .map(g -> {
                        String genreName = g.get("name") != null ? g.get("name").toString() : null;
                        if (genreName == null || genreName.trim().isEmpty())
                            return null; // Ignorar géneros sin nombre
                        return genreRepository.findByName(genreName)
                                .orElseGet(() -> {
                                    Genre newGenre = new Genre();
                                    newGenre.setName(genreName);
                                    newGenre.setSlug(Genre.toSlug(genreName));
                                    return genreRepository.save(newGenre);
                                });
                    })
                    .filter(Objects::nonNull) // Filtra los resultados nulos (géneros sin nombre).
                    .collect(Collectors.toSet());
            game.setGenres(genreSet);
        }

        // Plataformas
        List<Map<String, Object>> platformsData = (List<Map<String, Object>>) data.get("platforms");
        if (platformsData != null) {
            Set<Platform> platformSet = platformsData.stream()
                    .map(p -> {
                        Map<String, Object> platformMap = (Map<String, Object>) p.get("platform");
                        if (platformMap == null)
                            return null;

                        String platformName = platformMap.get("name") != null ? platformMap.get("name").toString()
                                : null;
                        if (platformName == null || platformName.trim().isEmpty())
                            return null; // Ignorar plataformas sin nombre

                        return platformRepository.findByName(platformName)
                                .orElseGet(() -> {
                                    Platform newPlatform = new Platform();
                                    newPlatform.setName(platformName);
                                    newPlatform.setSlug(Platform.toSlug(platformName));
                                    return platformRepository.save(newPlatform);
                                });
                    })
                    .filter(Objects::nonNull) // Filtra los resultados nulos.
                    .collect(Collectors.toSet());
            game.setPlatforms(platformSet);
        }

        System.out.println("Buscando tráiler para: " + game.getName());
        youtubeTrailerService.findTrailerUrl(game)
                .ifPresentOrElse(
                        trailerUrl -> {
                            game.setYoutubeUrl(trailerUrl);
                            System.out.println("✓ Tráiler encontrado: " + trailerUrl);
                        },
                        () -> System.out.println("✗ No se encontró tráiler para " + game.getName()));

        // 6. Guardar la entidad 'Game' completamente mapeada.
        gameRepository.save(game);
        System.out.println("✓ Juego guardado con éxito: " + game.getName() + " (rawgId: " + rawgId + ")");

    }
}