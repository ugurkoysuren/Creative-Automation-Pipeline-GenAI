package com.adobe.creative.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @JsonProperty("productId")
    private String productId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("assets")
    private ProductAssets assets;

    @JsonProperty("localizations")
    private Map<String, ProductLocalization> localizations;

    /**
     * Get localized name for a specific locale, fallback to default name
     */
    public String getLocalizedName(String locale) {
        if (localizations != null && localizations.containsKey(locale)) {
            ProductLocalization loc = localizations.get(locale);
            if (loc.getName() != null) {
                return loc.getName();
            }
        }
        return name;
    }

    /**
     * Get localized description for a specific locale, fallback to default description
     */
    public String getLocalizedDescription(String locale) {
        if (localizations != null && localizations.containsKey(locale)) {
            ProductLocalization loc = localizations.get(locale);
            if (loc.getDescription() != null) {
                return loc.getDescription();
            }
        }
        return description;
    }
}
