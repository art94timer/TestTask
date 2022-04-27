package com.game.service;

import com.game.controller.request.PlayerDTO;
import com.game.exception.ValidationException;

public interface PlayerValidationService {

    void validateCreateRequest(PlayerDTO person) throws ValidationException;

    void validateId(Long id);

    void validateUpdateRequest(PlayerDTO playerDTO);
}