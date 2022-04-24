package com.game.service;

import com.game.controller.request.PlayerCreateDTO;
import com.game.entity.Player;

public interface PlayerService {

    Player createPlayer(PlayerCreateDTO personDTO);

    Player savePlayer(Player player);
}