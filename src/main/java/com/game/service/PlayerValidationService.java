package com.game.service;

import com.game.controller.request.PlayerCreateDTO;
import com.game.exception.ValidationException;

public interface PlayerValidationService {

    void validate(PlayerCreateDTO person) throws ValidationException;

    void validate(Long id);
}