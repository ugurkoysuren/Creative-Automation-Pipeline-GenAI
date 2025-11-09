package com.adobe.creative.validators;

import com.adobe.creative.models.BrandGuidelines;
import com.adobe.creative.models.ComplianceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComplianceValidator.
 * 
 * @author Ugur Köysüren
 */
class ComplianceValidatorTest {

    private BrandGuidelines guidelines;
    private ComplianceValidator validator;

    @BeforeEach
    void setUp() {
        guidelines = new BrandGuidelines();
        guidelines.setPrimaryColors(Arrays.asList("#FFC72C", "#DA291C"));
        guidelines.setLogoRequired(true);
        guidelines.setProhibitedWords(Arrays.asList("guaranteed", "free", "miracle"));
        
        validator = new ComplianceValidator(guidelines);
    }

    @Test
    void testValidateAssetWithLogo() throws IOException {
        byte[] mockImage = createMockImage();
        String message = "Great product for your family";
        
        ComplianceResult result = validator.validateAsset(mockImage, message, true);
        
        assertTrue(result.isBrandCompliant());
        assertTrue(result.isLegalCompliant());
    }

    @Test
    void testValidateAssetWithoutLogoWhenRequired() throws IOException {
        byte[] mockImage = createMockImage();
        String message = "Great product";
        
        ComplianceResult result = validator.validateAsset(mockImage, message, false);
        
        assertFalse(result.isBrandCompliant());
        assertFalse(result.getIssues().isEmpty());
        assertTrue(result.getIssues().stream()
            .anyMatch(issue -> issue.contains("logo")));
    }

    @Test
    void testValidateAssetWithProhibitedWord() throws IOException {
        byte[] mockImage = createMockImage();
        String message = "This product is guaranteed to work";

        ComplianceResult result = validator.validateAsset(mockImage, message, true);

        assertFalse(result.isLegalCompliant());
        assertTrue(result.getIssues().stream()
            .anyMatch(issue -> issue.contains("Prohibited")));
    }

    @Test
    void testValidateMultipleProhibitedWords() throws IOException {
        byte[] mockImage = createMockImage();
        String message = "Get this free guaranteed miracle product";

        ComplianceResult result = validator.validateAsset(mockImage, message, true);

        assertFalse(result.isLegalCompliant());
        assertEquals(3, result.getIssues().stream()
            .filter(issue -> issue.contains("Prohibited"))
            .count());
    }

    @Test
    void testValidateAssetCaseInsensitive() throws IOException {
        byte[] mockImage = createMockImage();
        String message = "This is GUARANTEED to be FREE and a MIRACLE";
        
        ComplianceResult result = validator.validateAsset(mockImage, message, true);
        
        assertFalse(result.isLegalCompliant());
    }

    @Test
    void testValidateWithNullGuidelines() throws IOException {
        ComplianceValidator nullValidator = new ComplianceValidator(null);
        byte[] mockImage = createMockImage();

        // When null guidelines are passed, default guidelines are used which require logo
        // So we pass true for hasLogo to ensure brand compliance passes
        ComplianceResult result = nullValidator.validateAsset(mockImage, "Any message", true);

        assertTrue(result.isBrandCompliant());
        assertTrue(result.isLegalCompliant());
    }

    private byte[] createMockImage() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}

