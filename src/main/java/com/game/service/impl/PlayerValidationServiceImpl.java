package com.game.service.impl;

import com.game.controller.request.PlayerCreateDTO;
import com.game.exception.ValidationException;
import com.game.service.PlayerValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class PlayerValidationServiceImpl implements PlayerValidationService {

    private static final Logger log = LoggerFactory.getLogger(PlayerValidationServiceImpl.class);

    private static final Integer MAX_NAME_LENGTH = 12;
    private static final Integer MAX_TITLE_LENGTH = 30;

    //2000-01-01
    private static final Long MIN_BIRTHDAY_VALUE = 946_677_600_000L;
    //3000-12-31
    private static final Long MAX_BIRTHDAY_VALUE = 32_535_118_800_000L;

    private static final Integer MIN_EXPERIENCE_VALUE = 0;
    private static final Integer MAX_EXPERIENCE_VALUE = 10_000_000;

    private static final Integer MIN_PLAYER_ID = 1;


    @Override
    public void validate(PlayerCreateDTO player) throws ValidationException {
        log.debug("Validating create player request...");
        log.trace("Player {}.", player);

        if (Objects.isNull(player)) {
            log.warn("Invalid argument. Player is null.");
            throw new ValidationException("Player is null");
        }

        String name = player.getName();
        if (StringUtils.isEmpty(name) || name.length() > MAX_NAME_LENGTH) {
            log.warn("Invalid player's field. Name {}.", name);
            throw new ValidationException("Name has invalid value");
        }

        String title = player.getTitle();
        if (Objects.isNull(title) || title.length() > MAX_TITLE_LENGTH) {
            log.warn("Invalid player's field. Title {}.", name);
            throw new ValidationException("Title has invalid value");
        }

        Long birthday = player.getBirthday();
        if (Objects.isNull(birthday) || birthday < MIN_BIRTHDAY_VALUE || birthday > MAX_BIRTHDAY_VALUE) {
            log.warn("Invalid player's field. Birthday {}.", birthday);
            throw new ValidationException("Birthday has invalid value");
        }

        Integer experience = player.getExperience();
        if (Objects.isNull(experience) || experience < MIN_EXPERIENCE_VALUE || experience > MAX_EXPERIENCE_VALUE) {
            log.warn("Invalid player's field. Experience {}.", experience);
            throw new ValidationException("Experience has invalid value");
        }
    }

    @Override
    public void validate(Long id) {
        if (Objects.isNull(id) || id < MIN_PLAYER_ID) {
            log.warn("Invalid player's id. Id {}.", id);
            throw new ValidationException("Id has invalid value");
        }
    }
}