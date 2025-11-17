package com.gamescore.back.controller;

import com.gamescore.back.model.Game; // Asegúrate de importar tu modelo Game
import com.gamescore.back.service.GameService; // Y tu servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    /**
     * Muestra la página de inicio (index.html).
     * @return El nombre de la plantilla "index".
     */
    @GetMapping("/")
    public String showIndexPage() {
        return "index"; // Devuelve la plantilla /templates/index.html
    }
    
    /**
     * Muestra la lista completa de juegos (games.html).
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la plantilla "games".
     */
    @GetMapping("/juegos")
    public String showGamesListPage(Model model) {
        // Obtenemos las listas de juegos desde el servicio
        List<Game> allGames = gameService.findAll(); // Suponiendo que este método existe
        List<Game> featuredGames = gameService.findFeatured(); // Necesitarás implementar este método

        // Añadimos las listas al modelo con los nombres que espera la plantilla Thymeleaf
        model.addAttribute("games", allGames);
        model.addAttribute("featuredGames", featuredGames);

        return "games"; // Devuelve la plantilla /templates/games.html
    }

    /**
     * Muestra la página de detalle de un juego específico.
     * @param id El ID del juego a mostrar.
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la plantilla "game-detail".
     */
    @GetMapping("/juego/{id}")
    public String showGameDetailPage(@PathVariable("id") Long id, Model model) {
        // Buscamos el juego por su ID. Optional nos ayuda a manejar el caso de que no exista.
        Optional<Game> gameOptional = gameService.findById(id);

        if (gameOptional.isPresent()) {
            Game game = gameOptional.get();
            // Añadimos el objeto "game" completo al modelo
            model.addAttribute("game", game);
            return "game-detail"; // Devuelve la plantilla /templates/game-detail.html
        } else {
            // Si el juego no se encuentra, lanzamos una excepción que resultará en un error 404 Not Found.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado");
        }
    }
}