package com.game.controller;

import com.game.controller.request.PlayerCreateDTO;
import com.game.entity.Player;
import com.game.service.PlayerService;
import com.game.service.PlayerValidationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayerController {


    private final PlayerValidationService validationService;
    private final PlayerService playerService;

    public PlayerController(PlayerValidationService validationService,
                            PlayerService playerService) {
        this.validationService = validationService;
        this.playerService = playerService;
    }

    @PostMapping(value = "/rest/players")
    public Player createAndSavePlayer(@RequestBody PlayerCreateDTO personCreateRequest) {
        validationService.validate(personCreateRequest);
        Player player = playerService.createPlayer(personCreateRequest);
        return playerService.savePlayer(player);
    }
}