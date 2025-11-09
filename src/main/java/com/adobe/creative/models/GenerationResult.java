package com.adobe.creative.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResult {
    private boolean success;

    @Builder.Default
    private List<GeneratedAsset> assets = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    private GenerationSummary summary;
}
