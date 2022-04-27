package com.game.service;

import com.game.controller.request.PlayerDTO;
import com.game.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PlayerService {

    Player create(PlayerDTO player);

    Player save(Player player);

    void delete(Long id);

    Optional<Player> findById(Long id);

    List<Player> findAllByParams(Map<String, String> params);

    int countAllByParams(Map<String, String> params);

    Optional<Player> update(Long id, PlayerDTO player);
}