package com.adobe.creative.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAssets {
    @JsonProperty("heroImage")
    private String heroImage;
    
    @JsonProperty("image")
    private String image;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("additionalImages")
    private List<String> additionalImages;
}
