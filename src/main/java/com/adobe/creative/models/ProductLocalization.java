package com.adobe.creative.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductLocalization {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;
}

