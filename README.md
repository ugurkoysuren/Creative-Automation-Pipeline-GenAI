# Creative Automation Pipeline

A Java-based system for generating localized social media campaign assets using GenAI, with support for multi-locale campaigns, brand compliance validation, and automated asset generation.

**Author**: Ugur Köysüren  
**Position**: Adobe FDE AI Engineer  
**Date**: November 2025

## Overview

This pipeline automates the creation of social media ad assets for multiple products, aspect ratios, and localizations. It handles product image composition, brand overlay application, text localization, and compliance validation.

## Quick Start

```bash
# Build
mvn clean package

# Run McDonald's campaign (English + German) - Using convenience script
./run.sh -b examples/mcdonalds/campaign.yaml

# Or run directly with java
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml

# Run specific locale only
./run.sh -b examples/mcdonalds/campaign.yaml -l de-DE

# Enable AI image generation
export FAL_KEY=your_api_key
./run.sh -b examples/mercedes/campaign.yaml

# Using Makefile (recommended)
make install              # Clean, test, and build
make run-mcdonalds        # Run McDonald's example
```

## Features

- **Multi-locale Support**: Generate assets for multiple languages from one campaign file
- **Product Image Compositing**: Maintains aspect ratios, adds brand overlays
- **AI Fallback**: Generates images via fal.ai API when product images aren't available
- **Compliance Validation**: Checks brand guidelines and prohibited words per locale
- **Multiple Aspect Ratios**: 1:1 (Instagram), 9:16 (Stories), 16:9 (YouTube)
- **Flexible Output**: Generates native resolutions for each platform

## Project Structure

```
src/main/java/com/adobe/creative/
├── CreativeAutomationPipeline.java  # CLI entry point
├── models/                          # Data models
├── services/                        # Core business logic
├── validators/                      # Compliance checking
└── utils/                          # Utilities

examples/
├── mcdonalds/                       # With product images
│   ├── campaign.yaml
│   └── input/
└── mercedes/                        # AI-generated images
    └── campaign.yaml
```

## Campaign Format

Campaigns support product-level and campaign-level localizations:

```yaml
campaignId: my-campaign-2024

products:
  - productId: product-1
    name: "English Name"
    description: "English description"
    assets:
      logo: path/to/logo.png
      image: path/to/product.png
    localizations:
      de-DE:
        name: "German Name"
        description: "German description"

brandGuidelines:
  primaryColors: ["#FFC72C"]
  logoRequired: true
  prohibitedWords: ["guaranteed", "miracle"]

localizations:
  en-US:
    language: "English"
    message: "Campaign message"
    culturalNotes: "Market notes"
    prohibitedWords: ["word1"]
  de-DE:
    language: "German"
    message: "Kampagnennachricht"
    prohibitedWords: ["wort1"]
```

## Usage

```bash
# All localizations
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml

# Specific locale
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml -l en-US

# Verbose output
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml -v
```

## CLI Options

| Option | Description |
|--------|-------------|
| `-b, --brief <path>` | Path to campaign YAML file |
| `-l, --locale <code>` | Generate for specific locale (e.g., en-US, de-DE) |
| `-v, --verbose` | Enable debug logging |
| `-h, --help` | Show help message |

## Output Structure

```
assets/output/
├── campaign-id-en-us/
│   ├── product-1/
│   │   ├── product-1_1x1.png      # 1080x1080
│   │   ├── product-1_9x16.png     # 1080x1920
│   │   └── product-1_16x9.png     # 1920x1080
│   └── generation-report.txt
└── campaign-id-de-de/
    └── ...
```

## Examples

### McDonald's Campaign
Demonstrates product image composition with multi-locale support. Uses provided product images and applies brand overlays.

```bash
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml
```

Generates 18 assets (3 products × 3 ratios × 2 locales)

### Mercedes Campaign
Demonstrates AI image generation when product images aren't available.

```bash
export FAL_KEY=your_fal_ai_key
java -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mercedes/campaign.yaml
```

## Development

```bash
# Using Makefile (recommended)
make help                 # Show all available commands
make build                # Compile the project
make test                 # Run all tests
make package              # Build executable JAR
make install              # Clean, test, and package
make javadoc              # Generate JavaDoc

# Using Maven directly
mvn clean package         # Build
mvn test                  # Run tests
mvn javadoc:javadoc       # Generate JavaDoc

# Run without packaging
mvn exec:java -Dexec.mainClass="com.adobe.creative.CreativeAutomationPipeline" \
  -Dexec.args="-b examples/mcdonalds/campaign.yaml"

# Using convenience script
./run.sh -b examples/mcdonalds/campaign.yaml
```

## Technical Stack

- **Java 22**: Modern Java features
- **Maven**: Build and dependency management
- **Jackson**: JSON/YAML parsing
- **OkHttp**: HTTP client for GenAI API
- **Picocli**: CLI framework
- **SLF4J/Logback**: Logging
- **JUnit 5**: Testing

## Configuration

The application uses `application.properties` for default configuration, with optional environment variable overrides.

### Environment Variables (Optional)

Only these environment variables are supported:

```bash
# AI Image Generation (optional, uses mock mode if not set)
export FAL_KEY=your_fal_ai_api_key
export DEFAULT_IMAGE_MODEL=fal-ai/flux-pro  # Default model
export IMAGE_GENERATION_TIMEOUT=60000       # Timeout in milliseconds
export MAX_RETRIES=3                        # Number of retry attempts

# Logging
export LOG_LEVEL=INFO                       # DEBUG, INFO, WARN, ERROR
```

### Application Properties

Edit `src/main/resources/application.properties` to configure:

```properties
# Output directory
output.base.path=assets/output

# Brand compliance defaults
brand.logo.required=true
brand.primary.colors=#FFFFFF
```

**Note:** Input asset paths are specified directly in campaign YAML files (e.g., `examples/mcdonalds/input/mcdonalds.png`).

## How It Works

1. **Parse Campaign**: Read YAML and extract products, localizations
2. **Process Products**: For each product and locale:
   - Load or generate product image
   - Resize to target aspect ratio (maintaining proportions)
   - Apply logo overlay (if available)
   - Add localized text overlay
3. **Validate Compliance**: Check brand colors, prohibited words
4. **Save Assets**: Write to locale-specific output folders
5. **Generate Report**: Summary of generated assets

## Troubleshooting

### Out of Memory
```bash
java -Xmx2g -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar
```

### Graphics Issues on macOS
On macOS, you may need to run in headless mode to prevent AWT crashes:
```bash
java -Djava.awt.headless=true \
  -jar target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar \
  -b examples/mcdonalds/campaign.yaml
```

### Missing Localizations
If a localization is missing, the system falls back to default values (English).

## Performance

Typical generation times (MacBook Pro M1):
- 2 products, 3 ratios, mock mode: ~3 seconds
- 2 products, 3 ratios, AI mode: ~15 seconds
- 10 products, 3 ratios: ~45 seconds

## Future Enhancements

- Parallel product processing
- Template-based text layouts
- More image filters and effects
- REST API for campaign submission
- Database persistence
- **Agentic System**: See [AGENTIC_ARCHITECTURE.md](AGENTIC_ARCHITECTURE.md) for autonomous monitoring and orchestration design

## License

MIT License

---

Built by Ugur Köysüren for Adobe FDE AI Engineer position.
