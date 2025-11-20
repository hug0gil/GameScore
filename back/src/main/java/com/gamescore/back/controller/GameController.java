package com.gamescore.back.controller;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.DTOs.GameListDTO;
import com.gamescore.back.service.GameService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Random;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/")
    public String showIndexPage(Model model) {

        // Traer los primeros 25 juegos ligeros
        List<Game> allGames = gameService.findAll();

        List<Game> gamesWithTrailer = allGames.stream()
                .filter(g -> g.getYoutubeUrl() != null && !g.getYoutubeUrl().isEmpty())
                .toList();

        if (gamesWithTrailer.isEmpty()) {
            // No hay trailers, mostrar hero sin video o con un video por defecto
            model.addAttribute("randomTrailerGame",
                    "https://www.youtube.com/embed/defaultVideoId?autoplay=1&mute=1&loop=1&playlist=defaultVideoId");
        } else {
            Random rand = new Random();
            Game randomGame = gamesWithTrailer.get(rand.nextInt(gamesWithTrailer.size()));
            String youtubeUrl = randomGame.getYoutubeUrl();
            String videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
            String embedUrl = "https://www.youtube.com/embed/" + videoId + "?autoplay=1&mute=1&loop=1&playlist="
                    + videoId;
            model.addAttribute("randomTrailerGame", embedUrl);
        }

        return "index";
    }

    @GetMapping("/juegos")
    public String listGames(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Traer solo los datos necesarios
        Page<GameListDTO> gamesPage = gameService.findAllPagedLight(page, 25);
        List<GameListDTO> featured = gameService.findFeaturedLight();

        model.addAttribute("games", gamesPage.getContent());
        model.addAttribute("totalPages", gamesPage.getTotalPages());
        model.addAttribute("featuredGames", featured);

        return "games";
    }

    @GetMapping("/juego/{slug}")
    public String showGameDetailPage(@PathVariable("slug") String slug, Model model) {
        Game game = gameService.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));
        model.addAttribute("game", game);
        return "game-detail";
    }
}