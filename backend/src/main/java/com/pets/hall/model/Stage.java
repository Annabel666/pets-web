package com.pets.hall.model;

import org.springframework.lang.Nullable;

public class Stage {
    private String id;
    private String date;
    private String place;
    private String venue;
    private String title;
    private String note;
    private String kind;
    private String voiceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Nullable
    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(@Nullable String voiceId) {
        this.voiceId = voiceId;
    }
}
