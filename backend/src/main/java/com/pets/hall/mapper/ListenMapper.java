package com.pets.hall.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ListenMapper {
    List<String> findRecent(@Param("userId") long userId);

    int touch(@Param("userId") long userId, @Param("voiceId") String voiceId);

    int trim(@Param("userId") long userId);
}
