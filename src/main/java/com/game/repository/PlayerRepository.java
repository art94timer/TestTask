package com.game.repository;

import com.game.entity.Player;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends PagingAndSortingRepository<Player, Long> {

    String SELECT_FROM_PLAYER = "SELECT * FROM player WHERE 1=1";
    Integer DEFAULT_PAGE_NUMBER = 0;
    Integer DEFAULT_PAGE_SIZE = 3;

}