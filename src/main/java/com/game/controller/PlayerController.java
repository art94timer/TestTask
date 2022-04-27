package com.game.controller;

import com.game.controller.request.PlayerDTO;
import com.game.entity.Player;
import com.game.service.PlayerService;
import com.game.service.PlayerValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Player createAndSavePlayer(@RequestBody PlayerDTO personCreateRequest) {
        validationService.validateCreateRequest(personCreateRequest);
        Player player = playerService.create(personCreateRequest);
        return playerService.save(player);
    }

    @DeleteMapping(value = "/rest/players/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        validationService.validateId(id);
        Optional<Player> optionalPlayer = playerService.findById(id);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            playerService.delete(player.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/rest/players/{id}")
    public ResponseEntity<Player> findById(@PathVariable Long id) {
        validationService.validateId(id);
        Optional<Player> optionalPlayer = playerService.findById(id);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            return ResponseEntity
                    .ok(player);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/rest/players")
    public ResponseEntity<List<Player>> findAllByParams(@RequestParam Map<String, String> params) {
        List<Player> filteredByParamsPlayers = playerService.findAllByParams(params);
        return ResponseEntity.ok(filteredByParamsPlayers);
    }

    @GetMapping(value = "/rest/players/count")
    public ResponseEntity<Integer> countAllByParams(@RequestParam Map<String, String> params) {
        int count = playerService.countAllByParams(params);
        return ResponseEntity.ok(count);
    }

    @PostMapping(value = "/rest/players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id,
                                               @RequestBody PlayerDTO playerDTO) {
        validationService.validateId(id);
        validationService.validateUpdateRequest(playerDTO);
        Optional<Player> player = playerService.update(id, playerDTO);
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}