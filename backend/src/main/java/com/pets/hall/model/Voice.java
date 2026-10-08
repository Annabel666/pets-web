package com.pets.hall.model;

import org.springframework.lang.Nullable;

public class Voice {
    private String id;
    private String title;
    private Integer year;
    private String note;
    private String bvid;
    private String listName;
    private Integer autoplay;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getBvid() {
        return bvid;
    }

    public void setBvid(String bvid) {
        this.bvid = bvid;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    @Nullable
    public Integer getAutoplay() {
        return autoplay;
    }

    public void setAutoplay(@Nullable Integer autoplay) {
        this.autoplay = autoplay;
    }
}
