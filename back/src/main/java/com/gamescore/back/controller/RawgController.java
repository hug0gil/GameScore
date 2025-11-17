package com.gamescore.back.controller;

import com.gamescore.back.service.RawgService;
import com.gamescore.back.service.YoutubeTrailerService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/rawg")
@RequiredArgsConstructor
public class RawgController {

    private final RawgService rawgService;
    private final YoutubeTrailerService youtubeTrailerService;

    @PostMapping("/fetch")
    public String fetchGames() {
        rawgService.fetchAndSaveGames();
        return "Juegos importados correctamente";
    }

    /**
     * Endpoint para probar la API de YouTube.
     * Uso: /api/rawg/test-youtube?game=Grand Theft Auto V
     */
    @GetMapping("/test-youtube")
    public ResponseEntity<String> testYoutubeApi(
            @RequestParam(value = "game", defaultValue = "grand-theft-auto-v") String gameName) {
        // Captura el JSON devuelto por el servicio
        String jsonResponse = youtubeTrailerService.testYoutubeSearch(gameName);

        // Devuelve el JSON en el cuerpo de la respuesta
        return ResponseEntity.ok(jsonResponse);
    }
}
