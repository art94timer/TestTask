package com.game.service.impl;

import com.game.controller.PlayerOrder;
import com.game.controller.request.PlayerCreateDTO;
import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import com.game.service.PlayerService;
import com.game.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.*;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerRepository repository;

    @PersistenceContext
    private EntityManager entityManager;


    public PlayerServiceImpl(PlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Player create(PlayerCreateDTO playerCreateDTO) {
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
    public Player save(Player player) {
        log.debug("Saving person. Person {}.", player);
        Player saved = repository.save(player);
        log.trace("Successfully saved person. Person {}.", saved);
        return saved;
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting person. Id {}.", id);
        repository.deleteById(id);
    }

    @Override
    public Optional<Player> findById(Long id) {
        log.debug("Searching person. Id {}.", id);
        return repository.findById(id);
    }

    @Override
    public List<Player> findAllBy(Map<String, String> params) {
        String pageNumberStr = params.remove("pageNumber");
        String pageSizeStr = params.remove("pageSize");
        String orderStr = params.remove("order");
        int pageNumber = pageNumberStr == null ? PlayerRepository.DEFAULT_PAGE_NUMBER : Integer.parseInt(pageNumberStr);
        int pageSize = pageSizeStr == null ? PlayerRepository.DEFAULT_PAGE_SIZE : Integer.parseInt(pageSizeStr);
        PlayerOrder order = orderStr == null ? PlayerOrder.ID : PlayerOrder.valueOf(orderStr);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(PlayerRepository.SELECT_FROM_PLAYER);
        params.forEach((param, value) -> {
            if (param.startsWith("min")) {
                String column = param.substring(3).toLowerCase(Locale.ROOT);
                sqlBuilder.append(String.format(" AND %s >= %s", column, value));
            } else if (param.startsWith("max")) {
                String column = param.substring(3).toLowerCase(Locale.ROOT);
                sqlBuilder.append(String.format(" AND %s <= %s", column, value));
            } else if (param.equals("after")) {
                long milliseconds = Long.parseLong(value);
                LocalDate after = DateUtils.toLocalDate(milliseconds);
                sqlBuilder.append(String.format(" AND %s >= '%s'", "birthday", after.toString()));
            } else if (param.equals("before")) {
                long milliseconds = Long.parseLong(value);
                LocalDate before = DateUtils.toLocalDate(milliseconds);
                sqlBuilder.append(String.format(" AND %s <= '%s'", "birthday", before.toString()));
            } else if (param.equals("banned") || param.equals("race") || param.equals("profession")) {
                sqlBuilder.append(String.format(" AND %s = '%s'", param, value));
            } else {
                sqlBuilder.append(String.format(" AND %s LIKE '%%%s%%'", param, value));
            }
        });

        sqlBuilder.append(String.format(" ORDER BY %s", order.getFieldName()));

        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Player.class)
                .setFirstResult(pageSize * pageNumber)
                .setMaxResults(pageSize);

        return query.getResultList();
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