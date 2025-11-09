package com.adobe.creative.services;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ImageGenerator service.
 * 
 * @author Ugur Köysüren
 */
class ImageGeneratorTest {

    @Test
    void testBuildPrompt() {
        ImageGenerator generator = new ImageGenerator();
        
        String prompt = generator.buildPrompt(
            "Crispy Chicken Deluxe",
            "Tender chicken breast with lettuce and mayo",
            "Families and young professionals",
            "Europe",
            null
        );
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("Crispy Chicken Deluxe"));
        assertTrue(prompt.contains("Families and young professionals"));
        assertTrue(prompt.contains("Europe"));
        assertTrue(prompt.contains("Professional product photography"));
    }

    @Test
    void testGenerateMockImage() throws IOException {
        ImageGenerator generator = new ImageGenerator();
        
        byte[] imageData = generator.generateImage(
            "Test product",
            1080,
            1080
        );
        
        assertNotNull(imageData);
        assertTrue(imageData.length > 0);
    }

    @Test
    void testBuildPromptWithEmptyDescription() {
        ImageGenerator generator = new ImageGenerator();
        
        String prompt = generator.buildPrompt(
            "Product Name",
            "",
            "Target Audience",
            "Region",
            null
        );
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("Product Name"));
    }

    @Test
    void testBuildPromptWithCulturalNotes() {
        ImageGenerator generator = new ImageGenerator();
        
        String prompt = generator.buildPrompt(
            "Crispy Chicken Deluxe",
            "Tender chicken breast with lettuce and mayo",
            "Families and young professionals",
            "Europe",
            "Germans appreciate quality ingredients and local sourcing"
        );
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("Crispy Chicken Deluxe"));
        assertTrue(prompt.contains("Cultural context"));
        assertTrue(prompt.contains("Germans appreciate quality ingredients"));
    }

    @Test
    void testBuildPromptWithEmptyCulturalNotes() {
        ImageGenerator generator = new ImageGenerator();
        
        String prompt = generator.buildPrompt(
            "Product Name",
            "Description",
            "Audience",
            "Region",
            ""
        );
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("Product Name"));
        assertFalse(prompt.contains("Cultural context"));
    }

    @Test
    void testGenerateImageDifferentSizes() throws IOException {
        ImageGenerator generator = new ImageGenerator();
        
        byte[] square = generator.generateImage("Test", 1080, 1080);
        byte[] portrait = generator.generateImage("Test", 1080, 1920);
        byte[] landscape = generator.generateImage("Test", 1920, 1080);
        
        assertNotNull(square);
        assertNotNull(portrait);
        assertNotNull(landscape);
        
        assertTrue(square.length > 0);
        assertTrue(portrait.length > 0);
        assertTrue(landscape.length > 0);
    }
}

