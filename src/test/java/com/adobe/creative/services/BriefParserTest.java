package com.adobe.creative.services;

import com.adobe.creative.models.CampaignBrief;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BriefParser service.
 * 
 * @author Ugur Köysüren
 */
class BriefParserTest {

    @Test
    void testParseJsonBrief() throws Exception {
        String jsonContent = """
            {
              "campaignId": "test-campaign",
              "products": [
                {
                  "productId": "prod-1",
                  "name": "Test Product",
                  "description": "Test description",
                  "category": "test"
                }
              ],
              "targetRegion": "Europe",
              "targetMarket": "Germany",
              "targetAudience": "Test audience",
              "campaignMessage": "Test message"
            }
            """;

        Path tempFile = Files.createTempFile("test-brief", ".json");
        Files.writeString(tempFile, jsonContent);

        try {
            BriefParser parser = new BriefParser();
            CampaignBrief brief = parser.parseBrief(tempFile.toString());

            assertNotNull(brief);
            assertEquals("test-campaign", brief.getCampaignId());
            assertEquals(1, brief.getProducts().size());
            assertEquals("Test Product", brief.getProducts().get(0).getName());
            assertEquals("Europe", brief.getTargetRegion());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testParseYamlBrief() throws Exception {
        String yamlContent = """
            campaignId: test-campaign
            products:
              - productId: prod-1
                name: Test Product
                description: Test description
                category: test
            targetRegion: Europe
            targetMarket: Germany
            targetAudience: Test audience
            campaignMessage: Test message
            """;

        Path tempFile = Files.createTempFile("test-brief", ".yaml");
        Files.writeString(tempFile, yamlContent);

        try {
            BriefParser parser = new BriefParser();
            CampaignBrief brief = parser.parseBrief(tempFile.toString());

            assertNotNull(brief);
            assertEquals("test-campaign", brief.getCampaignId());
            assertEquals(1, brief.getProducts().size());
            assertEquals("Test Product", brief.getProducts().get(0).getName());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testParseNonexistentFile() {
        BriefParser parser = new BriefParser();

        assertThrows(IOException.class, () -> {
            parser.parseBrief("/nonexistent/file.json");
        });
    }
}

