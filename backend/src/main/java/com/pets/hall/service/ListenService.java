package com.pets.hall.service;

import com.pets.hall.mapper.ListenMapper;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ListenService {
    private static final Pattern VOICE_ID = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
    private final ListenMapper listenMapper;

    public ListenService(ListenMapper listenMapper) {
        this.listenMapper = listenMapper;
    }

    public List<String> recent(long userId) {
        return listenMapper.findRecent(userId);
    }

    public boolean remember(long userId, String voiceId) {
        if (voiceId == null || !VOICE_ID.matcher(voiceId).matches()) {
            return false;
        }
        listenMapper.touch(userId, voiceId);
        listenMapper.trim(userId);
        return true;
    }
}
