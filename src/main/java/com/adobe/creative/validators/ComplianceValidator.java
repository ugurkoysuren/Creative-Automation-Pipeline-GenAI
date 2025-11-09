package com.adobe.creative.validators;

import com.adobe.creative.models.BrandGuidelines;
import com.adobe.creative.models.ComplianceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComplianceValidator {
    private static final Logger logger = LoggerFactory.getLogger(ComplianceValidator.class);

    private final BrandGuidelines brandGuidelines;

    public ComplianceValidator(BrandGuidelines brandGuidelines) {
        this.brandGuidelines = brandGuidelines != null ? brandGuidelines : getDefaultGuidelines();
    }

    /**
     * Performs comprehensive compliance checks on an asset
     */
    public ComplianceResult validateAsset(byte[] imageData, String campaignMessage, boolean hasLogo) {
        logger.debug("Running compliance validation");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Brand compliance checks
        BrandComplianceResult brandChecks = validateBrandCompliance(imageData, hasLogo);
        issues.addAll(brandChecks.issues);
        warnings.addAll(brandChecks.warnings);

        // Legal compliance checks
        LegalComplianceResult legalChecks = validateLegalCompliance(campaignMessage);
        issues.addAll(legalChecks.issues);
        warnings.addAll(legalChecks.warnings);

        boolean brandCompliant = brandChecks.issues.isEmpty();
        boolean legalCompliant = legalChecks.issues.isEmpty();

        logger.info("Compliance check: Brand={}, Legal={}, Issues={}, Warnings={}",
            brandCompliant, legalCompliant, issues.size(), warnings.size());

        return ComplianceResult.builder()
            .brandCompliant(brandCompliant)
            .legalCompliant(legalCompliant)
            .issues(issues)
            .warnings(warnings)
            .build();
    }

    /**
     * Validates brand compliance
     */
    private BrandComplianceResult validateBrandCompliance(byte[] imageData, boolean hasLogo) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check if logo is required and present
        if (brandGuidelines.isLogoRequired() && !hasLogo) {
            issues.add("Brand logo is required but not present");
        }

        // Validate image quality
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            int width = image.getWidth();
            int height = image.getHeight();

            int minDimension = 1080;
            if (width < minDimension || height < minDimension) {
                warnings.add(String.format(
                    "Image dimensions (%dx%d) below recommended minimum (%dpx)",
                    width, height, minDimension
                ));
            }
        } catch (IOException e) {
            logger.error("Error validating image metadata: {}", e.getMessage());
            warnings.add("Could not validate image metadata");
        }

        return new BrandComplianceResult(issues, warnings);
    }

    /**
     * Validates legal compliance
     */
    private LegalComplianceResult validateLegalCompliance(String campaignMessage) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        List<String> prohibitedWords = brandGuidelines.getProhibitedWords() != null
            ? brandGuidelines.getProhibitedWords()
            : getDefaultProhibitedWords();

        // Check for prohibited words
        String messageLower = campaignMessage.toLowerCase();

        for (String word : prohibitedWords) {
            if (messageLower.contains(word.toLowerCase())) {
                issues.add("Prohibited word found: " + word);
            }
        }

        // Check message length
        if (campaignMessage.length() > 150) {
            warnings.add("Campaign message exceeds recommended length for social media (150 characters)");
        }

        // Check for claims that might need substantiation
        List<String> claimWords = Arrays.asList("best", "guaranteed", "proven", "scientific", "#1", "leading");
        List<String> foundClaims = new ArrayList<>();

        for (String word : claimWords) {
            if (messageLower.contains(word.toLowerCase())) {
                foundClaims.add(word);
            }
        }

        if (!foundClaims.isEmpty()) {
            warnings.add("Superlative claims found that may require substantiation: " +
                String.join(", ", foundClaims));
        }

        // Check for call-to-action
        boolean hasCallToAction = messageLower.contains("visit") ||
            messageLower.contains("shop") ||
            messageLower.contains("buy") ||
            messageLower.contains("discover") ||
            messageLower.contains("learn more") ||
            messageLower.contains("click");

        if (!hasCallToAction) {
            warnings.add("Consider adding a clear call-to-action");
        }

        return new LegalComplianceResult(issues, warnings);
    }

    private BrandGuidelines getDefaultGuidelines() {
        BrandGuidelines guidelines = new BrandGuidelines();
        String brandColors = System.getenv().getOrDefault("BRAND_COLORS", "#000000,#FFFFFF");
        guidelines.setPrimaryColors(Arrays.asList(brandColors.split(",")));
        guidelines.setLogoRequired(Boolean.parseBoolean(
            System.getenv().getOrDefault("BRAND_LOGO_REQUIRED", "true")
        ));
        guidelines.setProhibitedWords(getDefaultProhibitedWords());
        return guidelines;
    }

    private List<String> getDefaultProhibitedWords() {
        String prohibitedEnv = System.getenv().get("PROHIBITED_WORDS");
        if (prohibitedEnv != null) {
            return Arrays.asList(prohibitedEnv.split(","));
        }
        return Arrays.asList("guaranteed", "free", "miracle", "cure", "instant");
    }

    private static class BrandComplianceResult {
        List<String> issues;
        List<String> warnings;

        BrandComplianceResult(List<String> issues, List<String> warnings) {
            this.issues = issues;
            this.warnings = warnings;
        }
    }

    private static class LegalComplianceResult {
        List<String> issues;
        List<String> warnings;

        LegalComplianceResult(List<String> issues, List<String> warnings) {
            this.issues = issues;
            this.warnings = warnings;
        }
    }
}
