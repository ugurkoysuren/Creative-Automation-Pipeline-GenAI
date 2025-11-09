package com.adobe.creative.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationSummary {
    private int totalAssets;
    private int assetsGenerated;
    private int assetsReused;
    private int assetsResized;
    private int complianceIssues;
    private long duration; // milliseconds
}
