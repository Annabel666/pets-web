package com.pets.hall.service;

import com.pets.hall.mapper.UserMapper;
import com.pets.hall.model.HallUser;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final String USERNAME_PATTERN = "^[\\u4e00-\\u9fffA-Za-z0-9_]{2,20}$";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(@Nullable String username, @Nullable String password, @Nullable String confirm) {
        String name = username == null ? "" : username.trim();
        if (!name.matches(USERNAME_PATTERN)) {
            return "名字用 2 到 20 位中文、字母或数字。";
        }
        if (password == null || password.length() < 6 || password.length() > 64) {
            return "密码至少 6 位。";
        }
        if (!password.equals(confirm)) {
            return "两次输入的密码不一致。";
        }
        if (userMapper.findByUsername(name) != null) {
            return "这个名字已经注册过了。";
        }
        HallUser user = new HallUser();
        user.setUsername(name);
        user.setPasswordHash(passwordEncoder.encode(password));
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            return "这个名字已经注册过了。";
        }
        return "";
    }

    public Optional<HallUser> login(@Nullable String username, @Nullable String password) {
        if (username == null || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        HallUser user = userMapper.findByUsername(username.trim());
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
