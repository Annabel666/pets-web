package com.pets.hall.model;

public class SessionUser {
    private final Long id;
    private final String username;

    public SessionUser(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
