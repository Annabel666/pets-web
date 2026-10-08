package com.pets.hall.mapper;

import com.pets.hall.model.Quote;
import com.pets.hall.model.QuoteTag;
import java.util.List;

public interface QuoteMapper {
    List<Quote> findAll();

    List<QuoteTag> findTags();
}
