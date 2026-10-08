package com.pets.hall.service;

import com.pets.hall.mapper.AlbumMapper;
import com.pets.hall.mapper.ArtistMapper;
import com.pets.hall.mapper.QuoteMapper;
import com.pets.hall.mapper.StageMapper;
import com.pets.hall.mapper.VoiceMapper;
import com.pets.hall.model.Album;
import com.pets.hall.model.AlbumHit;
import com.pets.hall.model.Artist;
import com.pets.hall.model.Quote;
import com.pets.hall.model.QuoteTag;
import com.pets.hall.model.Stage;
import com.pets.hall.model.Voice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ArchiveService {
    private final ArtistMapper artistMapper;
    private final VoiceMapper voiceMapper;
    private final QuoteMapper quoteMapper;
    private final StageMapper stageMapper;
    private final AlbumMapper albumMapper;

    public ArchiveService(
            ArtistMapper artistMapper,
            VoiceMapper voiceMapper,
            QuoteMapper quoteMapper,
            StageMapper stageMapper,
            AlbumMapper albumMapper) {
        this.artistMapper = artistMapper;
        this.voiceMapper = voiceMapper;
        this.quoteMapper = quoteMapper;
        this.stageMapper = stageMapper;
        this.albumMapper = albumMapper;
    }

    public Optional<Voice> findVoice(String id) {
        if (id == null || !id.matches("^[A-Za-z0-9_-]{1,64}$")) {
            return Optional.empty();
        }
        return voiceMapper.findAll().stream().filter(voice -> id.equals(voice.getId())).findFirst();
    }

    public Map<String, Object> load() {
        Map<String, Object> archive = new LinkedHashMap<>();
        archive.put("artist", artistMap(artistMapper.findOne()));
        archive.put("voices", voices());
        archive.put("quotes", quotes());
        archive.put("stages", stages());
        archive.put("albums", albums());
        return archive;
    }

    private Map<String, Object> artistMap(Artist artist) {
        List<String> roles = Arrays.asList(artist.getRoles().split(" / "));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", artist.getName());
        map.put("english", artist.getEnglishName());
        map.put("born", artist.getBorn());
        map.put("bornPlace", artist.getBornPlace());
        map.put("roles", roles);
        map.put("rolesText", String.join(" · ", roles));
        map.put("tagline", artist.getTagline());
        map.put("bio", artist.getBio());
        return map;
    }

    private List<Map<String, Object>> voices() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Voice voice : voiceMapper.findAll()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", voice.getId());
            map.put("title", voice.getTitle());
            map.put("year", voice.getYear());
            map.put("note", voice.getNote());
            map.put("bvid", voice.getBvid());
            map.put("list", voice.getListName());
            if (Integer.valueOf(1).equals(voice.getAutoplay())) {
                map.put("autoplay", true);
            }
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> quotes() {
        Map<String, List<String>> tags = new LinkedHashMap<>();
        for (QuoteTag tag : quoteMapper.findTags()) {
            tagsFor(tags, tag.getQuoteId()).add(tag.getTag());
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Quote quote : quoteMapper.findAll()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", quote.getId());
            map.put("text", quote.getText());
            map.put("source", quote.getSource());
            map.put("year", quote.getYear());
            map.put("tags", tagsFor(tags, quote.getId()));
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> stages() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Stage stage : stageMapper.findAll()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", stage.getId());
            map.put("date", stage.getDate());
            map.put("place", stage.getPlace());
            map.put("venue", stage.getVenue());
            map.put("title", stage.getTitle());
            map.put("note", stage.getNote());
            map.put("kind", stage.getKind());
            String voiceId = stage.getVoiceId();
            if (voiceId != null && !voiceId.isBlank()) {
                map.put("voiceId", voiceId);
            }
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> albums() {
        Map<Integer, List<String>> hits = new LinkedHashMap<>();
        for (AlbumHit hit : albumMapper.findHits()) {
            titlesFor(hits, hit.getAlbumId()).add(hit.getTitle());
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Album album : albumMapper.findAll()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("year", album.getYear());
            map.put("title", album.getTitle());
            map.put("label", album.getLabel());
            map.put("hits", titlesFor(hits, album.getId()));
            list.add(map);
        }
        return list;
    }

    private static List<String> tagsFor(Map<String, List<String>> tags, String quoteId) {
        List<String> values = tags.get(quoteId);
        if (values == null) {
            values = new ArrayList<>();
            tags.put(quoteId, values);
        }
        return values;
    }

    private static List<String> titlesFor(Map<Integer, List<String>> hits, Integer albumId) {
        List<String> values = hits.get(albumId);
        if (values == null) {
            values = new ArrayList<>();
            hits.put(albumId, values);
        }
        return values;
    }
}
