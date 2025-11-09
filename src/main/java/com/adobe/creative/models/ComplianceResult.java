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
public class ComplianceResult {
    private boolean brandCompliant;
    private boolean legalCompliant;

    @Builder.Default
    private List<String> issues = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
