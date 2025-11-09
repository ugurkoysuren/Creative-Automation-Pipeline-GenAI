package com.adobe.creative.services;

import com.adobe.creative.models.CampaignBrief;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class BriefParser {
    private static final Logger logger = LoggerFactory.getLogger(BriefParser.class);
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public BriefParser() {
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Parses a campaign brief from a JSON or YAML file
     */
    public CampaignBrief parseBrief(String filePath) throws IOException {
        logger.info("Parsing campaign brief from: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Brief file not found: " + filePath);
        }

        String extension = getFileExtension(filePath).toLowerCase();
        CampaignBrief brief;

        try {
            if ("json".equals(extension)) {
                brief = jsonMapper.readValue(file, CampaignBrief.class);
            } else if ("yaml".equals(extension) || "yml".equals(extension)) {
                brief = yamlMapper.readValue(file, CampaignBrief.class);
            } else {
                throw new IllegalArgumentException(
                    "Unsupported file format: " + extension + ". Use JSON or YAML."
                );
            }

            validateBrief(brief);
            logger.info("Successfully parsed brief for campaign: {}", brief.getCampaignId());
            return brief;
        } catch (Exception e) {
            logger.error("Failed to parse brief: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates that a brief has all required fields
     */
    private void validateBrief(CampaignBrief brief) {
        if (brief.getCampaignId() == null || brief.getCampaignId().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: campaignId");
        }
        if (brief.getProducts() == null || brief.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Brief must contain at least one product");
        }
        if (brief.getTargetRegion() == null || brief.getTargetRegion().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: targetRegion");
        }
        if (brief.getTargetMarket() == null || brief.getTargetMarket().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: targetMarket");
        }
        if (brief.getTargetAudience() == null || brief.getTargetAudience().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: targetAudience");
        }
        if (brief.getCampaignMessage() == null || brief.getCampaignMessage().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: campaignMessage");
        }

        // Validate products
        brief.getProducts().forEach(product -> {
            if (product.getProductId() == null || product.getProductId().isEmpty()) {
                throw new IllegalArgumentException("Each product must have a productId");
            }
            if (product.getName() == null || product.getName().isEmpty()) {
                throw new IllegalArgumentException("Each product must have a name");
            }
        });

        logger.debug("Brief validation passed");
    }

    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        return lastDot > 0 ? filePath.substring(lastDot + 1) : "";
    }
}
