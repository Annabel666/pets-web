package com.pets.hall.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pets.hall.model.Voice;
import org.junit.jupiter.api.Test;

class SharePageTest {
    @Test
    void putsTheSongIntoTheTitleAndDescription() {
        String html = "<title>曾沛慈 · 礼物厅</title>"
                + "<meta property=\"og:title\" content=\"曾沛慈 · 礼物厅\" />"
                + "<meta name=\"description\" content=\"曾沛慈的粉丝档案：听歌、查舞台、看专辑、留言。不是官方站点。\" />";
        Voice voice = new Voice();
        voice.setTitle("半\"半");
        voice.setYear(2026);
        voice.setNote("一公");
        String page = SharePage.apply(html, SharePage.title(voice), SharePage.description(voice));
        assertTrue(page.contains("<title>半&quot;半 · 曾沛慈 · 礼物厅</title>"));
        assertTrue(page.contains("content=\"半&quot;半 · 曾沛慈 · 礼物厅\""));
        assertTrue(page.contains("《半&quot;半》 · 2026 · 一公。粉丝整理的档案，不是官方站点。"));
    }
}
