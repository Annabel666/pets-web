package com.pets.hall.web;

import com.pets.hall.model.Voice;

public final class SharePage {
    static final String DEFAULT_TITLE = "曾沛慈 · 礼物厅";
    static final String DEFAULT_DESC = "曾沛慈的粉丝档案：听歌、查舞台、看专辑、留言。不是官方站点。";

    private SharePage() {}

    public static String title(Voice voice) {
        return voice.getTitle() + " · " + DEFAULT_TITLE;
    }

    public static String description(Voice voice) {
        StringBuilder text = new StringBuilder();
        text.append("《").append(voice.getTitle()).append("》");
        if (voice.getYear() != null) {
            text.append(" · ").append(voice.getYear());
        }
        if (voice.getNote() != null && !voice.getNote().isBlank()) {
            text.append(" · ").append(voice.getNote());
        }
        text.append("。粉丝整理的档案，不是官方站点。");
        return text.toString();
    }

    public static String apply(String html, String title, String description) {
        return html.replace(DEFAULT_TITLE, escape(title)).replace(DEFAULT_DESC, escape(description));
    }

    static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
