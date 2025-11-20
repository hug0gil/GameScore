package com.gamescore.back.service;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.DTOs.GameListDTO;
import com.gamescore.back.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public List<Game> search(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return gameRepository.searchByName(keyword);
        } else {
            return gameRepository.findAll();
        }
    }

    public Page<GameListDTO> findAllPagedLight(int page, int size) {
        return gameRepository.findAllLight(PageRequest.of(page, size));
    }

    @Cacheable("featuredGames")
    public List<GameListDTO> findFeaturedLight() {
        return gameRepository.findTop3ByOrderByRatingDesc();
    }

    /**
     * Busca un juego por su ID.
     * 
     * @param id El ID del juego.
     * @return un Optional que contiene el juego si se encuentra.
     */
    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    public Optional<Game> findBySlug(String slug) {
        return gameRepository.findBySlug(slug);
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

    public List<Game> findAll() {
        return gameRepository.findAll();
    }
}