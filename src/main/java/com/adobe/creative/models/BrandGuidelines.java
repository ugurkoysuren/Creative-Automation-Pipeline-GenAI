package com.adobe.creative.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandGuidelines {
    @JsonProperty("primaryColors")
    private List<String> primaryColors;

    @JsonProperty("secondaryColors")
    private List<String> secondaryColors;

    @JsonProperty("fontFamily")
    private String fontFamily;

    @JsonProperty("logoRequired")
    private boolean logoRequired;

    @JsonProperty("prohibitedWords")
    private List<String> prohibitedWords;
}
