package com.epam.macromind.food;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record UsdaSearchDto(List<SearchHit> foods) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchHit(Integer fdcId, String description) {}
}
