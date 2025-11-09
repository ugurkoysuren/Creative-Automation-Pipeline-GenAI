package com.adobe.creative.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetMetadata {
    private String campaignId;
    private String product;
    private String region;
    private String aspectRatio;
    private String message;
    private String generationMethod; // "reused", "generated", "resized"
    private ComplianceResult complianceChecks;
}
