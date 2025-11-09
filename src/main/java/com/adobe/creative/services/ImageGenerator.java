package com.adobe.creative.services;

import com.adobe.creative.utils.ConfigManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating product images using fal.ai GenAI API.
 * Falls back to mock image generation when API key is not configured.
 * 
 * @author Ugur Köysüren
 */
public class ImageGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ImageGenerator.class);

    private final String apiKey;
    private final String model;
    private final int timeout;
    private final int maxRetries;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ImageGenerator() {
        this.apiKey = ConfigManager.getEnv("FAL_KEY", "");
        this.model = ConfigManager.getEnv("DEFAULT_IMAGE_MODEL", "fal-ai/imagen4/preview");
        this.timeout = ConfigManager.getInt("IMAGE_GENERATION_TIMEOUT", null, 60000);
        this.maxRetries = ConfigManager.getInt("MAX_RETRIES", null, 3);

        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .build();

        this.objectMapper = new ObjectMapper();

        if (apiKey.isEmpty()) {
            logger.warn("FAL_KEY not set. Image generation will use mock mode.");
        } else {
            logger.info("ImageGenerator configured with fal.ai API key");
        }
    }

    /**
     * Generates an image based on the provided prompt and dimensions.
     * Uses fal.ai API if configured, otherwise generates a mock gradient image.
     * 
     * @param prompt Text description of the desired image
     * @param width Target image width in pixels
     * @param height Target image height in pixels
     * @return Image data as byte array (PNG format)
     * @throws IOException if image generation fails
     */
    public byte[] generateImage(String prompt, int width, int height) throws IOException {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive");
        }

        logger.info("Generating image with prompt: \"{}...\"",
            prompt.substring(0, Math.min(50, prompt.length())));

        if (apiKey.isEmpty()) {
            return generateMockImage(width, height);
        }

        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                attempt++;
                logger.debug("Generation attempt {}/{}", attempt, maxRetries);

                // Build request payload
                String payload = objectMapper.writeValueAsString(new ImageRequest(
                    prompt, width, height
                ));

                Request request = new Request.Builder()
                    .url("https://fal.run/" + model)
                    .post(RequestBody.create(payload, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Key " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("API call failed: " + response.code());
                    }

                    String responseBody = response.body().string();
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);

                    String imageUrl = jsonResponse.at("/images/0/url").asText();

                    if (imageUrl == null || imageUrl.isEmpty()) {
                        throw new IOException("No image URL in response");
                    }

                    logger.info("Image generated successfully: {}", imageUrl);
                    return downloadImage(imageUrl);
                }
            } catch (Exception e) {
                logger.error("Generation attempt {} failed: {}", attempt, e.getMessage());

                if (attempt >= maxRetries) {
                    logger.error("Max retries reached. Using mock image.");
                    return generateMockImage(width, height);
                }

                // Wait before retrying
                try {
                    Thread.sleep(2000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry", ie);
                }
            }
        }

        throw new IOException("Image generation failed after all retries");
    }

    /**
     * Downloads generated image from fal.ai CDN.
     * 
     * @param imageUrl URL of the generated image
     * @return Image data as byte array
     * @throws IOException if download fails
     */
    private byte[] downloadImage(String imageUrl) throws IOException {
        Request request = new Request.Builder()
            .url(imageUrl)
            .get()
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download image: " + response.code());
            }

            try (InputStream inputStream = response.body().byteStream()) {
                return inputStream.readAllBytes();
            }
        }
    }

    /**
     * Generates a clean gradient placeholder image using Java2D.
     * Used when fal.ai API key is not configured.
     * 
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return PNG image data as byte array
     * @throws IOException if image encoding fails
     */
    private byte[] generateMockImage(int width, int height) throws IOException {
        logger.info("Generating mock placeholder image using Java2D");

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(240, 240, 245),
            width, height, new Color(200, 210, 220)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Constructs an optimized prompt for GenAI image generation.
     * 
     * @param productName Name of the product
     * @param productDescription Detailed product description
     * @param targetAudience Target demographic
     * @param region Geographic market
     * @param culturalNotes Optional cultural notes for market-specific adaptation
     * @return Formatted prompt string for image generation
     */
    public String buildPrompt(String productName, String productDescription,
                              String targetAudience, String region, String culturalNotes) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Professional product photography for ").append(productName).append(". ");
        prompt.append(productDescription).append(" ");
        prompt.append("Target audience: ").append(targetAudience).append(" in ").append(region).append(". ");
        
        if (culturalNotes != null && !culturalNotes.trim().isEmpty()) {
            prompt.append("Cultural context: ").append(culturalNotes).append(". ");
        }
        
        prompt.append("High quality, commercial advertising style. ");
        prompt.append("Clean background, excellent lighting, sharp focus. ");
        prompt.append("Photorealistic, 8K resolution.");
        
        return prompt.toString();
    }

    // Inner class for request payload
    private static class ImageRequest {
        public String prompt;
        public ImageSize image_size;
        public int num_inference_steps = 28;
        public double guidance_scale = 3.5;
        public int num_images = 1;
        public boolean enable_safety_checker = true;

        public ImageRequest(String prompt, int width, int height) {
            this.prompt = prompt;
            this.image_size = new ImageSize(width, height);
        }

        private static class ImageSize {
            public int width;
            public int height;

            public ImageSize(int width, int height) {
                this.width = width;
                this.height = height;
            }
        }
    }
}
