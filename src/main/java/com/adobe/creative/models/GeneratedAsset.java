package com.adobe.creative.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedAsset {
    private String productId;
    private AspectRatio aspectRatio;
    private String outputPath;
    private LocalDateTime generatedAt;
    private AssetMetadata metadata;
}
