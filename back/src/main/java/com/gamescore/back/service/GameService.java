package com.gamescore.back.service;

import com.gamescore.back.model.Game;
import com.gamescore.back.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    /**
     * Devuelve una lista de todos los juegos en la base de datos.
     * 
     * @return Lista de todos los juegos.
     */
    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    /**
     * Busca un juego por su ID.
     * 
     * @param id El ID del juego.
     * @return Un Optional que contiene el juego si se encuentra, o un Optional
     *         vacío si no.
     */
    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    public Optional<Game> findBySlug(String slug) {
        return gameRepository.findBySlug(slug);
    }

    /**
     * Devuelve una lista de juegos destacados.
     * Lógica de ejemplo: devuelve los 3 juegos con la puntuación (rating) más alta.
     * Puedes ajustar esta lógica según tus necesidades.
     * 
     * @return Lista de juegos destacados.
     */
    public List<Game> findFeatured() {
        // PageRequest.of(página, tamaño, orden)
        // Página 0 (la primera), tamaño 3, orden descendente por el campo "rating".
        return gameRepository.findAll(PageRequest.of(0, 3, Sort.by("rating").descending())).getContent();
    }

    /**
     * Guarda un juego en la base de datos (para crear uno nuevo o actualizar uno
     * existente).
     * 
     * @param game El objeto Game a guardar.
     * @return El juego guardado (con su ID si es nuevo).
     */
    public Game save(Game game) {
        return gameRepository.save(game);
    }

    /**
     * Elimina un juego por su ID.
     * 
     * @param id El ID del juego a eliminar.
     */
    public void deleteById(Long id) {
        gameRepository.deleteById(id);
    }



    /**
     * Busca juegos cuyo nombre contenga la palabra clave.
     * Este método requerirá que añadas un método personalizado en tu
     * GameRepository.
     * 
     * @param keyword La palabra clave para buscar.
     * @return Una lista de juegos que coinciden con la búsqueda.
     */
    public List<Game> search(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return gameRepository.searchByName(keyword);
        }
        // Si no hay palabra clave, devuelve todos los juegos.
        return gameRepository.findAll();
    }
}