package com.game.service;

import com.game.controller.request.PlayerCreateDTO;
import com.game.entity.Player;

import java.util.Optional;

public interface PlayerService {

    Player create(PlayerCreateDTO personDTO);

    Player save(Player player);

    void delete(Long id);

    Optional<Player> findById(Long id);
}