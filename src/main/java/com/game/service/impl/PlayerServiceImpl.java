package com.game.service.impl;

import com.game.controller.request.PlayerCreateDTO;
import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import com.game.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerServiceImpl.class);
    private final PlayerRepository repository;

    public PlayerServiceImpl(PlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Player createPlayer(PlayerCreateDTO playerCreateDTO) {
        log.debug("Creating new person. Create person request {}.", playerCreateDTO);
        Integer experience = playerCreateDTO.getExperience();
        int currentLevel = computePersonCurrentLevel(experience);
        int expUntilNextLevel = computeExperienceUntilNextLevel(currentLevel, experience);

        Player player = new Player();
        player.setBanned(playerCreateDTO.isBanned());
        player.setLevel(currentLevel);
        player.setBirthday(new Date(playerCreateDTO.getBirthday()));
        player.setName(playerCreateDTO.getName());
        player.setExperience(playerCreateDTO.getExperience());
        player.setProfession(playerCreateDTO.getProfession());
        player.setRace(playerCreateDTO.getRace());
        player.setTitle(playerCreateDTO.getTitle());
        player.setUntilNextLevel(expUntilNextLevel);

        return player;
    }

    @Override
    public Player savePlayer(Player player) {
        log.debug("Saving person. Person {}.", player);
        Player saved = repository.save(player);
        log.trace("Successfully saved person. Person {}.", saved);
        return saved;
    }

    private int computePersonCurrentLevel(Integer experience) {
        log.debug("Computing person current level. Person experience {}.", experience);
        int currentLevel = (int) ((Math.sqrt(2500d + 200 * experience) - 50) / 100);
        log.debug("Computed current level {}.", currentLevel);
        return currentLevel;
    }

    private int computeExperienceUntilNextLevel(int currentLevel, Integer experience) {
        log.debug("Computing person experience until next level. Current level {}, amount of experience {}.", currentLevel, experience);
        int expUntilNextLevel = 50 * (currentLevel + 1) * (currentLevel + 2) - experience;
        log.debug("Computed person experience until next level. Computed amount of experience {}.", experience);
        return expUntilNextLevel;
    }
}