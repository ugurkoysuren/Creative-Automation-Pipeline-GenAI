package com.adobe.creative.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalizationConfig {
    @JsonProperty("language")
    private String language;

    @JsonProperty("message")
    private String message;

    @JsonProperty("culturalNotes")
    private String culturalNotes;

    @JsonProperty("prohibitedWords")
    private List<String> prohibitedWords;
}
