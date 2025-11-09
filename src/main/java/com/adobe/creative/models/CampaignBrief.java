package com.adobe.creative.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignBrief {
    @JsonProperty("campaignId")
    private String campaignId;

    @JsonProperty("products")
    private List<Product> products;

    @JsonProperty("targetRegion")
    private String targetRegion;

    @JsonProperty("targetMarket")
    private String targetMarket;

    @JsonProperty("targetAudience")
    private String targetAudience;

    @JsonProperty("campaignMessage")
    private String campaignMessage;

    @JsonProperty("brandGuidelines")
    private BrandGuidelines brandGuidelines;

    @JsonProperty("localizations")
    private Map<String, LocalizationConfig> localizations;
}
