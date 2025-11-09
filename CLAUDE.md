# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Java-based creative automation pipeline for generating localized social media campaign assets using GenAI. The system processes campaign briefs (YAML/JSON) to generate product images across multiple aspect ratios (1:1, 9:16, 16:9) with brand overlays, localized text, and compliance validation.

Main entry point: `com.adobe.creative.CreativeAutomationPipeline`

## Build & Run Commands

### Using Makefile (Recommended)
```bash
make help                 # Show all available commands
make build                # Compile the project
make test                 # Run all tests
make test-verbose         # Run tests with detailed output
make package              # Build executable JAR
make install              # Clean, test, and package
make run-mcdonalds        # Run McDonald's example
make run-mercedes         # Run Mercedes example
make javadoc              # Generate JavaDoc
```

### Using Convenience Script
```bash
# Automatically builds if needed, includes proper JVM settings for macOS
./run.sh -b examples/mcdonalds/campaign.yaml
./run.sh -b examples/mcdonalds/campaign.yaml -l de-DE
./run.sh -b examples/mcdonalds/campaign.yaml -v
```

### Using Maven Directly
```bash
# Build the project
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=BriefParserTest

# Run a specific test method
mvn test -Dtest=ImageGeneratorTest#testGenerateImage

# Run with Maven exec (without packaging)
mvn exec:java -Dexec.mainClass="com.adobe.creative.CreativeAutomationPipeline" \
  -Dexec.args="-b examples/mcdonalds/campaign.yaml"
```

### Using Java Directly
```bash
# Run packaged JAR
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml

# Run with specific locale
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml -l de-DE

# Enable verbose logging
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml -v
```

## Architecture Overview

### Core Pipeline Flow

1. **BriefParser** (`services/BriefParser.java`) - Parses YAML/JSON campaign files into `CampaignBrief` model
2. **CreativeGenerator** (`services/CreativeGenerator.java`) - Main orchestrator that:
   - Iterates through products and localizations
   - Delegates to ImageGenerator for image creation/composition
   - Applies overlays via AssetManager
   - Validates output via ComplianceValidator
3. **ImageGenerator** (`services/ImageGenerator.java`) - Handles image sourcing:
   - Uses product images if provided in campaign YAML
   - Falls back to fal.ai API for GenAI generation (requires `FAL_KEY` env var)
   - Uses mock gradient images if no API key
4. **AssetManager** (`services/AssetManager.java`) - Image processing:
   - Resizes images to target aspect ratios while maintaining proportions
   - Adds logo overlays (top-right by default)
   - Adds text overlays with semi-transparent backgrounds
5. **ComplianceValidator** (`validators/ComplianceValidator.java`) - Validates against brand guidelines and prohibited words

### Generation Strategy

The system uses a **"native generation strategy"** - each aspect ratio (1:1, 9:16, 16:9) is generated at its native resolution (e.g., 1080x1080, 1080x1920, 1920x1080) rather than generating one master and resizing.

### Localization Architecture

Multi-locale support works at two levels:

1. **Campaign-level localizations**: Define locale-specific messages, cultural notes, and prohibited words in the `localizations` map of the campaign YAML
2. **Product-level localizations**: Each product can have locale-specific `name` and `description` in its `localizations` map

When generating assets for a locale:
- Campaign ID becomes `{campaignId}-{locale}` (e.g., `mcdonalds-autumn-specials-2024-de-de`)
- Product names/descriptions use localized versions if available
- Cultural notes are incorporated into AI image generation prompts
- Prohibited words validation uses locale-specific lists merged with brand-level prohibitions

### Key Models

- **CampaignBrief** - Top-level campaign definition with products, localizations, and brand guidelines
- **Product** - Product definition with assets and locale-specific variants
- **LocalizationConfig** - Locale-specific campaign message, cultural notes, prohibited words
- **ProductLocalization** - Locale-specific product name and description
- **AspectRatio** - Aspect ratio specifications (width, height, name, platforms)
- **BrandGuidelines** - Brand compliance rules (colors, fonts, prohibited words, logo requirements)

### Important Paths and Conventions

- Campaign briefs: `examples/{campaign-name}/campaign.yaml`
- Input assets: `examples/{campaign-name}/input/`
- Generated output: `assets/output/{campaignId}/` (locale-specific if multi-locale)
- Configuration: `src/main/resources/application.properties`
- Aspect ratios are defined in `AspectRatio.STANDARD_RATIOS` constant

## Environment Variables

Only these environment variables are supported:

```bash
# AI Image Generation (optional - uses mock mode if not set)
export FAL_KEY=your_fal_ai_api_key
export DEFAULT_IMAGE_MODEL=fal-ai/imagen4/preview
export IMAGE_GENERATION_TIMEOUT=60000  # milliseconds
export MAX_RETRIES=3

# Logging
export LOG_LEVEL=INFO  # DEBUG, INFO, WARN, ERROR
```

## Testing Strategy

Tests use JUnit 5 and are located in `src/test/java/`:
- **Model tests**: Verify data structures and localization getters
- **Service tests**: Mock-based unit tests for BriefParser, ImageGenerator
- **Integration tests**: ComplianceValidator tests with real BrandGuidelines

When adding new features, follow existing test patterns:
- Use descriptive test method names (e.g., `testParseYamlBrief`, `testLocalizedProductName`)
- Mock external dependencies (file I/O, HTTP calls) in unit tests
- Test both happy path and error conditions

## Common Development Patterns

### Adding a New Service

1. Create service class in `src/main/java/com/adobe/creative/services/`
2. Use SLF4J logger: `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`
3. Add constructor that initializes from ConfigManager
4. Document public methods with JavaDoc
5. Create corresponding test in `src/test/java/com/adobe/creative/services/`

### Working with Images

All image operations use Java2D (`BufferedImage`, `Graphics2D`) with high-quality rendering hints:
```java
g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
```

Images are passed as `byte[]` arrays (PNG format) between services. Use `ImageIO.read(new ByteArrayInputStream(imageData))` to convert to BufferedImage.

### Configuration Management

Use `ConfigManager` for all configuration:
- `ConfigManager.getProperty(key, defaultValue)` - reads from application.properties
- `ConfigManager.getEnv(key, defaultValue)` - reads from environment variables
- `ConfigManager.getInt(key, properties, defaultValue)` - parses integer config

### Error Handling

- Use `logger.error()` for errors, `logger.warn()` for warnings, `logger.info()` for important events
- Return `GenerationResult` with `success` flag and `errors` list for batch operations
- Throw `IOException` for I/O failures, `IllegalArgumentException` for validation failures

## macOS Graphics Note

The project runs in headless mode on macOS to prevent AWT crashes. If running directly:
```bash
java -Djava.awt.headless=true -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar
```

## Known Limitations

- Product image composition maintains aspect ratios by centering on white background (no cropping/fill)
- Text overlays use fixed Arial font (not customizable per brand)
- Parallel product processing not implemented (sequential only)
- No caching of generated AI images between runs
