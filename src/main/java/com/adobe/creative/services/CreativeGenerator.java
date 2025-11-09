package com.adobe.creative.services;

import com.adobe.creative.models.*;
import com.adobe.creative.validators.ComplianceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main orchestrator for generating localized campaign assets.
 * Handles product processing, image generation, overlay application,
 * and compliance validation across multiple localizations.
 * 
 * @author Ugur Köysüren
 */
public class CreativeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CreativeGenerator.class);

    private final ImageGenerator imageGenerator;
    private final AssetManager assetManager;
    private final ComplianceValidator complianceValidator;

    public CreativeGenerator() {
        this.imageGenerator = new ImageGenerator();
        this.assetManager = new AssetManager();
        this.complianceValidator = new ComplianceValidator(null);
    }

    /**
     * Generates all creative assets for a campaign brief (all localizations)
     */
    public GenerationResult generateCampaignAssets(CampaignBrief brief) {
        logger.info("Generating assets for all localizations");
        
        // If no localizations defined, generate with default settings
        if (brief.getLocalizations() == null || brief.getLocalizations().isEmpty()) {
            return generateCampaignAssetsForLocale(brief, null);
        }
        
        // Generate for each localization
        List<GeneratedAsset> allAssets = new ArrayList<>();
        List<String> allErrors = new ArrayList<>();
        long totalDuration = 0;
        int totalGenerated = 0;
        int totalReused = 0;
        int totalResized = 0;
        int totalCompliance = 0;
        
        for (String locale : brief.getLocalizations().keySet()) {
            logger.info("Generating assets for locale: {}", locale);
            GenerationResult localeResult = generateCampaignAssetsForLocale(brief, locale);
            
            allAssets.addAll(localeResult.getAssets());
            allErrors.addAll(localeResult.getErrors());
            totalDuration += localeResult.getSummary().getDuration();
            totalGenerated += localeResult.getSummary().getAssetsGenerated();
            totalReused += localeResult.getSummary().getAssetsReused();
            totalResized += localeResult.getSummary().getAssetsResized();
            totalCompliance += localeResult.getSummary().getComplianceIssues();
        }
        
        GenerationSummary summary = GenerationSummary.builder()
            .totalAssets(allAssets.size())
            .assetsGenerated(totalGenerated)
            .assetsReused(totalReused)
            .assetsResized(totalResized)
            .complianceIssues(totalCompliance)
            .duration(totalDuration)
            .build();
        
        return GenerationResult.builder()
            .success(allErrors.isEmpty())
            .assets(allAssets)
            .errors(allErrors)
            .summary(summary)
            .build();
    }

    /**
     * Generates creative assets for a specific locale
     */
    public GenerationResult generateCampaignAssetsForLocale(CampaignBrief brief, String locale) {
        long startTime = System.currentTimeMillis();

        // Validate inputs
        if (brief == null) {
            throw new IllegalArgumentException("Campaign brief cannot be null");
        }
        if (brief.getProducts() == null || brief.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Campaign brief must contain at least one product");
        }

        // Get localization config
        LocalizationConfig localeConfig = null;
        String campaignMessage = brief.getCampaignMessage();
        if (campaignMessage == null) {
            campaignMessage = ""; // Default to empty string to avoid NPE
        }
        String campaignId = brief.getCampaignId();
        
        if (locale != null && brief.getLocalizations() != null) {
            localeConfig = brief.getLocalizations().get(locale);
            if (localeConfig != null) {
                campaignMessage = localeConfig.getMessage();
                campaignId = brief.getCampaignId() + "-" + locale.toLowerCase();
            }
        }

        logger.info("Starting asset generation for campaign: {} (locale: {})", 
            campaignId, locale != null ? locale : "default");
        logger.info("Generating assets for {} products across {} aspect ratios",
            brief.getProducts().size(), AspectRatio.STANDARD_RATIOS.size());

        List<GeneratedAsset> generatedAssets = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int assetsGenerated = 0;
        int assetsReused = 0;
        int assetsResized = 0;
        int complianceIssues = 0;

        // Update compliance validator with brand guidelines and localized prohibited words
        BrandGuidelines guidelines = brief.getBrandGuidelines();
        if (localeConfig != null && localeConfig.getProhibitedWords() != null) {
            // Create a copy of brand guidelines with localized prohibited words
            BrandGuidelines localizedGuidelines = new BrandGuidelines();
            localizedGuidelines.setPrimaryColors(guidelines.getPrimaryColors());
            localizedGuidelines.setSecondaryColors(guidelines.getSecondaryColors());
            localizedGuidelines.setFontFamily(guidelines.getFontFamily());
            localizedGuidelines.setLogoRequired(guidelines.isLogoRequired());
            localizedGuidelines.setProhibitedWords(localeConfig.getProhibitedWords());
            guidelines = localizedGuidelines;
        }
        ComplianceValidator validator = new ComplianceValidator(guidelines);

        logger.info("Using NATIVE generation strategy - generating each aspect ratio at native resolution");

        // Process each product
        for (Product product : brief.getProducts()) {
            try {
                // Get localized product name and description
                String productName = locale != null ? product.getLocalizedName(locale) : product.getName();
                String productDesc = locale != null ? product.getLocalizedDescription(locale) : product.getDescription();
                
                logger.info("Processing product: {} ({}) [locale: {}]", 
                    productName, product.getProductId(), locale != null ? locale : "default");

                // Get logo if available
                byte[] logo = null;
                try {
                    logo = assetManager.getLogo(product);
                } catch (Exception e) {
                    logger.warn("Failed to load logo for {}: {}", product.getProductId(), e.getMessage());
                }

                // Generate at native resolution for each aspect ratio
                for (AspectRatio aspectRatio : AspectRatio.STANDARD_RATIOS) {
                    try {
                        logger.info("Generating native {} ({}x{}) for {}", 
                            aspectRatio.getName(), aspectRatio.getWidth(), aspectRatio.getHeight(), 
                            productName);

                        // Generate image at native aspect ratio resolution
                        byte[] nativeImage = generateImageAtResolution(brief, product, aspectRatio, locale);
                        assetsGenerated++;

                        // Add logo if available
                        byte[] finalImage = nativeImage;
                        if (logo != null) {
                            finalImage = assetManager.addLogoOverlay(finalImage, logo, "top-right");
                        }

                        // Add campaign message
                        finalImage = assetManager.addTextOverlay(finalImage, campaignMessage, "bottom");

                        // Run compliance checks
                        ComplianceResult complianceResult = validator.validateAsset(
                            finalImage, campaignMessage, logo != null
                        );

                        if (!complianceResult.isBrandCompliant() || !complianceResult.isLegalCompliant()) {
                            complianceIssues++;
                            logger.warn("Compliance issues for {} {}: {}",
                                product.getProductId(), aspectRatio.getName(),
                                String.join(", ", complianceResult.getIssues()));
                        }

                        // Save asset with localized campaign ID
                        String outputPath = assetManager.saveAsset(
                            finalImage, campaignId, product.getProductId(), aspectRatio.getName()
                        );

                        // Record generated asset
                        AssetMetadata metadata = AssetMetadata.builder()
                            .campaignId(campaignId)
                            .product(productName)
                            .region(brief.getTargetRegion())
                            .aspectRatio(aspectRatio.getName())
                            .message(campaignMessage)
                            .generationMethod("native")
                            .complianceChecks(complianceResult)
                            .build();

                        GeneratedAsset asset = new GeneratedAsset();
                        asset.setProductId(product.getProductId());
                        asset.setAspectRatio(aspectRatio);
                        asset.setOutputPath(outputPath);
                        asset.setGeneratedAt(LocalDateTime.now());
                        asset.setMetadata(metadata);

                        generatedAssets.add(asset);
                        logger.info("Successfully generated asset: {}", outputPath);

                    } catch (Exception e) {
                        String error = String.format("Failed to generate %s for %s: %s",
                            aspectRatio.getName(), product.getProductId(), e.getMessage());
                        logger.error(error);
                        errors.add(error);
                    }
                }
            } catch (Exception e) {
                String error = "Failed to process product " + product.getProductId() + ": " + e.getMessage();
                logger.error(error);
                errors.add(error);
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        GenerationSummary summary = GenerationSummary.builder()
            .totalAssets(generatedAssets.size())
            .assetsGenerated(assetsGenerated)
            .assetsReused(assetsReused)
            .assetsResized(assetsResized)
            .complianceIssues(complianceIssues)
            .duration(duration)
            .build();

        logger.info("Asset generation complete in {:.2f}s", duration / 1000.0);
        logger.info("Total assets: {}, Generated: {}, Reused: {}",
            summary.getTotalAssets(), assetsGenerated, assetsReused);

        return GenerationResult.builder()
            .success(errors.isEmpty())
            .assets(generatedAssets)
            .errors(errors)
            .summary(summary)
            .build();
    }

    /**
     * Generates an image at a specific aspect ratio resolution
     * Used for native generation strategy
     * First checks if product image exists, if yes uses it, otherwise generates
     */
    private byte[] generateImageAtResolution(CampaignBrief brief, Product product, AspectRatio aspectRatio, String locale) {
        try {
            // First, try to get existing product image
            byte[] productImage = assetManager.getProductImage(product);
            
            if (productImage != null) {
                // Use existing product image and resize to target aspect ratio
                logger.info("Using provided product image for {} at {}x{}", 
                    product.getProductId(), aspectRatio.getWidth(), aspectRatio.getHeight());
                return assetManager.resizeImage(productImage, aspectRatio);
            }
            
            // If no product image, generate with AI
            logger.info("No product image found, generating with AI for {}", product.getProductId());
            
            // Get localization config for cultural notes
            String culturalNotes = null;
            if (locale != null && brief.getLocalizations() != null) {
                LocalizationConfig localeConfig = brief.getLocalizations().get(locale);
                if (localeConfig != null && localeConfig.getCulturalNotes() != null) {
                    culturalNotes = localeConfig.getCulturalNotes();
                    logger.debug("Using cultural notes for locale {}: {}", locale, culturalNotes);
                }
            }
            
            // Use localized product name and description for AI prompt
            String productName = locale != null ? product.getLocalizedName(locale) : product.getName();
            String productDesc = locale != null ? product.getLocalizedDescription(locale) : product.getDescription();
            
            String prompt = imageGenerator.buildPrompt(
                productName,
                productDesc != null ? productDesc : "High-quality product",
                brief.getTargetAudience(),
                brief.getTargetRegion(),
                culturalNotes
            );
            
            // Add aspect ratio hint to prompt for better composition
            String enhancedPrompt = prompt + String.format(
                " Optimized composition for %s aspect ratio (%d x %d pixels).",
                aspectRatio.getName(), aspectRatio.getWidth(), aspectRatio.getHeight()
            );

            logger.debug("Generating image at native resolution: {}x{}", 
                aspectRatio.getWidth(), aspectRatio.getHeight());

            return imageGenerator.generateImage(
                enhancedPrompt, 
                aspectRatio.getWidth(), 
                aspectRatio.getHeight()
            );
        } catch (Exception e) {
            logger.error("Failed to generate image at resolution {}x{}: {}", 
                aspectRatio.getWidth(), aspectRatio.getHeight(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generates a detailed report of the generation process
     */
    public String generateReport(GenerationResult result) {
        StringBuilder report = new StringBuilder();

        report.append("=".repeat(80)).append("\n");
        report.append("CREATIVE AUTOMATION PIPELINE - GENERATION REPORT\n");
        report.append("=".repeat(80)).append("\n\n");

        report.append("SUMMARY:\n");
        report.append(String.format("  Total Assets Generated: %d\n", result.getSummary().getTotalAssets()));
        report.append(String.format("  New Images Generated:   %d\n", result.getSummary().getAssetsGenerated()));
        report.append(String.format("  Existing Images Reused: %d\n", result.getSummary().getAssetsReused()));
        report.append(String.format("  Images Resized:         %d\n", result.getSummary().getAssetsResized()));
        report.append(String.format("  Compliance Issues:      %d\n", result.getSummary().getComplianceIssues()));
        report.append(String.format("  Duration:               %.2fs\n", result.getSummary().getDuration() / 1000.0));
        report.append(String.format("  Status:                 %s\n", result.isSuccess() ? "SUCCESS" : "PARTIAL SUCCESS"));
        report.append("\n");

        if (!result.getAssets().isEmpty()) {
            report.append("GENERATED ASSETS:\n");
            for (GeneratedAsset asset : result.getAssets()) {
                report.append(String.format("  %s\n", asset.getOutputPath()));
                report.append(String.format("    Product: %s\n", asset.getMetadata().getProduct()));
                report.append(String.format("    Aspect Ratio: %s (%s)\n",
                    asset.getMetadata().getAspectRatio(),
                    String.join(", ", asset.getAspectRatio().getPlatforms())));
                report.append(String.format("    Method: %s\n", asset.getMetadata().getGenerationMethod()));

                if (asset.getMetadata().getComplianceChecks() != null) {
                    ComplianceResult compliance = asset.getMetadata().getComplianceChecks();
                    String status = compliance.isBrandCompliant() && compliance.isLegalCompliant()
                        ? "PASSED"
                        : !compliance.getIssues().isEmpty() ? "FAILED" : "WARNING";
                    report.append(String.format("    Compliance: %s\n", status));

                    if (!compliance.getIssues().isEmpty()) {
                        report.append(String.format("    Issues: %s\n", String.join("; ", compliance.getIssues())));
                    }
                    if (!compliance.getWarnings().isEmpty()) {
                        report.append(String.format("    Warnings: %s\n", String.join("; ", compliance.getWarnings())));
                    }
                }
                report.append("\n");
            }
        }

        if (!result.getErrors().isEmpty()) {
            report.append("ERRORS:\n");
            for (String error : result.getErrors()) {
                report.append(String.format("  - %s\n", error));
            }
            report.append("\n");
        }

        report.append("=".repeat(80)).append("\n");

        return report.toString();
    }
}
