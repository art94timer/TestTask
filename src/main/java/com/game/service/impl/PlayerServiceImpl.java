package com.game.service.impl;

import com.game.controller.PlayerOrder;
import com.game.controller.request.PlayerDTO;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import com.game.service.PlayerService;
import com.game.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerServiceImpl.class);
    public static final String PAGE_NUMBER = "pageNumber";
    public static final String PAGE_SIZE = "pageSize";
    public static final String ORDER = "order";

    private final PlayerRepository repository;

    @PersistenceContext
    private EntityManager entityManager;


    public PlayerServiceImpl(PlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Player create(PlayerDTO playerDTO) {
        log.debug("Creating new person. Create person request {}.", playerDTO);
        Integer experience = playerDTO.getExperience();
        int currentLevel = computePersonCurrentLevel(experience);
        int expUntilNextLevel = computeExperienceUntilNextLevel(currentLevel, experience);

        Player player = new Player();
        player.setBanned(playerDTO.getBanned());
        player.setLevel(currentLevel);
        player.setBirthday(new Date(playerDTO.getBirthday()));
        player.setName(playerDTO.getName());
        player.setExperience(playerDTO.getExperience());
        player.setProfession(playerDTO.getProfession());
        player.setRace(playerDTO.getRace());
        player.setTitle(playerDTO.getTitle());
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
    public List<Player> findAllByParams(Map<String, String> params) {
        String pageNumberStr = params.remove(PAGE_NUMBER);
        String pageSizeStr = params.remove(PAGE_SIZE);
        String orderStr = params.remove(ORDER);

        int pageNumber = pageNumberStr == null ? PlayerRepository.DEFAULT_PAGE_NUMBER : Integer.parseInt(pageNumberStr);
        int pageSize = pageSizeStr == null ? PlayerRepository.DEFAULT_PAGE_SIZE : Integer.parseInt(pageSizeStr);
        PlayerOrder order = orderStr == null ? PlayerOrder.ID : PlayerOrder.valueOf(orderStr);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(PlayerRepository.SELECT_FROM_PLAYER);
        constructSqlConditions(params, sqlBuilder);
        constructSqlOrder(sqlBuilder, order);

        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Player.class)
                .setFirstResult(pageSize * pageNumber)
                .setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public int countAllByParams(Map<String, String> params) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(PlayerRepository.SELECT_COUNT_FROM_PLAYER);
        constructSqlConditions(params, sqlBuilder);
        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        BigInteger result = (BigInteger) query.getSingleResult();
        return result.intValue();
    }

    @Override
    public Optional<Player> update(Long id, PlayerDTO playerDTO) {
        Optional<Player> existingPlayer = findById(id);
        if (existingPlayer.isPresent()) {
            Player player = updateNonNullableFields(existingPlayer.get(), playerDTO);
            repository.save(player);
        }
        return existingPlayer;
    }

    private Player updateNonNullableFields(Player player, PlayerDTO playerDTO) {
        String name = playerDTO.getName();
        if (Objects.nonNull(name)) {
            player.setName(name);
        }


        String title = playerDTO.getTitle();
        if (Objects.nonNull(title)) {
            player.setTitle(title);
        }

        Long birthday = playerDTO.getBirthday();
        if (Objects.nonNull(birthday)) {
            player.setBirthday(new Date(birthday));
        }


        Integer experience = playerDTO.getExperience();
        if (Objects.nonNull(experience) && !experience.equals(player.getExperience())) {
            int computePersonCurrentLevel = computePersonCurrentLevel(experience);
            player.setLevel(computePersonCurrentLevel);
            int computeExperienceUntilNextLevel = computeExperienceUntilNextLevel(player.getLevel(), experience);
            player.setUntilNextLevel(computeExperienceUntilNextLevel);
            player.setExperience(experience);
        }

        Profession profession = playerDTO.getProfession();
        if (Objects.nonNull(profession)) {
            player.setProfession(profession);
        }

        Race race = playerDTO.getRace();
        if (Objects.nonNull(race)) {
            player.setRace(race);
        }

        Boolean banned = playerDTO.getBanned();
        if (Objects.nonNull(banned)) {
            player.setBanned(banned);
        }
        return player;
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

    private void constructSqlConditions(Map<String, String> params, StringBuilder sqlBuilder) {
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
    }

    private void constructSqlOrder(StringBuilder sqlBuilder, PlayerOrder order) {
        sqlBuilder.append(String.format(" ORDER BY %s", order.getFieldName()));
    }
}