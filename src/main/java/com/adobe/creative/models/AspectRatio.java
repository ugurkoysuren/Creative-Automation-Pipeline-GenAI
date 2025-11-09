package com.adobe.creative.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AspectRatio {
    private String name;
    private int width;
    private int height;
    private List<String> platforms;

    public static final List<AspectRatio> STANDARD_RATIOS = List.of(
        new AspectRatio("1:1", 1080, 1080, List.of("Instagram Post", "Facebook Post")),
        new AspectRatio("9:16", 1080, 1920, List.of("Instagram Story", "TikTok", "Reels")),
        new AspectRatio("16:9", 1920, 1080, List.of("YouTube", "Facebook Video", "LinkedIn"))
    );
}
