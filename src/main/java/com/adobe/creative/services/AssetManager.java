package com.adobe.creative.services;

import com.adobe.creative.models.AspectRatio;
import com.adobe.creative.models.Product;
import com.adobe.creative.utils.ConfigManager;
import com.adobe.creative.utils.FileUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages asset loading, image processing, and overlay applications.
 * Handles resizing, logo placement, and text overlay generation.
 * 
 * @author Ugur Köysüren
 */
@Getter
public class AssetManager {
    private static final Logger logger = LoggerFactory.getLogger(AssetManager.class);

    private final String outputBasePath;

    public AssetManager() {
        this.outputBasePath = ConfigManager.getProperty("output.base.path", "assets/output");
        FileUtils.ensureDirectoryExists(outputBasePath);
    }

    /**
     * Checks if a product has existing assets
     */
    public boolean hasExistingAssets(Product product) {
        if (product.getAssets() == null) {
            return false;
        }
        
        // Check for either heroImage or image field
        String imagePath = product.getAssets().getImage();
        if (imagePath == null) {
            imagePath = product.getAssets().getHeroImage();
        }
        
        if (imagePath == null) {
            return false;
        }

        String resolvedPath = resolveAssetPath(imagePath);
        return resolvedPath != null && new File(resolvedPath).exists();
    }

    /**
     * Gets the product image (checks both 'image' and 'heroImage' fields)
     */
    public byte[] getProductImage(Product product) throws IOException {
        if (product.getAssets() == null) {
            return null;
        }

        // Prefer 'image' field, fallback to 'heroImage'
        String imagePathStr = product.getAssets().getImage();
        if (imagePathStr == null) {
            imagePathStr = product.getAssets().getHeroImage();
        }
        
        if (imagePathStr == null) {
            return null;
        }

        String imagePath = resolveAssetPath(imagePathStr);
        if (imagePath == null || !new File(imagePath).exists()) {
            logger.debug("Product image not found for product {}", product.getProductId());
            return null;
        }

        logger.info("Using existing product image for {}: {}", product.getProductId(), imagePath);
        return Files.readAllBytes(Paths.get(imagePath));
    }
    
    /**
     * Gets the hero image for a product (backwards compatibility)
     */
    public byte[] getHeroImage(Product product) throws IOException {
        return getProductImage(product);
    }

    /**
     * Gets the logo for a product
     */
    public byte[] getLogo(Product product) throws IOException {
        if (product.getAssets() == null || product.getAssets().getLogo() == null) {
            return null;
        }

        String logoPath = resolveAssetPath(product.getAssets().getLogo());
        if (logoPath == null || !new File(logoPath).exists()) {
            logger.debug("Logo not found for product {}", product.getProductId());
            return null;
        }

        logger.info("Using existing logo for product {}: {}", product.getProductId(), logoPath);
        return Files.readAllBytes(Paths.get(logoPath));
    }

    /**
     * Resolves an asset path (handles both absolute and relative paths)
     */
    private String resolveAssetPath(String assetPath) {
        if (assetPath == null) {
            return null;
        }

        File file = new File(assetPath);

        // If absolute path exists, return it
        if (file.isAbsolute() && file.exists()) {
            return assetPath;
        }

        // Try relative to current working directory
        Path cwdPath = Paths.get(assetPath);
        if (Files.exists(cwdPath)) {
            return cwdPath.toString();
        }

        return null;
    }

