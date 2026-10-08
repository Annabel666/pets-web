package com.pets.hall.mapper;

import com.pets.hall.model.HallUser;
import org.springframework.lang.Nullable;

public interface UserMapper {
    @Nullable
    HallUser findByUsername(String username);

    int insert(HallUser user);
}
