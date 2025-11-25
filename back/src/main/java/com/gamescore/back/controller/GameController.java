package com.gamescore.back.controller;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.Review;
import com.gamescore.back.model.DTOs.GameListDTO;
import com.gamescore.back.service.GameService;
import com.gamescore.back.service.ReviewService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/juegos")
public class GameController {

    private final GameService gameService;
    private final ReviewService reviewService;

    @GetMapping
    public String listGames(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageSize = 10; // 10 juegos por página
        Page<GameListDTO> gamesPage = gameService.findAllPagedLight(page, pageSize);
        List<GameListDTO> featured = gameService.findFeaturedLight();

        model.addAttribute("games", gamesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", gamesPage.getTotalPages());
        model.addAttribute("featuredGames", featured);

        return "games";
    }

    @GetMapping("/{slug}")
    public String showGameDetailPage(@PathVariable("slug") String slug, Model model) {
        System.out.println("Slug recibido: [" + slug + "]");

        Game game = gameService.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));

        List<Review> approvedReviews = reviewService.findApprovedReviewsByGameId(game.getId());

        model.addAttribute("game", game);
        model.addAttribute("reviews", approvedReviews);

        return "game-detail";
    }

}