    /**
     * Resizes an image to a specific aspect ratio while maintaining aspect ratio
     * Product images are scaled to fit within target dimensions without distortion
     */
    public byte[] resizeImage(byte[] imageData, AspectRatio aspectRatio) throws IOException {
        logger.debug("Resizing image to {}x{} ({}) while maintaining aspect ratio", 
            aspectRatio.getWidth(), aspectRatio.getHeight(), aspectRatio.getName());

        // Read the original image to get dimensions
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        int targetWidth = aspectRatio.getWidth();
        int targetHeight = aspectRatio.getHeight();

        // Calculate scaling to fit within target dimensions while maintaining aspect ratio
        double scaleWidth = (double) targetWidth / originalImage.getWidth();
        double scaleHeight = (double) targetHeight / originalImage.getHeight();
        double scale = Math.min(scaleWidth, scaleHeight);

        int scaledWidth = (int) (originalImage.getWidth() * scale);
        int scaledHeight = (int) (originalImage.getHeight() * scale);

        // Create target canvas
        BufferedImage canvas = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill with white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        // Center the scaled image on canvas
        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;
        
        g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // Convert to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(canvas, "PNG", outputStream);
        
        return outputStream.toByteArray();
    }

    /**
     * Adds text overlay to an image
     */
    public byte[] addTextOverlay(byte[] imageData, String text) throws IOException {
        return addTextOverlay(imageData, text, "bottom");
    }

    public byte[] addTextOverlay(byte[] imageData, String text, String position) throws IOException {
        logger.debug("Adding text overlay: \"{}\"", text);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        int width = image.getWidth();
        int height = image.getHeight();

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Calculate text properties
        int fontSize = Math.max(width / 25, 20);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);

        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();

        // Calculate position
        int padding = 40;
        int boxHeight = textHeight + padding;
        int y;

        switch (position.toLowerCase()) {
            case "top":
                y = padding;
                break;
            case "center":
                y = (height - boxHeight) / 2;
                break;
            case "bottom":
            default:
                y = height - boxHeight - padding;
                break;
        }

        // Draw semi-transparent background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, y, width, boxHeight);

        // Draw text
        g2d.setColor(Color.WHITE);
        int textX = (width - textWidth) / 2;
        int textY = y + (boxHeight + metrics.getAscent() - metrics.getDescent()) / 2;
        g2d.drawString(text, textX, textY);

        g2d.dispose();

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Adds a logo overlay to an image
     */
    public byte[] addLogoOverlay(byte[] imageData, byte[] logoData) throws IOException {
        return addLogoOverlay(imageData, logoData, "top-right");
    }

    public byte[] addLogoOverlay(byte[] imageData, byte[] logoData, String position) throws IOException {
        logger.debug("Adding logo overlay");

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        BufferedImage logo = ImageIO.read(new ByteArrayInputStream(logoData));

        int width = image.getWidth();
        int height = image.getHeight();

        // Resize logo to fit
        int maxLogoWidth = width / 5;
        int maxLogoHeight = height / 5;
        int padding = 20;

        // Calculate scaled dimensions
        double scale = Math.min(
            (double) maxLogoWidth / logo.getWidth(),
            (double) maxLogoHeight / logo.getHeight()
        );

        int scaledWidth = (int) (logo.getWidth() * scale);
        int scaledHeight = (int) (logo.getHeight() * scale);

        // Resize logo
        BufferedImage resizedLogo = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedLogo.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(logo, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();

        // Calculate logo position
        int x, y;
        switch (position.toLowerCase()) {
            case "top-left":
                x = padding;
                y = padding;
                break;
            case "bottom-left":
                x = padding;
                y = height - scaledHeight - padding;
                break;
            case "bottom-right":
                x = width - scaledWidth - padding;
                y = height - scaledHeight - padding;
                break;
            case "top-right":
            default:
                x = width - scaledWidth - padding;
                y = padding;
                break;
        }

        // Draw logo onto image
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(resizedLogo, x, y, null);
        g2d.dispose();

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Saves an asset to the output directory
     */
    public String saveAsset(byte[] imageData, String campaignId, String productId,
                           String aspectRatioName) throws IOException {
        String filename = productId + "_" + aspectRatioName.replace(":", "x") + ".png";
        Path outputDir = Paths.get(outputBasePath, campaignId, productId);
        Path outputPath = outputDir.resolve(filename);

        FileUtils.ensureDirectoryExists(outputDir.toString());
        Files.write(outputPath, imageData);

        logger.info("Saved asset: {}", outputPath);
        return outputPath.toString();
    }

}
