package com.pets.hall.mapper;

import com.pets.hall.model.Album;
import com.pets.hall.model.AlbumHit;
import java.util.List;

public interface AlbumMapper {
    List<Album> findAll();

    List<AlbumHit> findHits();
}
